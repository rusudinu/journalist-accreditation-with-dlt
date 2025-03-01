package com.rusudinu.backend.request.vc;

import lombok.Data;

@Data
public class VerifiableCredential {
	private String context;
	private String id;
	private String type;
	private String issuer;
	private String issuanceDate;
	private CredentialSubject credentialSubject;
	private Proof proof;

	@Data
	public static class CredentialSubject {
		private String id;
		private String fileHash;
		private String status;
	}

	@Data
	public static class Proof {
		private String type;
		private String created;
		private String proofPurpose;
		private String verificationMethod;
		private String jws;
	}
}
