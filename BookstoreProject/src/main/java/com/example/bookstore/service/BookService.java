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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final GenreRepository genreRepository;
    private final AuthorRepository authorRepository;

    public BookService(BookRepository bookRepository, UserRepository userRepository,
                       GenreRepository genreRepository, AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.genreRepository = genreRepository;
        this.authorRepository = authorRepository;
    }

    private BookResponse convertToResponse(Book book) {
        String authorsText = book.getAuthors().stream()
                .map(Author::getName)
                .collect(Collectors.joining(", "));

        String genresText = book.getGenres().stream()
                .map(Genre::getName)
                .collect(Collectors.joining(", "));

        return new BookResponse(
                book.getId(),
                book.getTitle(),
                authorsText,
                genresText,
                book.getReleaseYear(),
                book.getPrice() != null ? book.getPrice().doubleValue() : 0.0,
                book.getDescription(),
                book.getImageUrl()
        );
    }

    @Transactional
    public BookResponse createBook(CreateBookRequest request) {

        User currentSeller = userRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("სისტემაში გამყიდველი მომხმარებელი ვერ მოიძებნა"));

        Book book = new Book(
                request.getTitle(),
                java.math.BigDecimal.valueOf(request.getPrice()),
                currentSeller,
                true
        );
        book.setReleaseYear(request.getReleaseYear());
        book.setDescription(request.getDescription());
        book.setImageUrl(request.getImageUrl());

        if (request.getGenre() != null && !request.getGenre().trim().isEmpty()) {
            Genre genre = genreRepository.findByNameIgnoreCase(request.getGenre().trim())
                    .orElseGet(() -> genreRepository.save(new Genre(request.getGenre().trim())));
            book.setGenres(Set.of(genre));
        }

        if (request.getAuthor() != null && !request.getAuthor().trim().isEmpty()) {
            Author author = authorRepository.findByNameIgnoreCase(request.getAuthor().trim())
                    .orElseGet(() -> authorRepository.save(new Author(request.getAuthor().trim())));
            book.setAuthors(Set.of(author));
        }

        Book savedBook = bookRepository.save(book);
        return convertToResponse(savedBook);
    }

    @Transactional(readOnly = true)
    public List<BookResponse> getAllBooks() {
        List<Book> books = bookRepository.findAll();

        return books.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("წიგნი ვერ მოიძებნა ID-ით: " + id));

        return convertToResponse(book);
    }

    @Transactional
    public BookResponse updateBook(Long id, CreateBookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("წიგნი განახლებისთვის ვერ მოიძებნა ID-ით: " + id));

        book.setTitle(request.getTitle());
        book.setPrice(java.math.BigDecimal.valueOf(request.getPrice()));
        book.setReleaseYear(request.getReleaseYear());
        book.setDescription(request.getDescription());
        book.setImageUrl(request.getImageUrl());

        if (request.getGenre() != null && !request.getGenre().trim().isEmpty()) {
            Genre genre = genreRepository.findByNameIgnoreCase(request.getGenre().trim())
                    .orElseGet(() -> genreRepository.save(new Genre(request.getGenre().trim())));
            book.setGenres(Set.of(genre));
        }

        if (request.getAuthor() != null && !request.getAuthor().trim().isEmpty()) {
            Author author = authorRepository.findByNameIgnoreCase(request.getAuthor().trim())
                    .orElseGet(() -> authorRepository.save(new Author(request.getAuthor().trim())));
            book.setAuthors(Set.of(author));
        }

        Book updatedBook = bookRepository.save(book);

        return convertToResponse(updatedBook);
    }

    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new RuntimeException("წიგნი წაშლისთვის ვერ მოიძებნა ID-ით: " + id);
        }

        bookRepository.deleteById(id);
    }


    @Transactional
    public void addFavorite(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("მომხმარებელი ვერ მოიძებნა ID-ით: " + userId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("წიგნი ვერ მოიძებნა ID-ით: " + bookId));

        user.getFavouriteBooks().add(book);
    }

    @Transactional
    public void removeFavorite(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("მომხმარებელი ვერ მოიძებნა ID-ით: " + userId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("წიგნი ვერ მოიძებნა ID-ით: " + bookId));

        user.getFavouriteBooks().remove(book);
    }

    @Transactional
    public List<BookResponse> getFavorites(Long userId) {
        List<Book> favoriteBooks = bookRepository.findFavoriteBooksByUserId(userId);

        return favoriteBooks.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
}