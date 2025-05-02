package com.rusudinu.backend.document;

import com.rusudinu.backend.distributedStorage.DistributedStorageService;
import com.rusudinu.backend.hash.HashService;
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

@Service
@RequiredArgsConstructor
public class DocumentService {
    private static final String UPLOAD_DIR = "uploads/";

    private final DocumentRepository documentRepository;
    private final UserService userService;
	private final DistributedStorageService distributedStorageService;
	private final HashService hashService;

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

	Document addEconomicAndSocialCouncilComment(Long documentId, String comment) {
		Document document = documentRepository.findById(documentId).orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
		document.setEconomicAndSocialCouncilComment(comment);

		String commentKey = document.getId() + "_comment_economic_and_social_council";
		distributedStorageService.persistCommentHash(commentKey, hashService.shaHash(comment));

		return documentRepository.save(document);
	}

	Document addGeneralSecretariatComment(Long documentId, String comment) {
		Document document = documentRepository.findById(documentId).orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
		document.setGeneralSecretariatComment(comment);

		String commentKey = document.getId() + "_comment_general_secretariat";
		distributedStorageService.persistCommentHash(commentKey, hashService.shaHash(comment));

		return documentRepository.save(document);
	}

	Document addLegislativeCouncilComment(Long documentId, String comment) {
		Document document = documentRepository.findById(documentId).orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
		document.setLegislativeCouncilComment(comment);

		String commentKey = document.getId() + "_comment_legislative_council";
		distributedStorageService.persistCommentHash(commentKey, hashService.shaHash(comment));

		return documentRepository.save(document);
	}

	Document addLegalCommitteeComment(Long documentId, String comment) {
		Document document = documentRepository.findById(documentId).orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
		document.setLegalCommitteeComment(comment);

		String commentKey = document.getId() + "_comment_legal_committee";
		distributedStorageService.persistCommentHash(commentKey, hashService.shaHash(comment));

		return documentRepository.save(document);
	}

	Document addBudgetCommitteeComment(Long documentId, String comment) {
		Document document = documentRepository.findById(documentId).orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
		document.setBudgetCommitteeComment(comment);

		String commentKey = document.getId() + "_comment_budget_committee";
		distributedStorageService.persistCommentHash(commentKey, hashService.shaHash(comment));

		return documentRepository.save(document);
	}

	Document addPublicAdministrationComment(Long documentId, String comment) {
		Document document = documentRepository.findById(documentId).orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
		document.setPublicAdministrationComment(comment);

		String commentKey = document.getId() + "_comment_public_administration";
		distributedStorageService.persistCommentHash(commentKey, hashService.shaHash(comment));

		return documentRepository.save(document);
	}

	Document addSpecialtyCommissionComment(Long documentId, String comment) {
		Document document = documentRepository.findById(documentId).orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
		document.setSpecialtyCommissionComment(comment);

		String commentKey = document.getId() + "_comment_specialty_commission";
		distributedStorageService.persistCommentHash(commentKey, hashService.shaHash(comment));

		return documentRepository.save(document);
	}

	boolean isCommentHashValid(Long documentId, String prefix, String comment) {
		// we'll assume that the comment is valid if it's null or empty
		if(comment == null || comment.isEmpty()) {
			return true;
		}

		Document document = documentRepository.findById(documentId).orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
		String commentKey = document.getId() + "_comment_" + prefix;

		String blockchainCommentHash = distributedStorageService.getCommentHashByCommentKey(commentKey);
		String commentHash = hashService.shaHash(comment);

		return blockchainCommentHash.equals(commentHash);
	}

	boolean documentHasAllCommentsValid(Long documentId) {
		Document document = documentRepository.findById(documentId).orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));

		return isCommentHashValid(documentId, "economic_and_social_council", document.getEconomicAndSocialCouncilComment())
				&& isCommentHashValid(documentId, "general_secretariat", document.getGeneralSecretariatComment())
				&& isCommentHashValid(documentId, "legislative_council", document.getLegislativeCouncilComment())
				&& isCommentHashValid(documentId, "legal_committee", document.getLegalCommitteeComment())
				&& isCommentHashValid(documentId, "budget_committee", document.getBudgetCommitteeComment())
				&& isCommentHashValid(documentId, "public_administration", document.getPublicAdministrationComment())
				&& isCommentHashValid(documentId, "specialty_commission", document.getSpecialtyCommissionComment());
	}
}
