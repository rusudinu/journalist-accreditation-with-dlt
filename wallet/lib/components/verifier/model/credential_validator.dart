import 'verifiable_credential.dart';

/// Validation result for a Verifiable Credential
class ValidationResult {
  final bool isValid;
  final List<String> errors;
  final List<String> warnings;

  ValidationResult({
    required this.isValid,
    required this.errors,
    this.warnings = const [],
  });

  factory ValidationResult.valid({List<String> warnings = const []}) {
    return ValidationResult(
      isValid: true,
      errors: [],
      warnings: warnings,
    );
  }

  factory ValidationResult.invalid(List<String> errors,
      {List<String> warnings = const []}) {
    return ValidationResult(
      isValid: false,
      errors: errors,
      warnings: warnings,
    );
  }

  @override
  String toString() {
    final buffer = StringBuffer();
    buffer.writeln('ValidationResult(isValid: $isValid)');

    if (errors.isNotEmpty) {
      buffer.writeln('Errors:');
      for (var error in errors) {
        buffer.writeln('  - $error');
      }
    }

    if (warnings.isNotEmpty) {
      buffer.writeln('Warnings:');
      for (var warning in warnings) {
        buffer.writeln('  - $warning');
      }
    }

    return buffer.toString();
  }
}

/// Validator for W3C Verifiable Credentials
class CredentialValidator {
  /// Validate a Verifiable Credential
  /// Performs structural and semantic validation
  static ValidationResult validate(VerifiableCredential credential) {
    final errors = <String>[];
    final warnings = <String>[];

    // Structural validation (from the model itself)
    errors.addAll(credential.validate());

    // Additional semantic validation

    // Check if credential is expired
    if (credential.isExpired()) {
      errors.add('Credential has expired');
    }

    // Check if validity period is active
    if (!credential.isValidPeriodActive()) {
      warnings.add('Credential validity period is not currently active');
    }

    // Check if issuance date is in the future
    try {
      final issuanceDate = DateTime.parse(credential.issuanceDate);
      if (issuanceDate.isAfter(DateTime.now())) {
        errors.add('Issuance date is in the future');
      }
    } catch (e) {
      // Already caught in structural validation
    }

    // Validate context ordering
    if (credential.context.isNotEmpty &&
        credential.context.first != 'https://www.w3.org/2018/credentials/v1') {
      errors.add(
        'First context must be https://www.w3.org/2018/credentials/v1',
      );
    }

    // Validate journalist credential specific requirements
    if (credential.type.contains('JournalistAccreditationCredential')) {
      if (!credential.context
          .contains('https://example.org/journalist-credentials/v1')) {
        errors.add(
          'JournalistAccreditationCredential requires journalist credentials context',
        );
      }

      if (credential.credentialSubject.type != 'Journalist') {
        errors.add(
          'Credential subject type must be "Journalist" for JournalistAccreditationCredential',
        );
      }
    }

    // Validate proof creation time is not after current time
    try {
      final proofCreated = DateTime.parse(credential.proof.created);
      if (proofCreated.isAfter(DateTime.now())) {
        errors.add('Proof creation time is in the future');
      }
    } catch (e) {
      // Already caught in structural validation
    }

    // Validate proof creation is not before issuance
    try {
      final issuanceDate = DateTime.parse(credential.issuanceDate);
      final proofCreated = DateTime.parse(credential.proof.created);
      if (proofCreated.isBefore(issuanceDate)) {
        warnings.add('Proof was created before credential issuance date');
      }
    } catch (e) {
      // Already caught in structural validation
    }

    return errors.isEmpty
        ? ValidationResult.valid(warnings: warnings)
        : ValidationResult.invalid(errors, warnings: warnings);
  }

  /// Quick validation - only checks critical fields
  static bool quickValidate(VerifiableCredential credential) {
    return credential.context
            .contains('https://www.w3.org/2018/credentials/v1') &&
        credential.type.contains('VerifiableCredential') &&
        !credential.isExpired();
  }

  /// Validate JSON structure before parsing
  static ValidationResult validateJson(Map<String, dynamic> json) {
    final errors = <String>[];

    // Check required top-level fields
    final requiredFields = [
      '@context',
      'id',
      'type',
      'issuer',
      'issuanceDate',
      'credentialSubject',
      'proof',
    ];

    for (var field in requiredFields) {
      if (!json.containsKey(field)) {
        errors.add('Missing required field: $field');
      }
    }

    // Check @context is an array
    if (json.containsKey('@context') && json['@context'] is! List) {
      errors.add('@context must be an array');
    }

    // Check type is an array
    if (json.containsKey('type') && json['type'] is! List) {
      errors.add('type must be an array');
    }

    // Check credentialSubject is an object
    if (json.containsKey('credentialSubject') &&
        json['credentialSubject'] is! Map) {
      errors.add('credentialSubject must be an object');
    }

    // Check proof is an object
    if (json.containsKey('proof') && json['proof'] is! Map) {
      errors.add('proof must be an object');
    }

    return errors.isEmpty
        ? ValidationResult.valid()
        : ValidationResult.invalid(errors);
  }

  /// Validate credential against JSON Schema
  /// Note: This is a placeholder - actual JSON Schema validation
  /// would require a JSON Schema validator library
  static ValidationResult validateAgainstSchema(
    VerifiableCredential credential,
    Map<String, dynamic> schema,
  ) {
    // TODO: Implement JSON Schema validation
    // This would require adding a dependency like json_schema
    return ValidationResult.valid(
      warnings: ['Schema validation not yet implemented'],
    );
  }
}
