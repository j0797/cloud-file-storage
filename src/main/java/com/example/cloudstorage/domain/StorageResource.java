package com.example.cloudstorage.domain;

import java.time.Instant;

public record StorageResource(String path,
                              Long size,
                              boolean directory,
                              Instant lastModified) {
}