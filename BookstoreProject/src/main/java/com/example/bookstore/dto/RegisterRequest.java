package com.example.bookstore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "username should not be empty")
    @Size(min = 4, max = 20, message = "username length should be between 4 and 20 symbols")
    @Pattern(regexp = "^[^@]+$", message = "username should not contain '@' symbol")
    private String username;

    @NotBlank(message = "password should not be empty")
    @Size(min = 6, message = "password should be at least 6 symbols")
    private String password;

    @NotBlank(message = "phone number should not be empty")
    @Pattern(regexp = "^(\\+995)?5\\d{8}$", message = "phone number patter is wrong")
    private String phoneNumber;

    @NotBlank(message = "email should not be empty")
    @Email(message = "email pattern is wrong")
    private String email;

    public RegisterRequest() {}

    public RegisterRequest(String username, String password, String phoneNumber, String email) {
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.email=email;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}