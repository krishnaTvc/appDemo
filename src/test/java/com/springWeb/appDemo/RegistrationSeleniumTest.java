package com.springWeb.appDemo;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Selenium Sanity Tests – run end-to-end against a live server.
 *
 * During CI : mvn verify (Failsafe starts the Spring Boot app automatically)
 * Manually : start the app, then run: mvn failsafe:integration-test
 *
 * The tests assume the app is running at http://localhost:8080
 */
class RegistrationSeleniumTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = System.getProperty("app.url", "http://localhost:8080");

    @BeforeAll
    static void setupDriver() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--headless=new", // headless mode for CI
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--window-size=1920,1080");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterAll
    static void tearDownDriver() {
        if (driver != null)
            driver.quit();
    }

    @BeforeEach
    void openHomePage() {
        driver.get(BASE_URL + "/");
    }

    // ── TC-01: Registration form loads correctly ────────────────────────────
    @Test
    @DisplayName("TC-01: Registration page loads with all fields and buttons")
    void registrationPageLoads() {
        wait.until(ExpectedConditions.titleContains("Registration"));

        assertTrue(driver.findElement(By.id("name")).isDisplayed(), "Name field visible");
        assertTrue(driver.findElement(By.id("state")).isDisplayed(), "State field visible");
        assertTrue(driver.findElement(By.id("country")).isDisplayed(), "Country dropdown visible");
        assertTrue(driver.findElement(By.id("submitBtn")).isDisplayed(), "Submit button visible");
        assertTrue(driver.findElement(By.id("clearBtn")).isDisplayed(), "Clear button visible");
    }

    // ── TC-02: Happy-path registration ─────────────────────────────────────
    @Test
    @DisplayName("TC-02: Valid registration shows success page")
    void validRegistration_showsSuccessPage() {
        fillAndSubmit("Ravi Kumar", "Andhra Pradesh", "India");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("successHeading")));

        String heading = driver.findElement(By.id("successHeading")).getText();
        assertTrue(heading.contains("Success") || heading.contains("Successful"),
                "Success heading not found");
    }

    // ── TC-03: Clear button resets all fields ───────────────────────────────
    @Test
    @DisplayName("TC-03: Clear button resets all form fields")
    void clearButton_resetsFields() {
        WebElement nameField = driver.findElement(By.id("name"));
        WebElement stateField = driver.findElement(By.id("state"));
        WebElement countrySelect = driver.findElement(By.id("country"));

        // Type some values
        nameField.sendKeys("Test User");
        stateField.sendKeys("Karnataka");
        new Select(countrySelect).selectByValue("India");

        // Click Clear
        driver.findElement(By.id("clearBtn")).click();

        assertEquals("", nameField.getAttribute("value"), "Name should be cleared");
        assertEquals("", stateField.getAttribute("value"), "State should be cleared");
        assertEquals("", countrySelect.getAttribute("value"), "Country should be cleared");
    }

    // ── TC-04: Submit with blank Name shows validation error ────────────────
    @Test
    @DisplayName("TC-04: Blank name shows validation error")
    void blankName_showsValidationError() {
        driver.findElement(By.id("state")).sendKeys("Delhi");
        new Select(driver.findElement(By.id("country"))).selectByValue("India");
        driver.findElement(By.id("submitBtn")).click();

        // Should stay on registration form (server-side validation)
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlToBe(BASE_URL + "/register"),
                ExpectedConditions.urlToBe(BASE_URL + "/")));

        boolean hasError = !driver.findElements(By.className("error-msg")).isEmpty();
        assertTrue(hasError, "Expected a validation error for blank name");
    }

    // ── TC-05: Submit with blank State shows validation error ───────────────
    @Test
    @DisplayName("TC-05: Blank state shows validation error")
    void blankState_showsValidationError() {
        driver.findElement(By.id("name")).sendKeys("John");
        new Select(driver.findElement(By.id("country"))).selectByValue("India");
        driver.findElement(By.id("submitBtn")).click();

        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlToBe(BASE_URL + "/register"),
                ExpectedConditions.urlToBe(BASE_URL + "/")));

        boolean hasError = !driver.findElements(By.className("error-msg")).isEmpty();
        assertTrue(hasError, "Expected a validation error for blank state");
    }

    // ── TC-06: Submit with no Country shows validation error ────────────────
    @Test
    @DisplayName("TC-06: No country selected shows validation error")
    void noCountry_showsValidationError() {
        driver.findElement(By.id("name")).sendKeys("Jane");
        driver.findElement(By.id("state")).sendKeys("Goa");
        // Leave country as default "-- Select Country --"
        driver.findElement(By.id("submitBtn")).click();

        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlToBe(BASE_URL + "/register"),
                ExpectedConditions.urlToBe(BASE_URL + "/")));

        boolean hasError = !driver.findElements(By.className("error-msg")).isEmpty();
        assertTrue(hasError, "Expected a validation error when no country selected");
    }

    // ── TC-07: Back to Register link from success page ──────────────────────
    @Test
    @DisplayName("TC-07: Back button on success page returns to registration form")
    void successPage_backButton_returnsToForm() {
        fillAndSubmit("Priya Singh", "Maharashtra", "India");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("backBtn")));
        driver.findElement(By.id("backBtn")).click();

        wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));
        assertTrue(driver.findElement(By.id("name")).isDisplayed(), "Back to form");
    }

    // ── TC-08: Multiple registrations accumulate ────────────────────────────
    @Test
    @DisplayName("TC-08: Two registrations both succeed")
    void multipleRegistrations_bothSucceed() {
        fillAndSubmit("Alice", "Rajasthan", "India");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("backBtn")));
        driver.findElement(By.id("backBtn")).click();

        wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));
        fillAndSubmit("Bob", "Kerala", "India");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("successHeading")));

        assertTrue(driver.getTitle().toLowerCase().contains("success") ||
                driver.findElement(By.id("successHeading")).isDisplayed());
    }

    // ── Helper ──────────────────────────────────────────────────────────────
    private void fillAndSubmit(String name, String state, String country) {
        WebElement nameField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("name")));
        nameField.clear();
        nameField.sendKeys(name);

        WebElement stateField = driver.findElement(By.id("state"));
        stateField.clear();
        stateField.sendKeys(state);

        new Select(driver.findElement(By.id("country"))).selectByValue(country);

        driver.findElement(By.id("submitBtn")).click();
    }
}
