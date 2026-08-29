package com.example.cloudstorage.storage;

import org.springframework.stereotype.Component;

@Component
public class UserStoragePathResolver {

    private static final String PREFIX_FORMAT = "user-%d-files/";

    public String toStoragePath(Long userId, String userPath) {
        return buildPrefix(userId) + userPath;
    }

    public String toUserPath(Long userId, String storagePath) {
        String prefix = buildPrefix(userId);
        return storagePath.startsWith(prefix) ? storagePath.substring(prefix.length()) : storagePath;
    }

    private String buildPrefix(Long userId) {
        return PREFIX_FORMAT.formatted(userId);
    }
}