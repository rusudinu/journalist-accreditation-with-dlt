package com.rusudinu.backend.hash;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DigestAlgorithmIdentifierFinder;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;

import java.security.*;
import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class HashService {
	private final PublicKey ministryPublicKey;
	private final PrivateKey ministryPrivateKey;
	private final MessageDigest messageDigest;

	public String shaHash(String data) {
		if (data == null || data.isEmpty()) {
			log.warn("Data is null or empty, returning empty hash");
			throw new IllegalArgumentException("Data cannot be null or empty");
		}
		return DigestUtils.sha256Hex(data);
	}

	@SneakyThrows
	public byte[] hashString(String data) {
		return encryptWithRSA(createDigestInfo(data.getBytes()));
	}

	@SneakyThrows
	public boolean verifyString(byte[] encryptedMessageHash, String data) {
		return Arrays.equals(decryptWithRSA(encryptedMessageHash), createDigestInfo(data.getBytes()));
	}

	@SneakyThrows
	private byte[] createDigestInfo(byte[] data) {
		byte[] messageHash = messageDigest.digest(data);
		DigestAlgorithmIdentifierFinder hashAlgorithmFinder = new DefaultDigestAlgorithmIdentifierFinder();
		AlgorithmIdentifier hashingAlgorithmIdentifier = hashAlgorithmFinder.find("SHA-256");
		DigestInfo digestInfo = new DigestInfo(hashingAlgorithmIdentifier, messageHash);
		return digestInfo.getEncoded();
	}

	@SneakyThrows
	private byte[] encryptWithRSA(byte[] dataToEncrypt) {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, ministryPrivateKey);
		return cipher.doFinal(dataToEncrypt);
	}

	@SneakyThrows
	private byte[] decryptWithRSA(byte[] dataToDecrypt) {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, ministryPublicKey);
		return cipher.doFinal(dataToDecrypt);
	}
}
