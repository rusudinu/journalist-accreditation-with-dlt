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
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class HashService {
    private final String KEYS_FOLDER = "keys/";
    private final DocumentService documentService;

    @SneakyThrows
    public byte[] hashDocument(String documentName) {
        Path documentPath = Paths.get(KEYS_FOLDER, "ministry_key.jks");
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream(documentPath.toFile()), "ministry".toCharArray());

        PrivateKey privateKey = (PrivateKey) keyStore.getKey("ministry_key", "ministry".toCharArray());

        byte[] documentContent = documentService.getDocument(documentName);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] messageHash = md.digest(documentContent);

        DigestAlgorithmIdentifierFinder hashAlgorithmFinder = new DefaultDigestAlgorithmIdentifierFinder();
        AlgorithmIdentifier hashingAlgorithmIdentifier = hashAlgorithmFinder.find("SHA-256");
        DigestInfo digestInfo = new DigestInfo(hashingAlgorithmIdentifier, messageHash);

        byte[] hashToEncrypt = digestInfo.getEncoded();
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        return cipher.doFinal(hashToEncrypt);
    }

    @SneakyThrows
    public boolean verifyDocument(byte[] encryptedMessageHash, String documentName) {
        Path documentPath = Paths.get(KEYS_FOLDER, "ministry_key.jks");
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream(documentPath.toFile()), "ministry".toCharArray());
        Cipher cipher = Cipher.getInstance("RSA");
        Certificate certificate = keyStore.getCertificate("ministry_key");
        PublicKey publicKey = certificate.getPublicKey();
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] decryptedMessageHash = cipher.doFinal(encryptedMessageHash);

        byte[] documentToTestContent = documentService.getDocument(documentName);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] newMessageHash = md.digest(documentToTestContent);

        DigestAlgorithmIdentifierFinder hashAlgorithmFinder = new DefaultDigestAlgorithmIdentifierFinder();
        AlgorithmIdentifier hashingAlgorithmIdentifier = hashAlgorithmFinder.find("SHA-256");
        DigestInfo digestInfo = new DigestInfo(hashingAlgorithmIdentifier, newMessageHash);
        byte[] hashToEncrypt = digestInfo.getEncoded();

        return Arrays.equals(decryptedMessageHash, hashToEncrypt);
    }
}
