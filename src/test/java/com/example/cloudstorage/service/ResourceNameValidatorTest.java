package com.example.cloudstorage.service;

import com.example.cloudstorage.domain.ResourceNameValidator;
import com.example.cloudstorage.exception.InvalidPathException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResourceNameValidatorTest {

    @Test
    void validateSegment_shouldAcceptValidName() {
        assertDoesNotThrow(() -> ResourceNameValidator.validateSegment("my_folder"));
        assertDoesNotThrow(() -> ResourceNameValidator.validateSegment("файл.txt"));
        assertDoesNotThrow(() -> ResourceNameValidator.validateSegment("name with spaces"));
        assertDoesNotThrow(() -> ResourceNameValidator.validateSegment("a".repeat(200)));
    }

    @Test
    void validateSegment_shouldRejectNull() {
        assertThrows(InvalidPathException.class,
                () -> ResourceNameValidator.validateSegment(null));
    }

    @Test
    void validateSegment_shouldRejectEmpty() {
        assertThrows(InvalidPathException.class,
                () -> ResourceNameValidator.validateSegment(""));
    }

    @Test
    void validateSegment_shouldRejectTooLong() {
        assertThrows(InvalidPathException.class,
                () -> ResourceNameValidator.validateSegment("a".repeat(201)));
    }

    @Test
    void validateSegment_shouldRejectForbiddenChars() {
        String[] bad = {"/", "\\", ":", "*", "?", "\"", "<", ">", "|"};

        for (String ch : bad) {
            assertThrows(InvalidPathException.class,
                    () -> ResourceNameValidator.validateSegment("name" + ch + "x"),
                    "Should reject: " + ch);
        }
    }

    @Test
    void validatePathSegments_shouldAcceptSimplePath() {
        assertDoesNotThrow(() -> ResourceNameValidator.validatePathSegments("folder/"));
        assertDoesNotThrow(() -> ResourceNameValidator.validatePathSegments("a/b/c/"));
        assertDoesNotThrow(() -> ResourceNameValidator.validatePathSegments("file.txt"));
    }

    @Test
    void validatePathSegments_shouldRejectBadSegmentInside() {
        assertThrows(InvalidPathException.class,
                () -> ResourceNameValidator.validatePathSegments("good/bad*name/"));
        assertThrows(InvalidPathException.class,
                () -> ResourceNameValidator.validatePathSegments("a".repeat(201) + "/file.txt"));
    }

    @Test
    void validatePathSegments_shouldIgnoreEmptyAndRoot() {
        assertDoesNotThrow(() -> ResourceNameValidator.validatePathSegments(null));
        assertDoesNotThrow(() -> ResourceNameValidator.validatePathSegments(""));
        assertDoesNotThrow(() -> ResourceNameValidator.validatePathSegments("/"));
    }
}