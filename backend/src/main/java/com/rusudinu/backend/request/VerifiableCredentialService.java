package com.rusudinu.backend.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rusudinu.backend.request.vc.VerifiableCredential;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerifiableCredentialService {
	
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	private final PrivateKey ministryPrivateKey;
	
	private final PublicKey ministryPublicKey;
	
	public VerifiableCredential createVerifiableCredentialFromDocumentHash(String documentHash, Long requestId) {
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
}
