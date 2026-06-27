package com.example.bookstore.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {

    @NotBlank(message = "მომხმარებლის სახელი არ უნდა იყოს ცარიელი")
    private String username;

    @NotBlank(message = "პაროლი არ უნდა იყოს ცარიელი")
    @Size(min = 6, message = "პაროლი უნდა შეიცავდეს მინიმუმ 6 სიმბოლოს")
    private String password;

    public LoginRequest() {}

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}