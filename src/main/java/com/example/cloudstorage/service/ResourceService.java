package com.example.cloudstorage.service;

import com.example.cloudstorage.dto.ResourceInfoDto;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResourceService {

    ResourceInfoDto getResourceInfo(Long userId, String path);

    void deleteResource(Long userId, String path);

    InputStreamResource downloadResource(Long userId, String path);

    ResourceInfoDto moveResource(Long userId, String fromPath, String toPath);

    List<ResourceInfoDto> uploadFile(Long userId, String path, MultipartFile[] files);

    List<ResourceInfoDto> listDirectory(Long userId, String path);

    ResourceInfoDto createDirectory(Long userId, String path);

    List<ResourceInfoDto> searchResources(Long userId, String query);
}