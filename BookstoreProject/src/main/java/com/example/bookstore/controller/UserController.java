package com.example.bookstore.controller;

import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.dto.UserProfileResponse;
import com.example.bookstore.entity.User;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public UserController(BookRepository bookRepository,
                          UserRepository userRepository) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser(Authentication authentication) {

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber()
        ));
    }

    @GetMapping("/me/books")
    public ResponseEntity<List<BookResponse>> getUserBooks(
            Authentication authentication) {

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<BookResponse> books =
                bookRepository.findBySellerIdWithAuthorsAndGenres(user.getId())
                        .stream()
                        .map(BookResponse::new)
                        .toList();

        return ResponseEntity.ok(books);
    }

    @GetMapping("/me/favorites")
    public ResponseEntity<List<BookResponse>> getUserFavorites(
            Authentication authentication) {

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<BookResponse> favorites =
                bookRepository.findFavoriteBooksByUserIdWithAuthorsAndGenres(user.getId())
                        .stream()
                        .map(BookResponse::new)
                        .toList();

        return ResponseEntity.ok(favorites);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("მომხმარებელი ვერ მოიძებნა"));

        UserProfileResponse response = new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber()
        );

        return ResponseEntity.ok(response);
    }
}