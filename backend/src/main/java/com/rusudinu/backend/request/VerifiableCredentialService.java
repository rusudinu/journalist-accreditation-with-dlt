package com.rusudinu.backend.request;

import com.rusudinu.backend.request.vc.VerifiableCredential;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerifiableCredentialService {
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
		proof.setJws(generateJws(credential)); // Implement this method to generate the JWS
		credential.setProof(proof);

		return credential;
	}

	private String generateJws(VerifiableCredential credential) {
		// Implement the logic to generate the JWS (JSON Web Signature) for the credential
		// This typically involves signing the credential with a private key
		return "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEifQ..."; // Placeholder for the actual JWS
	}
}
