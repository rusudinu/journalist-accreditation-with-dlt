package com.rusudinu.backend.document;

import com.rusudinu.backend.comment.Comment;
import com.rusudinu.backend.comment.CommentDTO;
import com.rusudinu.backend.comment.CommentMapper;
import com.rusudinu.backend.comment.CommentRepository;
import com.rusudinu.backend.request.Request;
import com.rusudinu.backend.request.RequestService;
import com.rusudinu.backend.request.RequestStatus;
import com.rusudinu.backend.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private static final String UPLOAD_DIR = "uploads/";

    private final DocumentRepository documentRepository;
    private final RequestService requestService;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UserService userService;

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

    public CommentDTO addCommentToDocument(Long documentId, CommentDTO commentDTO) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));

        Comment comment = commentMapper.toComment(commentDTO);
        comment.setDocument(document);

        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toCommentDTO(savedComment);
    }

    public List<CommentDTO> getCommentsForDocument(Long documentId) {
        List<Comment> comments = commentRepository.findByDocumentId(documentId);
        return comments.stream()
                .map(commentMapper::toCommentDTO)
                .collect(Collectors.toList());
    }

    public Document getDocumentById(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
    }

    // possible names: budgetcommittee,economicandsocialcouncil,generalsecretariat,legalcommittee,legislativecouncil,publicadministration,specialtycommission
    List<Document> getNeedReviewDocuments(String name) {
        String normalizedName = name.trim().toLowerCase();

        if (normalizedName.length() > 20) {
            normalizedName = userService.findOrCreateByKeycloakId(normalizedName).getName();
        }

        normalizedName = normalizedName.replaceAll("\\s+", "").toLowerCase();

		return switch (normalizedName) {
			case "budgetcommittee" -> documentRepository.findByBudgetCommitteeCommentIsNull();
			case "economicandsocialcouncil" -> documentRepository.findByEconomicAndSocialCouncilCommentIsNull();
			case "generalsecretariat" -> documentRepository.findByGeneralSecretariatCommentIsNull();
			case "legalcommittee" -> documentRepository.findByLegalCommitteeCommentIsNull();
			case "legislativecouncil" -> documentRepository.findByLegislativeCouncilCommentIsNull();
			case "publicadministration" -> documentRepository.findByPublicAdministrationCommentIsNull();
			case "specialtycommission" -> documentRepository.findBySpecialtyCommissionCommentIsNull();
			default -> throw new IllegalArgumentException("Invalid name: " + name);
		};
    }
}
