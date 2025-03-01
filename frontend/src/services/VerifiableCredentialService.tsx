import axios from 'axios';

/**
 * Interface for verification response
 */
export interface VerificationResponse {
    valid: boolean;
    message: string;
    verifiedAt: string;
    verifiedBy: string;
}

/**
 * Interface for proof of a verifiable credential
 */
export interface Proof {
    type: string;
    created: string;
    proofPurpose: string;
    verificationMethod: string;
    jws: string;
}

/**
 * Interface for credential subject
 */
export interface CredentialSubject {
    id: string;
    fileHash: string;
    status: string;
}

/**
 * Interface for a verifiable credential
 */
export interface VerifiableCredential {
    context: string;
    id: string;
    type: string;
    issuer: string;
    issuanceDate: string;
    credentialSubject: CredentialSubject;
    proof: Proof;
}

/**
 * Service for verifiable credential operations
 */
export class VerifiableCredentialService {
    private baseUrl: string;

    constructor() {
        this.baseUrl = `${import.meta.env.VITE_BACKEND_URL}/api/v1/verifiable-credentials`;
    }

    /**
     * Get the latest verifiable credential for a request
     * @param requestId The ID of the request
     * @returns Promise with the latest verifiable credential or null if not found
     */
    async getLatestCredential(requestId: string): Promise<VerifiableCredential | null> {
        try {
            const response = await axios.get<VerifiableCredential>(
                `${this.baseUrl}/request/${requestId}/latest`
            );
            return response.data;
        } catch (error: unknown) {
            console.error('Error fetching latest credential:', error);
            return null;
        }
    }

    /**
     * Find a credential by its ID
     * @param credentialId The ID of the credential to find
     * @returns Promise with the credential or null if not found
     */
    async findByCredentialId(credentialId: string): Promise<VerifiableCredential | null> {
        try {
            const response = await axios.get<VerifiableCredential>(
                `${this.baseUrl}/${encodeURIComponent(credentialId)}`
            );
            return response.data;
        } catch (error: unknown) {
            console.error('Error finding credential:', error);
            return null;
        }
    }

    /**
     * Verify a verifiable credential
     * @param credential The credential to verify
     * @returns Promise with the verification response
     */
    async verifyCredential(credential: VerifiableCredential): Promise<VerificationResponse> {
        try {
            const response = await axios.post<VerificationResponse>(
                `${this.baseUrl}/verify`,
                credential
            );
            return response.data;
        } catch (error: unknown) {
            console.error('Error verifying credential:', error);
            return {
                valid: false,
                message: 'Error verifying credential',
                verifiedAt: new Date().toISOString(),
                verifiedBy: 'Client'
            };
        }
    }
}

// Create and export an instance of the service
export const verifiableCredentialService = new VerifiableCredentialService(); 