import 'verifiable_credential.dart';
import 'credential_subject.dart';
import 'proof.dart';

/// Builder for creating Journalist Accreditation Verifiable Credentials
class JournalistCredentialBuilder {
  String? _id;
  String? _issuer;
  String? _issuanceDate;
  String? _expirationDate;

  // Subject fields
  String? _subjectId;
  String? _accreditationNumber;
  String? _fullName;
  String? _organization;
  String? _validFrom;
  String? _validUntil;

  // Proof fields
  String? _proofType;
  String? _proofCreated;
  String? _proofPurpose;
  String? _verificationMethod;
  String? _jws;

  /// Set the credential ID
  JournalistCredentialBuilder withId(String id) {
    _id = id;
    return this;
  }

  /// Set the issuer DID or URI
  JournalistCredentialBuilder withIssuer(String issuer) {
    _issuer = issuer;
    return this;
  }

  /// Set the issuance date (ISO 8601 format)
  JournalistCredentialBuilder withIssuanceDate(String issuanceDate) {
    _issuanceDate = issuanceDate;
    return this;
  }

  /// Set the issuance date from DateTime
  JournalistCredentialBuilder withIssuanceDateFromDateTime(DateTime dateTime) {
    _issuanceDate = dateTime.toUtc().toIso8601String();
    return this;
  }

  /// Set the expiration date (ISO 8601 format)
  JournalistCredentialBuilder withExpirationDate(String expirationDate) {
    _expirationDate = expirationDate;
    return this;
  }

  /// Set the expiration date from DateTime
  JournalistCredentialBuilder withExpirationDateFromDateTime(
      DateTime dateTime) {
    _expirationDate = dateTime.toUtc().toIso8601String();
    return this;
  }

  /// Set the subject (journalist) DID
  JournalistCredentialBuilder withSubjectId(String subjectId) {
    _subjectId = subjectId;
    return this;
  }

  /// Set the accreditation number
  JournalistCredentialBuilder withAccreditationNumber(
      String accreditationNumber) {
    _accreditationNumber = accreditationNumber;
    return this;
  }

  /// Set the journalist's full name
  JournalistCredentialBuilder withFullName(String fullName) {
    _fullName = fullName;
    return this;
  }

  /// Set the news organization
  JournalistCredentialBuilder withOrganization(String organization) {
    _organization = organization;
    return this;
  }

  /// Set the validity start date (ISO 8601 format)
  JournalistCredentialBuilder withValidFrom(String validFrom) {
    _validFrom = validFrom;
    return this;
  }

  /// Set the validity start date from DateTime
  JournalistCredentialBuilder withValidFromDateTime(DateTime dateTime) {
    _validFrom = dateTime.toUtc().toIso8601String();
    return this;
  }

  /// Set the validity end date (ISO 8601 format)
  JournalistCredentialBuilder withValidUntil(String validUntil) {
    _validUntil = validUntil;
    return this;
  }

  /// Set the validity end date from DateTime
  JournalistCredentialBuilder withValidUntilDateTime(DateTime dateTime) {
    _validUntil = dateTime.toUtc().toIso8601String();
    return this;
  }

  /// Set the proof type
  JournalistCredentialBuilder withProofType(String proofType) {
    _proofType = proofType;
    return this;
  }

  /// Set the proof creation timestamp (ISO 8601 format)
  JournalistCredentialBuilder withProofCreated(String proofCreated) {
    _proofCreated = proofCreated;
    return this;
  }

  /// Set the proof creation timestamp from DateTime
  JournalistCredentialBuilder withProofCreatedDateTime(DateTime dateTime) {
    _proofCreated = dateTime.toUtc().toIso8601String();
    return this;
  }

  /// Set the proof purpose
  JournalistCredentialBuilder withProofPurpose(String proofPurpose) {
    _proofPurpose = proofPurpose;
    return this;
  }

  /// Set the verification method DID URL
  JournalistCredentialBuilder withVerificationMethod(
      String verificationMethod) {
    _verificationMethod = verificationMethod;
    return this;
  }

  /// Set the JWS signature
  JournalistCredentialBuilder withJws(String jws) {
    _jws = jws;
    return this;
  }

  /// Build the Verifiable Credential
  VerifiableCredential build() {
    // Validate required fields
    if (_id == null) throw ArgumentError('Credential ID is required');
    if (_issuer == null) throw ArgumentError('Issuer is required');
    if (_issuanceDate == null) {
      throw ArgumentError('Issuance date is required');
    }
    if (_subjectId == null) throw ArgumentError('Subject ID is required');
    if (_accreditationNumber == null) {
      throw ArgumentError('Accreditation number is required');
    }
    if (_fullName == null) throw ArgumentError('Full name is required');
    if (_organization == null) {
      throw ArgumentError('Organization is required');
    }
    if (_validFrom == null) throw ArgumentError('Valid from date is required');
    if (_validUntil == null) {
      throw ArgumentError('Valid until date is required');
    }
    if (_proofType == null) throw ArgumentError('Proof type is required');
    if (_proofCreated == null) {
      throw ArgumentError('Proof created timestamp is required');
    }
    if (_proofPurpose == null) {
      throw ArgumentError('Proof purpose is required');
    }
    if (_verificationMethod == null) {
      throw ArgumentError('Verification method is required');
    }
    if (_jws == null) throw ArgumentError('JWS signature is required');

    // Build credential subject
    final credentialSubject = CredentialSubject(
      id: _subjectId!,
      type: 'Journalist',
      accreditationNumber: _accreditationNumber!,
      fullName: _fullName!,
      organization: _organization!,
      validFrom: _validFrom!,
      validUntil: _validUntil!,
    );

    // Build proof
    final proof = Proof(
      type: _proofType!,
      created: _proofCreated!,
      proofPurpose: _proofPurpose!,
      verificationMethod: _verificationMethod!,
      jws: _jws!,
    );

    // Build and return credential
    return VerifiableCredential(
      context: [
        'https://www.w3.org/2018/credentials/v1',
        'https://example.org/journalist-credentials/v1',
      ],
      id: _id!,
      type: ['VerifiableCredential', 'JournalistAccreditationCredential'],
      issuer: _issuer!,
      issuanceDate: _issuanceDate!,
      expirationDate: _expirationDate,
      credentialSubject: credentialSubject,
      proof: proof,
    );
  }

  /// Create a builder with default values for testing
  static JournalistCredentialBuilder example() {
    final now = DateTime.now().toUtc();
    final oneYearLater = now.add(const Duration(days: 365));

    return JournalistCredentialBuilder()
      ..withId('https://accreditation.gov/credentials/12345')
      ..withIssuer('did:web:accreditation.gov')
      ..withIssuanceDateFromDateTime(now)
      ..withExpirationDateFromDateTime(oneYearLater)
      ..withSubjectId('did:key:z6MkjExample123456789')
      ..withAccreditationNumber('J-${now.year}-12345')
      ..withFullName('Jane Doe')
      ..withOrganization('News Media Co')
      ..withValidFromDateTime(now)
      ..withValidUntilDateTime(oneYearLater)
      ..withProofType('Ed25519Signature2020')
      ..withProofCreatedDateTime(now)
      ..withProofPurpose('assertionMethod')
      ..withVerificationMethod('did:web:accreditation.gov#key-1')
      ..withJws('eyJhbGciOiJFZERTQSJ9.example.signature');
  }
}
