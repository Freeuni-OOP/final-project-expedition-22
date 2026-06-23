package com.example.bookstore.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RegisterResponseTest {

    @Test
    void testDefaultConstructorAndDefaultValues() {
        RegisterResponse response = new RegisterResponse();

        assertFalse(response.isSuccess(), "on default constructor success should be false");
        assertNull(response.getMessage(), "on default constructor message should be null");
    }

    @Test
    void testFullConstructorSuccessCase() {
        RegisterResponse response = new RegisterResponse(true, "Registration was successful");

        assertTrue(response.isSuccess());
        assertEquals("Registration was successful", response.getMessage());
    }

    @Test
    void testFullConstructorFailureCase() {
        RegisterResponse response = new RegisterResponse(false, "That username is already taken");

        assertFalse(response.isSuccess());
        assertEquals("That username is already taken", response.getMessage());
    }

    @Test
    void testSettersAndGetters() {
        RegisterResponse response = new RegisterResponse();

        response.setSuccess(true);
        response.setMessage("Registration was successful");

        assertTrue(response.isSuccess(), "saved 'success' does not match the real 'success'");
        assertEquals("Registration was successful", response.getMessage(), "saved message does not match the real message");


        response.setSuccess(false);
        response.setMessage("Registration was not successful");

        assertFalse(response.isSuccess(), "saved 'success' does not match the real 'success'");
        assertEquals("Registration was not successful", response.getMessage(), "saved message does not match the real message");

    }
}