package com.rusudinu.backend.request.vc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rusudinu.backend.request.Request;
import com.rusudinu.backend.request.RequestService;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerifiableCredentialService {
	
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	private final PrivateKey ministryPrivateKey;
	
	private final PublicKey ministryPublicKey;
	
	private final VerifiableCredentialRepository verifiableCredentialRepository;
	
	private final RequestService requestService;
	
	/**
	 * Create a verifiable credential from a document hash and store it in the database
	 * @param documentHash The hash of the document
	 * @param requestId The ID of the request
	 * @return The created verifiable credential
	 */
	public VerifiableCredential createVerifiableCredentialFromDocumentHash(String documentHash, Long requestId) {
		log.info("Creating verifiable credential for request {}", requestId);
		
		VerifiableCredential credential = new VerifiableCredential();
		credential.setContext("https://www.w3.org/2018/credentials/v1");
		credential.setId("urn:uuid:" + UUID.randomUUID());
		credential.setType("VerifiableCredential");
		credential.setIssuer("https://example.com/issuer");
		credential.setIssuanceDate(Instant.now().toString());

		VerifiableCredential.CredentialSubject subject = new VerifiableCredential.CredentialSubject();
		subject.setId("urn:uuid:" + UUID.randomUUID());
		subject.setFileHash(documentHash);
		subject.setStatus("created");
		credential.setCredentialSubject(subject);

		VerifiableCredential.Proof proof = new VerifiableCredential.Proof();
		proof.setType("RsaSignature2018");
		proof.setCreated(Instant.now().toString());
		proof.setProofPurpose("assertionMethod");
		proof.setVerificationMethod("https://example.com/issuer/keys/" + requestId);
		proof.setJws(generateJws(credential));
		credential.setProof(proof);
		
		// Store the credential in the database
		VerifiableCredentialEntity entity = VerifiableCredentialEntity.fromVerifiableCredential(credential, requestId);
		log.info("Saving verifiable credential entity for request {}, value {}", requestId, entity);
		verifiableCredentialRepository.save(entity);

		log.info("Created verifiable credential for request {}", requestId);
		return credential;
	}

	private String generateJws(VerifiableCredential credential) {
		try {
			// Create a copy of the credential without the proof (to avoid circular reference)
			VerifiableCredential credentialWithoutProof = new VerifiableCredential();
			credentialWithoutProof.setContext(credential.getContext());
			credentialWithoutProof.setId(credential.getId());
			credentialWithoutProof.setType(credential.getType());
			credentialWithoutProof.setIssuer(credential.getIssuer());
			credentialWithoutProof.setIssuanceDate(credential.getIssuanceDate());
			credentialWithoutProof.setCredentialSubject(credential.getCredentialSubject());
			
			// Convert credential to JSON
			String payload = objectMapper.writeValueAsString(credentialWithoutProof);
			
			// Create the JWS header
			ObjectNode header = objectMapper.createObjectNode();
			header.put("alg", "RS256");
			header.put("typ", "JWT");
			
			// Base64-encode the header and payload
			String encodedHeader = Base64.getUrlEncoder().withoutPadding()
					.encodeToString(objectMapper.writeValueAsString(header).getBytes(StandardCharsets.UTF_8));
			String encodedPayload = Base64.getUrlEncoder().withoutPadding()
					.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
			
			// Create the signature input (header.payload)
			String signatureInput = encodedHeader + "." + encodedPayload;
			
			// Sign the input
			Signature signature = Signature.getInstance("SHA256withRSA");
			signature.initSign(ministryPrivateKey);
			signature.update(signatureInput.getBytes(StandardCharsets.UTF_8));
			String encodedSignature = Base64.getUrlEncoder().withoutPadding()
					.encodeToString(signature.sign());
			
			// Combine all parts to form the JWS
			return signatureInput + "." + encodedSignature;
		} catch (Exception e) {
			throw new RuntimeException("Failed to generate JWS: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Validates a verifiable credential by checking its signature and other properties.
	 * @param credential The credential to validate
	 * @return true if the credential is valid, false otherwise
	 */
	public boolean validateVerifiableCredential(VerifiableCredential credential) {
		try {
			// Check if the credential has all required fields
			if (credential == null || credential.getContext() == null || credential.getId() == null ||
					credential.getType() == null || credential.getIssuer() == null || 
					credential.getIssuanceDate() == null || credential.getCredentialSubject() == null ||
					credential.getProof() == null) {
				return false;
			}
			
			// Verify that the credential is not expired (assuming no expiration date in this implementation,
			// but you might want to add one in the future)
			
			// Extract the JWS parts
			String jws = credential.getProof().getJws();
			String[] jwsParts = jws.split("\\.");
			if (jwsParts.length != 3) {
				return false;
			}
			
			// Verify the signature
			String signatureInput = jwsParts[0] + "." + jwsParts[1];
			byte[] signatureBytes = Base64.getUrlDecoder().decode(jwsParts[2]);
			
			Signature signature = Signature.getInstance("SHA256withRSA");
			signature.initVerify(ministryPublicKey);
			signature.update(signatureInput.getBytes(StandardCharsets.UTF_8));
			
			// Return the signature verification result
			return signature.verify(signatureBytes);
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			throw new RuntimeException("Failed to validate credential: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Get all verifiable credentials for a request
	 * @param requestId The request ID
	 * @return List of verifiable credentials
	 */
	public List<VerifiableCredential> getVerifiableCredentialsForRequest(Long requestId) {
		return verifiableCredentialRepository.findByRequestId(requestId)
				.stream()
				.map(VerifiableCredentialEntity::toVerifiableCredential)
				.collect(Collectors.toList());
	}
	
	/**
	 * Get the most recent verifiable credential for a request
	 * @param requestId The request ID
	 * @return The most recent verifiable credential, if any
	 */
	public Optional<VerifiableCredential> getLatestVerifiableCredentialForRequest(Long requestId) {
		return verifiableCredentialRepository.findFirstByRequestIdOrderByCreatedDateDesc(requestId)
				.map(VerifiableCredentialEntity::toVerifiableCredential);
	}
	
	/**
	 * Find a verifiable credential by its ID
	 * @param credentialId The credential ID (W3C ID)
	 * @return The verifiable credential, if found
	 */
	public Optional<VerifiableCredential> findByCredentialId(String credentialId) {
		return verifiableCredentialRepository.findByVcId(credentialId)
				.map(VerifiableCredentialEntity::toVerifiableCredential);
	}
	
	/**
	 * Find verifiable credentials by file hash
	 * @param fileHash The hash of the file
	 * @return List of verifiable credentials
	 */
	public List<VerifiableCredential> findByFileHash(String fileHash) {
		return verifiableCredentialRepository.findByFileHash(fileHash)
				.stream()
				.map(VerifiableCredentialEntity::toVerifiableCredential)
				.collect(Collectors.toList());
	}
}
