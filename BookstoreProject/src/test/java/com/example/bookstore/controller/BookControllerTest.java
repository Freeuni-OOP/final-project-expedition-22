package com.example.bookstore.controller;

import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.dto.CreateBookRequest;
import com.example.bookstore.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@WithMockUser
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    private BookResponse bookResponse;
    private CreateBookRequest createBookRequest;

    @BeforeEach
    void setUp() {
        bookResponse = new BookResponse(
                1L,
                "Dracula",
                "Bram Stoker",
                "Horror",
                2016,
                9.99,
                "In good condition.",
                "https://www.goodreads.com/en/book/show/17245.Dracula"
        );

        createBookRequest = new CreateBookRequest();
        createBookRequest.setTitle("Dracula");
        createBookRequest.setAuthor("Bram Stoker");
        createBookRequest.setGenre("Horror");
        createBookRequest.setReleaseYear(2016);
        createBookRequest.setPrice(9.99);
        createBookRequest.setDescription("In good condition.");
        createBookRequest.setImageUrl("https://www.goodreads.com/en/book/show/17245.Dracula");
    }

    @Nested
    class CreateBook {

        @Test
        void createBook_success() throws Exception {
            when(bookService.createBook(any(CreateBookRequest.class))).thenReturn(bookResponse);

            mockMvc.perform(post("/books")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createBookRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.title").value("Dracula"))
                    .andExpect(jsonPath("$.author").value("Bram Stoker"))
                    .andExpect(jsonPath("$.genre").value("Horror"))
                    .andExpect(jsonPath("$.releaseYear").value(2016))
                    .andExpect(jsonPath("$.price").value(9.99))
                    .andExpect(jsonPath("$.description").value("In good condition."))
                    .andExpect(jsonPath("$.imageUrl").value("https://www.goodreads.com/en/book/show/17245.Dracula"));

            verify(bookService, times(1)).createBook(any(CreateBookRequest.class));
        }

        @Test
        void createBook_emptyBody_returns400() throws Exception {
            mockMvc.perform(post("/books")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());

            verify(bookService, never()).createBook(any());
        }

        @Test
        void createBook_noBody_returns400() throws Exception {
            mockMvc.perform(post("/books")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class AddToFavorites {

        @Test
        void addToFavorites_success() throws Exception {
            doNothing().when(bookService).addFavorite(10L, 1L);

            mockMvc.perform(post("/books/1/favorite")
                            .with(csrf())
                            .param("userId", "10"))
                    .andExpect(status().isOk());

            verify(bookService, times(1)).addFavorite(10L, 1L);
        }

        @Test
        void addToFavorites_missingUserId_returns400() throws Exception {
            mockMvc.perform(post("/books/1/favorite")
                            .with(csrf()))
                    .andExpect(status().isBadRequest());

            verify(bookService, never()).addFavorite(any(), any());
        }
    }

    @Nested
    class RemoveFromFavorites {

        @Test
        void removeFromFavorites_success() throws Exception {
            doNothing().when(bookService).removeFavorite(10L, 1L);

            mockMvc.perform(delete("/books/1/favorite")
                            .with(csrf())
                            .param("userId", "10"))
                    .andExpect(status().isNoContent());

            verify(bookService, times(1)).removeFavorite(10L, 1L);
        }

        @Test
        void removeFromFavorites_missingUserId_returns400() throws Exception {
            mockMvc.perform(delete("/books/1/favorite")
                            .with(csrf()))
                    .andExpect(status().isBadRequest());

            verify(bookService, never()).removeFavorite(any(), any());
        }
    }

    @Nested
    class GetFavorites {

        @Test
        void getFavorites_success() throws Exception {
            BookResponse second = new BookResponse(
                    2L,
                    "Hypnos",
                    "H. P. Lovecraft",
                    "Horror",
                    1999,
                    34.99,
                    "Improving the design of existing code.",
                    "https://example.com/Hypnos.jpg"
            );

            when(bookService.getFavorites(10L)).thenReturn(List.of(bookResponse, second));

            mockMvc.perform(get("/books/users/10/favorites"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].title").value("Dracula"))
                    .andExpect(jsonPath("$[0].price").value(9.99))
                    .andExpect(jsonPath("$[1].id").value(2L))
                    .andExpect(jsonPath("$[1].title").value("Hypnos"))
                    .andExpect(jsonPath("$[1].price").value(34.99));

            verify(bookService, times(1)).getFavorites(10L);
        }

        @Test
        void getFavorites_emptyList() throws Exception {
            when(bookService.getFavorites(10L)).thenReturn(List.of());

            mockMvc.perform(get("/books/users/10/favorites"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        void ReturnBookById() throws Exception {
            BookResponse response = new BookResponse(
                    1L,
                    "Refactoring",
                    "Martin Fowler",
                    "Technology",
                    1999,
                    34.99,
                    "Improving the design of existing code.",
                    "https://example.com/refactoring.jpg"
            );

            when(bookService.getBookById(1L))
                    .thenReturn(response);

            mockMvc.perform(get("/books/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Refactoring"))
                    .andExpect(jsonPath("$.price").value(34.99));
        }

        @Test
        void UpdateBook() throws Exception {
            when(bookService.updateBook(eq(1L), any(CreateBookRequest.class)))
                    .thenReturn(bookResponse);

            mockMvc.perform(put("/books/1").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                              "title": "Dracula",
                              "author": "Bram Stoker",
                              "genre": "Horror",
                              "releaseYear": 2016,
                              "price": 9.99,
                              "description": "In good condition.",
                              "imageUrl": "https://www.goodreads.com/en/book/show/17245.Dracula"
                            }
                            """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Dracula"))
                    .andExpect(jsonPath("$.author").value("Bram Stoker"))
                    .andExpect(jsonPath("$.genre").value("Horror"))
                    .andExpect(jsonPath("$.releaseYear").value(2016))
                    .andExpect(jsonPath("$.price").value(9.99))
                    .andExpect(jsonPath("$.description").value("In good condition."))
                    .andExpect(jsonPath("$.imageUrl").value("https://www.goodreads.com/en/book/show/17245.Dracula"));

            verify(bookService).updateBook(eq(1L), any(CreateBookRequest.class));
        }

        @Test
        void DeleteBook() throws Exception {
            doNothing().when(bookService).deleteBook(1L);

            mockMvc.perform(delete("/books/1")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Book deleted successfully"));

            verify(bookService).deleteBook(1L);
        }

        @Test
        void ReturnNotFoundWhenDeletingMissingBook() throws Exception {
            doThrow(new RuntimeException("Book not found"))
                    .when(bookService).deleteBook(1L);

            mockMvc.perform(delete("/books/1")
                            .with(csrf()))
                    .andExpect(status().isNotFound());

            verify(bookService).deleteBook(1L);
        }

    }
    @Nested
    class GetAllBooks {
        @Test
        void shouldReturnAllBooks() throws Exception {
            BookResponse second = new BookResponse(
                    2L,
                    "Hypnos",
                    "H. P. Lovecraft",
                    "Horror",
                    1999,
                    34.99,
                    "Improving the design of existing code.",
                    "https://example.com/Hypnos.jpg"
            );

            when(bookService.getAllBooks()).thenReturn(List.of(bookResponse, second));

            mockMvc.perform(get("/books"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].title").value("Dracula"))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].title").value("Hypnos"));

            verify(bookService, times(1)).getAllBooks();
        }

        @Test
        void shouldReturnEmptyListWhenNoBooksExist() throws Exception {
            when(bookService.getAllBooks()).thenReturn(List.of());

            mockMvc.perform(get("/books"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(bookService, times(1)).getAllBooks();
        }
    }
}