package com.rusudinu.backend.request.vc;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response object for verifiable credential verification
 */
@Data
@NoArgsConstructor
public class VerificationResponse {
    private boolean valid;
    private String message;
    
    // Optional fields for additional verification details
    private String verifiedAt;
    private String verifiedBy;
} 