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

	// possible names: politicscommittee,economicandsocialcouncil,generalsecretariat,transportcommittee,legislativecouncil,specialtycommission
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
			case "transportcommittee" -> documentRepository.findByTransportCommitteeCommentIsNull().stream()
					.filter(d -> d.getEconomicAndSocialCouncilComment() != null && d.getGeneralSecretariatComment() != null && d.getLegislativeCouncilComment() != null 
					// Check that all required documents exist
					&& d.getEconomicAndSocialCouncilDocumentName() != null && d.getGeneralSecretariatDocumentName() != null && d.getLegislativeCouncilDocumentName() != null)
					.toList();
			case "politicscommittee" -> documentRepository.findByPoliticsCommitteeCommentIsNull().stream()
					.filter(d -> d.getEconomicAndSocialCouncilComment() != null && d.getGeneralSecretariatComment() != null && d.getLegislativeCouncilComment() != null 
					// Check that all required documents exist
					&& d.getEconomicAndSocialCouncilDocumentName() != null && d.getGeneralSecretariatDocumentName() != null && d.getLegislativeCouncilDocumentName() != null)
					.toList();
			case "specialtycommission" ->
					documentRepository.findByDecidingSpecialtyCommissionDocumentNameIsNull().stream()
							.filter(d -> d.getEconomicAndSocialCouncilComment() != null && d.getGeneralSecretariatComment() != null && d.getLegislativeCouncilComment() != null 
							&& d.getTransportCommitteeComment() != null && d.getPoliticsCommitteeComment() != null
							// Check that all required documents exist
							&& d.getEconomicAndSocialCouncilDocumentName() != null && d.getGeneralSecretariatDocumentName() != null && d.getLegislativeCouncilDocumentName() != null
							&& d.getTransportCommitteeDocumentName() != null && d.getPoliticsCommitteeDocumentName() != null)
							.toList();
			case "chamberprezident" -> documentRepository.findByDecidingSpecialtyCommissionDocumentNameIsNotNull();
			case "legislativeproposer" -> new ArrayList<>();
			default -> {
				if (normalizedName.startsWith("memberofparliment")) {
					yield documentRepository.findDocumentsThatNeedVote();
				}
				throw new IllegalArgumentException("Invalid name: " + normalizedName);
			}
		};
	}

	Document createDocument(String documentName) {
		Document document = new Document();
		document.setLawName(documentName);
		return documentRepository.save(document);
	}

	Document addEconomicAndSocialCouncilComment(Long documentId, String comment) {
		Document document = documentRepository.findById(documentId)
				.orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
		document.setEconomicAndSocialCouncilComment(comment);

		String commentKey = document.getId() + "_comment_economic_and_social_council";
		distributedStorageService.persistCommentHash(commentKey, hashService.shaHash(comment));

		return documentRepository.save(document);
	}

	Document addEconomicAndSocialCouncilDocument(MultipartFile file, Long documentId) {
		File directory = new File(UPLOAD_DIR); 
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				throw new RuntimeException("Failed to create directory: " + UPLOAD_DIR);
			}
		}

		String uniqueFileName = System.currentTimeMillis() + "_" + UUID.randomUUID() + "." + Objects.requireNonNull(file.getOriginalFilename())
				.split("\\.")[1]; 
		Path filePath = Paths.get(UPLOAD_DIR, uniqueFileName);

		Document document = documentRepository.findById(documentId)
				.orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
		document.setEconomicAndSocialCouncilDocumentName(uniqueFileName); 
		try {
			Files.write(filePath, file.getBytes());

			// store the document hash in the blockchain
			String documentHash = hashService.hashDocument(getDocument(uniqueFileName));
			String documentHashKey = document.getId() + "_economic_and_social_council_document_hash";
			distributedStorageService.persistCommentHash(documentHashKey, documentHash);

			return documentRepository.save(document);
		}
		catch (IOException e) {
			return null;
		}
	}

	Document addGeneralSecretariatComment(Long documentId, String comment) {
		Document document = documentRepository.findById(documentId)
				.orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
		document.setGeneralSecretariatComment(comment);

		String commentKey = document.getId() + "_comment_general_secretariat";
		distributedStorageService.persistCommentHash(commentKey, hashService.shaHash(comment));

		return documentRepository.save(document);
	}
	
	Document addGeneralSecretariatDocument(MultipartFile file, Long documentId) {
		File directory = new File(UPLOAD_DIR); 
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				throw new RuntimeException("Failed to create directory: " + UPLOAD_DIR);
			}
		}

		String uniqueFileName = System.currentTimeMillis() + "_" + UUID.randomUUID() + "." + Objects.requireNonNull(file.getOriginalFilename())
				.split("\\.")[1]; 
		Path filePath = Paths.get(UPLOAD_DIR, uniqueFileName);

		Document document = documentRepository.findById(documentId)
				.orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
		document.setGeneralSecretariatDocumentName(uniqueFileName); 
		try {
			Files.write(filePath, file.getBytes());

			// store the document hash in the blockchain
			String documentHash = hashService.hashDocument(getDocument(uniqueFileName));
			String documentHashKey = document.getId() + "_general_secretariat_document_hash";
			distributedStorageService.persistCommentHash(documentHashKey, documentHash);

			return documentRepository.save(document);
		}
		catch (IOException e) {
			return null;
		}
	}

	Document addLegislativeCouncilComment(Long documentId, String comment) {
		Document document = documentRepository.findById(documentId)
				.orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
		document.setLegislativeCouncilComment(comment);

		String commentKey = document.getId() + "_comment_legislative_council";
		distributedStorageService.persistCommentHash(commentKey, hashService.shaHash(comment));

		return documentRepository.save(document);
	}
	
	Document addLegislativeCouncilDocument(MultipartFile file, Long documentId) {
		File directory = new File(UPLOAD_DIR); 
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				throw new RuntimeException("Failed to create directory: " + UPLOAD_DIR);
			}
		}

		String uniqueFileName = System.currentTimeMillis() + "_" + UUID.randomUUID() + "." + Objects.requireNonNull(file.getOriginalFilename())
				.split("\\.")[1]; 
		Path filePath = Paths.get(UPLOAD_DIR, uniqueFileName);

		Document document = documentRepository.findById(documentId)
				.orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
		document.setLegislativeCouncilDocumentName(uniqueFileName); 
		try {
			Files.write(filePath, file.getBytes());

			// store the document hash in the blockchain
			String documentHash = hashService.hashDocument(getDocument(uniqueFileName));
			String documentHashKey = document.getId() + "_legislative_council_document_hash";
			distributedStorageService.persistCommentHash(documentHashKey, documentHash);

			return documentRepository.save(document);
		}
		catch (IOException e) {
			return null;
		}
	}

	Document addTransportCommitteeComment(Long documentId, String comment) {
		Document document = documentRepository.findById(documentId)
				.orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
		document.setTransportCommitteeComment(comment);

		String commentKey = document.getId() + "_comment_transport_committee";
		distributedStorageService.persistCommentHash(commentKey, hashService.shaHash(comment));

		return documentRepository.save(document);
	}
	
	Document addTransportCommitteeDocument(MultipartFile file, Long documentId) {
		File directory = new File(UPLOAD_DIR); 
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				throw new RuntimeException("Failed to create directory: " + UPLOAD_DIR);
			}
		}

		String uniqueFileName = System.currentTimeMillis() + "_" + UUID.randomUUID() + "." + Objects.requireNonNull(file.getOriginalFilename())
				.split("\\.")[1]; 
		Path filePath = Paths.get(UPLOAD_DIR, uniqueFileName);

		Document document = documentRepository.findById(documentId)
				.orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
		document.setTransportCommitteeDocumentName(uniqueFileName); 
		try {
			Files.write(filePath, file.getBytes());

			// store the document hash in the blockchain
			String documentHash = hashService.hashDocument(getDocument(uniqueFileName));
			String documentHashKey = document.getId() + "_transport_committee_document_hash";
			distributedStorageService.persistCommentHash(documentHashKey, documentHash);

			return documentRepository.save(document);
		}
		catch (IOException e) {
			return null;
		}
	}

	Document addPoliticsCommitteeComment(Long documentId, String comment) {
		Document document = documentRepository.findById(documentId)
				.orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
		document.setPoliticsCommitteeComment(comment);

		String commentKey = document.getId() + "_comment_politics_committee";
		distributedStorageService.persistCommentHash(commentKey, hashService.shaHash(comment));

		return documentRepository.save(document);
	}
	
	Document addPoliticsCommitteeDocument(MultipartFile file, Long documentId) {
		File directory = new File(UPLOAD_DIR); 
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				throw new RuntimeException("Failed to create directory: " + UPLOAD_DIR);
			}
		}

		String uniqueFileName = System.currentTimeMillis() + "_" + UUID.randomUUID() + "." + Objects.requireNonNull(file.getOriginalFilename())
				.split("\\.")[1]; 
		Path filePath = Paths.get(UPLOAD_DIR, uniqueFileName);

		Document document = documentRepository.findById(documentId)
				.orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
		document.setPoliticsCommitteeDocumentName(uniqueFileName); 
		try {
			Files.write(filePath, file.getBytes());

			// store the document hash in the blockchain
			String documentHash = hashService.hashDocument(getDocument(uniqueFileName));
			String documentHashKey = document.getId() + "_politics_committee_document_hash";
			distributedStorageService.persistCommentHash(documentHashKey, documentHash);

			return documentRepository.save(document);
		}
		catch (IOException e) {
			return null;
		}
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

		boolean commentsValid = isCommentHashValid(documentId, "economic_and_social_council", document.getEconomicAndSocialCouncilComment()) 
				&& isCommentHashValid(documentId, "general_secretariat", document.getGeneralSecretariatComment()) 
				&& isCommentHashValid(documentId, "legislative_council", document.getLegislativeCouncilComment()) 
				&& isCommentHashValid(documentId, "transport_committee", document.getTransportCommitteeComment()) 
				&& isCommentHashValid(documentId, "politics_committee", document.getPoliticsCommitteeComment());
				
		// Now also check if all required documents are present for submitted comments
		boolean documentsPresent = true;
		
		if (document.getEconomicAndSocialCouncilComment() != null && document.getEconomicAndSocialCouncilDocumentName() == null) {
			documentsPresent = false;
		}
		
		if (document.getGeneralSecretariatComment() != null && document.getGeneralSecretariatDocumentName() == null) {
			documentsPresent = false;
		}
		
		if (document.getLegislativeCouncilComment() != null && document.getLegislativeCouncilDocumentName() == null) {
			documentsPresent = false;
		}
		
		if (document.getTransportCommitteeComment() != null && document.getTransportCommitteeDocumentName() == null) {
			documentsPresent = false;
		}
		
		if (document.getPoliticsCommitteeComment() != null && document.getPoliticsCommitteeDocumentName() == null) {
			documentsPresent = false;
		}
		
		return commentsValid && documentsPresent;
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

		if (document.getDebateAndApprovalPlenarySessionVoteResults() == null) {
			document.setDebateAndApprovalPlenarySessionVoteResults(new ArrayList<>());
		}
		document.getDebateAndApprovalPlenarySessionVoteResults().add(voteKey);
		document.setDebateAndApprovalPlenarySessionVoteResults(document.getDebateAndApprovalPlenarySessionVoteResults());

		if (document.getDebateAndApprovalPlenarySessionVoteResults().size() > 1) {
			document.setPlenarySessionFinished(true);
		}

		return documentRepository.save(document);
	}

	DocumentStatus getDocumentStatus(Long documentId) {
		Document document = documentRepository.findById(documentId)
				.orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));

		if (document.isPlenarySessionFinished()) {
			return DocumentStatus.FINALIZED;
		}
		else if (document.getDebateAndApprovalPlenarySessionVoteResults() != null) {
			return DocumentStatus.DEBATE;
		}
		else if (document.getTransportCommitteeComment() != null && document.getPoliticsCommitteeComment() != null
				// Check that all required documents exist
				&& document.getTransportCommitteeDocumentName() != null && document.getPoliticsCommitteeDocumentName() != null) {
			return DocumentStatus.AGGREGATION;
		}
		else if ((document.getTransportCommitteeComment() != null && document.getTransportCommitteeDocumentName() != null) 
				|| (document.getPoliticsCommitteeComment() != null && document.getPoliticsCommitteeDocumentName() != null)) {
			return DocumentStatus.AMENDMENTS;
		}
		else if ((document.getGeneralSecretariatComment() != null && document.getGeneralSecretariatDocumentName() != null) 
				|| (document.getLegislativeCouncilComment() != null && document.getLegislativeCouncilDocumentName() != null) 
				|| (document.getEconomicAndSocialCouncilComment() != null && document.getEconomicAndSocialCouncilDocumentName() != null)) {
			return DocumentStatus.REGISTRATION_PARLIAMENT;
		}
		return DocumentStatus.LEGISLATIVE_PROPOSAL;
	}
}
