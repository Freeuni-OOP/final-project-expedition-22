package com.example.bookstore.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginEndpoint_ShouldBePermittedForAnonymousUsers() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());    }

    @Test
    void registerEndpoint_ShouldBePermittedForAnonymousUsers() throws Exception {
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void securedEndpoint_ShouldAllowAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/any-other-protected-endpoint")).andExpect(status().isNotFound());
    }

    @Test
    void booksGet_ShouldBePermittedForAnonymousUsers() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk());
    }

    @Test
    void booksPost_ShouldRequireAuthentication() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void favoritesEndpoint_ShouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/favorites"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void chatEndpoint_ShouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/chat"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void sortEndpoint_ShouldBePermittedForAnonymousUsers() throws Exception {
        mockMvc.perform(get("/sort"))
                .andExpect(status().isOk());
    }
}