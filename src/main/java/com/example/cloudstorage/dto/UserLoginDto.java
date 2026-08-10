package com.example.cloudstorage.dto;

import jakarta.validation.constraints.NotBlank;

public record UserLoginDto(@NotBlank(message = "Login is required.")
                           String username,

                           @NotBlank(message = "Password is required.")
                           String password) {
}