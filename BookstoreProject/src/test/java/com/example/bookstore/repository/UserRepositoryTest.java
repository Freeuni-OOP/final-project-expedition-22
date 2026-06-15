package com.example.bookstore.repository;

import com.example.bookstore.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void SaveUser() {
        User user = new User("jhon", "password123", "555123456");

        User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId());
        assertEquals("jhon", savedUser.getUsername());
    }

    @Test
    void FindUserById() {
        User user = new User("jhon", "password123", "599123456");
        User savedUser = userRepository.save(user);

        User foundUser = userRepository.findById(savedUser.getId()).orElse(null);

        assertNotNull(foundUser);
        assertEquals("jhon", foundUser.getUsername());
    }

    @Test
    void DeleteUser() {
        User user = new User("nino", "password123", "577123456");
        User savedUser = userRepository.save(user);

        userRepository.deleteById(savedUser.getId());

        assertFalse(userRepository.findById(savedUser.getId()).isPresent());
    }
}
