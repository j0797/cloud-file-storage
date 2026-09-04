package com.example.cloudstorage.service.impl;

import com.example.cloudstorage.domain.ResourcePath;
import com.example.cloudstorage.domain.StorageResource;
import com.example.cloudstorage.dto.ResourceInfoDto;
import com.example.cloudstorage.exception.*;
import com.example.cloudstorage.mapper.ResourceMapper;
import com.example.cloudstorage.service.ResourceService;
import com.example.cloudstorage.storage.Storage;
import com.example.cloudstorage.storage.UserStoragePathResolver;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ResourceServiceImpl implements ResourceService {

    private final Storage storage;
    private final UserStoragePathResolver pathResolver;
    private final ResourceMapper resourceMapper;

    public ResourceServiceImpl(Storage storage, UserStoragePathResolver pathResolver, ResourceMapper resourceMapper) {
        this.storage = storage;
        this.pathResolver = pathResolver;
        this.resourceMapper = resourceMapper;
    }

    @Override
    public ResourceInfoDto createDirectory(Long userId, String path) {
        ResourcePath resourcePath = new ResourcePath(path);
        ensureDirectory(resourcePath);

        String folderKey = pathResolver.toStoragePath(userId, path);
        ensureNotExists(folderKey);

        String parentPath = resourcePath.parentPath();
        if (!parentPath.isEmpty()) {
            String parentKey = pathResolver.toStoragePath(userId, parentPath);
            if (!storage.exists(parentKey)) {
                throw new ResourceNotFoundException("Parent directory does not exist");
            }
        }

        storage.createDirectory(folderKey);

        StorageResource resource = storage.getInfo(folderKey)
                .orElseThrow(() -> new StorageException("Directory was created but could not be verified: " + folderKey));
        return resourceMapper.toDto(resource, userId);
    }

    @Override
    public ResourceInfoDto getResourceInfo(Long userId, String path) {
        ResourcePath resourcePath = new ResourcePath(path);
        String storageKey = pathResolver.toStoragePath(userId, resourcePath.path());
        StorageResource resource = storage.getInfo(storageKey)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + path));
        return resourceMapper.toDto(resource, userId);
    }

    @Override
    public List<ResourceInfoDto> listDirectory(Long userId, String path) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<ResourceInfoDto> uploadFile(Long userId, String path, MultipartFile[] files) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void deleteResource(Long userId, String path) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public InputStreamResource downloadResource(Long userId, String path) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ResourceInfoDto moveResource(Long userId, String fromPath, String toPath) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<ResourceInfoDto> searchResources(Long userId, String query) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private void ensureDirectory(ResourcePath resourcePath) {
        if (!resourcePath.isDirectory()) {
            throw new InvalidPathException("Path must end with '/' for directory operations");
        }
    }

    private void ensureNotExists(String storageKey) {
        if (storage.exists(storageKey)) {
            throw new ResourceAlreadyExistsException("Resource already exists");
        }
    }
}