package com.example.cloudstorage.service;

import com.example.cloudstorage.dto.UserLoginDto;
import com.example.cloudstorage.dto.UserRegisterDto;
import com.example.cloudstorage.exception.UserAlreadyExistsException;
import com.example.cloudstorage.model.User;
import com.example.cloudstorage.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
class AuthServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldCreateUserInDatabaseOnRegister() {
        String username = uniqueUsername();
        authService.register(new UserRegisterDto(username, "password123"));

        Optional<User> saved = userRepository.findByUsernameIgnoreCase(username);
        assertTrue(saved.isPresent());
        assertEquals(username, saved.get().getUsername());
    }

    @Test
    void shouldStoreHashedPasswordNotPlainText() {
        String username = uniqueUsername();
        String rawPassword = "password123";
        authService.register(new UserRegisterDto(username, rawPassword));

        User saved = userRepository.findByUsernameIgnoreCase(username).orElseThrow();
        assertNotEquals(rawPassword, saved.getPassword());
        assertTrue(passwordEncoder.matches(rawPassword, saved.getPassword()));
    }

    @Test
    void shouldThrowWhenUsernameAlreadyExists() {
        String username = uniqueUsername();
        authService.register(new UserRegisterDto(username, "password123"));

        assertThrows(UserAlreadyExistsException.class, () ->
                authService.register(new UserRegisterDto(username, "differentPassword")));
    }

    @Test
    void shouldAuthenticateUserAfterRegister() {
        String username = uniqueUsername();
        authService.register(new UserRegisterDto(username, "password123"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.isAuthenticated());
        assertEquals(username, auth.getName());
    }

    @Test
    void shouldAuthenticateWithCorrectCredentials() {
        String username = uniqueUsername();
        String password = "password123";
        authService.register(new UserRegisterDto(username, password));
        SecurityContextHolder.clearContext();

        authService.authenticate(new UserLoginDto(username, password));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(username, auth.getName());
    }

    @Test
    void shouldThrowWhenPasswordIsIncorrect() {
        String username = uniqueUsername();
        authService.register(new UserRegisterDto(username, "correctPassword"));
        SecurityContextHolder.clearContext();

        assertThrows(BadCredentialsException.class, () ->
                authService.authenticate(new UserLoginDto(username, "wrongPassword")));
    }

    @Test
    void shouldThrowBadCredentialsWhenUsernameDoesNotExist() {
        assertThrows(BadCredentialsException.class, () ->
                authService.authenticate(new UserLoginDto("nonexistent_user_xyz", "password123")));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private String uniqueUsername() {
        return "user_" + UUID.randomUUID();
    }
}