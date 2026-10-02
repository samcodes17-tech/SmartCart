package com.smartcart.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:uploads/products}")
    private String uploadDir;


    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);

            String original = StringUtils.cleanPath(
                    file.getOriginalFilename() != null ? file.getOriginalFilename() : "image");
            String extension = original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
            String filename = UUID.randomUUID() + extension;

            Path target = dir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/products/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store uploaded image", e);
        }
    }
}
