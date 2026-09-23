package com.example.cloudstorage.dto;

import com.example.cloudstorage.domain.ResourceType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResourceInfoDto(
        @Schema(description = "Путь к родительской папке", example = "folder/")
        String path,

        @Schema(description = "Имя ресурса (для папок с '/' на конце)", example = "file.txt")
        String name,

        @Schema(description = "Размер в байтах (null для папок)", example = "1024")
        Long size,

        @Schema(description = "Тип ресурса", example = "FILE")
        ResourceType type
) {
}