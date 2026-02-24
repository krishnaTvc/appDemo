package com.springWeb.appDemo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    // Spring Data JPA auto-provides: save(), findAll(), findById(), deleteById(),
    // count(), etc.
}
