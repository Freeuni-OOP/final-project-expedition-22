package com.example.bookstore.service;

import com.example.bookstore.dto.LoginRequest;
import com.example.bookstore.dto.LoginResponse;
import com.example.bookstore.dto.RegisterRequest;
import com.example.bookstore.dto.RegisterResponse;
import com.example.bookstore.entity.User;
import com.example.bookstore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder);
    }

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

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request)
        );

        assertEquals(
                "username:ეს მომხმარებლის სახელი უკვე დაკავებულია",
                exception.getMessage()
        );

        verify(userRepository).findByUsername("john");
        verify(userRepository, never()).findByPhoneNumber(anyString());
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void registerShouldFailWhenPhoneNumberAlreadyExists() {
        RegisterRequest request = new RegisterRequest(
                "john",
                "password123",
                "599123456",
                "john@example.com"
        );

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.empty());

        when(userRepository.findByPhoneNumber("599123456"))
                .thenReturn(Optional.of(new User()));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request)
        );

        assertEquals(
                "phoneNumber:ეს ტელეფონის ნომერი უკვე გამოყენებულია",
                exception.getMessage()
        );

        verify(userRepository).findByUsername("john");
        verify(userRepository).findByPhoneNumber("599123456");
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void registerShouldFailWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest(
                "john",
                "password123",
                "599123456",
                "john@example.com"
        );

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.empty());

        when(userRepository.findByPhoneNumber("599123456"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(new User()));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request)
        );

        assertEquals(
                "email:ეს ელ-ფოსტა უკვე რეგისტრირებულია",
                exception.getMessage()
        );

        verify(userRepository).findByUsername("john");
        verify(userRepository).findByPhoneNumber("599123456");
        verify(userRepository).findByEmail("john@example.com");
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void registerShouldEncodePasswordSaveUserAndReturnSuccess() {
        RegisterRequest request = new RegisterRequest(
                "john",
                "password123",
                "599123456",
                "john@example.com"
        );

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.empty());

        when(userRepository.findByPhoneNumber("599123456"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("password123"))
                .thenReturn("encoded-password");

        RegisterResponse response = authService.register(request);

        assertTrue(response.isSuccess());
        assertEquals("Registration successful", response.getMessage());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals("john", savedUser.getUsername());
        assertEquals("encoded-password", savedUser.getPassword());
        assertEquals("599123456", savedUser.getPhoneNumber());
        assertEquals("john@example.com", savedUser.getEmail());
        assertNotNull(savedUser.getCreatedAt());

        verify(passwordEncoder).encode("password123");
    }

    @Test
    void loginShouldSucceedWhenPasswordMatches() {
        LoginRequest request = new LoginRequest(
                "john",
                "password123"
        );

        User user = new User(
                "john",
                "encoded-password",
                "599123456",
                "john@example.com"
        );

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "password123",
                "encoded-password"
        )).thenReturn(true);

        LoginResponse response = authService.login(request);

        assertTrue(response.isSuccess());
        assertEquals("შესვლა წარმატებულია", response.getMessage());

        verify(userRepository).findByUsername("john");
        verify(passwordEncoder).matches(
                "password123",
                "encoded-password"
        );
    }

    @Test
    void loginShouldFailWhenPasswordDoesNotMatch() {
        LoginRequest request = new LoginRequest(
                "john",
                "wrong-password"
        );

        User user = new User(
                "john",
                "encoded-password",
                "599123456",
                "john@example.com"
        );

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrong-password",
                "encoded-password"
        )).thenReturn(false);

        LoginResponse response = authService.login(request);

        assertFalse(response.isSuccess());
        assertEquals(
                "მომხმარებლის სახელი ან პაროლი არასწორია",
                response.getMessage()
        );

        verify(userRepository).findByUsername("john");
        verify(passwordEncoder).matches(
                "wrong-password",
                "encoded-password"
        );
    }

    @Test
    void loginShouldFailWhenUserDoesNotExist() {
        LoginRequest request = new LoginRequest(
                "missing-user",
                "password123"
        );

        when(userRepository.findByUsername("missing-user"))
                .thenReturn(Optional.empty());

        LoginResponse response = authService.login(request);

        assertFalse(response.isSuccess());
        assertEquals(
                "მომხმარებლის სახელი ან პაროლი არასწორია",
                response.getMessage()
        );

        verify(userRepository).findByUsername("missing-user");
        verifyNoInteractions(passwordEncoder);
    }
}