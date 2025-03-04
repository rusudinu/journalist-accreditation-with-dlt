class VerifiableCredential {
  final String context;
  final String id;
  final String type;
  final String issuer;
  final String issuanceDate;
  final CredentialSubject credentialSubject;
  final Proof proof;

  VerifiableCredential({
    required this.context,
    required this.id,
    required this.type,
    required this.issuer,
    required this.issuanceDate,
    required this.credentialSubject,
    required this.proof,
  });

  factory VerifiableCredential.fromJson(Map<String, dynamic> json) {
    return VerifiableCredential(
      context: json['context'],
      id: json['id'],
      type: json['type'],
      issuer: json['issuer'],
      issuanceDate: json['issuanceDate'],
      credentialSubject: CredentialSubject.fromJson(json['credentialSubject']),
      proof: Proof.fromJson(json['proof']),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'context': context,
      'id': id,
      'type': type,
      'issuer': issuer,
      'issuanceDate': issuanceDate,
      'credentialSubject': credentialSubject.toJson(),
      'proof': proof.toJson(),
    };
  }
}

class CredentialSubject {
  final String id;
  final String fileHash;
  final String status;

  CredentialSubject({
    required this.id,
    required this.fileHash,
    required this.status,
  });

  factory CredentialSubject.fromJson(Map<String, dynamic> json) {
    return CredentialSubject(
      id: json['id'],
      fileHash: json['fileHash'],
      status: json['status'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'fileHash': fileHash,
      'status': status,
    };
  }
}

class Proof {
  final String type;
  final String created;
  final String proofPurpose;
  final String verificationMethod;
  final String jws;

  Proof({
    required this.type,
    required this.created,
    required this.proofPurpose,
    required this.verificationMethod,
    required this.jws,
  });

  factory Proof.fromJson(Map<String, dynamic> json) {
    return Proof(
      type: json['type'],
      created: json['created'],
      proofPurpose: json['proofPurpose'],
      verificationMethod: json['verificationMethod'],
      jws: json['jws'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'type': type,
      'created': created,
      'proofPurpose': proofPurpose,
      'verificationMethod': verificationMethod,
      'jws': jws,
    };
  }
}
