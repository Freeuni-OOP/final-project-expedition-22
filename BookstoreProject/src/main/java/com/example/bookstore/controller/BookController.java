package com.example.bookstore.controller;

import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.dto.CreateBookRequest;
import com.example.bookstore.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody CreateBookRequest request) {
        BookResponse response = bookService.createBook(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<BookResponse>> getAllBooks() {
        List<BookResponse> books = bookService.getAllBooks();

        return ResponseEntity.ok(books);
    }

    @PostMapping("/{id}/favorite")
    public ResponseEntity<Void> addToFavorites(@PathVariable("id") Long bookId, @RequestParam Long userId) {
        bookService.addFavorite(userId, bookId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/favorite")
    public ResponseEntity<Void> removeFromFavorites(@PathVariable("id") Long bookId, @RequestParam Long userId) {
        bookService.removeFavorite(userId, bookId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{userId}/favorites")
    public ResponseEntity<List<BookResponse>> getFavorites(@PathVariable("userId") Long userId) {
        List<BookResponse> favorites = bookService.getFavorites(userId);

        return ResponseEntity.ok(favorites);
    }
}