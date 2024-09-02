package com.rusudinu.backend.document;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private final DocumentRepository documentRepository;
    private static final String UPLOAD_DIR = "uploads/";

    public String uploadDocument(MultipartFile file) {
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new RuntimeException("Failed to create directory: " + UPLOAD_DIR);
            }
        }

        String uniqueFileName = System.currentTimeMillis() + "_" + UUID.randomUUID() + "." + file.getOriginalFilename().split("\\.")[1];
        Path filePath = Paths.get(UPLOAD_DIR, uniqueFileName);

        try {
            Files.write(filePath, file.getBytes());
            return "File uploaded successfully: " + file.getOriginalFilename();
        } catch (IOException e) {
            return "Failed to upload file: " + e.getMessage();
        }
    }
}
