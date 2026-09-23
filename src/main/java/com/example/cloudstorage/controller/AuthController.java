package com.example.cloudstorage.controller;

import com.example.cloudstorage.dto.AuthResponse;
import com.example.cloudstorage.dto.ErrorResponse;
import com.example.cloudstorage.dto.UserLoginDto;
import com.example.cloudstorage.dto.UserRegisterDto;
import com.example.cloudstorage.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "Регистрация, вход и выход. Сессия хранится в cookie SESSION")
@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Зарегистрировать нового пользователя",
            description = "Создаёт пользователя и сразу аутентифицирует его (создаёт сессию)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Успешная регистрация",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации входных параметров",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким логином уже существует",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/auth/sign-up")
    public ResponseEntity<AuthResponse> signUp(@Valid @RequestBody UserRegisterDto userRegisterDto) {
        authService.register(userRegisterDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(userRegisterDto.username()));
    }

    @Operation(
            summary = "Войти",
            description = "Аутентифицирует пользователя и создаёт сессию (cookie SESSION)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешный вход",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации входных параметров",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неверное имя пользователя/неверный пароль",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/auth/sign-in")
    public ResponseEntity<AuthResponse> signIn(@Valid @RequestBody UserLoginDto userLoginDto) {
        authService.authenticate(userLoginDto);
        return ResponseEntity.ok(new AuthResponse(userLoginDto.username()));
    }
}