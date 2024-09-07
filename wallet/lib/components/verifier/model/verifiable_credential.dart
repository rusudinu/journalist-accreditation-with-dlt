import 'dart:convert';
import 'proof.dart';
import 'credential_subject.dart';

/// W3C Verifiable Credential Data Model implementation
/// Spec: https://www.w3.org/TR/vc-data-model/
class VerifiableCredential {
  /// JSON-LD context defining the semantic meaning of fields
  /// MUST include https://www.w3.org/2018/credentials/v1 as first element
  final List<String> context;

  /// Unique identifier for this credential (URI)
  final String id;

  /// Types of this credential
  /// MUST include "VerifiableCredential"
  final List<String> type;

  /// DID or URI of the credential issuer
  /// Can be a string (DID/URI) or an object with id and optional name
  final dynamic issuer;

  /// ISO 8601 date-time when the credential was issued
  final String issuanceDate;

  /// Optional ISO 8601 date-time when the credential expires
  final String? expirationDate;

  /// Claims about the credential subject (the journalist)
  final CredentialSubject credentialSubject;

  /// Cryptographic proof securing the credential
  final Proof proof;

  VerifiableCredential({
    required this.context,
    required this.id,
    required this.type,
    required this.issuer,
    required this.issuanceDate,
    this.expirationDate,
    required this.credentialSubject,
    required this.proof,
  }) {
    // Validate required context
    if (!context.contains('https://www.w3.org/2018/credentials/v1')) {
      throw ArgumentError(
        'Context must include https://www.w3.org/2018/credentials/v1',
      );
    }

    // Validate required type
    if (!type.contains('VerifiableCredential')) {
      throw ArgumentError('Type must include VerifiableCredential');
    }
  }

  /// Create a VerifiableCredential from JSON
  factory VerifiableCredential.fromJson(Map<String, dynamic> json) {
    return VerifiableCredential(
      context: List<String>.from(json['@context'] ?? []),
      id: json['id'] as String,
      type: List<String>.from(json['type'] ?? []),
      issuer: json['issuer'],
      issuanceDate: json['issuanceDate'] as String,
      expirationDate: json['expirationDate'] as String?,
      credentialSubject: CredentialSubject.fromJson(
        json['credentialSubject'] as Map<String, dynamic>,
      ),
      proof: Proof.fromJson(json['proof'] as Map<String, dynamic>),
    );
  }

  /// Convert to JSON
  Map<String, dynamic> toJson() {
    final Map<String, dynamic> json = {
      '@context': context,
      'id': id,
      'type': type,
      'issuer': issuer,
      'issuanceDate': issuanceDate,
      'credentialSubject': credentialSubject.toJson(),
      'proof': proof.toJson(),
    };

    if (expirationDate != null) {
      json['expirationDate'] = expirationDate;
    }

    return json;
  }

  /// Convert to JSON string
  String toJsonString({bool pretty = false}) {
    if (pretty) {
      const encoder = JsonEncoder.withIndent('  ');
      return encoder.convert(toJson());
    }
    return jsonEncode(toJson());
  }

  /// Create from JSON string
  factory VerifiableCredential.fromJsonString(String jsonString) {
    final json = jsonDecode(jsonString) as Map<String, dynamic>;
    return VerifiableCredential.fromJson(json);
  }

  /// Check if the credential is expired
  bool isExpired() {
    if (expirationDate == null) return false;
    final expiration = DateTime.parse(expirationDate!);
    return DateTime.now().isAfter(expiration);
  }

  /// Check if the credential subject's validity period is active
  bool isValidPeriodActive() {
    return credentialSubject.isValidPeriodActive();
  }

  /// Validate the credential structure
  /// Returns list of validation errors (empty if valid)
  List<String> validate() {
    final errors = <String>[];

    // Validate context
    if (context.isEmpty) {
      errors.add('Context cannot be empty');
    }
    if (!context.contains('https://www.w3.org/2018/credentials/v1')) {
      errors.add('Context must include https://www.w3.org/2018/credentials/v1');
    }

    // Validate type
    if (type.isEmpty) {
      errors.add('Type cannot be empty');
    }
    if (!type.contains('VerifiableCredential')) {
      errors.add('Type must include VerifiableCredential');
    }

    // Validate ID format (should be a URI)
    if (!id.startsWith('http://') &&
        !id.startsWith('https://') &&
        !id.startsWith('did:') &&
        !id.startsWith('urn:')) {
      errors.add('ID should be a valid URI');
    }

    // Validate issuer
    if (issuer is String) {
      final issuerStr = issuer as String;
      if (!issuerStr.startsWith('did:') &&
          !issuerStr.startsWith('http://') &&
          !issuerStr.startsWith('https://')) {
        errors.add('Issuer should be a DID or HTTP(S) URI');
      }
    } else if (issuer is Map) {
      final issuerMap = issuer as Map<String, dynamic>;
      if (!issuerMap.containsKey('id')) {
        errors.add('Issuer object must have an id field');
      }
    } else {
      errors.add('Issuer must be a string or object');
    }

    // Validate issuanceDate format
    try {
      DateTime.parse(issuanceDate);
    } catch (e) {
      errors.add('Invalid issuanceDate format: $e');
    }

    // Validate expirationDate format if present
    if (expirationDate != null) {
      try {
        DateTime.parse(expirationDate!);
      } catch (e) {
        errors.add('Invalid expirationDate format: $e');
      }
    }

    // Validate credential subject
    errors.addAll(credentialSubject.validate());

    // Validate proof
    errors.addAll(proof.validate());

    return errors;
  }

  /// Copy with new values
  VerifiableCredential copyWith({
    List<String>? context,
    String? id,
    List<String>? type,
    dynamic issuer,
    String? issuanceDate,
    String? expirationDate,
    CredentialSubject? credentialSubject,
    Proof? proof,
  }) {
    return VerifiableCredential(
      context: context ?? this.context,
      id: id ?? this.id,
      type: type ?? this.type,
      issuer: issuer ?? this.issuer,
      issuanceDate: issuanceDate ?? this.issuanceDate,
      expirationDate: expirationDate ?? this.expirationDate,
      credentialSubject: credentialSubject ?? this.credentialSubject,
      proof: proof ?? this.proof,
    );
  }

  @override
  String toString() {
    return 'VerifiableCredential(id: $id, type: $type, issuer: $issuer)';
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;

    return other is VerifiableCredential && other.id == id;
  }

  @override
  int get hashCode => id.hashCode;
}
