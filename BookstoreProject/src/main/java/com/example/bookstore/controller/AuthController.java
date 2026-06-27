package com.example.bookstore.controller;

import com.example.bookstore.dto.RegisterRequest;
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

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();

            for (FieldError error : bindingResult.getFieldErrors()) {
                if (error.getCode().equals("NotBlank")) {
                    errors.put(error.getField(), error.getDefaultMessage());
                }
            }
            for (FieldError error : bindingResult.getFieldErrors()) {
                if (!errors.containsKey(error.getField())) {
                    errors.put(error.getField(), error.getDefaultMessage());
                }
            }
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            authService.register(request);

            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("message", "რეგისტრაცია წარმატებით დასრულდა!");
            return ResponseEntity.ok(successResponse);

        } catch (IllegalArgumentException e) {
            String[] parts = e.getMessage().split(":", 2);
            String field = parts[0];
            String message = parts[1];

            Map<String, String> errors = new HashMap<>();
            errors.put(field, message);
            return ResponseEntity.badRequest().body(errors);
        }
    }
}