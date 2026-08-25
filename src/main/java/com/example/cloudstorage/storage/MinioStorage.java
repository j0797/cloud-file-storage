package com.example.cloudstorage.storage;

import com.example.cloudstorage.exception.StorageException;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class MinioStorage implements Storage {

    private final MinioClient minioClient;
    private final String bucketName;

    public MinioStorage(@Value("${minio.bucket-name}") String bucketName, MinioClient minioClient) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    @Override
    public void createDirectory(String path) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageException("Failed to create directory: " + path, e);
        }
    }

    @Override
    public void upload(String path, InputStream stream, long size, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .stream(stream, size, -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageException("Failed to upload file: " + path, e);
        }
    }

    @Override
    public Optional<StorageResource> getInfo(String path) {
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucketName).object(path).build()
            );
            boolean isDir = path.endsWith("/");
            return Optional.of(new StorageResource(
                    path,
                    isDir ? null : stat.size(),
                    isDir,
                    stat.lastModified().toInstant()
            ));
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return Optional.empty();
            }
            throw new StorageException("Failed to get info for: " + path, e);
        } catch (Exception e) {
            throw new StorageException("Failed to get info for: " + path, e);
        }
    }

    @Override
    public void delete(String path) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucketName).object(path).build()
            );
        } catch (Exception e) {
            throw new StorageException("Failed to delete: " + path, e);
        }
    }

    @Override
    public void deleteObjects(List<String> paths) {
        for (String name : paths) {
            delete(name);
        }
    }

    @Override
    public void copy(String source, String destination) {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucketName)
                            .object(destination)
                            .source(CopySource.builder().bucket(bucketName).object(source).build())
                            .build()
            );
        } catch (Exception e) {
            throw new StorageException("Failed to copy: " + source + " -> " + destination, e);
        }
    }

    @Override
    public InputStream download(String path) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder().bucket(bucketName).object(path).build()
            );
        } catch (Exception e) {
            throw new StorageException("Failed to download: " + path, e);
        }
    }

    @Override
    public List<StorageResource> list(String prefix, boolean recursive) {
        List<StorageResource> resources = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(prefix)
                            .recursive(recursive)
                            .delimiter(recursive ? null : "/")
                            .build()
            );
            for (Result<Item> result : results) {
                Item item = result.get();
                String path = item.objectName();
                if (path.equals(prefix) && path.endsWith("/")) {
                    continue;
                }
                boolean isDir = path.endsWith("/");
                resources.add(new StorageResource(
                        path,
                        isDir ? null : item.size(),
                        isDir,
                        item.lastModified().toInstant()
                ));
            }
        } catch (Exception e) {
            throw new StorageException("Failed to list objects: " + prefix, e);
        }
        return resources;
    }

    @Override
    public boolean exists(String path) {
        return getInfo(path).isPresent();
    }
}