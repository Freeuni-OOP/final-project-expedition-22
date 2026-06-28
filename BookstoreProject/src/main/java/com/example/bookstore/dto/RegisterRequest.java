package com.example.bookstore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public class RegisterRequest {

    @NotBlank(message = "გთხოვთ, შეიყვანოთ მომხმარებლის სახელი")
    @Size(min = 4, max = 20, message = "მომხმარებლის სახელი უნდა იყოს მინიმუმ 4 და მაქსიმუმ 20 სიგრძის")
    @Pattern(regexp = "^[^@]*$", message = "სახელში არ უნდა იყოს '@' სიმბოლო")
    private String username;

    @NotBlank(message = "გთხოვთ, შეიყვანოთ პაროლი")
    @Size(min = 6, message = "პაროლი უნდა იყოს მინიმუმ 6 სიგრძის")
    private String password;

    @NotBlank(message = "გთხოვთ, შეიყვანოთ ტელეფონის ნომერი")
    @Pattern(regexp = "^(\\+995)?5\\d{8}$", message = "ტელეფონის ნომერი არ არის ვალიდური. მაგ: 599123456, 995599123456")
    private String phoneNumber;

    @NotBlank(message = "გთხოვთ, შეიყვანოთ თქვენი ელ-ფოსტა")
    @Email(message = "ელ-ფოსტა არ არის ვალიდური. მაგ: name@gmail.com")
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