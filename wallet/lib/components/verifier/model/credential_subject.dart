/// Credential Subject for Journalist Accreditation
/// Contains claims about the journalist being accredited
class CredentialSubject {
  /// DID or URI identifying the credential subject (the journalist)
  final String id;

  /// Type of the credential subject
  final String type;

  /// Unique accreditation number (format: J-YYYY-NNNNN)
  final String accreditationNumber;

  /// Full legal name of the journalist
  final String fullName;

  /// Name of the news organization
  final String organization;

  /// ISO 8601 date-time when accreditation becomes valid
  final String validFrom;

  /// ISO 8601 date-time when accreditation expires
  final String validUntil;

  CredentialSubject({
    required this.id,
    required this.type,
    required this.accreditationNumber,
    required this.fullName,
    required this.organization,
    required this.validFrom,
    required this.validUntil,
  });

  /// Create from JSON
  factory CredentialSubject.fromJson(Map<String, dynamic> json) {
    return CredentialSubject(
      id: json['id'] as String,
      type: json['type'] as String,
      accreditationNumber: json['accreditationNumber'] as String,
      fullName: json['fullName'] as String,
      organization: json['organization'] as String,
      validFrom: json['validFrom'] as String,
      validUntil: json['validUntil'] as String,
    );
  }

  /// Convert to JSON
  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'type': type,
      'accreditationNumber': accreditationNumber,
      'fullName': fullName,
      'organization': organization,
      'validFrom': validFrom,
      'validUntil': validUntil,
    };
  }

  /// Check if the current date/time is within the valid period
  bool isValidPeriodActive() {
    final now = DateTime.now();
    final from = DateTime.parse(validFrom);
    final until = DateTime.parse(validUntil);

    return now.isAfter(from) && now.isBefore(until);
  }

  /// Validate the credential subject
  /// Returns list of validation errors (empty if valid)
  List<String> validate() {
    final errors = <String>[];

    // Validate ID (should be a DID or URI)
    if (!id.startsWith('did:') &&
        !id.startsWith('http://') &&
        !id.startsWith('https://') &&
        !id.startsWith('urn:')) {
      errors.add('Subject ID should be a DID or URI');
    }

    // Validate type
    if (type.isEmpty) {
      errors.add('Subject type cannot be empty');
    }

    // Validate accreditation number format (J-YYYY-NNNNN)
    final accreditationPattern = RegExp(r'^J-\d{4}-\d{5}$');
    if (!accreditationPattern.hasMatch(accreditationNumber)) {
      errors.add(
        'Invalid accreditation number format. Expected: J-YYYY-NNNNN',
      );
    }

    // Validate fullName
    if (fullName.isEmpty || fullName.length > 255) {
      errors.add('Full name must be between 1 and 255 characters');
    }

    // Validate organization
    if (organization.isEmpty || organization.length > 255) {
      errors.add('Organization must be between 1 and 255 characters');
    }

    // Validate date formats
    try {
      DateTime.parse(validFrom);
    } catch (e) {
      errors.add('Invalid validFrom date format: $e');
    }

    try {
      DateTime.parse(validUntil);
    } catch (e) {
      errors.add('Invalid validUntil date format: $e');
    }

    // Validate that validUntil is after validFrom
    try {
      final from = DateTime.parse(validFrom);
      final until = DateTime.parse(validUntil);
      if (until.isBefore(from) || until.isAtSameMomentAs(from)) {
        errors.add('validUntil must be after validFrom');
      }
    } catch (e) {
      // Date parsing errors already caught above
    }

    return errors;
  }

  /// Copy with new values
  CredentialSubject copyWith({
    String? id,
    String? type,
    String? accreditationNumber,
    String? fullName,
    String? organization,
    String? validFrom,
    String? validUntil,
  }) {
    return CredentialSubject(
      id: id ?? this.id,
      type: type ?? this.type,
      accreditationNumber: accreditationNumber ?? this.accreditationNumber,
      fullName: fullName ?? this.fullName,
      organization: organization ?? this.organization,
      validFrom: validFrom ?? this.validFrom,
      validUntil: validUntil ?? this.validUntil,
    );
  }

  @override
  String toString() {
    return 'CredentialSubject(id: $id, accreditationNumber: $accreditationNumber, fullName: $fullName)';
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;

    return other is CredentialSubject &&
        other.id == id &&
        other.accreditationNumber == accreditationNumber;
  }

  @override
  int get hashCode => id.hashCode ^ accreditationNumber.hashCode;
}
