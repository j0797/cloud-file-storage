package com.example.cloudstorage.dto;

import com.example.cloudstorage.domain.ResourceType;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResourceInfoDto(String path,
                              String name,
                              Long size,
                              ResourceType type) {
}