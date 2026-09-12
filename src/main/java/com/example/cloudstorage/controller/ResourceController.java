package com.example.cloudstorage.controller;

import com.example.cloudstorage.domain.ResourcePath;
import com.example.cloudstorage.dto.ResourceInfoDto;
import com.example.cloudstorage.security.UserProvider;
import com.example.cloudstorage.service.ResourceService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @GetMapping("/resource")
    public ResponseEntity<ResourceInfoDto> getResourceInfo(@RequestParam String path) {
        Long userId = userProvider.getCurrentUserId();
        ResourceInfoDto result = resourceService.getResourceInfo(userId, path);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/directory")
    public ResponseEntity<List<ResourceInfoDto>> listDirectory(@RequestParam String path) {
        Long userId = userProvider.getCurrentUserId();
        List<ResourceInfoDto> result = resourceService.listDirectory(userId, path);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/resource")
    public ResponseEntity<Void> deleteResource(@RequestParam String path) {
        Long userId = userProvider.getCurrentUserId();
        resourceService.deleteResource(userId, path);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/resource")
    public ResponseEntity<List<ResourceInfoDto>> uploadFile(
            @RequestParam String path,
            @RequestPart("file") MultipartFile[] files) {
        Long userId = userProvider.getCurrentUserId();
        List<ResourceInfoDto> result = resourceService.uploadFile(userId, path, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/resource/search")
    public ResponseEntity<List<ResourceInfoDto>> searchResources(@RequestParam String query) {
        Long userId = userProvider.getCurrentUserId();
        List<ResourceInfoDto> result = resourceService.searchResources(userId, query);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/resource/move")
    public ResponseEntity<ResourceInfoDto> moveResource(
            @RequestParam String from,
            @RequestParam String to) {
        Long userId = userProvider.getCurrentUserId();
        ResourceInfoDto result = resourceService.moveResource(userId, from, to);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/resource/download")
    public ResponseEntity<InputStreamResource> downloadResource(@RequestParam String path) {
        Long userId = userProvider.getCurrentUserId();
        InputStreamResource resource = resourceService.downloadResource(userId, path);

        String filename = new ResourcePath(path).fileName();
        if (path.endsWith("/")) {
            filename += ".zip";
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}