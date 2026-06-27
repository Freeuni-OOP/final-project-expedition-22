package com.example.bookstore.controller;

import com.example.bookstore.dto.RegisterRequest;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();

            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errors);
        }


        if (userRepository.existsByUsername(request.getUsername())) {
            Map<String, String> errors = new HashMap<>();
            errors.put("username", "ეს მომხმარებლის სახელი უკვე დაკავებულია");
            return ResponseEntity.badRequest().body(errors);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            Map<String, String> errors = new HashMap<>();
            errors.put("email", "ეს ელ-ფოსტა უკვე რეგისტრირებულია");
            return ResponseEntity.badRequest().body(errors);
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            Map<String, String> errors = new HashMap<>();
            errors.put("phoneNumber", "ეს ტელეფონის ნომერი უკვე გამოყენებულია");
            return ResponseEntity.badRequest().body(errors);
        }
        try {
            authService.register(request);
            return ResponseEntity.ok(Map.of("message", "Success"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}