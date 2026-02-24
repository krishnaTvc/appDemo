package com.springWeb.appDemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Spring MVC unit tests – run with: mvn clean test
 * These tests use an in-memory H2 database (see application-test.yaml).
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.yaml")
class RegistrationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() throws Exception {
		// verifies application context starts without errors
	}

	@Test
	void showRegistrationForm_shouldReturnFormView() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(view().name("registration"))
				.andExpect(model().attributeExists("registration"));
	}

	@Test
	void submitForm_withValidData_shouldRedirectToSuccess() throws Exception {
		mockMvc.perform(post("/register")
				.param("name", "John Doe")
				.param("state", "Tamil Nadu")
				.param("country", "India"))
				.andExpect(status().isOk())
				.andExpect(view().name("success"))
				.andExpect(model().attribute("name", "John Doe"))
				.andExpect(model().attribute("state", "Tamil Nadu"))
				.andExpect(model().attribute("country", "India"));
	}

	@Test
	void submitForm_withBlankName_shouldReturnFormWithErrors() throws Exception {
		mockMvc.perform(post("/register")
				.param("name", "") // blank → validation error
				.param("state", "California")
				.param("country", "United States"))
				.andExpect(status().isOk())
				.andExpect(view().name("registration"))
				.andExpect(model().attributeHasFieldErrors("registration", "name"));
	}

	@Test
	void submitForm_withBlankState_shouldReturnFormWithErrors() throws Exception {
		mockMvc.perform(post("/register")
				.param("name", "Jane Doe")
				.param("state", "") // blank → validation error
				.param("country", "India"))
				.andExpect(status().isOk())
				.andExpect(view().name("registration"))
				.andExpect(model().attributeHasFieldErrors("registration", "state"));
	}

	@Test
	void submitForm_withBlankCountry_shouldReturnFormWithErrors() throws Exception {
		mockMvc.perform(post("/register")
				.param("name", "John")
				.param("state", "Delhi")
				.param("country", "")) // blank → validation error
				.andExpect(status().isOk())
				.andExpect(view().name("registration"))
				.andExpect(model().attributeHasFieldErrors("registration", "country"));
	}

	@Test
	void apiEndpoint_shouldReturnJsonList() throws Exception {
		mockMvc.perform(get("/api/registrations"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("application/json"));
	}
}
