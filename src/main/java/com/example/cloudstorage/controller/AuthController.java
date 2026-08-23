package com.example.cloudstorage.controller;

import com.example.cloudstorage.dto.AuthResponse;
import com.example.cloudstorage.dto.UserLoginDto;
import com.example.cloudstorage.dto.UserRegisterDto;
import com.example.cloudstorage.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/sign-up")
    public ResponseEntity<AuthResponse> signUp(@Valid @RequestBody UserRegisterDto userRegisterDto) {
        authService.register(userRegisterDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(userRegisterDto.username()));
    }

    @PostMapping("/auth/sign-in")
    public ResponseEntity<AuthResponse> signIn(@Valid @RequestBody UserLoginDto userLoginDto) {
        authService.authenticate(userLoginDto);
        return ResponseEntity.ok(new AuthResponse(userLoginDto.username()));
    }
}