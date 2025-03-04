/*
{
    "context": "http://beard.biz/",
    "id": "6d4ae207-345d-4d8d-b369-4acbffe9da63",
    "type": "Credential",
    "issuer": "Obrien, Haney and Jimenez",
    "issuanceDate": "1998-03-23T20:30:06",
    "credentialSubject": {
        "id": "b1f42322-9989-4bdb-8618-0a1eafe291a7",
        "fileHash": "322ba6bd6bc4a8fd92cafcf78a3c4a8e291d36d1882a3eb15d8c78466ebed32f",
        "status": "active"
    },
    "proof": {
        "type": "Ed25519Signature2018",
        "created": "1971-02-23T12:54:51",
        "proofPurpose": "assertionMethod",
        "verificationMethod": "http://jones.com/",
        "jws": "4e2cbb71c0a0835160169add8dafa0d8f02b1c55e7226a7e336dc731bac93647"
    }
}

 */

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
