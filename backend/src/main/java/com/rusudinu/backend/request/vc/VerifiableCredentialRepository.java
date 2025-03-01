package com.rusudinu.backend.request.vc;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerifiableCredentialRepository extends JpaRepository<VerifiableCredentialEntity, Long> {
    
    /**
     * Find all verifiable credentials for a request by its ID
     * @param requestId The request ID
     * @return List of verifiable credentials
     */
    List<VerifiableCredentialEntity> findByRequestId(Long requestId);
    
    /**
     * Find the most recent verifiable credential for a request
     * @param requestId The request ID
     * @return The most recent verifiable credential, if any
     */
    Optional<VerifiableCredentialEntity> findFirstByRequestIdOrderByCreatedDateDesc(Long requestId);
    
    /**
     * Find a verifiable credential by its ID (the W3C ID, not the database ID)
     * @param vcId The verifiable credential ID
     * @return The verifiable credential, if found
     */
    Optional<VerifiableCredentialEntity> findByVcId(String vcId);
    
    /**
     * Find verifiable credentials by file hash
     * @param fileHash The hash of the file
     * @return List of verifiable credentials
     */
    List<VerifiableCredentialEntity> findByFileHash(String fileHash);
} 