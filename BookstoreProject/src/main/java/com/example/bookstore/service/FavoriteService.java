package com.example.bookstore.service;

import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.User;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FavoriteService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public FavoriteService(UserRepository userRepository, BookRepository bookRepository) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional
    public boolean toggleFavorite(String username, Long bookId) {
        User user = userRepository.findByUsernameWithFavorites(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (user.getFavouriteBooks().contains(book)) {
            user.getFavouriteBooks().remove(book);
            userRepository.save(user);
            return false;
        } else {
            user.getFavouriteBooks().add(book);
            userRepository.save(user);
            return true;
        }
    }
}