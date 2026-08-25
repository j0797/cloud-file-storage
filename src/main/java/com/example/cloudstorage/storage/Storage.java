package com.example.cloudstorage.storage;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public interface Storage {

    void createDirectory(String path);

    void upload(String path, InputStream stream, long size, String contentType);

    Optional<StorageResource> getInfo(String path);

    void delete(String path);

    void deleteObjects(List<String> paths);

    void copy(String source, String destination);

    InputStream download(String path);

    List<StorageResource> list(String prefix, boolean recursive);

    boolean exists(String path);
}