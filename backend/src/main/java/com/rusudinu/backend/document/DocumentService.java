package com.rusudinu.backend.document;

import com.rusudinu.backend.distributedStorage.DistributedStorageService;
import com.rusudinu.backend.hash.HashService;
import com.rusudinu.backend.user.UserService;
import java.time.ZonedDateTime;
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
		File directory = new File(UPLOAD_DIR); if (!directory.exists()) {
			if (!directory.mkdirs()) {
				throw new RuntimeException("Failed to create directory: " + UPLOAD_DIR);
			}
		}


		String uniqueFileName = System.currentTimeMillis() + "_" + UUID.randomUUID() + "." + Objects.requireNonNull(file.getOriginalFilename())
				.split("\\.")[1]; Path filePath = Paths.get(UPLOAD_DIR, uniqueFileName);

		Document document = documentRepository.findById(documentId)
				.orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
		document.setStoredDocumentName(uniqueFileName); try {
			Files.write(filePath, file.getBytes());

			// store the document hash in the blockchain
			String documentHash = hashService.hashDocument(getDocument(uniqueFileName));
			String documentHashKey = document.getId() + "_document_hash";
			distributedStorageService.persistCommentHash(documentHashKey, documentHash);

			return documentRepository.save(document);
		}
		catch (IOException e) {
			return null;
		}
	}

	public byte[] getDocument(String storedDocumentName) {
		try {
			Path documentPath = Paths.get(UPLOAD_DIR, storedDocumentName); if (!Files.exists(documentPath)) {
				throw new IOException("File not found: " + storedDocumentName);
			} return Files.readAllBytes(documentPath);
		}
		catch (IOException e) {
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
			case "legalcommittee" -> documentRepository.findByLegalCommitteeCommentIsNull().stream()
					.filter(d -> d.getEconomicAndSocialCouncilComment() != null && d.getGeneralSecretariatComment() != null && d.getLegislativeCouncilComment() != null)
					.toList();
			case "budgetcommittee" -> documentRepository.findByBudgetCommitteeCommentIsNull().stream()
					.filter(d -> d.getEconomicAndSocialCouncilComment() != null && d.getGeneralSecretariatComment() != null && d.getLegislativeCouncilComment() != null)
					.toList();
			case "publicadministration" -> documentRepository.findByPublicAdministrationCommentIsNull().stream()
					.filter(d -> d.getEconomicAndSocialCouncilComment() != null && d.getGeneralSecretariatComment() != null && d.getLegislativeCouncilComment() != null)
					.toList();
			case "specialtycommission" ->
					documentRepository.findByDecidingSpecialtyCommissionDocumentNameIsNull().stream()
							.filter(d -> d.getEconomicAndSocialCouncilComment() != null && d.getGeneralSecretariatComment() != null && d.getLegislativeCouncilComment() != null && d.getLegalCommitteeComment() != null && d.getBudgetCommitteeComment() != null && d.getPublicAdministrationComment() != null)
							.toList();
			case "chamberprezident" -> documentRepository.findByDecidingSpecialtyCommissionDocumentNameIsNotNull();
			case "proposer" -> new ArrayList<>();
			default -> {
				if (normalizedName.startsWith("memberofparliment")) {
					yield documentRepository.findDocumentsThatNeedVote();
				}
				throw new IllegalArgumentException("Invalid name: " + normalizedName);
			}
		};
	}

	Document createDocument() {
		return documentRepository.save(new Document());
	}

	Document addEconomicAndSocialCouncilComment(Long documentId, String comment) {
		Document document = documentRepository.findById(documentId)
				.orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
		document.setEconomicAndSocialCouncilComment(comment);

		String commentKey = document.getId() + "_comment_economic_and_social_council";
		distributedStorageService.persistCommentHash(commentKey, hashService.shaHash(comment));

		return documentRepository.save(document);
	}

	Document addGeneralSecretariatComment(Long documentId, String comment) {
		Document document = documentRepository.findById(documentId)
				.orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
		document.setGeneralSecretariatComment(comment);

		String commentKey = document.getId() + "_comment_general_secretariat";
		distributedStorageService.persistCommentHash(commentKey, hashService.shaHash(comment));

		return documentRepository.save(document);
	}

	Document addLegislativeCouncilComment(Long documentId, String comment) {
		Document document = documentRepository.findById(documentId)
				.orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
		document.setLegislativeCouncilComment(comment);

		String commentKey = document.getId() + "_comment_legislative_council";
		distributedStorageService.persistCommentHash(commentKey, hashService.shaHash(comment));

		return documentRepository.save(document);
	}

	Document addLegalCommitteeComment(Long documentId, String comment) {
		Document document = documentRepository.findById(documentId)
				.orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
		document.setLegalCommitteeComment(comment);

		String commentKey = document.getId() + "_comment_legal_committee";
		distributedStorageService.persistCommentHash(commentKey, hashService.shaHash(comment));

		return documentRepository.save(document);
	}

	Document addBudgetCommitteeComment(Long documentId, String comment) {
		Document document = documentRepository.findById(documentId)
				.orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
		document.setBudgetCommitteeComment(comment);

		String commentKey = document.getId() + "_comment_budget_committee";
		distributedStorageService.persistCommentHash(commentKey, hashService.shaHash(comment));

		return documentRepository.save(document);
	}

	Document addPublicAdministrationComment(Long documentId, String comment) {
		Document document = documentRepository.findById(documentId)
				.orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
		document.setPublicAdministrationComment(comment);

		String commentKey = document.getId() + "_comment_public_administration";
		distributedStorageService.persistCommentHash(commentKey, hashService.shaHash(comment));

		return documentRepository.save(document);
	}

	Document addSpecialtyCommissionDocument(MultipartFile file, Long documentId) {
		File directory = new File(UPLOAD_DIR); if (!directory.exists()) {
			if (!directory.mkdirs()) {
				throw new RuntimeException("Failed to create directory: " + UPLOAD_DIR);
			}
		}


		String uniqueFileName = System.currentTimeMillis() + "_" + UUID.randomUUID() + "." + Objects.requireNonNull(file.getOriginalFilename())
				.split("\\.")[1]; Path filePath = Paths.get(UPLOAD_DIR, uniqueFileName);

		Document document = documentRepository.findById(documentId)
				.orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
		document.setDecidingSpecialtyCommissionDocumentName(uniqueFileName);
		document.setDebateAndApprovalStartDate(ZonedDateTime.now());
		try {
			Files.write(filePath, file.getBytes());

			// store the document hash in the blockchain
			String documentHash = hashService.hashDocument(getDocument(uniqueFileName));
			String documentHashKey = document.getId() + "_specialty_commission_document_hash";
			distributedStorageService.persistCommentHash(documentHashKey, documentHash);

			return documentRepository.save(document);
		}

		catch (IOException e) {
			return null;
		}
	}

	boolean isCommentHashValid(Long documentId, String prefix, String comment) {
		// we'll assume that the comment is valid if it's null or empty
		if (comment == null || comment.isEmpty()) {
			return true;
		}

		Document document = documentRepository.findById(documentId)
				.orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
		String commentKey = document.getId() + "_comment_" + prefix;

		String blockchainCommentHash = distributedStorageService.getCommentHashByCommentKey(commentKey);
		String commentHash = hashService.shaHash(comment);

		return blockchainCommentHash.equals(commentHash);
	}

	boolean documentHasAllCommentsValid(Long documentId) {
		Document document = documentRepository.findById(documentId)
				.orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));

		return isCommentHashValid(documentId, "economic_and_social_council", document.getEconomicAndSocialCouncilComment()) && isCommentHashValid(documentId, "general_secretariat", document.getGeneralSecretariatComment()) && isCommentHashValid(documentId, "legislative_council", document.getLegislativeCouncilComment()) && isCommentHashValid(documentId, "legal_committee", document.getLegalCommitteeComment()) && isCommentHashValid(documentId, "budget_committee", document.getBudgetCommitteeComment()) && isCommentHashValid(documentId, "public_administration", document.getPublicAdministrationComment());
	}

	boolean validateDocumentHash(Long documentId) {
		Document document = documentRepository.findById(documentId)
				.orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));

		String storedDocumentName = document.getStoredDocumentName();
		if (storedDocumentName == null || storedDocumentName.isEmpty()) {
			return false;
		} String documentHash = hashService.hashDocument(getDocument(storedDocumentName));
		String documentHashKey = document.getId() + "_document_hash";

		String blockchainDocumentHash = distributedStorageService.getCommentHashByCommentKey(documentHashKey);
		return blockchainDocumentHash.equals(documentHash);
	}

	boolean validateSpecialtyCommissionDocumentHash(Long documentId) {
		Document document = documentRepository.findById(documentId)
				.orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));

		String storedDocumentName = document.getDecidingSpecialtyCommissionDocumentName();
		if (storedDocumentName == null || storedDocumentName.isEmpty()) {
			return false;
		} String documentHash = hashService.hashDocument(getDocument(storedDocumentName));
		String documentHashKey = document.getId() + "_specialty_commission_document_hash";

		String blockchainDocumentHash = distributedStorageService.getCommentHashByCommentKey(documentHashKey);
		return blockchainDocumentHash.equals(documentHash);
	}

	Document startPlenarySession(Long documentId) {
		Document document = documentRepository.findById(documentId)
				.orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
		document.setCountdownAutoApproval(false);
		return documentRepository.save(document);
	}

	Document vote(Long documentId, String vote, String name) {
		String normalizedName = name.trim().toLowerCase();

		if (normalizedName.length() > 20) {
			normalizedName = userService.findOrCreateByKeycloakId(normalizedName).getName();
		}

		normalizedName = normalizedName.replaceAll("\\s+", "").toLowerCase();

		Document document = documentRepository.findById(documentId)
				.orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));

		String voteKey = normalizedName + ":" + vote;

		document.getDebateAndApprovalPlenarySessionVoteResults().add(voteKey);
		document.setDebateAndApprovalPlenarySessionVoteResults(document.getDebateAndApprovalPlenarySessionVoteResults());
		return documentRepository.save(document);
	}
}
