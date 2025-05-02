package com.rusudinu.backend.hash;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusudinu.backend.document.Document;
import com.rusudinu.backend.document.DocumentRepository;
import com.rusudinu.backend.document.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DigestAlgorithmIdentifierFinder;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.security.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HashService {
    private final PublicKey ministryPublicKey;
    private final PrivateKey ministryPrivateKey;
    private final MessageDigest messageDigest;
    private final DocumentService documentService;
    private final DocumentRepository documentRepository;
    private final ObjectMapper objectMapper;

    public String shaHash(String data){
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
    public byte[] hashDocument(String documentName) {
        // Get the document content
        byte[] documentContent = documentService.getDocument(documentName);

        // Find the document entity by stored name to get comments
        Document document = documentRepository.findAll().stream()
                .filter(doc -> doc.getStoredDocumentName().equals(documentName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Document not found with name: " + documentName));

        // Create a map with document content and comments
        Map<String, Object> documentData = new HashMap<>();
        documentData.put("content", documentContent);

//        // Add comments to the map
//        List<Comment> comments = document.getComments();
//        if (comments != null && !comments.isEmpty()) {
//            documentData.put("comments", comments);
//        }

        // Convert the map to JSON and hash it
        String jsonData = objectMapper.writeValueAsString(documentData);
        return encryptWithRSA(createDigestInfo(jsonData.getBytes()));
    }

    @SneakyThrows
    public boolean verifyDocument(byte[] encryptedMessageHash, String documentName) {
        // Get the document content
        byte[] documentContent = documentService.getDocument(documentName);

        // Find the document entity by stored name to get comments
        Document document = documentRepository.findAll().stream()
                .filter(doc -> doc.getStoredDocumentName().equals(documentName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Document not found with name: " + documentName));

        // Create a map with document content and comments
        Map<String, Object> documentData = new HashMap<>();
        documentData.put("content", documentContent);

        // Add comments to the map
//        List<Comment> comments = document.getComments();
//        if (comments != null && !comments.isEmpty()) {
//            documentData.put("comments", comments);
//        }

        // Convert the map to JSON and verify the hash
        String jsonData = objectMapper.writeValueAsString(documentData);
        return Arrays.equals(decryptWithRSA(encryptedMessageHash), createDigestInfo(jsonData.getBytes()));
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
