package com.example.bookstore.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    @Mock
    private Cloudinary cloudinary;
    @Mock
    private Uploader uploader;

    @InjectMocks
    private BookService bookService;

    private User sampleUser;
    private Book sampleBook;
    private CreateBookRequest sampleRequest;
    private MockMultipartFile sampleMultipartFile;

    @BeforeEach
    void setUp() {
        sampleUser = new User("seller_john", "password", "555123456", "john@gmail.com");
        ReflectionTestUtils.setField(sampleUser, "id", 1L);
        sampleUser.setFavouriteBooks(new HashSet<>());

        sampleBook = new Book("Animal Farm", BigDecimal.valueOf(12.50), sampleUser, true);
        ReflectionTestUtils.setField(sampleBook, "id", 10L);

        sampleBook.setReleaseYear(1945);
        sampleBook.setDescription("Classic satirical novella");
        sampleBook.setImageUrl("https://res.cloudinary.com/mock/image/upload/animal_farm.png");
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

        sampleMultipartFile = new MockMultipartFile(
                "image", "test.png", MediaType.IMAGE_PNG_VALUE, "fake-image-bytes".getBytes()
        );
    }

    @Test
    void CreateBook_Success() throws IOException {
        when(cloudinary.uploader()).thenReturn(uploader);
        Map<String, String> cloudinaryResponse = Map.of("secure_url", "https://res.cloudinary.com/mock/image/upload/animal_farm.png");
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(cloudinaryResponse);

        when(userRepository.findAll()).thenReturn(List.of(sampleUser));
        when(genreRepository.findByNameIgnoreCase("Fiction")).thenReturn(Optional.empty());
        when(genreRepository.save(any(Genre.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(authorRepository.findByNameIgnoreCase("George Orwell")).thenReturn(Optional.empty());
        when(authorRepository.save(any(Author.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

        BookResponse response = bookService.createBook(sampleRequest, sampleMultipartFile);

        assertNotNull(response);
        assertEquals("Animal Farm", response.getTitle());
        assertEquals(12.50, response.getPrice());
        assertEquals("George Orwell", response.getAuthor());
        assertEquals("Fiction", response.getGenre());
        assertEquals("https://res.cloudinary.com/mock/image/upload/animal_farm.png", response.getImageUrl());
    }

    @Test
    void CreateBook_InvalidFileType_ThrowsException() {
        MockMultipartFile badFile = new MockMultipartFile(
                "image", "danger.exe", MediaType.APPLICATION_OCTET_STREAM_VALUE, "malicious".getBytes()
        );

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bookService.createBook(sampleRequest, badFile);
        });

        assertTrue(exception.getMessage().contains("არასწორი ფაილის ფორმატი"));
    }

    @Test
    void CreateBook_FileTooLarge_ThrowsException() {
        byte[] massiveBytes = new byte[(2 * 1024 * 1024) + 10];
        MockMultipartFile massiveFile = new MockMultipartFile(
                "image", "huge.jpg", MediaType.IMAGE_JPEG_VALUE, massiveBytes
        );

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bookService.createBook(sampleRequest, massiveFile);
        });

        assertTrue(exception.getMessage().contains("ფაილის ზომა აჭარბებს ლიმიტს"));
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
    void UpdateBook_Success() throws IOException {
        when(cloudinary.uploader()).thenReturn(uploader);
        Map<String, String> cloudinaryResponse = Map.of("secure_url", "https://res.cloudinary.com/mock/image/upload/animal_farm.png");
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(cloudinaryResponse);

        when(bookRepository.findById(10L)).thenReturn(Optional.of(sampleBook));
        when(genreRepository.findByNameIgnoreCase("Fiction")).thenReturn(Optional.of(new Genre("Fiction")));
        when(authorRepository.findByNameIgnoreCase("George Orwell")).thenReturn(Optional.of(new Author("George Orwell")));
        when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

        BookResponse response = bookService.updateBook(10L, sampleRequest, sampleMultipartFile);

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

    @Test
    void shouldSearchBooksByTitle() {
        Book book = new Book("Clean Code", BigDecimal.valueOf(30), new User(), true);

        when(bookRepository.findByTitleContainingIgnoreCase("clean"))
                .thenReturn(List.of(book));

        List<BookResponse> result = bookService.searchByTitle("clean");

        assertEquals(1, result.size());
        assertEquals("Clean Code", result.get(0).getTitle());
        verify(bookRepository).findByTitleContainingIgnoreCase("clean");
    }

    @Test
    void shouldSearchBooksByAuthor() {
        Book book = new Book("Some Book", BigDecimal.valueOf(30), new User(), true);
        book.setAuthors(Set.of(new Author("some other")));

        when(bookRepository.findByAuthors_NameContainingIgnoreCase("some other"))
                .thenReturn(List.of(book));

        List<BookResponse> result = bookService.searchByAuthor("some other");

        assertEquals(1, result.size());
        assertEquals("some other", result.get(0).getAuthor());
        verify(bookRepository).findByAuthors_NameContainingIgnoreCase("some other");
    }

    @Test
    void shouldSearchBooksByGenre() {
        Book book = new Book("Dune", BigDecimal.valueOf(40), new User(), true);
        book.setGenres(Set.of(new Genre("Sci-Fi")));

        when(bookRepository.findByGenres_NameContainingIgnoreCase("sci"))
                .thenReturn(List.of(book));

        List<BookResponse> result = bookService.searchByGenre("sci");

        assertEquals(1, result.size());
        assertEquals("Sci-Fi", result.get(0).getGenre());
        verify(bookRepository).findByGenres_NameContainingIgnoreCase("sci");
    }

    @Test
    void shouldSearchBooksByReleaseYear() {
        Book book = new Book("Book 2020", BigDecimal.valueOf(20), new User(), true);
        book.setReleaseYear(2020);

        when(bookRepository.findByReleaseYear(2020))
                .thenReturn(List.of(book));

        List<BookResponse> result = bookService.searchByReleaseYear(2020);

        assertEquals(1, result.size());
        assertEquals(2020, result.get(0).getReleaseYear());
        verify(bookRepository).findByReleaseYear(2020);
    }

    @Test
    void shouldSortBooksByPrice() {
        Book cheap = new Book("Cheap Book", BigDecimal.valueOf(20), new User(), true);
        Book expensive = new Book("Expensive Book", BigDecimal.valueOf(60), new User(), true);

        when(bookRepository.findAllByOrderByPriceAsc())
                .thenReturn(List.of(cheap, expensive));

        List<BookResponse> result = bookService.sortBooks("price");

        assertEquals("Cheap Book", result.get(0).getTitle());
        assertEquals("Expensive Book", result.get(1).getTitle());
        verify(bookRepository).findAllByOrderByPriceAsc();
    }

    @Test
    void shouldSortBooksByDate() {
        Book book = new Book("Newest Book", BigDecimal.valueOf(30), new User(), true);

        when(bookRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(book));

        List<BookResponse> result = bookService.sortBooks("date");

        assertEquals(1, result.size());
        assertEquals("Newest Book", result.get(0).getTitle());
        verify(bookRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void shouldSortBooksByYear() {
        Book book = new Book("Recent Book", BigDecimal.valueOf(30), new User(), true);
        book.setReleaseYear(2024);

        when(bookRepository.findAllByOrderByReleaseYearDesc())
                .thenReturn(List.of(book));

        List<BookResponse> result = bookService.sortBooks("year");

        assertEquals(1, result.size());
        assertEquals(2024, result.get(0).getReleaseYear());
        verify(bookRepository).findAllByOrderByReleaseYearDesc();
    }

    @Test
    void shouldThrowExceptionForInvalidSortType() {
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> bookService.sortBooks("unknown")
        );

        assertEquals("Invalid sort type", exception.getMessage());
    }
}