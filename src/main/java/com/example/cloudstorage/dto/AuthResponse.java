package com.example.cloudstorage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthResponse(
        @Schema(description = "Имя пользователя", example = "string")
        String username
) {
}