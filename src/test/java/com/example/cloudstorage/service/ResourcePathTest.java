package com.example.cloudstorage.service;

import com.example.cloudstorage.domain.ResourcePath;
import com.example.cloudstorage.domain.ResourceType;
import com.example.cloudstorage.exception.InvalidPathException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResourcePathTest {

    @Test
    void shouldThrowWhenPathIsNull() {
        assertThrows(InvalidPathException.class, () -> new ResourcePath(null));
    }

    @Test
    void shouldThrowWhenPathIsEmpty() {
        assertThrows(InvalidPathException.class, () -> new ResourcePath(""));
    }

    @Test
    void shouldThrowWhenPathIsBlank() {
        assertThrows(InvalidPathException.class, () -> new ResourcePath("   "));
    }

    @Test
    void shouldThrowWhenPathStartsWithSlash() {
        assertThrows(InvalidPathException.class, () -> new ResourcePath("/folder/"));
    }

    @Test
    void shouldThrowWhenPathContainsConsecutiveSlashes() {
        assertThrows(InvalidPathException.class, () -> new ResourcePath("a//b/"));
    }

    @Test
    void shouldThrowWhenPathContainsTraversal() {
        assertThrows(InvalidPathException.class, () -> new ResourcePath("../etc/"));
        assertThrows(InvalidPathException.class, () -> new ResourcePath("a/../b/"));
    }

    @Test
    void shouldThrowWhenSegmentTooLong() {
        String longSegment = "a".repeat(201);
        assertThrows(InvalidPathException.class, () -> new ResourcePath(longSegment + "/"));
    }

    @Test
    void shouldThrowWhenSegmentContainsForbiddenChars() {
        String[] bad = {"*", "?", "\"", "<", ">", "|", ":", "\\"};

        for (String ch : bad) {
            assertThrows(InvalidPathException.class,
                    () -> new ResourcePath("bad" + ch + "name/"),
                    "Should reject segment with: " + ch);
        }
    }

    @Test
    void shouldAcceptValidPath() {
        assertDoesNotThrow(() -> new ResourcePath("folder/"));
        assertDoesNotThrow(() -> new ResourcePath("a/b/c.txt"));
        assertDoesNotThrow(() -> new ResourcePath("a/b/c/"));
    }

    @Test
    void isDirectory_shouldReturnTrueForTrailingSlash() {
        assertTrue(new ResourcePath("folder/").isDirectory());
        assertTrue(new ResourcePath("a/b/").isDirectory());
    }

    @Test
    void isDirectory_shouldReturnFalseWithoutTrailingSlash() {
        assertFalse(new ResourcePath("file.txt").isDirectory());
        assertFalse(new ResourcePath("a/b.txt").isDirectory());
    }

    @Test
    void fileName_shouldReturnLastSegment() {
        assertEquals("file.txt", new ResourcePath("file.txt").fileName());
        assertEquals("file.txt", new ResourcePath("a/file.txt").fileName());
        assertEquals("file.txt", new ResourcePath("a/b/file.txt").fileName());
    }

    @Test
    void fileName_shouldIgnoreTrailingSlashForDirectories() {
        assertEquals("folder", new ResourcePath("folder/").fileName());
        assertEquals("folder", new ResourcePath("a/b/folder/").fileName());
    }

    @Test
    void parentPath_shouldReturnPathUpToParent() {
        assertEquals("", new ResourcePath("file.txt").parentPath());
        assertEquals("a/", new ResourcePath("a/file.txt").parentPath());
        assertEquals("a/b/", new ResourcePath("a/b/file.txt").parentPath());
    }

    @Test
    void parentPath_shouldReturnParentForDirectory() {
        assertEquals("", new ResourcePath("folder/").parentPath());
        assertEquals("a/", new ResourcePath("a/folder/").parentPath());
        assertEquals("a/b/", new ResourcePath("a/b/folder/").parentPath());
    }

    @Test
    void type_shouldReturnDirectoryForTrailingSlash() {
        assertEquals(ResourceType.DIRECTORY, new ResourcePath("folder/").type());
    }

    @Test
    void type_shouldReturnFileWithoutTrailingSlash() {
        assertEquals(ResourceType.FILE, new ResourcePath("file.txt").type());
    }

    @Test
    void parentDirectories_shouldReturnEmptyForRootLevelResource() {
        assertEquals(List.of(), new ResourcePath("file.txt").parentDirectories());
        assertEquals(List.of(), new ResourcePath("folder/").parentDirectories());
    }

    @Test
    void parentDirectories_shouldReturnAllAncestors() {
        assertEquals(List.of("a/"), new ResourcePath("a/file.txt").parentDirectories());
        assertEquals(List.of("a/", "a/b/"), new ResourcePath("a/b/file.txt").parentDirectories());
        assertEquals(List.of("a/", "a/b/", "a/b/c/"),
                new ResourcePath("a/b/c/file.txt").parentDirectories());
    }
}