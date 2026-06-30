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

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable Long id) {
        BookResponse response = bookService.getBookById(id);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(@PathVariable Long id,
            @Valid @RequestBody CreateBookRequest request) {

        BookResponse response = bookService.updateBook(id, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok("Book deleted successfully");
    }
}