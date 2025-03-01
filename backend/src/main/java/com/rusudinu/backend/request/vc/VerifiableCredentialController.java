package com.rusudinu.backend.request.vc;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
    
    /**
     * Get all verifiable credentials for a request
     * @param requestId The request ID
     * @return List of verifiable credentials
     */
    @GetMapping("/request/{requestId}")
    public ResponseEntity<List<VerifiableCredential>> getCredentialsForRequest(@PathVariable Long requestId) {
        List<VerifiableCredential> credentials = verifiableCredentialService.getVerifiableCredentialsForRequest(requestId);
        return ResponseEntity.ok(credentials);
    }
    
    /**
     * Get the most recent verifiable credential for a request
     * @param requestId The request ID
     * @return The most recent verifiable credential
     */
    @GetMapping("/request/{requestId}/latest")
    public ResponseEntity<VerifiableCredential> getLatestCredentialForRequest(@PathVariable Long requestId) {
        Optional<VerifiableCredential> credential = verifiableCredentialService.getLatestVerifiableCredentialForRequest(requestId);
        return credential.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Find a verifiable credential by its ID
     * @param credentialId The credential ID (W3C ID)
     * @return The verifiable credential
     */
    @GetMapping("/{credentialId}")
    public ResponseEntity<VerifiableCredential> getCredentialById(@PathVariable String credentialId) {
        log.info("Get credential by id: {}", credentialId);
        Optional<VerifiableCredential> credential = verifiableCredentialService.findByCredentialId(credentialId);
        return credential.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Find verifiable credentials by file hash
     * @param fileHash The hash of the file
     * @return List of verifiable credentials
     */
    @GetMapping("/file-hash/{fileHash}")
    public ResponseEntity<List<VerifiableCredential>> getCredentialsByFileHash(@PathVariable String fileHash) {
        List<VerifiableCredential> credentials = verifiableCredentialService.findByFileHash(fileHash);
        return ResponseEntity.ok(credentials);
    }
} 
