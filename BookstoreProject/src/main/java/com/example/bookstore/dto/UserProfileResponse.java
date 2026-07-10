package com.example.bookstore.dto;

public class UserProfileResponse {

    private Long id;
    private String username;
    private String email;
    private String phoneNumber;

    public UserProfileResponse(
            Long id,
            String username,
            String email,
            String phoneNumber
    ) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}