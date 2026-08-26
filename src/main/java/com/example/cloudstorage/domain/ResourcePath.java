package com.example.cloudstorage.domain;

import com.example.cloudstorage.exception.InvalidPathException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public record ResourcePath(String path) {

    private static final String SLASH = "/";
    private static final Pattern FULL_PATH_PATTERN = Pattern.compile("^(?!/)(?!.*//).+$");

    public ResourcePath {
        validatePath(path);
    }

    public boolean isDirectory() {
        return path.endsWith(SLASH);
    }

    public String fileName() {
        String trimmed = pathWithoutTrailingSlash();
        int lastSlash = trimmed.lastIndexOf(SLASH);
        return lastSlash >= 0 ? trimmed.substring(lastSlash + 1) : trimmed;
    }

    public String parentPath() {
        String trimmed = pathWithoutTrailingSlash();
        int lastSlash = trimmed.lastIndexOf(SLASH);
        return lastSlash >= 0 ? trimmed.substring(0, lastSlash + 1) : "";
    }

    public ResourceType type() {
        return isDirectory() ? ResourceType.DIRECTORY : ResourceType.FILE;
    }

    public List<String> parentDirectories() {
        String parent = parentPath();
        if (parent.isEmpty()) {
            return List.of();
        }

        String[] segments = parent.split(SLASH);
        StringBuilder accumulator = new StringBuilder();
        List<String> directories = new ArrayList<>();

        for (String segment : segments) {
            if (segment.isBlank()) continue;
            accumulator.append(segment).append(SLASH);
            directories.add(accumulator.toString());
        }

        return directories;
    }

    private String pathWithoutTrailingSlash() {
        return isDirectory() ? path.substring(0, path.length() - 1) : path;
    }

    private static void validatePath(String path) {
        if (path == null || path.isBlank()) {
            throw new InvalidPathException("Path is required");
        }
        if (!FULL_PATH_PATTERN.matcher(path).matches()) {
            throw new InvalidPathException(
                    "Path must not start with '/', be empty, or contain consecutive '/'"
            );
        }
        if (containsPathTraversal(path)) {
            throw new InvalidPathException("Path traversal ('..') is not allowed");
        }
    }

    private static boolean containsPathTraversal(String path) {
        for (String segment : path.split(SLASH)) {
            if (segment.equals("..")) {
                return true;
            }
        }
        return false;
    }
}