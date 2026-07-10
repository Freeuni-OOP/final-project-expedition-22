package com.example.bookstore.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final GenreRepository genreRepository;
    private final AuthorRepository authorRepository;
    private final Cloudinary cloudinary;

    public BookService(BookRepository bookRepository, UserRepository userRepository,
                       GenreRepository genreRepository, AuthorRepository authorRepository, Cloudinary cloudinary) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.genreRepository = genreRepository;
        this.authorRepository = authorRepository;
        this.cloudinary = cloudinary;
    }

    private String saveImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("სურათის ფაილი ცარიელია.");
        }

        String contentType = image.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") &&
                !contentType.equals("image/jpg") &&
                !contentType.equals("image/png") &&
                !contentType.equals("image/webp"))) {
            throw new IllegalArgumentException("არასწორი ფაილის ფორმატი. ნებადართულია მხოლოდ: JPG, JPEG, PNG, WEBP.");
        }

        long maxSizeBytes = 2 * 1024 * 1024;
        if (image.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("ფაილის ზომა აჭარბებს ლიმიტს (მაქსიმუმ 2MB).");
        }

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(image.getBytes(), ObjectUtils.asMap(
                    "folder", "book_system_images"
            ));
            return (String) uploadResult.get("secure_url");

        } catch (IOException e) {
            throw new RuntimeException("ფაილის ატვირთვა Cloudinary-ზე ვერ მოხერხდა.", e);
        }
    }

    private BookResponse convertToResponse(Book book) {
        String authorsText = "";
        if (book.getAuthors() != null) {
            authorsText = book.getAuthors().stream()
                    .map(Author::getName)
                    .collect(Collectors.joining(", "));
        }

        String genresText = "";
        if (book.getGenres() != null) {
            genresText = book.getGenres().stream()
                    .map(Genre::getName)
                    .collect(Collectors.joining(", "));
        }

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
    public BookResponse createBook(CreateBookRequest request, MultipartFile image) {
        if (image != null && !image.isEmpty()) {
            long MAX_FILE_SIZE = 2 * 1024 * 1024;
            if (image.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("ფაილის ზომა აჭარბებს ლიმიტს (მაქს. 2MB)");
            }

            String contentType = image.getContentType();
            java.util.List<String> allowedTypes = java.util.Arrays.asList("image/jpeg", "image/png", "image/jpg", "image/webp");
            if (contentType == null || !allowedTypes.contains(contentType.toLowerCase())) {
                throw new IllegalArgumentException("არასწორი ფაილის ფორმატი. ნებადართულია მხოლოდ სურათები (JPEG, PNG, WEBP)");
            }
        }

        org.springframework.security.core.Authentication authentication =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new org.springframework.security.access.AccessDeniedException("მომხმარებელი არ არის ავტორიზებული.");
        }

        Object principal = authentication.getPrincipal();
        String currentUsername;
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            currentUsername = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else {
            currentUsername = principal.toString();
        }

        User currentSeller = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("სისტემაში ავტორიზებული მომხმარებელი ვერ მოიძებნა: " + currentUsername));

        Book book = new Book(
                request.getTitle(),
                java.math.BigDecimal.valueOf(request.getPrice()),
                currentSeller,
                true
        );
        book.setReleaseYear(request.getReleaseYear());
        book.setDescription(request.getDescription());

        if (image != null && !image.isEmpty()) {
            String uploadedImageUrl = saveImage(image);
            book.setImageUrl(uploadedImageUrl);
        } else {
            book.setImageUrl(request.getImageUrl());
        }

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
        return bookRepository.findAll().stream()
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
    public BookResponse updateBook(Long id, CreateBookRequest request, MultipartFile image, String username) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("წიგნი განახლებისთვის ვერ მოიძებნა ID-ით: " + id));

        if (book.getSeller() == null || !book.getSeller().getUsername().equals(username)) {
            throw new org.springframework.security.access.AccessDeniedException("You do not own this book");
        }

        book.setTitle(request.getTitle());
        book.setPrice(java.math.BigDecimal.valueOf(request.getPrice()));
        book.setReleaseYear(request.getReleaseYear());
        book.setDescription(request.getDescription());

        if (image != null && !image.isEmpty()) {
            String uploadedImageUrl = saveImage(image);
            book.setImageUrl(uploadedImageUrl);
        } else if (request.getImageUrl() != null) {
            book.setImageUrl(request.getImageUrl());
        }

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

    @Transactional
    public void deleteBook(Long id, String username) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("წიგნი წაშლისთვის ვერ მოიძებნა ID-ით: " + id));

        if (book.getSeller() == null || !book.getSeller().getUsername().equals(username)) {
            throw new org.springframework.security.access.AccessDeniedException("You do not own this book");
        }

        bookRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<BookResponse> searchBooksCombined(String title, String genre, Integer year) {
        List<Book> books = bookRepository.findAll();

        return books.stream()
                .filter(book -> {
                    if (title != null && !title.trim().isEmpty()) {
                        return book.getTitle() != null &&
                                book.getTitle().toLowerCase().contains(title.trim().toLowerCase());
                    }
                    return true;
                })
                .filter(book -> {
                    if (genre != null && !genre.trim().isEmpty()) {
                        if (book.getGenres() == null || book.getGenres().isEmpty()) {
                            return false;
                        }
                        String searchGenre = genre.trim().toLowerCase();
                        return book.getGenres().stream()
                                .anyMatch(g -> g.getName() != null &&
                                        g.getName().toLowerCase().contains(searchGenre));
                    }
                    return true;
                })
                .filter(book -> {
                    if (year != null) {
                        return book.getReleaseYear() != null && book.getReleaseYear().equals(year);
                    }
                    return true;
                })
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addFavorite(String username, Long bookId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("მომხმარებელი ვერ მოიძებნა: " + username));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("წიგნი ვერ მოიძებნა ID-ით: " + bookId));

        user.getFavouriteBooks().add(book);
    }

    @Transactional
    public void removeFavorite(String username, Long bookId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("მომხმარებელი ვერ მოიძებნა: " + username));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("წიგნი ვერ მოიძებნა ID-ით: " + bookId));

        user.getFavouriteBooks().remove(book);
    }

    @Transactional(readOnly = true)
    public List<BookResponse> getFavorites(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("მომხმარებელი ვერ მოიძებნა: " + username));

        List<Book> favoriteBooks = bookRepository.findFavoriteBooksByUserId(user.getId());

        return favoriteBooks.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public String getOwnerPhoneNumberByBookId(Long bookId) {
        return bookRepository.findById(bookId)
                .map(book -> {
                    if (book.getSeller() != null) {
                        return book.getSeller().getPhoneNumber();
                    }
                    return "Not Provided";
                })
                .orElse("Not Provided");
    }

    @Transactional(readOnly = true)
    public String getOwnerNameByBookId(Long bookId) {
        return bookRepository.findById(bookId)
                .map(book -> {
                    if (book.getSeller() != null) {
                        return book.getSeller().getUsername();
                    }
                    return "Unknown";
                })
                .orElse("Unknown");
    }

    @Transactional(readOnly = true)
    public List<BookResponse> searchByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookResponse> searchByAuthor(String author) {
        return bookRepository.findByAuthors_NameContainingIgnoreCase(author)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookResponse> searchByGenre(String genre) {
        return bookRepository.findByGenres_NameContainingIgnoreCase(genre)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookResponse> searchByReleaseYear(Integer year) {
        return bookRepository.findByReleaseYear(year)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookResponse> sortByPrice() {
        return bookRepository.findAllByOrderByPriceAsc()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookResponse> sortByCreatedAt() {
        return bookRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookResponse> sortByReleaseYear() {
        return bookRepository.findAllByOrderByReleaseYearDesc()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookResponse> sortBooks(String field, String direction) {
        List<Book> books;
        boolean isDesc = "desc".equalsIgnoreCase(direction);

        switch (field.toLowerCase()) {
            case "price":
                books = isDesc ? bookRepository.findAllByOrderByPriceDesc() : bookRepository.findAllByOrderByPriceAsc();
                break;
            case "date":
                books = isDesc ? bookRepository.findAllByOrderByCreatedAtDesc() : bookRepository.findAllByOrderByCreatedAtAsc();
                break;
            case "year":
                books = isDesc ? bookRepository.findAllByOrderByReleaseYearDesc() : bookRepository.findAllByOrderByReleaseYearAsc();
                break;
            default:
                throw new IllegalArgumentException("Invalid sort type: " + field);
        }

        return books.stream()
                .map(this::convertToResponse)
                .toList();
    }
}