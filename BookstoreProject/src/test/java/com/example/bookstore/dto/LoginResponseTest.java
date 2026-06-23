package com.example.bookstore.dto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginResponseTest {

    @Test
    void testDefaultConstructorAndDefaultValues() {
        LoginResponse response = new LoginResponse();

        assertFalse(response.isSuccess(), "On default success should be false");
        assertNull(response.getMessage(), "On default message should be null");
        assertNull(response.getToken(), "On default token should be null");
    }

    @Test
    void testTwoParameterConstructor() {
        LoginResponse response = new LoginResponse(false, "wrong password");

        assertFalse(response.isSuccess());
        assertEquals("wrong password", response.getMessage());
        assertNull(response.getToken(), "for two parameter constructor token is null");
    }

    @Test
    void testThreeParameterConstructor() {
        LoginResponse response = new LoginResponse(true, "success", "jwttokenmko");

        assertTrue(response.isSuccess());
        assertEquals("success", response.getMessage());
        assertEquals("jwttokenmko", response.getToken());
    }

    @Test
    void testGetterSetters() {
        LoginResponse response = new LoginResponse();

        response.setSuccess(true);
        response.setMessage("new message");
        response.setToken("newtoken");

        assertTrue(response.isSuccess(), "saved 'success' does not match real 'success'");
        assertEquals("new message", response.getMessage(), "saved message does not match real message");
        assertEquals("newtoken", response.getToken(), "saved token does not match real token");


        response.setSuccess(false);
        response.setMessage("real new message");
        response.setToken("newnewtoken");

        assertFalse(response.isSuccess(), "saved 'success' does not match real 'success'");
        assertEquals("real new message", response.getMessage(), "saved message does not match real message");
        assertEquals("newnewtoken", response.getToken(), "saved token does not match real token");
    }
}