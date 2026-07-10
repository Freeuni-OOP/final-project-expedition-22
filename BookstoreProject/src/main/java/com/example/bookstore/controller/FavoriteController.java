package com.example.bookstore.controller;

import com.example.bookstore.service.FavoriteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/toggle/{bookId}")
    public ResponseEntity<?> toggleFavorite(@PathVariable Long bookId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("message", "გთხოვთ, გაიაროთ ავტორიზაცია"));
        }

        boolean isFavorite = favoriteService.toggleFavorite(principal.getName(), bookId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "isFavorite", isFavorite
        ));
    }
}