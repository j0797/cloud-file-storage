package com.example.cloudstorage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ErrorResponse(
        @Schema(description = "Сообщение об ошибке")
        String message
) {
}