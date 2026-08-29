package com.example.cloudstorage.mapper;

import com.example.cloudstorage.domain.ResourcePath;
import com.example.cloudstorage.domain.StorageResource;
import com.example.cloudstorage.dto.ResourceInfoDto;
import com.example.cloudstorage.storage.UserStoragePathResolver;
import org.springframework.stereotype.Component;

@Component
public class ResourceMapper {

    private final UserStoragePathResolver pathResolver;

    public ResourceMapper(UserStoragePathResolver pathResolver) {
        this.pathResolver = pathResolver;
    }

    public ResourceInfoDto toDto(StorageResource storageResource, Long userId) {
        String userFacingPath = pathResolver.toUserPath(userId, storageResource.path());
        ResourcePath resourcePath = new ResourcePath(userFacingPath);

        Long size = resourcePath.isDirectory() ? null : storageResource.size();

        return new ResourceInfoDto(
                resourcePath.parentPath(),
                resourcePath.fileName(),
                size,
                resourcePath.type()
        );
    }
}