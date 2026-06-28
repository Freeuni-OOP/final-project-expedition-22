package com.example.bookstore.dto;

import org.junit.jupiter.api.Test;
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
}