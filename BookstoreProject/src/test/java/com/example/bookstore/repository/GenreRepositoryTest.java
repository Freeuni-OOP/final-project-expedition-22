package com.example.bookstore.repository;

import com.example.bookstore.entity.Genre;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class GenreRepositoryTest {

    @Autowired
    private GenreRepository genreRepository;

    @Test
    void SaveGenre() {
        Genre genre = new Genre("fiction");

        Genre savedGenre = genreRepository.save(genre);

        assertNotNull(savedGenre.getId());
        assertEquals("fiction", savedGenre.getName());
    }

    @Test
    void FindGenreById() {
        Genre genre = new Genre("fantasy");
        Genre savedGenre = genreRepository.save(genre);

        Genre foundGenre = genreRepository.findById(savedGenre.getId()).orElse(null);

        assertNotNull(foundGenre);
        assertEquals("fantasy", foundGenre.getName());
    }

    @Test
    void UpdateGenre() {
        Genre genre = new Genre("Sci-Fi");
        Genre savedGenre = genreRepository.save(genre);

        savedGenre.setName("science Fiction");
        genreRepository.save(savedGenre);

        Genre updatedGenre = genreRepository.findById(savedGenre.getId()).orElse(null);

        assertNotNull(updatedGenre);
        assertEquals("science Fiction", updatedGenre.getName());
    }

    @Test
    void DeleteGenre() {
        Genre genre = new Genre("horror");
        Genre savedGenre = genreRepository.save(genre);

        assertTrue(genreRepository.findById(savedGenre.getId()).isPresent());
        genreRepository.deleteById(savedGenre.getId());
        assertFalse(genreRepository.findById(savedGenre.getId()).isPresent());
    }

    @Test
    void FindByNameIgnoreCase() {
        Genre genre1 = new Genre("DRAMA");
        genreRepository.save(genre1);

        Optional<Genre> foundGenre1 = genreRepository.findByNameIgnoreCase("drama");
        assertTrue(foundGenre1.isPresent());
        assertEquals("DRAMA", foundGenre1.get().getName());

        Genre genre2 = new Genre("ROMANCE");
        genreRepository.save(genre2);

        Optional<Genre> foundGenre2 = genreRepository.findByNameIgnoreCase("romance");
        assertTrue(foundGenre2.isPresent());
        assertEquals("ROMANCE", foundGenre2.get().getName());
    }

    @Test
    void FindByNameIgnoreCase_NotFound() {
        Optional<Genre> foundGenre = genreRepository.findByNameIgnoreCase("NonExistentGenre");

        assertFalse(foundGenre.isPresent());
    }
}