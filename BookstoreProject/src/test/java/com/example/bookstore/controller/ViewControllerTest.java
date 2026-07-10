package com.example.bookstore.controller;

import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ViewController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
class ViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Test
    void home_shouldReturnIndexPageWithBooks() throws Exception {
        when(bookService.getAllBooks()).thenReturn(List.of(new BookResponse()));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("allBooks"));
    }

    @Test
    void sortBooks_shouldReturnIndexWithSortedBooks() throws Exception {
        when(bookService.sortBooks("price", "asc")).thenReturn(List.of());

        mockMvc.perform(get("/sort")
                        .param("field", "price")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("allBooks"));
    }

    @Test
    void showBookDetails_success() throws Exception {
        Long bookId = 1L;
        BookResponse mockBook = new BookResponse();

        when(bookService.getBookById(bookId)).thenReturn(mockBook);
        when(bookService.getOwnerPhoneNumberByBookId(bookId)).thenReturn("123456789");
        when(bookService.getOwnerNameByBookId(bookId)).thenReturn("TestUser");

        mockMvc.perform(get("/books/details/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("book-details"))
                .andExpect(model().attribute("book", mockBook))
                .andExpect(model().attribute("ownerPhone", "123456789"))
                .andExpect(model().attribute("ownerName", "TestUser"));
    }

    @Test
    void showBookDetails_notFound_shouldRedirect() throws Exception {
        when(bookService.getBookById(99L)).thenReturn(null);

        mockMvc.perform(get("/books/details/99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void registerPage_shouldReturnView() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    void loginPage_shouldReturnView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void addBookPage_shouldReturnView() throws Exception {
        mockMvc.perform(get("/books/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/add-book"));
    }

    @Test
    void profilePage_shouldReturnView() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/user-profile"));
    }
}