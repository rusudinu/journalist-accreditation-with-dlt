package com.rusudinu.backend.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.rusudinu.backend.eth.EthService;
import com.rusudinu.backend.hash.HashService;
import com.rusudinu.backend.request.Request;
import com.rusudinu.backend.request.RequestService;
import com.rusudinu.backend.request.RequestStatus;
import com.rusudinu.backend.request.snapshot.RequestSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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
    private static final String UPLOAD_DIR = "uploads/";

    private final DocumentRepository documentRepository;
    private final RequestService requestService;

    public Document uploadDocument(MultipartFile file, RequestStatus status, Long requestId) {
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new RuntimeException("Failed to create directory: " + UPLOAD_DIR);
            }
        }

        Request request = requestService.getRequestById(requestId);

        String uniqueFileName = System.currentTimeMillis() + "_" + UUID.randomUUID() + "." + file.getOriginalFilename().split("\\.")[1];
        Path filePath = Paths.get(UPLOAD_DIR, uniqueFileName);

        Document document = Document.builder()
                .request(request)
                .storedDocumentName(uniqueFileName)
                .build();

        try {
            Files.write(filePath, file.getBytes());
            Document documentEntity = documentRepository.save(document);
            requestService.updateRequestStatus(requestId, status);
            return documentEntity;
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
