package com.example.cloudstorage.service;

import com.example.cloudstorage.exception.UserAlreadyExistsException;
import com.example.cloudstorage.model.User;
import com.example.cloudstorage.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class UserServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldCreateUserInDatabase() {
        String username = uniqueUsername();
        userService.createUser(username, "password123");
        Optional<User> saved = userRepository.findByUsernameIgnoreCase(username);
        assertTrue(saved.isPresent());
        assertEquals(username, saved.get().getUsername());
    }

    @Test
    void shouldStoreHashedPasswordNotPlainText() {
        String username = uniqueUsername();
        String rawPassword = "password123";
        userService.createUser(username, rawPassword);
        User saved = userRepository.findByUsernameIgnoreCase(username).orElseThrow();
        assertNotEquals(rawPassword, saved.getPassword());
        assertTrue(passwordEncoder.matches(rawPassword, saved.getPassword()));
    }

    @Test
    void shouldThrowWhenUsernameAlreadyExists() {
        String username = uniqueUsername();
        userService.createUser(username, "password123");
        assertThrows(UserAlreadyExistsException.class, () ->
                userService.createUser(username, "differentPassword")
        );
    }

    private String uniqueUsername() {
        return "user_" + UUID.randomUUID();
    }
}