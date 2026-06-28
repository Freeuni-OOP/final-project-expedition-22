package com.example.bookstore.service;

import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.dto.CreateBookRequest;
import com.example.bookstore.entity.Author;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Genre;
import com.example.bookstore.entity.User;
import com.example.bookstore.repository.AuthorRepository;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.GenreRepository;
import com.example.bookstore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GenreRepository genreRepository;
    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private BookService bookService;

    private User sampleUser;
    private Book sampleBook;
    private CreateBookRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleUser = new User("seller_john", "password", "555123456", "john@gmail.com");
        ReflectionTestUtils.setField(sampleUser, "id", 1L);
        sampleUser.setFavouriteBooks(new HashSet<>());

        sampleBook = new Book("Animal Farm", BigDecimal.valueOf(12.50), sampleUser, true);
        ReflectionTestUtils.setField(sampleBook, "id", 10L);

        sampleBook.setReleaseYear(1945);
        sampleBook.setDescription("Classic satirical novella");
        sampleBook.setImageUrl("http://image.com/animal_farm.png");
        sampleBook.setGenres(new HashSet<>(Set.of(new Genre("Fiction"))));
        sampleBook.setAuthors(new HashSet<>(Set.of(new Author("George Orwell"))));

        sampleRequest = new CreateBookRequest();
        sampleRequest.setTitle("Animal Farm");
        sampleRequest.setPrice(12.50);
        sampleRequest.setReleaseYear(1945);
        sampleRequest.setDescription("Classic satirical novella");
        sampleRequest.setImageUrl("http://image.com/animal_farm.png");
        sampleRequest.setGenre("Fiction");
        sampleRequest.setAuthor("George Orwell");
    }

    @Test
    void CreateBook_Success() {
        when(userRepository.findAll()).thenReturn(List.of(sampleUser));
        when(genreRepository.findByNameIgnoreCase("Fiction")).thenReturn(Optional.empty());
        when(genreRepository.save(any(Genre.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(authorRepository.findByNameIgnoreCase("George Orwell")).thenReturn(Optional.empty());
        when(authorRepository.save(any(Author.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

        BookResponse response = bookService.createBook(sampleRequest);

        assertNotNull(response);
        assertEquals("Animal Farm", response.getTitle());
        assertEquals(12.50, response.getPrice());
        assertEquals("George Orwell", response.getAuthor());
        assertEquals("Fiction", response.getGenre());
    }

    @Test
    void GetAllBooks() {
        when(bookRepository.findAll()).thenReturn(List.of(sampleBook));

        List<BookResponse> result = bookService.getAllBooks();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Animal Farm", result.get(0).getTitle());
    }

    @Test
    void GetBookById_Success() {
        when(bookRepository.findById(10L)).thenReturn(Optional.of(sampleBook));

        BookResponse response = bookService.getBookById(10L);

        assertNotNull(response);
        assertEquals("Animal Farm", response.getTitle());
    }

    @Test
    void GetBookById_ThrowsException_WhenNotFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            bookService.getBookById(99L);
        });

        assertTrue(exception.getMessage().contains("წიგნი ვერ მოიძებნა ID-ით: 99"));
    }

    @Test
    void UpdateBook_Success() {
        when(bookRepository.findById(10L)).thenReturn(Optional.of(sampleBook));
        when(genreRepository.findByNameIgnoreCase("Fiction")).thenReturn(Optional.of(new Genre("Fiction")));
        when(authorRepository.findByNameIgnoreCase("George Orwell")).thenReturn(Optional.of(new Author("George Orwell")));
        when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

        BookResponse response = bookService.updateBook(10L, sampleRequest);

        assertNotNull(response);
        assertEquals("Animal Farm", response.getTitle());
    }

    @Test
    void DeleteBook_Success() {
        when(bookRepository.existsById(10L)).thenReturn(true);

        bookService.deleteBook(10L);
        verify(bookRepository, times(1)).deleteById(10L);
    }

    @Test
    void DeleteBook_ThrowsException_WhenNotFound() {
        when(bookRepository.existsById(99L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            bookService.deleteBook(99L);
        });
    }

    @Test
    void AddFavorite_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(sampleBook));

        bookService.addFavorite(1L, 10L);

        assertTrue(sampleUser.getFavouriteBooks().contains(sampleBook));
    }

    @Test
    void RemoveFavorite_Success() {
        sampleUser.getFavouriteBooks().add(sampleBook);
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(sampleBook));

        bookService.removeFavorite(1L, 10L);

        assertFalse(sampleUser.getFavouriteBooks().contains(sampleBook));
    }

    @Test
    void GetFavorites() {
        when(bookRepository.findFavoriteBooksByUserId(1L)).thenReturn(List.of(sampleBook));

        List<BookResponse> favorites = bookService.getFavorites(1L);

        assertNotNull(favorites);
        assertEquals(1, favorites.size());
        assertEquals("Animal Farm", favorites.get(0).getTitle());
    }
}