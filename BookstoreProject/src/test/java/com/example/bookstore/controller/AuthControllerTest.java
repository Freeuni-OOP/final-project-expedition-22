package com.example.bookstore.controller;

import com.example.bookstore.dto.LoginRequest;
import com.example.bookstore.dto.LoginResponse;
import com.example.bookstore.dto.RegisterRequest;
import com.example.bookstore.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private AuthService authService;
    @Mock private SecurityContextRepository securityContextRepository;
    @Mock private HttpServletRequest httpRequest;
    @Mock private HttpServletResponse httpResponse;
    @Mock private HttpSession httpSession;
    @Mock private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    private LoginRequest validLoginRequest;
    private RegisterRequest validRegisterRequest;

    @BeforeEach
    void setUp() throws Exception {
        // SecurityContextRepository-ს ინექცია რეფლექციის გამოყენებით
        Field repositoryField = AuthController.class.getDeclaredField("securityContextRepository");
        repositoryField.setAccessible(true);
        repositoryField.set(authController, securityContextRepository);

        validLoginRequest = new LoginRequest();
        validLoginRequest.setUsername("testuser");
        validLoginRequest.setPassword("password123");

        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setUsername("newuser");
        validRegisterRequest.setPassword("password123");
        validRegisterRequest.setEmail("newuser@example.com");

        SecurityContextHolder.clearContext();
    }

    @Test
    void login_Success_ShouldSaveSecurityContext() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // Act
        ResponseEntity<?> response = authController.login(
                validLoginRequest,
                new BeanPropertyBindingResult(validLoginRequest, "loginRequest"),
                httpRequest,
                httpResponse
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(securityContextRepository, times(1))
                .saveContext(any(SecurityContext.class), eq(httpRequest), eq(httpResponse));

        LoginResponse body = (LoginResponse) response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
    }

    @Test
    void login_InvalidCredentials_ShouldReturnUnauthorized() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new AuthenticationException("Invalid") {});

        ResponseEntity<?> response = authController.login(
                validLoginRequest,
                new BeanPropertyBindingResult(validLoginRequest, "loginRequest"),
                httpRequest,
                httpResponse
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verifyNoInteractions(securityContextRepository);
    }

    @Test
    void login_ValidationErrors_ShouldReturnBadRequest() {
        BindingResult bindingResult = new BeanPropertyBindingResult(validLoginRequest, "loginRequest");
        bindingResult.addError(new FieldError("loginRequest", "username", "Required"));

        ResponseEntity<?> response = authController.login(validLoginRequest, bindingResult, httpRequest, httpResponse);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void logout_ShouldClearContextAndInvalidateSession() {
        when(httpRequest.getSession(false)).thenReturn(httpSession);

        authController.logout(httpRequest);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(httpSession).invalidate();
    }

    @Test
    void register_Success_ShouldReturnOk() {
        ResponseEntity<?> response = authController.registerUser(
                validRegisterRequest,
                new BeanPropertyBindingResult(validRegisterRequest, "reg")
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void register_WithMixedValidationErrors_ShouldPrioritizeNotBlank() {
        BindingResult bindingResult = new BeanPropertyBindingResult(validRegisterRequest, "registerRequest");

        bindingResult.addError(new FieldError("registerRequest", "email", "Invalid email", false,
                new String[]{"Email"}, null, "Invalid email"));

        bindingResult.addError(new FieldError("registerRequest", "username", "Username required", false,
                new String[]{"NotBlank"}, null, "Username required"));

        ResponseEntity<?> response = authController.registerUser(validRegisterRequest, bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();

        assertNotNull(responseBody);
        assertTrue(responseBody.containsKey("email"));
        assertTrue(responseBody.containsKey("username"));
        assertEquals("Username required", responseBody.get("username"));
        assertEquals("Invalid email", responseBody.get("email"));
    }

    @Test
    void register_WhenServiceThrowsIllegalArgumentException_ShouldReturnFormattedError() {
        String errorMessage = "username:მომხმარებელი უკვე დაკავებულია";

        doThrow(new IllegalArgumentException(errorMessage))
                .when(authService).register(any(RegisterRequest.class));

        ResponseEntity<?> response = authController.registerUser(
                validRegisterRequest,
                new BeanPropertyBindingResult(validRegisterRequest, "reg")
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody);

        assertTrue(responseBody.containsKey("username"));
        assertEquals("მომხმარებელი უკვე დაკავებულია", responseBody.get("username"));
    }
}