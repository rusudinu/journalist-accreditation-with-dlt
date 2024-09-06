package com.rusudinu.backend.eth;

import com.rusudinu.backend.eth.model.DocumentRegistry;
import com.rusudinu.backend.hash.HashService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.gas.DefaultGasProvider;


@Service
@RequiredArgsConstructor
public class EthService {
    private final Web3j web3;
    private final String CONTRACT_ADDRESS = "0x0B306BF915C4d645ff596e518fAf3F9669b97016";
    private final String ACCOUNT_PRIVATE_KEY = "0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80";

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

    public String getSnapshotHash(Long requestId) {
        DocumentRegistry document = DocumentRegistry.load(CONTRACT_ADDRESS, web3, Credentials.create(ACCOUNT_PRIVATE_KEY), new DefaultGasProvider());
        try {
            return document.getDocumentsForRequest(String.valueOf(requestId)).send();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
