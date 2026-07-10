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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.AfterEach;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

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

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void CreateBook_Success() throws IOException {
        when(cloudinary.uploader()).thenReturn(uploader);
        Map<String, String> cloudinaryResponse = Map.of("secure_url", "https://res.cloudinary.com/mock/image/upload/animal_farm.png");
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(cloudinaryResponse);

        when(userRepository.findByUsername(any())).thenReturn(Optional.of(sampleUser));
        when(genreRepository.findByNameIgnoreCase("Fiction")).thenReturn(Optional.empty());
        when(genreRepository.save(any(Genre.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(authorRepository.findByNameIgnoreCase("George Orwell")).thenReturn(Optional.empty());
        when(authorRepository.save(any(Author.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

        org.springframework.security.core.Authentication auth = mock(org.springframework.security.core.Authentication.class);
        when(auth.getPrincipal()).thenReturn("seller_john");
        when(auth.isAuthenticated()).thenReturn(true);
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        BookResponse response = bookService.createBook(sampleRequest, sampleMultipartFile);

        assertNotNull(response);
        assertEquals("Animal Farm", response.getTitle());
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

        BookResponse response = bookService.updateBook(10L, sampleRequest, sampleMultipartFile, "seller_john");

        assertNotNull(response);
        assertEquals("Animal Farm", response.getTitle());
    }

    @Test
    void UpdateBook_Fails_WhenUserNotOwner() {
        when(bookRepository.findById(10L)).thenReturn(Optional.of(sampleBook));

        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            bookService.updateBook(10L, sampleRequest, sampleMultipartFile, "wrong_user");
        });
    }

    @Test
    void DeleteBook_Success() {
        when(bookRepository.findById(10L)).thenReturn(Optional.of(sampleBook));

        bookService.deleteBook(10L, "seller_john");
        verify(bookRepository, times(1)).deleteById(10L);
    }

    @Test
    void DeleteBook_ThrowsException_WhenNotFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            bookService.deleteBook(99L, "seller_john");
        });
    }

    @Test
    void AddFavorite_Success() {
        when(userRepository.findByUsername("seller_john")).thenReturn(Optional.of(sampleUser));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(sampleBook));

        bookService.addFavorite("seller_john", 10L);

        assertTrue(sampleUser.getFavouriteBooks().contains(sampleBook));
    }

    @Test
    void RemoveFavorite_Success() {
        sampleUser.getFavouriteBooks().add(sampleBook);
        when(userRepository.findByUsername("seller_john")).thenReturn(Optional.of(sampleUser));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(sampleBook));

        bookService.removeFavorite("seller_john", 10L);

        assertFalse(sampleUser.getFavouriteBooks().contains(sampleBook));
    }

    @Test
    void GetFavorites() {
        when(userRepository.findByUsername("seller_john")).thenReturn(Optional.of(sampleUser));
        when(bookRepository.findFavoriteBooksByUserId(1L)).thenReturn(List.of(sampleBook));

        List<BookResponse> favorites = bookService.getFavorites("seller_john");

        assertNotNull(favorites);
        assertEquals(1, favorites.size());
    }

    @Test
    void shouldSearchBooksByTitle() {
        Book book = new Book("Clean Code", BigDecimal.valueOf(30), new User(), true);

        when(bookRepository.findByTitleContainingIgnoreCase("clean"))
                .thenReturn(List.of(book));

        List<BookResponse> result = bookService.searchByTitle("clean");

        assertEquals(1, result.size());
        assertEquals("Clean Code", result.get(0).getTitle());
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
    }

    @Test
    void shouldSortBooksByPrice() {
        Book cheap = new Book("Cheap Book", BigDecimal.valueOf(20), new User(), true);
        Book expensive = new Book("Expensive Book", BigDecimal.valueOf(60), new User(), true);

        when(bookRepository.findAllByOrderByPriceAsc()).thenReturn(List.of(cheap, expensive));

        List<BookResponse> result = bookService.sortBooks("price", "asc");

        assertEquals("Cheap Book", result.get(0).getTitle());
        assertEquals("Expensive Book", result.get(1).getTitle());
    }

    @Test
    void shouldSortBooksByDate() {
        Book book = new Book("Newest Book", BigDecimal.valueOf(30), new User(), true);

        when(bookRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(book));

        List<BookResponse> result = bookService.sortBooks("date", "desc");

        assertEquals(1, result.size());
        assertEquals("Newest Book", result.get(0).getTitle());
    }

    @Test
    void shouldSortBooksByYear() {
        Book book = new Book("Recent Book", BigDecimal.valueOf(30), new User(), true);
        book.setReleaseYear(2024);

        when(bookRepository.findAllByOrderByReleaseYearDesc()).thenReturn(List.of(book));

        List<BookResponse> result = bookService.sortBooks("year", "desc");

        assertEquals(1, result.size());
        assertEquals(2024, result.get(0).getReleaseYear());
    }

    @Test
    void shouldThrowExceptionForInvalidSortType() {
        assertThrows(IllegalArgumentException.class, () -> {
            bookService.sortBooks("unknown", "asc");
        });
    }

    @Test
    void createBookWithoutImageShouldUseRequestImageUrl() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("seller_john");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userRepository.findByUsername("seller_john"))
                .thenReturn(Optional.of(sampleUser));

        Genre genre = new Genre("Fiction");
        Author author = new Author("George Orwell");

        when(genreRepository.findByNameIgnoreCase("Fiction"))
                .thenReturn(Optional.of(genre));
        when(authorRepository.findByNameIgnoreCase("George Orwell"))
                .thenReturn(Optional.of(author));

        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> {
                    Book book = invocation.getArgument(0);
                    ReflectionTestUtils.setField(book, "id", 20L);
                    return book;
                });

        BookResponse response = bookService.createBook(sampleRequest, null);

        assertEquals(sampleRequest.getImageUrl(), response.getImageUrl());

        verifyNoInteractions(cloudinary);
        verify(genreRepository, never()).save(any());
        verify(authorRepository, never()).save(any());
    }

    @Test
    void createBookShouldFailWhenAuthenticationIsMissing() {
        SecurityContextHolder.clearContext();

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> bookService.createBook(sampleRequest, null)
        );

        assertEquals(
                "მომხმარებელი არ არის ავტორიზებული.",
                exception.getMessage()
        );

        verifyNoInteractions(bookRepository);
    }

    @Test
    void createBookShouldFailForAnonymousUser() {
        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThrows(
                AccessDeniedException.class,
                () -> bookService.createBook(sampleRequest, null)
        );
    }

    @Test
    void createBookShouldFailWhenAuthenticatedUserDoesNotExist() {
        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("missing_user");

        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userRepository.findByUsername("missing_user"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> bookService.createBook(sampleRequest, null)
        );

        assertTrue(exception.getMessage().contains("missing_user"));
    }

    @Test
    void createBookShouldReadUsernameFromUserDetails() {
        Authentication authentication = mock(Authentication.class);
        UserDetails principal = mock(UserDetails.class);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(principal.getUsername()).thenReturn("seller_john");

        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userRepository.findByUsername("seller_john"))
                .thenReturn(Optional.of(sampleUser));
        when(genreRepository.findByNameIgnoreCase("Fiction"))
                .thenReturn(Optional.of(new Genre("Fiction")));
        when(authorRepository.findByNameIgnoreCase("George Orwell"))
                .thenReturn(Optional.of(new Author("George Orwell")));
        when(bookRepository.save(any(Book.class)))
                .thenReturn(sampleBook);

        BookResponse response = bookService.createBook(sampleRequest, null);

        assertEquals("Animal Farm", response.getTitle());
        verify(userRepository).findByUsername("seller_john");
    }

    @Test
    void createBookShouldHandleCloudinaryIOException() throws IOException {
        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("seller_john");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userRepository.findByUsername("seller_john"))
                .thenReturn(Optional.of(sampleUser));

        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        when(image.getContentType()).thenReturn("image/png");
        when(image.getSize()).thenReturn(100L);
        when(image.getBytes()).thenThrow(new IOException("upload failure"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> bookService.createBook(sampleRequest, image)
        );

        assertEquals(
                "ფაილის ატვირთვა Cloudinary-ზე ვერ მოხერხდა.",
                exception.getMessage()
        );
    }

    @Test
    void getAllBooksShouldHandleNullCollectionsAndNullPrice() {
        Book book = new Book();
        book.setTitle("Incomplete Book");
        book.setPrice(null);
        book.setAuthors(null);
        book.setGenres(null);

        when(bookRepository.findAll()).thenReturn(List.of(book));

        List<BookResponse> responses = bookService.getAllBooks();

        assertEquals(1, responses.size());
        assertEquals("", responses.get(0).getAuthor());
        assertEquals("", responses.get(0).getGenre());
        assertEquals(0.0, responses.get(0).getPrice());
    }

    @Test
    void updateBookShouldFailWhenBookDoesNotExist() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> bookService.updateBook(
                        99L,
                        sampleRequest,
                        null,
                        "seller_john"
                )
        );

        assertTrue(exception.getMessage().contains("99"));
    }

    @Test
    void updateBookShouldFailWhenBookHasNoSeller() {
        sampleBook.setSeller(null);

        when(bookRepository.findById(10L))
                .thenReturn(Optional.of(sampleBook));

        assertThrows(
                AccessDeniedException.class,
                () -> bookService.updateBook(
                        10L,
                        sampleRequest,
                        null,
                        "seller_john"
                )
        );
    }

    @Test
    void updateBookWithoutUploadedImageShouldUseRequestImageUrl() {
        when(bookRepository.findById(10L))
                .thenReturn(Optional.of(sampleBook));

        when(genreRepository.findByNameIgnoreCase("Fiction"))
                .thenReturn(Optional.of(new Genre("Fiction")));

        when(authorRepository.findByNameIgnoreCase("George Orwell"))
                .thenReturn(Optional.of(new Author("George Orwell")));

        when(bookRepository.save(sampleBook)).thenReturn(sampleBook);

        sampleRequest.setImageUrl("https://example.com/new-cover.jpg");

        BookResponse response = bookService.updateBook(
                10L,
                sampleRequest,
                null,
                "seller_john"
        );

        assertEquals(
                "https://example.com/new-cover.jpg",
                response.getImageUrl()
        );
    }

    @Test
    void updateBookShouldCreateMissingAuthorAndGenre() {
        when(bookRepository.findById(10L))
                .thenReturn(Optional.of(sampleBook));

        when(genreRepository.findByNameIgnoreCase("Fiction"))
                .thenReturn(Optional.empty());
        when(genreRepository.save(any(Genre.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(authorRepository.findByNameIgnoreCase("George Orwell"))
                .thenReturn(Optional.empty());
        when(authorRepository.save(any(Author.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(bookRepository.save(sampleBook)).thenReturn(sampleBook);

        BookResponse response = bookService.updateBook(
                10L,
                sampleRequest,
                null,
                "seller_john"
        );

        assertEquals("Fiction", response.getGenre());
        assertEquals("George Orwell", response.getAuthor());

        verify(genreRepository).save(any(Genre.class));
        verify(authorRepository).save(any(Author.class));
    }

    @Test
    void deleteBookShouldFailWhenUserIsNotOwner() {
        when(bookRepository.findById(10L))
                .thenReturn(Optional.of(sampleBook));

        assertThrows(
                AccessDeniedException.class,
                () -> bookService.deleteBook(10L, "wrong_user")
        );

        verify(userRepository, never())
                .deleteFavoriteReferencesByBookId(anyLong());

        verify(bookRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteBookShouldRemoveFavoriteReferencesBeforeDeleting() {
        when(bookRepository.findById(10L))
                .thenReturn(Optional.of(sampleBook));

        bookService.deleteBook(10L, "seller_john");

        var inOrder = inOrder(userRepository, bookRepository);

        inOrder.verify(userRepository)
                .deleteFavoriteReferencesByBookId(10L);

        inOrder.verify(bookRepository).deleteById(10L);
    }

    @Test
    void searchBooksCombinedShouldFilterByTitleGenreAndYear() {
        Book matching = new Book(
                "Animal Farm",
                BigDecimal.TEN,
                sampleUser,
                true
        );
        matching.setReleaseYear(1945);
        matching.setGenres(Set.of(new Genre("Fiction")));

        Book wrongTitle = new Book(
                "Dune",
                BigDecimal.TEN,
                sampleUser,
                true
        );
        wrongTitle.setReleaseYear(1945);
        wrongTitle.setGenres(Set.of(new Genre("Fiction")));

        when(bookRepository.findAll())
                .thenReturn(List.of(matching, wrongTitle));

        List<BookResponse> result = bookService.searchBooksCombined(
                "animal",
                "fiction",
                1945
        );

        assertEquals(1, result.size());
        assertEquals("Animal Farm", result.get(0).getTitle());
    }

    @Test
    void searchBooksCombinedShouldRejectBookWithoutGenres() {
        Book book = new Book(
                "Animal Farm",
                BigDecimal.TEN,
                sampleUser,
                true
        );
        book.setGenres(Collections.emptySet());

        when(bookRepository.findAll()).thenReturn(List.of(book));

        List<BookResponse> result = bookService.searchBooksCombined(
                null,
                "Fiction",
                null
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void searchBooksCombinedWithNoFiltersShouldReturnAllBooks() {
        when(bookRepository.findAll()).thenReturn(List.of(sampleBook));

        List<BookResponse> result =
                bookService.searchBooksCombined(null, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void addFavoriteShouldFailWhenUserDoesNotExist() {
        when(userRepository.findByUsername("missing"))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> bookService.addFavorite("missing", 10L)
        );

        verify(bookRepository, never()).findById(anyLong());
    }

    @Test
    void addFavoriteShouldFailWhenBookDoesNotExist() {
        when(userRepository.findByUsername("seller_john"))
                .thenReturn(Optional.of(sampleUser));
        when(bookRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> bookService.addFavorite("seller_john", 99L)
        );
    }

    @Test
    void removeFavoriteShouldFailWhenUserDoesNotExist() {
        when(userRepository.findByUsername("missing"))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> bookService.removeFavorite("missing", 10L)
        );
    }

    @Test
    void removeFavoriteShouldFailWhenBookDoesNotExist() {
        when(userRepository.findByUsername("seller_john"))
                .thenReturn(Optional.of(sampleUser));
        when(bookRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> bookService.removeFavorite("seller_john", 99L)
        );
    }

    @Test
    void removeFavoriteShouldSaveUser() {
        sampleUser.getFavouriteBooks().add(sampleBook);

        when(userRepository.findByUsername("seller_john"))
                .thenReturn(Optional.of(sampleUser));
        when(bookRepository.findById(10L))
                .thenReturn(Optional.of(sampleBook));

        bookService.removeFavorite("seller_john", 10L);

        verify(userRepository).save(sampleUser);
    }

    @Test
    void getFavoritesShouldFailWhenUserDoesNotExist() {
        when(userRepository.findByUsername("missing"))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> bookService.getFavorites("missing")
        );
    }

    @Test
    void getOwnerPhoneNumberShouldReturnSellerPhone() {
        when(bookRepository.findById(10L))
                .thenReturn(Optional.of(sampleBook));

        assertEquals(
                "555123456",
                bookService.getOwnerPhoneNumberByBookId(10L)
        );
    }

    @Test
    void getOwnerPhoneNumberShouldReturnDefaultWhenSellerIsMissing() {
        sampleBook.setSeller(null);

        when(bookRepository.findById(10L))
                .thenReturn(Optional.of(sampleBook));

        assertEquals(
                "Not Provided",
                bookService.getOwnerPhoneNumberByBookId(10L)
        );
    }

    @Test
    void getOwnerPhoneNumberShouldReturnDefaultWhenBookIsMissing() {
        when(bookRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertEquals(
                "Not Provided",
                bookService.getOwnerPhoneNumberByBookId(99L)
        );
    }

    @Test
    void getOwnerNameShouldReturnUsername() {
        when(bookRepository.findById(10L))
                .thenReturn(Optional.of(sampleBook));

        assertEquals(
                "seller_john",
                bookService.getOwnerNameByBookId(10L)
        );
    }

    @Test
    void getOwnerNameShouldReturnUnknownWhenSellerIsMissing() {
        sampleBook.setSeller(null);

        when(bookRepository.findById(10L))
                .thenReturn(Optional.of(sampleBook));

        assertEquals(
                "Unknown",
                bookService.getOwnerNameByBookId(10L)
        );
    }

    @Test
    void getOwnerNameShouldReturnUnknownWhenBookIsMissing() {
        when(bookRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertEquals(
                "Unknown",
                bookService.getOwnerNameByBookId(99L)
        );
    }

    @Test
    void sortByPriceShouldUseAscendingRepositoryMethod() {
        when(bookRepository.findAllByOrderByPriceAsc())
                .thenReturn(List.of(sampleBook));

        assertEquals(1, bookService.sortByPrice().size());

        verify(bookRepository).findAllByOrderByPriceAsc();
    }

    @Test
    void sortByCreatedAtShouldUseDescendingRepositoryMethod() {
        when(bookRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(sampleBook));

        assertEquals(1, bookService.sortByCreatedAt().size());

        verify(bookRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void sortByReleaseYearShouldUseDescendingRepositoryMethod() {
        when(bookRepository.findAllByOrderByReleaseYearDesc())
                .thenReturn(List.of(sampleBook));

        assertEquals(1, bookService.sortByReleaseYear().size());

        verify(bookRepository).findAllByOrderByReleaseYearDesc();
    }

    @Test
    void sortBooksShouldSortPriceDescending() {
        when(bookRepository.findAllByOrderByPriceDesc())
                .thenReturn(List.of(sampleBook));

        bookService.sortBooks("price", "desc");

        verify(bookRepository).findAllByOrderByPriceDesc();
    }

    @Test
    void sortBooksShouldSortDateAscending() {
        when(bookRepository.findAllByOrderByCreatedAtAsc())
                .thenReturn(List.of(sampleBook));

        bookService.sortBooks("date", "asc");

        verify(bookRepository).findAllByOrderByCreatedAtAsc();
    }

    @Test
    void sortBooksShouldSortYearAscending() {
        when(bookRepository.findAllByOrderByReleaseYearAsc())
                .thenReturn(List.of(sampleBook));

        bookService.sortBooks("year", "asc");

        verify(bookRepository).findAllByOrderByReleaseYearAsc();
    }

    @Test
    void isBookFavoriteShouldReturnFalseWhenUserDoesNotExist() {
        when(userRepository.findByUsernameWithFavorites("missing"))
                .thenReturn(Optional.empty());

        assertFalse(
                bookService.isBookFavoriteForUser("missing", 10L)
        );

        verify(bookRepository, never()).findById(anyLong());
    }

    @Test
    void isBookFavoriteShouldReturnFalseWhenBookDoesNotExist() {
        when(userRepository.findByUsernameWithFavorites("seller_john"))
                .thenReturn(Optional.of(sampleUser));
        when(bookRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertFalse(
                bookService.isBookFavoriteForUser("seller_john", 99L)
        );
    }

    @Test
    void isBookFavoriteShouldReturnTrueWhenBookIsFavorite() {
        sampleUser.getFavouriteBooks().add(sampleBook);

        when(userRepository.findByUsernameWithFavorites("seller_john"))
                .thenReturn(Optional.of(sampleUser));
        when(bookRepository.findById(10L))
                .thenReturn(Optional.of(sampleBook));

        assertTrue(
                bookService.isBookFavoriteForUser("seller_john", 10L)
        );
    }

    @Test
    void isBookFavoriteShouldReturnFalseWhenBookIsNotFavorite() {
        when(userRepository.findByUsernameWithFavorites("seller_john"))
                .thenReturn(Optional.of(sampleUser));
        when(bookRepository.findById(10L))
                .thenReturn(Optional.of(sampleBook));

        assertFalse(
                bookService.isBookFavoriteForUser("seller_john", 10L)
        );
    }


}