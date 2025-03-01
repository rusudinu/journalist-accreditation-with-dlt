package com.rusudinu.backend.request.vc;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;

/**
 * Entity to store verified credentials in the database
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "verifiable_credentials")
public class VerifiableCredentialEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @CreationTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime createdDate;
    
    @Column(name = "request_id", nullable = false, columnDefinition = "TEXT")
    private Long requestId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String vcId;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String issuer;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String issuanceDate;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String subjectId;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String fileHash;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String status;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String proofType;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String proofCreated;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String proofPurpose;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String verificationMethod;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String jws;
    
    /**
     * Create entity from a verifiable credential object
     * @param credential The verifiable credential
     * @param requestId The ID of the associated request
     * @return A new entity populated with credential data
     */
    public static VerifiableCredentialEntity fromVerifiableCredential(VerifiableCredential credential, Long requestId) {
        return VerifiableCredentialEntity.builder()
                .requestId(requestId)
                .vcId(credential.getId())
                .issuer(credential.getIssuer())
                .issuanceDate(credential.getIssuanceDate())
                .subjectId(credential.getCredentialSubject().getId())
                .fileHash(credential.getCredentialSubject().getFileHash())
                .status(credential.getCredentialSubject().getStatus())
                .proofType(credential.getProof().getType())
                .proofCreated(credential.getProof().getCreated())
                .proofPurpose(credential.getProof().getProofPurpose())
                .verificationMethod(credential.getProof().getVerificationMethod())
                .jws(credential.getProof().getJws())
                .build();
    }
    
    /**
     * Convert this entity to a VerifiableCredential object
     * @return A verifiable credential object populated with this entity's data
     */
    public VerifiableCredential toVerifiableCredential() {
        VerifiableCredential credential = new VerifiableCredential();
        credential.setContext("https://www.w3.org/2018/credentials/v1");
        credential.setId(this.vcId);
        credential.setType("VerifiableCredential");
        credential.setIssuer(this.issuer);
        credential.setIssuanceDate(this.issuanceDate);
        
        VerifiableCredential.CredentialSubject subject = new VerifiableCredential.CredentialSubject();
        subject.setId(this.subjectId);
        subject.setFileHash(this.fileHash);
        subject.setStatus(this.status);
        credential.setCredentialSubject(subject);
        
        VerifiableCredential.Proof proof = new VerifiableCredential.Proof();
        proof.setType(this.proofType);
        proof.setCreated(this.proofCreated);
        proof.setProofPurpose(this.proofPurpose);
        proof.setVerificationMethod(this.verificationMethod);
        proof.setJws(this.jws);
        credential.setProof(proof);
        
        return credential;
    }
} 
