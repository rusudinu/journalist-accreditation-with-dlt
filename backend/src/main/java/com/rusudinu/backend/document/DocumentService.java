package com.rusudinu.backend.document;

import com.rusudinu.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.parsers.DocumentBuilder;
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

    public Document uploadDocument(MultipartFile file, User user) {
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new RuntimeException("Failed to create directory: " + UPLOAD_DIR);
            }
        }

        String uniqueFileName = System.currentTimeMillis() + "_" + UUID.randomUUID() + "." + file.getOriginalFilename().split("\\.")[1];
        Path filePath = Paths.get(UPLOAD_DIR, uniqueFileName);

        Document document = Document.builder()
                .user(user)
                .storedDocumentName(uniqueFileName)
                .build();

        try {
            Files.write(filePath, file.getBytes());
            return documentRepository.save(document);
        } catch (IOException e) {
            return null;
        }
    }
}
