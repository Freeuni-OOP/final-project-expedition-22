package com.example.bookstore.dto;

import com.example.bookstore.entity.Author;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Genre;
import com.example.bookstore.entity.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BookResponseTest {

    @Test
    void testAllArgsConstructor() {
        BookResponse response = new BookResponse(
                1L,
                "სამოსელი პირველი",
                "გურამ დოჩანაშვილი",
                "რომანი",
                1975,
                29.90,
                "ქართული რომანი....",
                "https://example.com/samoseli.jpg"
        );

        assertEquals(1L, response.getId());
        assertEquals("სამოსელი პირველი", response.getTitle());
        assertEquals("გურამ დოჩანაშვილი", response.getAuthor());
        assertEquals("რომანი", response.getGenre());
        assertEquals(1975, response.getReleaseYear());
        assertEquals(29.90, response.getPrice());
        assertEquals("ქართული რომანი....", response.getDescription());
        assertEquals("https://example.com/samoseli.jpg", response.getImageUrl());
    }

    @Test
    void testEmptyConstructorAndSetters() {
        BookResponse response = new BookResponse();

        assertNotNull(response, "object should be created");
        assertNull(response.getId());
        assertNull(response.getTitle());
        assertNull(response.getAuthor());
        assertNull(response.getPrice());
        assertNull(response.getDescription());
        assertNull(response.getGenre());
        assertNull(response.getImageUrl());
        assertNull(response.getReleaseYear());

        response.setTitle("title");
        response.setPrice(233.2);
        assertEquals("title", response.getTitle());
        assertEquals(233.2, response.getPrice());
    }

    @Test
    void testEmptyConstructorAndGettersSetters() {
        BookResponse response = new BookResponse();

        response.setId(10L);
        response.setTitle("book");
        response.setAuthor("book's author");
        response.setGenre("დრამა");
        response.setReleaseYear(2001);
        response.setPrice(14.00);
        response.setDescription("story is great.");
        response.setImageUrl("https://example.com/jeans.jpg");

        assertEquals(10L, response.getId());
        assertEquals("book", response.getTitle());
        assertEquals("book's author", response.getAuthor());
        assertEquals("დრამა", response.getGenre());
        assertEquals(2001, response.getReleaseYear());
        assertEquals(14.00, response.getPrice());
        assertEquals("story is great.", response.getDescription());
        assertEquals("https://example.com/jeans.jpg", response.getImageUrl());
    }
    @Test
    void defaultConstructorAndSettersShouldWork() {
        BookResponse response = new BookResponse();

        response.setId(1L);
        response.setTitle("Clean Code");
        response.setAuthor("Robert Martin");
        response.setGenre("Programming");
        response.setReleaseYear(2008);
        response.setPrice(35.5);
        response.setDescription("Best practices");
        response.setImageUrl("image.jpg");
        response.setFavorite(true);

        assertEquals(1L, response.getId());
        assertEquals("Clean Code", response.getTitle());
        assertEquals("Robert Martin", response.getAuthor());
        assertEquals("Programming", response.getGenre());
        assertEquals(2008, response.getReleaseYear());
        assertEquals(35.5, response.getPrice());
        assertEquals("Best practices", response.getDescription());
        assertEquals("image.jpg", response.getImageUrl());
        assertTrue(response.isFavorite());
    }

    @Test
    void fullConstructorShouldInitializeFields() {
        BookResponse response = new BookResponse(
                2L,
                "Dune",
                "Frank Herbert",
                "Sci-Fi",
                1965,
                29.99,
                "Classic novel",
                "cover.png"
        );

        assertEquals(2L, response.getId());
        assertEquals("Dune", response.getTitle());
        assertEquals("Frank Herbert", response.getAuthor());
        assertEquals("Sci-Fi", response.getGenre());
        assertEquals(1965, response.getReleaseYear());
        assertEquals(29.99, response.getPrice());
        assertEquals("Classic novel", response.getDescription());
        assertEquals("cover.png", response.getImageUrl());
        assertFalse(response.isFavorite());
    }

    @Test
    void constructorFromBookShouldPopulateFields() {
        User seller = new User();

        Book book = new Book(
                "Effective Java",
                BigDecimal.valueOf(55.50),
                seller,
                true
        );

        book.setReleaseYear(2018);
        book.setDescription("Java best practices");
        book.setImageUrl("effective-java.jpg");

        Author author = new Author("Joshua Bloch");
        Genre genre = new Genre("Programming");

        book.setAuthors(Set.of(author));
        book.setGenres(Set.of(genre));

        BookResponse response = new BookResponse(book);

        assertEquals("Effective Java", response.getTitle());
        assertEquals(55.50, response.getPrice());
        assertEquals(2018, response.getReleaseYear());
        assertEquals("Java best practices", response.getDescription());
        assertEquals("effective-java.jpg", response.getImageUrl());
        assertEquals("Joshua Bloch", response.getAuthor());
        assertEquals("Programming", response.getGenre());
    }

    @Test
    void constructorFromBookShouldHandleEmptyAuthorAndGenre() {
        User seller = new User();

        Book book = new Book(
                "Unknown",
                BigDecimal.TEN,
                seller,
                true
        );

        book.setAuthors(new HashSet<>());
        book.setGenres(new HashSet<>());

        BookResponse response = new BookResponse(book);

        assertEquals("", response.getAuthor());
        assertEquals("", response.getGenre());
    }

}