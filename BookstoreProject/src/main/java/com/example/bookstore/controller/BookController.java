package com.example.bookstore.controller;

import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.dto.CreateBookRequest;
import com.example.bookstore.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;

import org.springframework.data.domain.Sort;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createBook(
            @Valid @ModelAttribute CreateBookRequest request,
            BindingResult bindingResult,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        if (bindingResult.hasErrors()) {
            Map<String, String> fieldErrors = new HashMap<>();

            for (FieldError error : bindingResult.getFieldErrors()) {
                fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
            }

            Map<String, Object> body = new HashMap<>();
            body.put("success", false);
            body.put("fieldErrors", fieldErrors);

            return ResponseEntity.badRequest().body(body);
        }

        BookResponse response = bookService.createBook(request, image);

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
    public ResponseEntity<Void> removeFromFavorites(
            @PathVariable Long id,
            Authentication authentication) {

        bookService.removeFavorite(id, authentication.getName());

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

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long id,
            @Valid @ModelAttribute CreateBookRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        BookResponse response = bookService.updateBook(id, request, image);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok("Book deleted successfully");
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        if (ex.getMessage().contains("ვერ მოიძებნა") || ex.getMessage().contains("not found")) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
        return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @GetMapping("/search/title")
    public ResponseEntity<List<BookResponse>> searchByTitle(@RequestParam String title) {
        return ResponseEntity.ok(bookService.searchByTitle(title));
    }

    @GetMapping("/search/author")
    public ResponseEntity<List<BookResponse>> searchByAuthor(@RequestParam String author) {
        return ResponseEntity.ok(bookService.searchByAuthor(author));
    }

    @GetMapping("/search/genre")
    public ResponseEntity<List<BookResponse>> searchByGenre(@RequestParam String genre) {
        return ResponseEntity.ok(bookService.searchByGenre(genre));
    }

    @GetMapping("/search/year")
    public ResponseEntity<List<BookResponse>> searchByYear(@RequestParam Integer year) {
        return ResponseEntity.ok(bookService.searchByReleaseYear(year));
    }

    @GetMapping("/sort")
    public String sortBooks(
            @RequestParam(name = "field", defaultValue = "price") String field,
            @RequestParam(name = "direction", defaultValue = "asc") String direction,
            org.springframework.ui.Model model) {


        List<BookResponse> sortedBooks = bookService.sortBooks(field, direction);
        model.addAttribute("allBooks", sortedBooks);
        return "index";
    }

    @GetMapping("/search")
    public ResponseEntity<List<BookResponse>> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Integer year) {

        List<BookResponse> books = bookService.searchBooksCombined(title, genre, year);
        return ResponseEntity.ok(books);
    }
}