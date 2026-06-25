package com.example.bookstore.service;

import com.example.bookstore.dto.LoginRequest;
import com.example.bookstore.dto.LoginResponse;
import com.example.bookstore.dto.RegisterRequest;
import com.example.bookstore.dto.RegisterResponse;
import com.example.bookstore.entity.User;
import com.example.bookstore.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

    private final AuthService authService =
            new AuthService(userRepository, passwordEncoder);

    @Test
    void registerShouldFailWhenUsernameAlreadyExists() {
        RegisterRequest request = new RegisterRequest(
                "john",
                "password123",
                "599123456",
                "john@example.com"
        );

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(new User()));

        RegisterResponse response = authService.register(request);

        assertFalse(response.isSuccess());
        assertEquals("Username already exists", response.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerShouldSaveUserWithEncodedPassword() {
        RegisterRequest request = new RegisterRequest(
                "john",
                "password123",
                "599123456",
                "john@example.com"
        );

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        RegisterResponse response = authService.register(request);

        assertTrue(response.isSuccess());
        assertEquals("Registration successful", response.getMessage());

        verify(userRepository).save(argThat(user ->
                user.getUsername().equals("john")
                        && user.getPassword().equals("encodedPassword")
                        && user.getPhoneNumber().equals("599123456")
                        && user.getEmail().equals("john@example.com")
        ));
    }

    @Test
    void loginShouldFailWhenUserNotFound() {
        LoginRequest request = new LoginRequest("john", "password123");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.empty());

        LoginResponse response = authService.login(request);

        assertFalse(response.isSuccess());
        assertEquals("Invalid username or password", response.getMessage());
    }

    @Test
    void loginShouldFailWhenPasswordDoesNotMatch() {
        LoginRequest request = new LoginRequest("john", "wrongPassword");

        User user = new User(
                "john",
                "encodedPassword",
                "599123456",
                "john@example.com"
        );

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("wrongPassword", "encodedPassword"))
                .thenReturn(false);

        LoginResponse response = authService.login(request);

        assertFalse(response.isSuccess());
        assertEquals("Invalid username or password", response.getMessage());
    }

    @Test
    void loginShouldSucceedWhenPasswordMatches() {
        LoginRequest request = new LoginRequest("john", "password123");

        User user = new User(
                "john",
                "encodedPassword",
                "599123456",
                "john@example.com"
        );

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password123", "encodedPassword"))
                .thenReturn(true);

        LoginResponse response = authService.login(request);

        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
    }
}