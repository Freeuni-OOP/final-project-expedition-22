package com.example.bookstore.controller;

import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.service.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;

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

    @GetMapping("/books/details/{id}")
    public String showBookDetails(@PathVariable("id") Long id, Model model) {
        BookResponse book = bookService.getBookById(id);
        if (book == null) {
            return "redirect:/";
        }
        model.addAttribute("book", book);

        String ownerPhone = bookService.getOwnerPhoneNumberByBookId(id);
        model.addAttribute("ownerPhone", ownerPhone);

        return "book-details";
    }
}