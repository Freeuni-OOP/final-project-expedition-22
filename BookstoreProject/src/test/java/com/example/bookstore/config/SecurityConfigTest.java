package com.example.bookstore.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
        mockMvc.perform(post("/login")).andExpect(status().isNotFound());
    }

    @Test
    void registerEndpoint_ShouldBePermittedForAnonymousUsers() throws Exception {
        mockMvc.perform(post("/register")).andExpect(status().isNotFound());
    }

    @Test
    void securedEndpoint_ShouldDenyAnonymousUser() throws Exception {
        mockMvc.perform(get("/any-other-protected-endpoint")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void securedEndpoint_ShouldAllowAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/any-other-protected-endpoint")).andExpect(status().isNotFound());
    }
}