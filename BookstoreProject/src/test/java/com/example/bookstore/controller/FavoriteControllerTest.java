package com.example.bookstore.controller;

import com.example.bookstore.service.FavoriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link FavoriteController}.
 *
 * Standalone MockMvc, FavoriteService mocked. No Spring Security filter chain
 * is involved, so the 401 path is reached purely via the controller's own
 * `principal == null` check (i.e. simply not attaching a Principal to the
 * request), not via any security config.
 */
@ExtendWith(MockitoExtension.class)
class FavoriteControllerTest {

    @Mock
    private FavoriteService favoriteService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        FavoriteController controller = new FavoriteController(favoriteService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void toggleFavorite_unauthenticated_returns401WithGeorgianMessage() throws Exception {
        mockMvc.perform(post("/api/favorites/toggle/{bookId}", 10L))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("გთხოვთ, გაიაროთ ავტორიზაცია"));

        verifyNoInteractions(favoriteService);
    }

    @Test
    void toggleFavorite_authenticated_addsFavorite_returnsTrue() throws Exception {
        Principal principal = () -> "alice";
        when(favoriteService.toggleFavorite("alice", 10L)).thenReturn(true);

        mockMvc.perform(post("/api/favorites/toggle/{bookId}", 10L).principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.isFavorite").value(true));

        verify(favoriteService).toggleFavorite("alice", 10L);
    }

    @Test
    void toggleFavorite_authenticated_removesFavorite_returnsFalse() throws Exception {
        Principal principal = () -> "alice";
        when(favoriteService.toggleFavorite("alice", 10L)).thenReturn(false);

        mockMvc.perform(post("/api/favorites/toggle/{bookId}", 10L).principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.isFavorite").value(false));
    }

    @Test
    void toggleFavorite_differentUsers_areIsolated() throws Exception {
        Principal alice = () -> "alice";
        Principal bob = () -> "bob";
        when(favoriteService.toggleFavorite("alice", 1L)).thenReturn(true);
        when(favoriteService.toggleFavorite("bob", 1L)).thenReturn(false);

        mockMvc.perform(post("/api/favorites/toggle/{bookId}", 1L).principal(alice))
                .andExpect(jsonPath("$.isFavorite").value(true));

        mockMvc.perform(post("/api/favorites/toggle/{bookId}", 1L).principal(bob))
                .andExpect(jsonPath("$.isFavorite").value(false));
    }

    @Test
    void toggleFavorite_nonNumericBookId_returns400() throws Exception {
        mockMvc.perform(post("/api/favorites/toggle/{bookId}", "not-a-number"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(favoriteService);
    }

    @Test
    void toggleFavorite_serviceThrows_propagates() throws Exception {
        // FavoriteController has no @ExceptionHandler of its own, so an
        // unhandled exception from the service surfaces as an unhandled
        // ServletException in standalone MockMvc (unless a global
        // @ControllerAdvice is registered in setUp()).
        Principal principal = () -> "alice";
        when(favoriteService.toggleFavorite("alice", 404L))
                .thenThrow(new RuntimeException("Book not found"));

        org.junit.jupiter.api.Assertions.assertThrows(jakarta.servlet.ServletException.class,
                () -> mockMvc.perform(post("/api/favorites/toggle/{bookId}", 404L).principal(principal)));
    }
}