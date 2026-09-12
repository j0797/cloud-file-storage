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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
        ensureNotExists(folderKey, "Resource already exists");
        ensureParentExists(userId, resourcePath.parentPath());

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
        ResourcePath resourcePath = new ResourcePath(path);
        ensureDirectory(resourcePath);
        String storageKey = pathResolver.toStoragePath(userId, path);
        ensureExists(storageKey, "Directory not found: " + path);
        List<StorageResource> resources = storage.list(storageKey, false);
        return resources.stream()
                .map(resource -> resourceMapper.toDto(resource, userId))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteResource(Long userId, String path) {
        ResourcePath resourcePath = new ResourcePath(path);
        String storageKey = pathResolver.toStoragePath(userId, path);
        ensureExists(storageKey, "Resource not found: " + path);

        if (resourcePath.isDirectory()) {
            List<StorageResource> children = storage.list(storageKey, true);
            List<String> keysToDelete = children.stream()
                    .map(StorageResource::path)
                    .collect(Collectors.toList());
            keysToDelete.add(storageKey);
            storage.deleteObjects(keysToDelete);
        } else {
            storage.delete(storageKey);
        }
    }

    @Override
    public List<ResourceInfoDto> uploadFile(Long userId, String path, MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new InvalidPathException("No files to upload");
        }
        if (!path.endsWith("/") && !path.isEmpty()) {
            path += "/";
        }

        ResourcePath resourcePath = new ResourcePath(path);
        ensureDirectory(resourcePath);
        String folderKey = pathResolver.toStoragePath(userId, path);
        ensureExists(folderKey, "Target directory does not exist: " + path);
        Map<String, MultipartFile> filesToUpload = buildUploadPlan(folderKey, files);
        return uploadFiles(filesToUpload, userId);
    }

    @Override
    public List<ResourceInfoDto> searchResources(Long userId, String query) {
        if (query == null || query.isBlank()) {
            throw new InvalidPathException("Search query cannot be empty");
        }
        String userPrefix = pathResolver.toStoragePath(userId, "");
        List<StorageResource> allResources = storage.list(userPrefix, true);
        String lowerQuery = query.toLowerCase();
        return allResources.stream()
                .map(resource -> resourceMapper.toDto(resource, userId))
                .filter(dto -> dto.name().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }

    @Override
    public ResourceInfoDto moveResource(Long userId, String fromPath, String toPath) {
        ResourcePath fromResourcePath = new ResourcePath(fromPath);
        ResourcePath toResourcePath = new ResourcePath(toPath);
        String fromKey = pathResolver.toStoragePath(userId, fromPath);
        String toKey = pathResolver.toStoragePath(userId, toPath);
        validateMove(fromResourcePath, toResourcePath, fromKey, toKey, userId, toResourcePath.parentPath());

        if (fromResourcePath.isDirectory()) {
            moveDirectory(fromKey, toKey);
        } else {
            storage.copy(fromKey, toKey);
            storage.delete(fromKey);
        }
        return getResourceInfo(userId, toPath);
    }

    @Override
    public InputStreamResource downloadResource(Long userId, String path) {
        ResourcePath resourcePath = new ResourcePath(path);
        String storageKey = pathResolver.toStoragePath(userId, path);
        ensureExists(storageKey, "Resource not found: " + path);

        if (resourcePath.isDirectory()) {
            return new InputStreamResource(zipDirectory(storageKey));
        }
        return new InputStreamResource(storage.download(storageKey));
    }

    private InputStream zipDirectory(String folderKey) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            List<StorageResource> children = storage.list(folderKey, true);
            for (StorageResource child : children) {
                String entryName = child.path().substring(folderKey.length());
                if (entryName.isEmpty()) continue;

                zos.putNextEntry(new ZipEntry(entryName));
                try (InputStream is = storage.download(child.path())) {
                    is.transferTo(zos);
                }
                zos.closeEntry();
            }
            zos.finish();
        } catch (IOException e) {
            throw new StorageException("Failed to create ZIP archive for: " + folderKey, e);
        }
        return new ByteArrayInputStream(baos.toByteArray());
    }

    private void ensureDirectory(ResourcePath resourcePath) {
        if (!resourcePath.isDirectory()) {
            throw new InvalidPathException("Path must end with '/' for directory operations");
        }
    }

    private void ensureExists(String storageKey, String message) {
        if (!storage.exists(storageKey)) {
            throw new ResourceNotFoundException(message);
        }
    }

    private void ensureNotExists(String storageKey, String message) {
        if (storage.exists(storageKey)) {
            throw new ResourceAlreadyExistsException(message);
        }
    }

    private void ensureParentExists(Long userId, String parentPath) {
        if (parentPath.isEmpty()) {
            return;
        }
        String parentKey = pathResolver.toStoragePath(userId, parentPath);
        ensureExists(parentKey, "Parent directory does not exist: " + parentPath);
    }

    private void validateMove(ResourcePath fromResourcePath, ResourcePath toResourcePath,
                              String fromKey, String toKey, Long userId, String toParent) {

        ensureExists(fromKey, "Source resource not found: " + fromResourcePath.path());
        ensureNotExists(toKey, "Destination already exists: " + toResourcePath.path());
        ensureParentExists(userId, toParent);

        if (fromResourcePath.isDirectory() != toResourcePath.isDirectory()) {
            throw new InvalidPathException("Source and destination must be of the same type");
        }

        if (fromResourcePath.isDirectory() && toKey.startsWith(fromKey)) {
            throw new InvalidPathException("Cannot move a directory into itself");
        }
    }

    private void moveDirectory(String fromKey, String toKey) {
        List<StorageResource> children = storage.list(fromKey, true);
        List<String> keysToDelete = new ArrayList<>();
        storage.copy(fromKey, toKey);
        keysToDelete.add(fromKey);

        for (StorageResource child : children) {
            String relativePath = child.path().substring(fromKey.length());
            storage.copy(child.path(), toKey + relativePath);
            keysToDelete.add(child.path());
        }
        storage.deleteObjects(keysToDelete);
    }

    private Map<String, MultipartFile> buildUploadPlan(String folderKey, MultipartFile[] files) {
        Map<String, MultipartFile> filesToUpload = new LinkedHashMap<>();
        for (MultipartFile file : files) {
            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.isBlank()) {
                continue;
            }
            String fileKey = folderKey + originalName;
            ensureNotExists(fileKey, "File already exists: " + originalName);
            filesToUpload.put(fileKey, file);
        }
        return filesToUpload;
    }

    private List<ResourceInfoDto> uploadFiles(Map<String, MultipartFile> files, Long userId) {
        List<ResourceInfoDto> uploaded = new ArrayList<>();
        for (var entry : files.entrySet()) {
            String fileKey = entry.getKey();
            MultipartFile file = entry.getValue();

            try (InputStream inputStream = file.getInputStream()) {
                storage.upload(fileKey, inputStream, file.getSize(), file.getContentType());
            } catch (IOException e) {
                throw new FileUploadException("Failed to read file: " + file.getOriginalFilename(), e);
            }

            StorageResource resource = storage.getInfo(fileKey)
                    .orElseThrow(() -> new FileUploadException(
                            "Failed to get info for uploaded file: " + file.getOriginalFilename()));
            uploaded.add(resourceMapper.toDto(resource, userId));
        }
        return uploaded;
    }
}