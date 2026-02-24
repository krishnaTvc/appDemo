package com.springWeb.appDemo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "registration")
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "State is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String state;

    @NotBlank(message = "Country is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String country;

    // ---- Constructors ----
    public Registration() {
    }

    public Registration(String name, String state, String country) {
        this.name = name;
        this.state = state;
        this.country = country;
    }

    // ---- Getters & Setters ----
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
