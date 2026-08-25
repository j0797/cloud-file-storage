package com.example.cloudstorage.storage;

import java.time.Instant;

public record StorageResource(String path,
                              Long size,
                              boolean directory,
                              Instant lastModified) {
}