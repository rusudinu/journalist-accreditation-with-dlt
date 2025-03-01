package com.rusudinu.backend.request.vc;

import com.rusudinu.backend.request.VerifiableCredentialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/verifiable-credentials")
@RequiredArgsConstructor
public class VerifiableCredentialController {
    
    private final VerifiableCredentialService verifiableCredentialService;
    
    /**
     * Endpoint to verify a Verifiable Credential
     * @param credential The verifiable credential to verify
     * @return A response entity containing the verification result
     */
    @PostMapping("/verify")
    public ResponseEntity<VerificationResponse> verifyCredential(@RequestBody VerifiableCredential credential) {
        boolean isValid = verifiableCredentialService.validateVerifiableCredential(credential);
        
        VerificationResponse response = new VerificationResponse();
        response.setValid(isValid);
        response.setVerifiedAt(Instant.now().toString());
        response.setVerifiedBy("Ministry Verification System");
        
        if (isValid) {
            response.setMessage("The credential is valid and its signature has been verified.");
            return ResponseEntity.ok(response);
        } else {
            response.setMessage("The credential is invalid. Either the signature could not be verified or the credential structure is incorrect.");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Endpoint to retrieve details from a Verifiable Credential without validation
     * @param credential The verifiable credential to extract information from
     * @return A response entity containing the credential details
     */
    @PostMapping("/details")
    public ResponseEntity<Map<String, Object>> getCredentialDetails(@RequestBody VerifiableCredential credential) {
        Map<String, Object> details = new HashMap<>();
        
        // Skip validation and extract information
        details.put("id", credential.getId());
        details.put("type", credential.getType());
        details.put("issuer", credential.getIssuer());
        details.put("issuanceDate", credential.getIssuanceDate());
        
        if (credential.getCredentialSubject() != null) {
            Map<String, Object> subjectInfo = new HashMap<>();
            subjectInfo.put("id", credential.getCredentialSubject().getId());
            subjectInfo.put("fileHash", credential.getCredentialSubject().getFileHash());
            subjectInfo.put("status", credential.getCredentialSubject().getStatus());
            details.put("credentialSubject", subjectInfo);
        }
        
        if (credential.getProof() != null) {
            Map<String, Object> proofInfo = new HashMap<>();
            proofInfo.put("type", credential.getProof().getType());
            proofInfo.put("created", credential.getProof().getCreated());
            proofInfo.put("proofPurpose", credential.getProof().getProofPurpose());
            proofInfo.put("verificationMethod", credential.getProof().getVerificationMethod());
            // Don't include the JWS to keep the response cleaner
            details.put("proof", proofInfo);
        }
        
        return ResponseEntity.ok(details);
    }
} 