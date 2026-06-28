package com.example.bookstore.repository;

import com.example.bookstore.entity.Author;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AuthorRepositoryTest {

    @Autowired
    private AuthorRepository authorRepository;

    @Test
    void SaveAuthor() {
        Author author = new Author("George Orwell");

        Author savedAuthor = authorRepository.save(author);

        assertNotNull(savedAuthor.getId());
        assertEquals("George Orwell", savedAuthor.getName());
    }

    @Test
    void FindAuthorById() {
        Author author = new Author("George Orwell");
        Author savedAuthor = authorRepository.save(author);

        Author foundAuthor = authorRepository.findById(savedAuthor.getId()).orElse(null);

        assertNotNull(foundAuthor);
        assertEquals("George Orwell", foundAuthor.getName());
    }

    @Test
    void UpdateAuthor() {
        Author author = new Author("Old Name");
        Author savedAuthor = authorRepository.save(author);

        savedAuthor.setName("Better Name");
        authorRepository.save(savedAuthor);

        Author updatedAuthor = authorRepository.findById(savedAuthor.getId()).orElse(null);

        assertNotNull(updatedAuthor);
        assertEquals("Better Name", updatedAuthor.getName());
    }

    @Test
    void DeleteAuthor() {
        Author author = new Author("George Orwell");
        Author savedAuthor = authorRepository.save(author);

        authorRepository.deleteById(savedAuthor.getId());

        assertFalse(authorRepository.findById(savedAuthor.getId()).isPresent());
    }

    @Test
    void FindByNameIgnoreCase() {
        Author author1 = new Author("MIKE BEATLES");
        authorRepository.save(author1);

        Optional<Author> foundAuthor1 = authorRepository.findByNameIgnoreCase("Mike Beatles");
        assertTrue(foundAuthor1.isPresent());
        assertEquals("MIKE BEATLES", foundAuthor1.get().getName());


        Author author2 = new Author("GEORGE ORWELL");
        authorRepository.save(author2);

        Optional<Author> foundAuthor2 = authorRepository.findByNameIgnoreCase("George Orwell");
        assertTrue(foundAuthor2.isPresent());
        assertEquals("GEORGE ORWELL", foundAuthor2.get().getName());
    }

    @Test
    void FindByNameIgnoreCaseNotFound() {
        Optional<Author> foundAuthor = authorRepository.findByNameIgnoreCase("Author");

        assertFalse(foundAuthor.isPresent());
    }
}