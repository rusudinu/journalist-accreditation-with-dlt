package com.rusudinu.backend.document;

import com.rusudinu.backend.user.User;
import com.rusudinu.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private static final String UPLOAD_DIR = "uploads/";

    public Document uploadDocument(MultipartFile file, User user, String status, Optional<Long> uploadedForUserWithIdOpt) {
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new RuntimeException("Failed to create directory: " + UPLOAD_DIR);
            }
        }

        User userLinkedToDocument = user;
        Long uploadedForUserWithId = uploadedForUserWithIdOpt.orElse(user.getId());

        if (!user.getId().equals(uploadedForUserWithId)) {
            userLinkedToDocument = userRepository.findById(uploadedForUserWithId).orElseThrow(
                    () -> new RuntimeException("User with id " + uploadedForUserWithId + " not found")
            );
        }

        String uniqueFileName = System.currentTimeMillis() + "_" + UUID.randomUUID() + "." + file.getOriginalFilename().split("\\.")[1];
        Path filePath = Paths.get(UPLOAD_DIR, uniqueFileName);

        Document document = Document.builder()
                .user(userLinkedToDocument)
                .uploadedByUserId(uploadedForUserWithId)
                .status(status)
                .storedDocumentName(uniqueFileName)
                .build();

        try {
            Files.write(filePath, file.getBytes());
            return documentRepository.save(document);
        } catch (IOException e) {
            return null;
        }
    }

    public byte[] getDocument(String storedDocumentName) {
        try {
            Path documentPath = Paths.get(UPLOAD_DIR, storedDocumentName);
            if (!Files.exists(documentPath)) {
                throw new IOException("File not found: " + storedDocumentName);
            }
            return Files.readAllBytes(documentPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to retrieve document content", e);
        }
    }

}
