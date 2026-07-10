package com.example.bookstore.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserProfileResponseTest {

    @Test
    void constructorShouldInitializeAllFields() {
        UserProfileResponse response = new UserProfileResponse(
                1L,
                "ivane",
                "ivane@example.com",
                "555123456"
        );

        assertEquals(1L, response.getId());
        assertEquals("ivane", response.getUsername());
        assertEquals("ivane@example.com", response.getEmail());
        assertEquals("555123456", response.getPhoneNumber());
    }
}