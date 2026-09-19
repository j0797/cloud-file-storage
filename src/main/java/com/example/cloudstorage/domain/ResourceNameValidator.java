package com.example.cloudstorage.domain;

import com.example.cloudstorage.exception.InvalidPathException;

import java.util.regex.Pattern;

public final class ResourceNameValidator {

    private static final Pattern FORBIDDEN_CHARS = Pattern.compile("[/\\\\:*?\"<>|]");
    private static final int MAX_SEGMENT_LENGTH = 200;

    private ResourceNameValidator() {
    }

    public static void validateSegment(String segment) {
        if (segment == null || segment.isEmpty()) {
            throw new InvalidPathException("Name segment cannot be empty");
        }
        if (segment.length() > MAX_SEGMENT_LENGTH) {
            throw new InvalidPathException(
                    "Name segment too long: " + segment.length() + " characters (max " + MAX_SEGMENT_LENGTH + ")"
            );
        }
        if (FORBIDDEN_CHARS.matcher(segment).find()) {
            throw new InvalidPathException("Name contains forbidden characters");
        }
    }

    public static void validatePathSegments(String path) {
        if (path == null || path.isEmpty() || path.equals("/")) {
            return;
        }
        String trimmed = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        for (String segment : trimmed.split("/")) {
            validateSegment(segment);
        }
    }
}