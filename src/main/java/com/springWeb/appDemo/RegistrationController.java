package com.springWeb.appDemo;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class RegistrationController {

    private final RegistrationRepository registrationRepository;

    public RegistrationController(RegistrationRepository registrationRepository) {
        this.registrationRepository = registrationRepository;
    }

    /** GET / → show empty registration form */
    @GetMapping("/")
    public String showForm(Model model) {
        model.addAttribute("registration", new Registration());
        return "registration";
    }

    /** POST /register → validate & save, then redirect to success */
    @PostMapping("/register")
    public String submitForm(
            @Valid @ModelAttribute("registration") Registration registration,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "registration"; // redisplay form with validation messages
        }

        registrationRepository.save(registration);
        model.addAttribute("name", registration.getName());
        model.addAttribute("state", registration.getState());
        model.addAttribute("country", registration.getCountry());
        return "success";
    }

    /** GET /success → show success page */
    @GetMapping("/success")
    public String successPage() {
        return "success";
    }

    /**
     * REST API: GET /api/registrations → returns all records as JSON (useful for
     * testing)
     */
    @GetMapping("/api/registrations")
    @ResponseBody
    public java.util.List<Registration> getAllRegistrations() {
        return registrationRepository.findAll();
    }
}
