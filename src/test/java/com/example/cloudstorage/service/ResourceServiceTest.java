package com.example.cloudstorage.service;

import com.example.cloudstorage.dto.ResourceInfoDto;
import com.example.cloudstorage.exception.InvalidPathException;
import com.example.cloudstorage.exception.ResourceAlreadyExistsException;
import com.example.cloudstorage.exception.ResourceNotFoundException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class ResourceServiceTest {

    private static final AtomicLong USER_ID_SEQ = new AtomicLong(1000L);

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Container
    static GenericContainer<?> minio = new GenericContainer<>("minio/minio:RELEASE.2025-09-07T16-13-09Z")
            .withExposedPorts(9000)
            .withEnv("MINIO_ROOT_USER", "testadmin")
            .withEnv("MINIO_ROOT_PASSWORD", "testpassword")
            .withCommand("server", "/data")
            .waitingFor(Wait.forHttp("/minio/health/live").forPort(9000));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("minio.url", () -> "http://" + minio.getHost() + ":" + minio.getMappedPort(9000));
        registry.add("minio.root.user", () -> "testadmin");
        registry.add("minio.root.password", () -> "testpassword");
        registry.add("minio.bucket-name", () -> "test-bucket");
    }

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @BeforeEach
    void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build());
        if (!exists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    private String uniquePath() {
        return "test_" + UUID.randomUUID() + "/";
    }

    private Long uniqueUserId() {
        return USER_ID_SEQ.incrementAndGet();
    }

    @Test
    void shouldCreateDirectory() {
        Long userId = uniqueUserId();
        String path = uniquePath();

        ResourceInfoDto dto = resourceService.createDirectory(userId, path);

        assertNotNull(dto);
        assertEquals("DIRECTORY", dto.type().name());
    }

    @Test
    void shouldThrowWhenDirectoryAlreadyExists() {
        Long userId = uniqueUserId();
        String path = uniquePath();
        resourceService.createDirectory(userId, path);

        assertThrows(ResourceAlreadyExistsException.class,
                () -> resourceService.createDirectory(userId, path));
    }

    @Test
    void shouldThrowWhenParentDirectoryMissing() {
        Long userId = uniqueUserId();
        String parent = uniquePath();
        String path = parent + "child/";

        assertThrows(ResourceNotFoundException.class,
                () -> resourceService.createDirectory(userId, path));
    }

    @Test
    void shouldThrowWhenDirectoryPathWithoutTrailingSlash() {
        Long userId = uniqueUserId();
        String path = "folder_no_slash_" + UUID.randomUUID(); // без '/'

        assertThrows(InvalidPathException.class,
                () -> resourceService.createDirectory(userId, path));
    }

    @Test
    void shouldReturnDirectoryWithNullSize() {
        Long userId = uniqueUserId();
        String path = uniquePath();
        resourceService.createDirectory(userId, path);

        ResourceInfoDto dto = resourceService.getResourceInfo(userId, path);

        assertEquals("DIRECTORY", dto.type().name());
        assertNull(dto.size());
    }

    @Test
    void shouldReturnFileWithSize() {
        Long userId = uniqueUserId();
        String folder = uniquePath();
        resourceService.createDirectory(userId, folder);
        resourceService.uploadFile(userId, folder, new MultipartFile[]{file("info.txt", "Hello")});

        ResourceInfoDto dto = resourceService.getResourceInfo(userId, folder + "info.txt");

        assertEquals("FILE", dto.type().name());
        assertEquals(5L, dto.size());
    }

    @Test
    void shouldThrowWhenResourceNotFound() {
        Long userId = uniqueUserId();

        assertThrows(ResourceNotFoundException.class,
                () -> resourceService.getResourceInfo(userId, "missing_" + UUID.randomUUID() + ".txt"));
    }

    @Test
    void shouldUploadSingleFile() {
        Long userId = uniqueUserId();
        String folder = uniquePath();
        resourceService.createDirectory(userId, folder);

        List<ResourceInfoDto> result = resourceService.uploadFile(
                userId, folder, new MultipartFile[]{file("hello.txt", "Hello")});

        assertEquals(1, result.size());
        assertEquals("hello.txt", result.getFirst().name());
        assertEquals("FILE", result.getFirst().type().name());
        assertEquals(5L, result.getFirst().size());
    }

    @Test
    void shouldUploadMultipleFiles() {
        Long userId = uniqueUserId();
        String folder = uniquePath();
        resourceService.createDirectory(userId, folder);

        List<ResourceInfoDto> result = resourceService.uploadFile(
                userId, folder,
                new MultipartFile[]{file("a.txt", "aaa"), file("b.txt", "bbb")});

        assertEquals(2, result.size());
    }

    @Test
    void shouldThrowWhenFileAlreadyExists() {
        Long userId = uniqueUserId();
        String folder = uniquePath();
        resourceService.createDirectory(userId, folder);
        resourceService.uploadFile(userId, folder, new MultipartFile[]{file("dup.txt", "v1")});

        assertThrows(ResourceAlreadyExistsException.class,
                () -> resourceService.uploadFile(userId, folder, new MultipartFile[]{file("dup.txt", "v2")}));
    }

    @Test
    void shouldThrowWhenFilesEmpty() {
        Long userId = uniqueUserId();
        String folder = uniquePath();
        resourceService.createDirectory(userId, folder);

        assertThrows(InvalidPathException.class,
                () -> resourceService.uploadFile(userId, folder, new MultipartFile[0]));
    }

    @Test
    void shouldNotUploadAnythingWhenPartialConflict() {
        Long userId = uniqueUserId();
        String folder = uniquePath();
        resourceService.createDirectory(userId, folder);
        resourceService.uploadFile(userId, folder, new MultipartFile[]{file("exists.txt", "v1")});

        assertThrows(ResourceAlreadyExistsException.class,
                () -> resourceService.uploadFile(userId, folder,
                        new MultipartFile[]{file("new.txt", "n"), file("exists.txt", "v2")}));

        List<ResourceInfoDto> listing = resourceService.listDirectory(userId, folder);
        assertTrue(listing.stream().noneMatch(dto -> dto.name().equals("new.txt")));
    }

    @Test
    void shouldReturnEmptyListForEmptyDirectory() {
        Long userId = uniqueUserId();
        String folder = uniquePath();
        resourceService.createDirectory(userId, folder);

        List<ResourceInfoDto> result = resourceService.listDirectory(userId, folder);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnFilesAndSubfolders() {
        Long userId = uniqueUserId();
        String folder = uniquePath();
        resourceService.createDirectory(userId, folder);
        resourceService.createDirectory(userId, folder + "sub/");
        resourceService.uploadFile(userId, folder, new MultipartFile[]{file("a.txt", "aaa")});

        List<ResourceInfoDto> result = resourceService.listDirectory(userId, folder);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.name().equals("sub") && dto.type().name().equals("DIRECTORY")));
        assertTrue(result.stream().anyMatch(dto -> dto.name().equals("a.txt") && dto.type().name().equals("FILE")));
    }

    @Test
    void shouldThrowWhenDirectoryNotFound() {
        Long userId = uniqueUserId();

        assertThrows(ResourceNotFoundException.class,
                () -> resourceService.listDirectory(userId, "missing_" + UUID.randomUUID() + "/"));
    }

    @Test
    void shouldThrowWhenListPathWithoutTrailingSlash() {
        Long userId = uniqueUserId();
        String folder = uniquePath();
        resourceService.createDirectory(userId, folder);

        assertThrows(InvalidPathException.class,
                () -> resourceService.listDirectory(userId, folder.substring(0, folder.length() - 1)));
    }

    @Test
    void shouldDeleteFile() {
        Long userId = uniqueUserId();
        String folder = uniquePath();
        resourceService.createDirectory(userId, folder);
        resourceService.uploadFile(userId, folder, new MultipartFile[]{file("del.txt", "x")});

        resourceService.deleteResource(userId, folder + "del.txt");

        assertThrows(ResourceNotFoundException.class,
                () -> resourceService.getResourceInfo(userId, folder + "del.txt"));
    }

    @Test
    void shouldDeleteDirectoryWithContent() {
        Long userId = uniqueUserId();
        String folder = uniquePath();
        resourceService.createDirectory(userId, folder);
        resourceService.createDirectory(userId, folder + "sub/");
        resourceService.uploadFile(userId, folder + "sub/", new MultipartFile[]{file("deep.txt", "d")});

        resourceService.deleteResource(userId, folder);

        assertThrows(ResourceNotFoundException.class,
                () -> resourceService.getResourceInfo(userId, folder));
        assertThrows(ResourceNotFoundException.class,
                () -> resourceService.getResourceInfo(userId, folder + "sub/deep.txt"));
    }

    @Test
    void shouldThrowWhenDeletingMissingResource() {
        assertThrows(ResourceNotFoundException.class,
                () -> resourceService.deleteResource(uniqueUserId(), "missing_" + UUID.randomUUID() + ".txt"));
        assertThrows(ResourceNotFoundException.class,
                () -> resourceService.deleteResource(uniqueUserId(), "missing_" + UUID.randomUUID() + "/"));
    }

    @Test
    void shouldRenameFile() {
        Long userId = uniqueUserId();
        String folder = uniquePath();
        resourceService.createDirectory(userId, folder);
        resourceService.uploadFile(userId, folder, new MultipartFile[]{file("old.txt", "data")});

        ResourceInfoDto moved = resourceService.moveResource(
                userId, folder + "old.txt", folder + "new.txt");

        assertEquals("new.txt", moved.name());
        assertThrows(ResourceNotFoundException.class,
                () -> resourceService.getResourceInfo(userId, folder + "old.txt"));
        assertDoesNotThrow(() -> resourceService.getResourceInfo(userId, folder + "new.txt"));
    }

    @Test
    void shouldMoveFolder() {
        Long userId = uniqueUserId();
        String folder = uniquePath();
        resourceService.createDirectory(userId, folder);
        resourceService.createDirectory(userId, folder + "src/");
        resourceService.uploadFile(userId, folder + "src/", new MultipartFile[]{file("f.txt", "x")});

        ResourceInfoDto moved = resourceService.moveResource(
                userId, folder + "src/", folder + "dst/");

        assertEquals("dst", moved.name());
        assertThrows(ResourceNotFoundException.class,
                () -> resourceService.getResourceInfo(userId, folder + "src/"));
        assertDoesNotThrow(() -> resourceService.getResourceInfo(userId, folder + "dst/f.txt"));
    }

    @Test
    void shouldThrowWhenMoveDestinationExists() {
        Long userId = uniqueUserId();
        String folder = uniquePath();
        resourceService.createDirectory(userId, folder);
        resourceService.uploadFile(userId, folder, new MultipartFile[]{file("a.txt", "1")});
        resourceService.uploadFile(userId, folder, new MultipartFile[]{file("b.txt", "2")});

        assertThrows(ResourceAlreadyExistsException.class,
                () -> resourceService.moveResource(userId, folder + "a.txt", folder + "b.txt"));
    }

    @Test
    void shouldThrowWhenMoveSourceNotFound() {
        Long userId = uniqueUserId();
        String folder = uniquePath();
        resourceService.createDirectory(userId, folder);

        assertThrows(ResourceNotFoundException.class,
                () -> resourceService.moveResource(userId, folder + "missing.txt", folder + "new.txt"));
    }

    @Test
    void shouldThrowWhenMoveTargetParentMissing() {
        Long userId = uniqueUserId();
        String folder = uniquePath();
        resourceService.createDirectory(userId, folder);
        resourceService.uploadFile(userId, folder, new MultipartFile[]{file("a.txt", "1")});

        assertThrows(ResourceNotFoundException.class,
                () -> resourceService.moveResource(userId, folder + "a.txt", folder + "missing/a.txt"));
    }

    @Test
    void shouldThrowWhenMovingDirectoryIntoItself() {
        Long userId = uniqueUserId();
        String folder = uniquePath();
        resourceService.createDirectory(userId, folder);
        resourceService.createDirectory(userId, folder + "sub/");

        assertThrows(InvalidPathException.class,
                () -> resourceService.moveResource(userId, folder + "sub/", folder + "sub/inner/"));
    }

    @Test
    void shouldDownloadFileWithCorrectContent() throws Exception {
        Long userId = uniqueUserId();
        String folder = uniquePath();
        resourceService.createDirectory(userId, folder);
        resourceService.uploadFile(userId, folder, new MultipartFile[]{file("dl.txt", "Hello World")});

        InputStreamResource resource = resourceService.downloadResource(userId, folder + "dl.txt");

        byte[] bytes = resource.getInputStream().readAllBytes();
        assertEquals("Hello World", new String(bytes));
    }

    @Test
    void shouldDownloadDirectoryAsZipWithFlatStructure() throws Exception {
        Long userId = uniqueUserId();
        String folder = uniquePath();
        resourceService.createDirectory(userId, folder);
        resourceService.uploadFile(userId, folder, new MultipartFile[]{
                file("one.txt", "1"), file("two.txt", "2")});

        InputStreamResource resource = resourceService.downloadResource(userId, folder);

        byte[] bytes = resource.getInputStream().readAllBytes();
        assertTrue(bytes.length > 0);

        try (var zip = new java.util.zip.ZipInputStream(new java.io.ByteArrayInputStream(bytes))) {
            var names = new java.util.HashSet<String>();
            java.util.zip.ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                names.add(entry.getName());
            }
            assertTrue(names.contains("one.txt"));
            assertTrue(names.contains("two.txt"));
            assertEquals(2, names.size(), "The archive must contain exactly two files, without an extra folder entry.");
        }
    }

    @Test
    void shouldDownloadEmptyDirectoryAsValidZipWithFolderEntry() throws Exception {
        Long userId = uniqueUserId();
        String folder = uniquePath();
        resourceService.createDirectory(userId, folder);

        InputStreamResource resource = resourceService.downloadResource(userId, folder);

        byte[] bytes = resource.getInputStream().readAllBytes();
        assertTrue(bytes.length > 0, "Zip mustn't be empty.");

        try (var zip = new java.util.zip.ZipInputStream(new java.io.ByteArrayInputStream(bytes))) {
            var entries = new java.util.ArrayList<String>();
            java.util.zip.ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                entries.add(entry.getName());
            }
            assertEquals(1, entries.size(), "The archive should contain a single entry: the folder itself.");
            assertTrue(entries.getFirst().endsWith("/"), "The entry must be a folder.");
        }
    }

    @Test
    void shouldThrowWhenDownloadingMissingResource() {
        Long userId = uniqueUserId();

        assertThrows(ResourceNotFoundException.class,
                () -> resourceService.downloadResource(userId, "missing_" + UUID.randomUUID() + ".txt"));
    }

    @Test
    void shouldFindResourceByPartialName() {
        Long userId = uniqueUserId();
        String folder = uniquePath();
        resourceService.createDirectory(userId, folder);
        resourceService.uploadFile(userId, folder, new MultipartFile[]{
                file("report_2024.pdf", "x"), file("notes.txt", "y")});

        List<ResourceInfoDto> result = resourceService.searchResources(userId, "report");

        assertEquals(1, result.size());
        assertEquals("report_2024.pdf", result.getFirst().name());
    }

    @Test
    void shouldReturnEmptyListWhenNothingMatches() {
        Long userId = uniqueUserId();
        String folder = uniquePath();
        resourceService.createDirectory(userId, folder);
        resourceService.uploadFile(userId, folder, new MultipartFile[]{file("a.txt", "x")});

        List<ResourceInfoDto> result = resourceService.searchResources(userId, "zzz_no_match");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldThrowWhenSearchQueryBlank() {
        Long userId = uniqueUserId();

        assertThrows(InvalidPathException.class,
                () -> resourceService.searchResources(userId, "   "));
    }

    @Test
    void shouldOpenVirtualDirectoryWithoutMarkerObject() {
        Long userId = uniqueUserId();
        String folder = uniquePath();
        resourceService.createDirectory(userId, folder);
        resourceService.uploadFile(userId, folder,
                new MultipartFile[]{file("subfolder/file.txt", "content")});

        List<ResourceInfoDto> contents = resourceService.listDirectory(userId, folder + "subfolder/");

        assertEquals(1, contents.size());
        assertEquals("file.txt", contents.getFirst().name());
    }

    @Test
    void shouldDeleteVirtualDirectoryWithoutMarkerObject() {
        Long userId = uniqueUserId();
        String folder = uniquePath();
        resourceService.createDirectory(userId, folder);
        resourceService.uploadFile(userId, folder,
                new MultipartFile[]{file("subfolder/file.txt", "content")});
        resourceService.deleteResource(userId, folder + "subfolder/");

        assertThrows(ResourceNotFoundException.class,
                () -> resourceService.listDirectory(userId, folder + "subfolder/"));
    }

    private MultipartFile file(String name, String content) {
        return new MockMultipartFile("file", name, "text/plain", content.getBytes());
    }
}