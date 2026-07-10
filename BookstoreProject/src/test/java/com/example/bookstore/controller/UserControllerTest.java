package com.example.bookstore.controller;

import com.example.bookstore.dto.UserProfileResponse;
import com.example.bookstore.entity.User;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private BookRepository bookRepository;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setUsername("testuser");
        mockUser.setEmail("test@example.com");
        mockUser.setPhoneNumber("123456789");
    }

    @Test
    @WithMockUser(username = "testuser")
    void getCurrentUser_success() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUserBooks_success() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(bookRepository.findBySellerIdWithAuthorsAndGenres(1L)).thenReturn(java.util.List.of());

        mockMvc.perform(get("/users/me/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUserFavorites_success() throws Exception {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        // ვინაიდან user.setId(1L) გავწერეთ setUp-ში, აქ გამოიყენებს 1L-ს
        when(bookRepository.findFavoriteBooksByUserIdWithAuthorsAndGenres(1L))
                .thenReturn(java.util.List.of());

        // Act & Assert
        mockMvc.perform(get("/users/me/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "testuser") // ეს დაამატე
    void getUser_success() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }
}
