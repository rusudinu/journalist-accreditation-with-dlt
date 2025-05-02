package com.rusudinu.backend.document;

import com.rusudinu.backend.user.UserService;
import java.util.ArrayList;
import java.util.Objects;
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
    private final UserService userService;

    public Document uploadDocument(MultipartFile file, Long documentId) {
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new RuntimeException("Failed to create directory: " + UPLOAD_DIR);
            }
        }


        String uniqueFileName = System.currentTimeMillis() + "_" + UUID.randomUUID() + "." + Objects.requireNonNull(file.getOriginalFilename())
				.split("\\.")[1];
        Path filePath = Paths.get(UPLOAD_DIR, uniqueFileName);

        Document document = documentRepository.findById(documentId).orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
        document.setStoredDocumentName(uniqueFileName);
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
			case "economicandsocialcouncil" -> documentRepository.findByEconomicAndSocialCouncilCommentIsNull();
			case "generalsecretariat" -> documentRepository.findByGeneralSecretariatCommentIsNull();
			case "legislativecouncil" -> documentRepository.findByLegislativeCouncilCommentIsNull();
			case "specialtycommission" -> documentRepository.findBySpecialtyCommissionCommentIsNull();
			case "legalcommittee" -> documentRepository.findByLegalCommitteeCommentIsNull()
					.stream()
					.filter(d -> d.getEconomicAndSocialCouncilComment() != null
							&& d.getGeneralSecretariatComment() != null
							&& d.getLegislativeCouncilComment() != null)
					.toList();
			case "budgetcommittee" -> documentRepository.findByBudgetCommitteeCommentIsNull()
					.stream()
					.filter(d -> d.getEconomicAndSocialCouncilComment() != null
							&& d.getGeneralSecretariatComment() != null
							&& d.getLegislativeCouncilComment() != null)
					.toList();
			case "publicadministration" -> documentRepository.findByPublicAdministrationCommentIsNull()
					.stream()
					.filter(d -> d.getEconomicAndSocialCouncilComment() != null
							&& d.getGeneralSecretariatComment() != null
							&& d.getLegislativeCouncilComment() != null)
					.toList();
			case "proposer" -> new ArrayList<>();
			default -> throw new IllegalArgumentException("Invalid name: " + name);
		};
	}

	Document createDocument() {
        return documentRepository.save(new Document());
    }
}
