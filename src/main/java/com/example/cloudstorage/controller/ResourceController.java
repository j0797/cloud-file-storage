package com.example.cloudstorage.controller;

import com.example.cloudstorage.dto.ResourceInfoDto;
import com.example.cloudstorage.security.UserProvider;
import com.example.cloudstorage.service.ResourceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ResourceController {

    private final ResourceService resourceService;
    private final UserProvider userProvider;

    public ResourceController(ResourceService resourceService, UserProvider userProvider) {
        this.resourceService = resourceService;
        this.userProvider = userProvider;
    }

    @PostMapping("/directory")
    public ResponseEntity<ResourceInfoDto> createDirectory(
            @RequestParam String path) {
        Long userId = userProvider.getCurrentUserId();
        ResourceInfoDto result = resourceService.createDirectory(userId, path);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}