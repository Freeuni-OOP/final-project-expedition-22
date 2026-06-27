package com.example.bookstore.service;

import com.example.bookstore.dto.LoginRequest;
import com.example.bookstore.dto.LoginResponse;
import com.example.bookstore.dto.RegisterRequest;
import com.example.bookstore.dto.RegisterResponse;
import com.example.bookstore.entity.User;
import com.example.bookstore.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public RegisterResponse register(RegisterRequest request){
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return new RegisterResponse(false, "Username already exists");
        }

        if (userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            return new RegisterResponse(false, "PhoneNumber already in use");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return new RegisterResponse(false, "Email already in use");
        }

        User user = new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getPhoneNumber(),
                request.getEmail()
        );

        userRepository.save(user);

        return new RegisterResponse(true, "Registration successful");
    }

    public LoginResponse login(LoginRequest request) {
        return userRepository.findByUsername(request.getUsername())
                .map(user -> {
                    boolean matches = passwordEncoder.matches(
                            request.getPassword(),
                            user.getPassword()
                    );

                    if (matches) {
                        return new LoginResponse(true, "შესვლა წარმატებულია");
                    }

                    return new LoginResponse(false, "მომხმარებლის სახელი ან პაროლი არასწორია");
                })
                .orElse(new LoginResponse(false, "მომხმარებლის სახელი ან პაროლი არასწორია"));
    }
}
