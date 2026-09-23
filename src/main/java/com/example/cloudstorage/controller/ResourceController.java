package com.example.cloudstorage.controller;

import com.example.cloudstorage.domain.ResourcePath;
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
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Tag(name = "Resources", description = "Операции с файлами и папками: получение информации, загрузка, скачивание, перемещение, удаление, поиск")
@RestController
@RequestMapping("/api")
public class ResourceController {

    private final ResourceService resourceService;
    private final UserProvider userProvider;

    public ResourceController(ResourceService resourceService, UserProvider userProvider) {
        this.resourceService = resourceService;
        this.userProvider = userProvider;
    }

    @Operation(
            summary = "Получить информацию о ресурсе",
            description = "Возвращает метаданные (имя, размер, тип) для файла или папки по указанному пути"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ресурс найден",
                    content = @Content(schema = @Schema(implementation = ResourceInfoDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный путь",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ресурс не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/resource")
    public ResponseEntity<ResourceInfoDto> getResourceInfo(
            @Parameter(description = "Путь к ресурсу, например 'folder/file.txt'", required = true, example = "folder/")
            @RequestParam String path) {
        Long userId = userProvider.getCurrentUserId();
        ResourceInfoDto result = resourceService.getResourceInfo(userId, path);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Удалить ресурс",
            description = "Удаляет файл или папку (вместе со всем содержимым) по указанному пути"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ресурс удалён"),
            @ApiResponse(responseCode = "400", description = "Некорректный путь",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ресурс не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/resource")
    public ResponseEntity<Void> deleteResource(
            @Parameter(description = "Путь к ресурсу", required = true, example = "folder/file.txt")
            @RequestParam String path) {
        Long userId = userProvider.getCurrentUserId();
        resourceService.deleteResource(userId, path);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Загрузить файлы",
            description = "Загружает один или несколько файлов в указанную папку (multipart/form-data)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Файлы загружены",
                    content = @Content(schema = @Schema(implementation = ResourceInfoDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный путь, имя файла или пустой список файлов",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Файл с таким именем уже существует",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/resource", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ResourceInfoDto>> uploadFile(
            @Parameter(description = "Путь к целевой папке (пустая строка — корень)", required = true, example = "folder/")
            @RequestParam String path,
            @Parameter(description = "Файлы для загрузки")
            @RequestPart("object") MultipartFile[] files) {
        Long userId = userProvider.getCurrentUserId();
        List<ResourceInfoDto> result = resourceService.uploadFile(userId, path, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Operation(
            summary = "Поиск ресурсов",
            description = "Ищет файлы и папки по части имени, рекурсивно по всему дереву пользователя"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список найденных ресурсов",
                    content = @Content(schema = @Schema(implementation = ResourceInfoDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации входных параметров",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/resource/search")
    public ResponseEntity<List<ResourceInfoDto>> searchResources(
            @Parameter(description = "Часть имени для поиска (регистр не важен)", required = true, example = "report")
            @RequestParam String query) {
        Long userId = userProvider.getCurrentUserId();
        List<ResourceInfoDto> result = resourceService.searchResources(userId, query);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Переместить или переименовать ресурс",
            description = "Перемещает файл или папку по новому пути. Работает и для переименования"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ресурс перемещён",
                    content = @Content(schema = @Schema(implementation = ResourceInfoDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный путь или попытка переместить папку в саму себя",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Источник не найден или целевая папка не существует",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "По целевому пути уже существует ресурс",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/resource/move")
    public ResponseEntity<ResourceInfoDto> moveResource(
            @Parameter(description = "Текущий путь ресурса", required = true, example = "folder/old.txt")
            @RequestParam String from,
            @Parameter(description = "Новый путь ресурса", required = true, example = "folder/new.txt")
            @RequestParam String to) {
        Long userId = userProvider.getCurrentUserId();
        ResourceInfoDto result = resourceService.moveResource(userId, from, to);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Скачать файл или папку",
            description = "Скачивает файл или папку (папка упаковывается в zip). Имя файла возвращается в заголовке Content-Disposition"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Содержимое файла или zip-архив",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)),
            @ApiResponse(responseCode = "400", description = "Некорректный путь",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ресурс не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/resource/download")
    public ResponseEntity<InputStreamResource> downloadResource(
            @Parameter(description = "Путь к файлу или папке", required = true, example = "folder/file.txt")
            @RequestParam String path) {
        Long userId = userProvider.getCurrentUserId();
        InputStreamResource resource = resourceService.downloadResource(userId, path);

        String filename = new ResourcePath(path).fileName();
        if (path.endsWith("/")) {
            filename += ".zip";
        }

        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}