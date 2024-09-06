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
        byte[] dataBytes = data.getBytes();
        byte[] messageHash = messageDigest.digest(dataBytes);

        DigestAlgorithmIdentifierFinder hashAlgorithmFinder = new DefaultDigestAlgorithmIdentifierFinder();
        AlgorithmIdentifier hashingAlgorithmIdentifier = hashAlgorithmFinder.find("SHA-256");
        DigestInfo digestInfo = new DigestInfo(hashingAlgorithmIdentifier, messageHash);

        byte[] hashToEncrypt = digestInfo.getEncoded();
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, ministryPrivateKey);
        return cipher.doFinal(hashToEncrypt);
    }

    @SneakyThrows
    public byte[] hashDocument(String documentName) {
        byte[] documentContent = documentService.getDocument(documentName);
        byte[] messageHash = messageDigest.digest(documentContent);

        DigestAlgorithmIdentifierFinder hashAlgorithmFinder = new DefaultDigestAlgorithmIdentifierFinder();
        AlgorithmIdentifier hashingAlgorithmIdentifier = hashAlgorithmFinder.find("SHA-256");
        DigestInfo digestInfo = new DigestInfo(hashingAlgorithmIdentifier, messageHash);

        byte[] hashToEncrypt = digestInfo.getEncoded();
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, ministryPrivateKey);
        return cipher.doFinal(hashToEncrypt);
    }

    @SneakyThrows
    public boolean verifyDocument(byte[] encryptedMessageHash, String documentName) {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, ministryPublicKey);
        byte[] decryptedMessageHash = cipher.doFinal(encryptedMessageHash);

        byte[] documentToTestContent = documentService.getDocument(documentName);
        byte[] newMessageHash = messageDigest.digest(documentToTestContent);

        DigestAlgorithmIdentifierFinder hashAlgorithmFinder = new DefaultDigestAlgorithmIdentifierFinder();
        AlgorithmIdentifier hashingAlgorithmIdentifier = hashAlgorithmFinder.find("SHA-256");
        DigestInfo digestInfo = new DigestInfo(hashingAlgorithmIdentifier, newMessageHash);
        byte[] hashToEncrypt = digestInfo.getEncoded();

        return Arrays.equals(decryptedMessageHash, hashToEncrypt);
    }
}
