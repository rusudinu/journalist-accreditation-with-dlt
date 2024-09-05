package com.rusudinu.backend.eth;

import com.rusudinu.backend.document.DocumentService;
import com.rusudinu.backend.eth.model.DocumentRegistry;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.operator.DigestAlgorithmIdentifierFinder;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.gas.DefaultGasProvider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Arrays;


@Service
@RequiredArgsConstructor
public class EthService {
    private final Web3j web3;
    private final String KEYS_FOLDER = "keys/";
    private final String CONTRACT_ADDRESS = "0x0B306BF915C4d645ff596e518fAf3F9669b97016";
    private final String ACCOUNT_PRIVATE_KEY = "0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80";
    private final DocumentService documentService;

    public void saveDocumentSignature(Long requestId, String documentSignature) {
        DocumentRegistry document = DocumentRegistry.load(CONTRACT_ADDRESS, web3, Credentials.create(ACCOUNT_PRIVATE_KEY), new DefaultGasProvider());
        try {
            document.addDocument(String.valueOf(requestId), documentSignature).send();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void getDocumentsForRequest(Long requestId) {
        DocumentRegistry document = DocumentRegistry.load(CONTRACT_ADDRESS, web3, Credentials.create(ACCOUNT_PRIVATE_KEY), new DefaultGasProvider());
        try {
            String result = document.getDocumentsForRequest(String.valueOf(requestId)).send();

            // Output or use the list of document hashes
            System.out.println("Document hashes: " + result);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void testKey() throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
        Path documentPath = Paths.get(KEYS_FOLDER, "ministry_key.jks");
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance("JKS");
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }
        try {
            keyStore.load(new FileInputStream(documentPath.toFile()), "ministry".toCharArray());
        } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new RuntimeException(e);
        }
        Certificate certificate = null;
        try {
            certificate = keyStore.getCertificate("ministry_key");
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }
        PublicKey publicKey = certificate.getPublicKey();
        PrivateKey privateKey = (PrivateKey) keyStore.getKey("ministry_key", "ministry".toCharArray());

        String pdfId = "test.pdf";
        byte[] documentContent = documentService.getDocument(pdfId);
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] messageHash = md.digest(documentContent);
        System.out.println("Message hash: " + messageHash);
        DigestAlgorithmIdentifierFinder hashAlgorithmFinder = new DefaultDigestAlgorithmIdentifierFinder();
        AlgorithmIdentifier hashingAlgorithmIdentifier = hashAlgorithmFinder.find("SHA-256");
        DigestInfo digestInfo = new DigestInfo(hashingAlgorithmIdentifier, messageHash);
        byte[] hashToEncrypt = null;
        try {
            hashToEncrypt = digestInfo.getEncoded();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
        try {
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }

        byte[] encryptedMessageHash = null;
        try {
            encryptedMessageHash = cipher.doFinal(hashToEncrypt);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }


        // verify

        cipher = null;
        try {
            cipher = Cipher.getInstance("RSA");
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
        try {
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        byte[] decryptedMessageHash = null;
        try {
            decryptedMessageHash = cipher.doFinal(encryptedMessageHash);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }

        byte[] documentToTestContent = documentService.getDocument(pdfId);

        md = MessageDigest.getInstance("SHA-256");
        byte[] newMessageHash = md.digest(documentToTestContent);

        hashAlgorithmFinder = new DefaultDigestAlgorithmIdentifierFinder();
        hashingAlgorithmIdentifier = hashAlgorithmFinder.find("SHA-256");
        digestInfo = new DigestInfo(hashingAlgorithmIdentifier, newMessageHash);
        try {
            hashToEncrypt = digestInfo.getEncoded();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        boolean isCorrect = Arrays.equals(decryptedMessageHash, hashToEncrypt);
        System.out.println("Is correct: " + isCorrect);
    }
}
