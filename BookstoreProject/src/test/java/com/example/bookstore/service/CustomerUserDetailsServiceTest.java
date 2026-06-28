package com.example.bookstore.service;

import com.example.bookstore.entity.User;
import com.example.bookstore.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


class CustomerUserDetailsServiceTest {


    private final UserRepository userRepository = Mockito.mock(UserRepository.class);

    private final CustomerUserDetailsService userDetailsService =
            new CustomerUserDetailsService(userRepository);



    @Test
    void loadUserByUsernameTest() {

        User user = new User();

        user.setUsername("name");
        user.setPassword("encodedPassword");


        when(userRepository.findByUsername("name"))
                .thenReturn(Optional.of(user));


        UserDetails result =
                userDetailsService.loadUserByUsername("name");


        assertEquals("name", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());

        assertTrue(
                result.getAuthorities()
                        .stream()
                        .anyMatch(a -> a.getAuthority().equals("USER"))
        );
    }



    @Test
    void loadUserByUsernameExceptionTest() {


        when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());


        assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("unknown")
        );
    }
}