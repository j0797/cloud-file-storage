package com.example.cloudstorage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UserLoginDto(
        @Schema(description = "Логин", example = "user1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Login is required.")
        String username,

        @Schema(description = "Пароль", example = "test12345", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Password is required.")
        String password
) {
}