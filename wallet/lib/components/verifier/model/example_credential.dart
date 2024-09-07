import 'verifiable_credential.dart';
import 'credential_builder.dart';

/// Example credentials for testing and demonstration
class ExampleCredentials {
  /// Create a valid journalist credential example
  static VerifiableCredential validJournalistCredential() {
    final now = DateTime.now().toUtc();
    final oneYearLater = now.add(const Duration(days: 365));

    return JournalistCredentialBuilder()
        .withId('https://accreditation.gov/credentials/12345')
        .withIssuer('did:web:accreditation.gov')
        .withIssuanceDateFromDateTime(now)
        .withExpirationDateFromDateTime(oneYearLater)
        .withSubjectId('did:key:z6Mkj7af4TJjKXuLgXoRQyiQJ2b8YBqCfY3jvYnQzaYWGvYb')
        .withAccreditationNumber('J-2025-12345')
        .withFullName('Jane Doe')
        .withOrganization('News Media Co')
        .withValidFromDateTime(DateTime(2025, 1, 1))
        .withValidUntilDateTime(DateTime(2026, 1, 1))
        .withProofType('Ed25519Signature2020')
        .withProofCreatedDateTime(now)
        .withProofPurpose('assertionMethod')
        .withVerificationMethod('did:web:accreditation.gov#key-1')
        .withJws(
            'eyJhbGciOiJFZERTQSIsImI2NCI6ZmFsc2UsImNyaXQiOlsiYjY0Il19..example_signature')
        .build();
  }

  /// Create an expired credential example
  static VerifiableCredential expiredJournalistCredential() {
    final pastDate = DateTime(2024, 1, 1);
    final expirationDate = DateTime(2024, 12, 31);

    return JournalistCredentialBuilder()
        .withId('https://accreditation.gov/credentials/99999')
        .withIssuer('did:web:accreditation.gov')
        .withIssuanceDateFromDateTime(pastDate)
        .withExpirationDateFromDateTime(expirationDate)
        .withSubjectId('did:key:z6MkjExpiredExample123456789')
        .withAccreditationNumber('J-2024-99999')
        .withFullName('John Smith')
        .withOrganization('Old News Corp')
        .withValidFromDateTime(pastDate)
        .withValidUntilDateTime(expirationDate)
        .withProofType('Ed25519Signature2020')
        .withProofCreatedDateTime(pastDate)
        .withProofPurpose('assertionMethod')
        .withVerificationMethod('did:web:accreditation.gov#key-1')
        .withJws('eyJhbGciOiJFZERTQSJ9.expired.signature')
        .build();
  }

  /// Get the JSON representation of a valid credential
  static String validCredentialJson() {
    return '''
{
  "@context": [
    "https://www.w3.org/2018/credentials/v1",
    "https://example.org/journalist-credentials/v1"
  ],
  "id": "https://accreditation.gov/credentials/12345",
  "type": ["VerifiableCredential", "JournalistAccreditationCredential"],
  "issuer": "did:web:accreditation.gov",
  "issuanceDate": "2025-01-15T00:00:00Z",
  "credentialSubject": {
    "id": "did:key:z6Mkj7af4TJjKXuLgXoRQyiQJ2b8YBqCfY3jvYnQzaYWGvYb",
    "type": "Journalist",
    "accreditationNumber": "J-2025-12345",
    "fullName": "Jane Doe",
    "organization": "News Media Co",
    "validFrom": "2025-01-01T00:00:00Z",
    "validUntil": "2026-01-01T00:00:00Z"
  },
  "proof": {
    "type": "Ed25519Signature2020",
    "created": "2025-01-15T00:00:00Z",
    "proofPurpose": "assertionMethod",
    "verificationMethod": "did:web:accreditation.gov#key-1",
    "jws": "eyJhbGciOiJFZERTQSIsImI2NCI6ZmFsc2UsImNyaXQiOlsiYjY0Il19..example_signature"
  }
}
''';
  }

  /// Parse and return a credential from the example JSON
  static VerifiableCredential fromValidJson() {
    return VerifiableCredential.fromJsonString(validCredentialJson());
  }
}
