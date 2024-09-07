/// Cryptographic proof for a Verifiable Credential
/// Provides integrity protection and authorship verification
class Proof {
  /// Type of cryptographic signature suite used
  /// Examples: Ed25519Signature2020, JsonWebSignature2020
  final String type;

  /// ISO 8601 date-time when the proof was created
  final String created;

  /// Purpose of the proof
  /// Common values: assertionMethod, authentication
  final String proofPurpose;

  /// DID URL referencing the verification method (public key)
  /// Example: did:web:accreditation.gov#key-1
  final String verificationMethod;

  /// JSON Web Signature (JWS) value
  /// Base64url-encoded signature
  final String jws;

  Proof({
    required this.type,
    required this.created,
    required this.proofPurpose,
    required this.verificationMethod,
    required this.jws,
  });

  /// Create from JSON
  factory Proof.fromJson(Map<String, dynamic> json) {
    return Proof(
      type: json['type'] as String,
      created: json['created'] as String,
      proofPurpose: json['proofPurpose'] as String,
      verificationMethod: json['verificationMethod'] as String,
      jws: json['jws'] as String,
    );
  }

  /// Convert to JSON
  Map<String, dynamic> toJson() {
    return {
      'type': type,
      'created': created,
      'proofPurpose': proofPurpose,
      'verificationMethod': verificationMethod,
      'jws': jws,
    };
  }

  /// Validate the proof structure
  /// Returns list of validation errors (empty if valid)
  List<String> validate() {
    final errors = <String>[];

    // Validate type
    final validTypes = [
      'Ed25519Signature2020',
      'JsonWebSignature2020',
      'EcdsaSecp256k1Signature2019',
      'RsaSignature2018',
    ];
    if (!validTypes.contains(type)) {
      errors.add(
        'Proof type must be one of: ${validTypes.join(", ")}',
      );
    }

    // Validate created date format
    try {
      DateTime.parse(created);
    } catch (e) {
      errors.add('Invalid proof created date format: $e');
    }

    // Validate proofPurpose
    final validPurposes = [
      'assertionMethod',
      'authentication',
      'keyAgreement',
      'capabilityInvocation',
      'capabilityDelegation',
    ];
    if (!validPurposes.contains(proofPurpose)) {
      errors.add(
        'Proof purpose must be one of: ${validPurposes.join(", ")}',
      );
    }

    // Validate verificationMethod format (should be a DID URL or URI)
    if (!verificationMethod.startsWith('did:') &&
        !verificationMethod.startsWith('http://') &&
        !verificationMethod.startsWith('https://')) {
      errors.add(
        'Verification method should be a DID URL or HTTP(S) URI',
      );
    }

    // Validate JWS is not empty
    if (jws.isEmpty) {
      errors.add('JWS signature cannot be empty');
    }

    // Basic JWS format validation (should be base64url encoded)
    final jwsPattern = RegExp(r'^[A-Za-z0-9_-]+$');
    if (!jwsPattern.hasMatch(jws)) {
      errors.add(
        'JWS should be base64url encoded (only A-Z, a-z, 0-9, -, _)',
      );
    }

    return errors;
  }

  /// Copy with new values
  Proof copyWith({
    String? type,
    String? created,
    String? proofPurpose,
    String? verificationMethod,
    String? jws,
  }) {
    return Proof(
      type: type ?? this.type,
      created: created ?? this.created,
      proofPurpose: proofPurpose ?? this.proofPurpose,
      verificationMethod: verificationMethod ?? this.verificationMethod,
      jws: jws ?? this.jws,
    );
  }

  @override
  String toString() {
    return 'Proof(type: $type, proofPurpose: $proofPurpose, verificationMethod: $verificationMethod)';
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;

    return other is Proof &&
        other.type == type &&
        other.created == created &&
        other.proofPurpose == proofPurpose &&
        other.verificationMethod == verificationMethod &&
        other.jws == jws;
  }

  @override
  int get hashCode {
    return type.hashCode ^
        created.hashCode ^
        proofPurpose.hashCode ^
        verificationMethod.hashCode ^
        jws.hashCode;
  }
}
