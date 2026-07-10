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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@WithMockUser(username = "user")
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
            when(bookService.createBook(any(CreateBookRequest.class), any())).thenReturn(bookResponse);

            MockMultipartFile mockFile = new MockMultipartFile(
                    "image", "test.png", MediaType.IMAGE_PNG_VALUE, "image-content".getBytes()
            );

            mockMvc.perform(multipart("/books")
                            .file(mockFile)
                            .param("title", createBookRequest.getTitle())
                            .param("author", createBookRequest.getAuthor())
                            .param("genre", createBookRequest.getGenre())
                            .param("releaseYear", String.valueOf(createBookRequest.getReleaseYear()))
                            .param("price", String.valueOf(createBookRequest.getPrice()))
                            .param("description", createBookRequest.getDescription())
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.title").value("Dracula"))
                    .andExpect(jsonPath("$.author").value("Bram Stoker"))
                    .andExpect(jsonPath("$.genre").value("Horror"))
                    .andExpect(jsonPath("$.releaseYear").value(2016))
                    .andExpect(jsonPath("$.price").value(9.99))
                    .andExpect(jsonPath("$.description").value("In good condition."))
                    .andExpect(jsonPath("$.imageUrl").value("https://www.goodreads.com/en/book/show/17245.Dracula"));

            verify(bookService, times(1)).createBook(any(CreateBookRequest.class), any());
        }

        @Test
        void createBook_emptyBody_returns400() throws Exception {
            mockMvc.perform(multipart("/books")
                            .with(csrf()))
                    .andExpect(status().isBadRequest());

            verify(bookService, never()).createBook(any(), any());
        }
    }

    @Nested
    class FavoritesManagement {

        @Test
        void addToFavorites_success() throws Exception {
            doNothing().when(bookService).addFavorite("user", 1L);

            mockMvc.perform(post("/books/1/favorite")
                            .with(csrf()))
                    .andExpect(status().isOk());

            verify(bookService, times(1)).addFavorite("user", 1L);
        }

        @Test
        void removeFromFavorites_success() throws Exception {
            doNothing().when(bookService).removeFavorite("user", 1L);

            mockMvc.perform(delete("/books/1/favorite")
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(bookService, times(1)).removeFavorite("user", 1L);
        }

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

            when(bookService.getFavorites("user")).thenReturn(List.of(bookResponse, second));

            mockMvc.perform(get("/books/favorites"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].title").value("Dracula"))
                    .andExpect(jsonPath("$[1].id").value(2L))
                    .andExpect(jsonPath("$[1].title").value("Hypnos"));

            verify(bookService, times(1)).getFavorites("user");
        }

        @Test
        void getFavorites_emptyList() throws Exception {
            when(bookService.getFavorites("user")).thenReturn(List.of());

            mockMvc.perform(get("/books/favorites"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    class BookOperations {

        @Test
        void ReturnBookById() throws Exception {
            when(bookService.getBookById(1L)).thenReturn(bookResponse);

            mockMvc.perform(get("/books/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Dracula"))
                    .andExpect(jsonPath("$.price").value(9.99));
        }

        @Test
        void UpdateBook() throws Exception {
            when(bookService.updateBook(eq(1L), any(CreateBookRequest.class), any(), eq("user")))
                    .thenReturn(bookResponse);

            MockMultipartFile mockFile = new MockMultipartFile(
                    "image", "test.png", MediaType.IMAGE_PNG_VALUE, "image-content".getBytes()
            );

            mockMvc.perform(multipart(HttpMethod.PUT, "/books/1")
                            .file(mockFile)
                            .param("title", "Dracula")
                            .param("author", "Bram Stoker")
                            .param("genre", "Horror")
                            .param("releaseYear", "2016")
                            .param("price", "9.99")
                            .param("description", "In good condition.")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Dracula"));

            verify(bookService).updateBook(eq(1L), any(CreateBookRequest.class), any(), eq("user"));
        }

        @Test
        void DeleteBook() throws Exception {
            doNothing().when(bookService).deleteBook(1L, "user");

            mockMvc.perform(delete("/books/1")
                            .with(csrf()))
                    .andExpect(status().isOk());

            verify(bookService).deleteBook(1L, "user");
        }

    }

    @Nested
    class SearchAndFilterBooks {

        @Test
        void shouldReturnAllBooks() throws Exception {
            when(bookService.getAllBooks()).thenReturn(List.of(bookResponse));

            mockMvc.perform(get("/books"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].title").value("Dracula"));

            verify(bookService, times(1)).getAllBooks();
        }

        @Test
        void shouldSearchBooksByTitle() throws Exception {
            when(bookService.searchByTitle("Dracula")).thenReturn(List.of(bookResponse));

            mockMvc.perform(get("/books/search/title")
                            .param("title", "Dracula"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title").value("Dracula"));

            verify(bookService).searchByTitle("Dracula");
        }

        @Test
        void shouldSearchBooksByAuthor() throws Exception {
            when(bookService.searchByAuthor("Bram Stoker")).thenReturn(List.of(bookResponse));

            mockMvc.perform(get("/books/search/author")
                            .param("author", "Bram Stoker"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].author").value("Bram Stoker"));

            verify(bookService).searchByAuthor("Bram Stoker");
        }

        @Test
        void shouldSearchBooksByGenre() throws Exception {
            when(bookService.searchByGenre("Horror")).thenReturn(List.of(bookResponse));

            mockMvc.perform(get("/books/search/genre")
                            .param("genre", "Horror"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].genre").value("Horror"));

            verify(bookService).searchByGenre("Horror");
        }

        @Test
        void shouldSearchBooksByYear() throws Exception {
            when(bookService.searchByReleaseYear(2016)).thenReturn(List.of(bookResponse));

            mockMvc.perform(get("/books/search/year")
                            .param("year", "2016"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].releaseYear").value(2016));

            verify(bookService).searchByReleaseYear(2016);
        }


    }
}