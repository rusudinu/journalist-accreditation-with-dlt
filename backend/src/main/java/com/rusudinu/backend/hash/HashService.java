package com.rusudinu.backend.hash;

import com.rusudinu.backend.document.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DigestAlgorithmIdentifierFinder;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.security.*;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class HashService {
    private final DocumentService documentService;
    private final PublicKey ministryPublicKey;
    private final PrivateKey ministryPrivateKey;
    private final MessageDigest messageDigest;

    @SneakyThrows
    public byte[] hashString(String data) {
        return encryptWithRSA(createDigestInfo(data.getBytes()));
    }

    @SneakyThrows
    public byte[] hashDocument(String documentName) {
        return encryptWithRSA(createDigestInfo(documentService.getDocument(documentName)));
    }

    @SneakyThrows
    public boolean verifyDocument(byte[] encryptedMessageHash, String documentName) {
        return Arrays.equals(decryptWithRSA(encryptedMessageHash), createDigestInfo(documentService.getDocument(documentName)));
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
