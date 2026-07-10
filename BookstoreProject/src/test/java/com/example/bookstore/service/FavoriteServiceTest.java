package com.example.bookstore.service;

import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.User;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    private FavoriteService favoriteService;

    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        favoriteService = new FavoriteService(userRepository, bookRepository);

        user = new User();
        user.setFavouriteBooks(new HashSet<>());

        book = new Book();
    }

    @Test
    void toggleFavoriteShouldAddBookWhenNotAlreadyFavorite() {
        when(userRepository.findByUsernameWithFavorites("john"))
                .thenReturn(Optional.of(user));
        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        boolean result = favoriteService.toggleFavorite("john", 1L);

        assertTrue(result);
        assertTrue(user.getFavouriteBooks().contains(book));

        verify(userRepository).save(user);
    }

    @Test
    void toggleFavoriteShouldRemoveBookWhenAlreadyFavorite() {
        user.getFavouriteBooks().add(book);

        when(userRepository.findByUsernameWithFavorites("john"))
                .thenReturn(Optional.of(user));
        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        boolean result = favoriteService.toggleFavorite("john", 1L);

        assertFalse(result);
        assertFalse(user.getFavouriteBooks().contains(book));

        verify(userRepository).save(user);
    }

    @Test
    void toggleFavoriteShouldThrowWhenUserDoesNotExist() {
        when(userRepository.findByUsernameWithFavorites("john"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> favoriteService.toggleFavorite("john", 1L)
        );

        assertEquals("User not found", exception.getMessage());

        verify(bookRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any());
    }

    @Test
    void toggleFavoriteShouldThrowWhenBookDoesNotExist() {
        when(userRepository.findByUsernameWithFavorites("john"))
                .thenReturn(Optional.of(user));
        when(bookRepository.findById(1L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> favoriteService.toggleFavorite("john", 1L)
        );

        assertEquals("Book not found", exception.getMessage());

        verify(userRepository, never()).save(any());
    }
}