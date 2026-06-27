package com.example.bookstore.controller;

import com.example.bookstore.dto.LoginRequest;
import com.example.bookstore.dto.LoginResponse;
import com.example.bookstore.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login (@Valid @RequestBody LoginRequest request,  BindingResult result) {
        if(result.hasErrors()) {
            Map<String,String> errors = new HashMap<>();

            for(FieldError error : result.getFieldErrors()) {
                errors.putIfAbsent(error.getField(), error.getDefaultMessage());
            }

            Map<String,Object> body = new HashMap<>();
            body.put("success", false);
            body.put("errors", errors);
            return ResponseEntity.badRequest().body(body);
        }

        LoginResponse response = authService.login(request);
        if(response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);    }
}
