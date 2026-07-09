package com.example.bookstore.controller;

import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.entity.User;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{id}/books")
    public ResponseEntity<List<BookResponse>> getUserBooks(@PathVariable Long id) {
        List<BookResponse> books = bookRepository.findBySeller_Id(id)
                .stream()
                .map(BookResponse::new)
                .toList();

        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}/favorites")
    public ResponseEntity<List<BookResponse>> getUserFavorites(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<BookResponse> favorites = user.getFavouriteBooks()
                .stream()
                .map(BookResponse::new)
                .toList();

        return ResponseEntity.ok(favorites);
    }
}