package com.example.cloudstorage.controller;

import com.example.cloudstorage.dto.ErrorResponse;
import com.example.cloudstorage.dto.ResourceInfoDto;
import com.example.cloudstorage.security.UserProvider;
import com.example.cloudstorage.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Directories", description = "Создание и просмотр содержимого папок")
@RestController
@RequestMapping("/api")
public class DirectoryController {

    private final ResourceService resourceService;
    private final UserProvider userProvider;

    public DirectoryController(ResourceService resourceService, UserProvider userProvider) {
        this.resourceService = resourceService;
        this.userProvider = userProvider;
    }

    @Operation(
            summary = "Создать папку",
            description = "Создаёт новую папку по указанному пути. Путь должен заканчиваться на '/'"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Папка создана",
                    content = @Content(schema = @Schema(implementation = ResourceInfoDto.class))),
            @ApiResponse(responseCode = "400", description = "Путь не заканчивается на '/' или содержит запрещённые символы",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Родительская папка не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Папка уже существует",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/directory")
    public ResponseEntity<ResourceInfoDto> createDirectory(
            @Parameter(description = "Путь к новой папке, обязательно с '/' на конце",
                    required = true, example = "folder/new/")
            @RequestParam String path) {
        Long userId = userProvider.getCurrentUserId();
        ResourceInfoDto result = resourceService.createDirectory(userId, path);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Operation(
            summary = "Список содержимого папки",
            description = "Возвращает содержимое папки. Пустой path или отсутствие параметра — корневая папка"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список файлов и папок",
                    content = @Content(schema = @Schema(implementation = ResourceInfoDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный путь",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Папка не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/directory")
    public ResponseEntity<List<ResourceInfoDto>> listDirectory(
            @Parameter(description = "Путь к папке (оставьте пустым для корня)", example = "folder/")
            @RequestParam(required = false) String path) {
        Long userId = userProvider.getCurrentUserId();
        List<ResourceInfoDto> result = resourceService.listDirectory(userId, path);
        return ResponseEntity.ok(result);
    }
}