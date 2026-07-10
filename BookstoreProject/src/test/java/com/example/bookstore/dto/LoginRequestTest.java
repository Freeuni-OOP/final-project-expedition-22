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

    @Test
    void defaultConstructorAndSettersShouldWork() {
        LoginRequest request = new LoginRequest();

        request.setUsername("john");
        request.setPassword("password123");

        assertEquals("john", request.getUsername());
        assertEquals("password123", request.getPassword());
    }

    @Test
    void constructorShouldInitializeFields() {
        LoginRequest request = new LoginRequest(
                "alice",
                "secret123"
        );

        assertEquals("alice", request.getUsername());
        assertEquals("secret123", request.getPassword());
    }

    @Test
    void validRequestShouldHaveNoViolations() {
        LoginRequest request = new LoginRequest(
                "john",
                "password123"
        );

        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void blankUsernameShouldFailValidation() {
        LoginRequest request = new LoginRequest(
                "",
                "password123"
        );

        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void blankPasswordShouldFailValidation() {
        LoginRequest request = new LoginRequest(
                "john",
                ""
        );

        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shortPasswordShouldFailValidation() {
        LoginRequest request = new LoginRequest(
                "john",
                "12345"
        );

        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }

    @Test
    void nullFieldsShouldFailValidation() {
        LoginRequest request = new LoginRequest();

        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
    }
}
