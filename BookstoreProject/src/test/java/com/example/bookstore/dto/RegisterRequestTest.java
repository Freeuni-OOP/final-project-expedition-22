package com.example.bookstore.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class RegisterRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidRegisterRequest() {
        RegisterRequest request = new RegisterRequest("user22", "1234password", "599123456", "some@gmail.com");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty(), "there should not be any violation for valid inputs");
    }

    @Test
    void testBlankFields() {
        RegisterRequest request = new RegisterRequest("", "   ", "", "");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty(), "for blank inputs there should be violations");
        //@Email does not show violation on empty string that is why it should be 7
        assertEquals(7, violations.size(), "there should be 7 violation for blank inputs");
    }

    @Test
    void testShortUsernameAndPassword() {
        RegisterRequest request = new RegisterRequest("gio", "pass", "599123456", "some@gmail.com");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertEquals(2, violations.size(), "there should be exactly 2 violation because of strings' length");
    }

    @Test
    void testInvalidPhoneNumberPattern() {
        RegisterRequest request = new RegisterRequest("name19", "pass1212", "499123", "some@gmail.com");
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        String errorMessage = violations.iterator().next().getMessage();
        assertEquals("phone number patter is wrong", errorMessage);
    }

    @Test
    void testValidInternationalPhoneNumber() {
        RegisterRequest request = new RegisterRequest("name20", "pass13132", "+995599123456", "some@gmail.com");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty(), "+995 pattern should be allowed");
    }

    @Test
    void testValidEmailPattern() {
        RegisterRequest request = new RegisterRequest("name20", "pass13132", "+995599123456", "some.com");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size(), "there should be violation because of wrong email pattern");

    }


    @Test
    void testDefaultConstructorValues() {
        RegisterRequest request = new RegisterRequest();

        assertNull(request.getUsername(), "on default username should be null");
        assertNull(request.getPassword(), "on default password should be null");
        assertNull(request.getPhoneNumber(), "on default phone number should be null");
        assertNull(request.getEmail(), "on default Email should be null");
    }

    @Test
    void testSettersAndGetters() {
        RegisterRequest request = new RegisterRequest();

        request.setUsername("bestUser");
        request.setPassword("superSecretpass");
        request.setPhoneNumber("555443322");
        request.setEmail("some1@gmail.com");

        assertEquals("bestUser", request.getUsername(), "saved Username is wrong");
        assertEquals("superSecretpass", request.getPassword(), "saved Password is wrong");
        assertEquals("555443322", request.getPhoneNumber(), "saved PhoneNumber is wrong");
        assertEquals("some1@gmail.com", request.getEmail(), "saved Email is wrong");


        request.setUsername("betterUser");
        request.setPassword("superSecretpassword");
        request.setPhoneNumber("555442321");
        request.setEmail("some2@gmail.com");

        assertEquals("betterUser", request.getUsername(), "saved Username is wrong");
        assertEquals("superSecretpassword", request.getPassword(), "saved Password is wrong");
        assertEquals("555442321", request.getPhoneNumber(), "saved PhoneNumber is wrong");
        assertEquals("some2@gmail.com", request.getEmail(), "saved Email is wrong");

    }
}