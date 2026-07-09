package com.example.bookstore.controller;

import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.service.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ViewController {

    private final BookService bookService;

    public ViewController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<BookResponse> books = bookService.getAllBooks();
        model.addAttribute("allBooks", books);
        return "index";
    }

    @GetMapping("/sort")
    public String sortBooks(
            @RequestParam(name = "field", defaultValue = "price") String field,
            @RequestParam(name = "direction", defaultValue = "asc") String direction,
            Model model) {
        List<BookResponse> sortedBooks = bookService.sortBooks(field, direction);
        model.addAttribute("allBooks", sortedBooks);

        return "index";
    }


        @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/books/add")
    public String addBookPage() {
        return "pages/add-book";
    }
}