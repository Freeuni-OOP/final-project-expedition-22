package com.example.bookstore.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void CreateUserWithConstructor() {
        User user = new User("john", "password123", "555123456", "gagasdh@gmail.com");

        assertEquals("john", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertEquals("555123456", user.getPhoneNumber());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getFavouriteBooks());
        assertTrue(user.getFavouriteBooks().isEmpty());
    }

    @Test
    void UpdateUserFieldsUsingSetters() {
        User user = new User();
        LocalDateTime now = LocalDateTime.now();
        Set<Book> favouriteBooks = new HashSet<>();

        user.setUsername("john");
        user.setPassword("newPassword");
        user.setPhoneNumber("599123456");
        user.setCreatedAt(now);
        user.setFavouriteBooks(favouriteBooks);

        assertEquals("john", user.getUsername());
        assertEquals("newPassword", user.getPassword());
        assertEquals("599123456", user.getPhoneNumber());
        assertEquals(now, user.getCreatedAt());
        assertEquals(favouriteBooks, user.getFavouriteBooks());
    }
}
