package com.example.cloudstorage.controller;

import com.example.cloudstorage.dto.ResourceInfoDto;
import com.example.cloudstorage.security.UserProvider;
import com.example.cloudstorage.service.ResourceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DirectoryController {

    private final ResourceService resourceService;
    private final UserProvider userProvider;

    public DirectoryController(ResourceService resourceService, UserProvider userProvider) {
        this.resourceService = resourceService;
        this.userProvider = userProvider;
    }

    @PostMapping("/directory")
    public ResponseEntity<ResourceInfoDto> createDirectory(@RequestParam String path) {
        Long userId = userProvider.getCurrentUserId();
        ResourceInfoDto result = resourceService.createDirectory(userId, path);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/directory")
    public ResponseEntity<List<ResourceInfoDto>> listDirectory(
            @RequestParam(required = false) String path) {
        Long userId = userProvider.getCurrentUserId();
        List<ResourceInfoDto> result = resourceService.listDirectory(userId, path);
        return ResponseEntity.ok(result);
    }
}