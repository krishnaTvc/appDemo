#!/usr/bin/env bash
# ═══════════════════════════════════════════════════════════════════════════
#  deploy.sh  –  Run on Azure VM (Ubuntu 24.04) by Azure Pipelines
#
#  This script is idempotent – safe to run on every deployment.
#
#  Required env var passed by pipeline:
#    DB_PASSWORD  – MySQL appuser password
# ═══════════════════════════════════════════════════════════════════════════
set -e

APP_NAME="registration-app"
JAR_SRC="/tmp/appDemo.jar"
DEPLOY_DIR="/opt/appDemo"
JAR_DEST="${DEPLOY_DIR}/appDemo.jar"
DB_NAME="registrationdb"
DB_USER="appuser"
DB_PASS="${DB_PASSWORD:-apppass}"   # fallback only for local testing

echo "======================================================"
echo "  Deploying Spring Boot Registration App"
echo "======================================================"

# ── 1. Install Java 17 if not present ──────────────────
if ! java -version 2>&1 | grep -q "17"; then
  echo "▶ Installing OpenJDK 17..."
  apt-get update -qq
  apt-get install -y openjdk-17-jdk
fi
echo "✔ Java: $(java -version 2>&1 | head -1)"

# ── 2. Install MySQL if not present ────────────────────
if ! command -v mysql &>/dev/null; then
  echo "▶ Installing MySQL Server..."
  DEBIAN_FRONTEND=noninteractive apt-get install -y mysql-server
  systemctl enable mysql
  systemctl start mysql
fi
echo "✔ MySQL: $(mysql --version)"

# ── 3. Create database and user if they don't exist ────
echo "▶ Configuring MySQL database..."
mysql -u root <<SQL
CREATE DATABASE IF NOT EXISTS ${DB_NAME}
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS '${DB_USER}'@'localhost' IDENTIFIED BY '${DB_PASS}';

GRANT ALL PRIVILEGES ON ${DB_NAME}.* TO '${DB_USER}'@'localhost';
FLUSH PRIVILEGES;
SQL
echo "✔ Database '${DB_NAME}' and user '${DB_USER}' ready"

# ── 4. Create deployment directory ─────────────────────
mkdir -p "${DEPLOY_DIR}"
echo "▶ Copying JAR to ${JAR_DEST}..."
cp "${JAR_SRC}" "${JAR_DEST}"
chmod 644 "${JAR_DEST}"
echo "✔ JAR deployed"

# ── 5. Install / update systemd service ────────────────
SERVICE_FILE="/etc/systemd/system/${APP_NAME}.service"
cat > "${SERVICE_FILE}" <<EOF
[Unit]
Description=Spring Boot Registration App
After=network.target mysql.service
Wants=mysql.service

[Service]
User=nobody
WorkingDirectory=${DEPLOY_DIR}
ExecStart=/usr/bin/java -jar ${JAR_DEST} \\
  --spring.datasource.password=${DB_PASS} \\
  --server.port=8080
SuccessExitStatus=143
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=${APP_NAME}

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable "${APP_NAME}"

# ── 6. Restart service ─────────────────────────────────
echo "▶ Restarting ${APP_NAME} service..."
systemctl restart "${APP_NAME}"
sleep 8

# ── 7. Health check ────────────────────────────────────
echo "▶ Health check..."
if curl -sf http://localhost:8080/ > /dev/null; then
  echo "✔ App is UP at http://localhost:8080"
else
  echo "✗ Health check failed – check logs: journalctl -u ${APP_NAME} -n 50"
  exit 1
fi

echo "======================================================"
echo "  Deployment SUCCESSFUL"
echo "======================================================"
