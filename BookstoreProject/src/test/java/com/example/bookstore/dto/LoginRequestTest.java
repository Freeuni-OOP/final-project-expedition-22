package com.example.bookstore.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LoginRequestTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidLoginRequest() {
        LoginRequest request = new LoginRequest("gi123o", "pa1ss2wo3rd");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "For valid inputs there should not be any error");
    }

    @Test
    void testBlankUsernameAndPassword() {
        LoginRequest request = new LoginRequest("", "   ");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertEquals(3, violations.size(), "There should be 3 error");
    }

    @Test
    void testShortPassword() {
        LoginRequest request = new LoginRequest("b1o2o3k", "pass?");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        String errorMessage = violations.iterator().next().getMessage();
        assertEquals("Password should be at least 6 symbols", errorMessage);
    }

    @Test
    void testConstructorAndGettersSetters() {
        String username="er2";
        String password="secretPassword0";
        LoginRequest request = new LoginRequest(username, password);

        assertEquals(username, request.getUsername(), "Returned username does not match real username");
        assertEquals(password, request.getPassword(), "Returned password does not match real password");

        request.setUsername("er3");
        request.setPassword("secretPassword1");
        assertEquals("er3", request.getUsername(), "Returned username does not match new changed username");
        assertEquals("secretPassword1", request.getPassword(), "Returned password does not match new changed password");


    }

    @Test
    void testDefaultConstructorAndGetterSetters() {
        LoginRequest request = new LoginRequest();

        assertNull(request.getUsername(), "On default username should be null");
        assertNull(request.getPassword(), "On default password should be null");

        request.setUsername("new user");
        request.setPassword("mySecurePassword");

        assertEquals("new user", request.getUsername(), "Returned username does not match the real username");
        assertEquals("mySecurePassword", request.getPassword(), "Returned password does not match the real password");
    }

}
