package com.rusudinu.backend.distributedStorage.eth;

import com.rusudinu.backend.distributedStorage.DistributedStorageService;
import com.rusudinu.backend.distributedStorage.eth.model.DocumentRegistry;
import com.rusudinu.backend.distributedStorage.hyperledger.HyperledgerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.gas.DefaultGasProvider;


@Profile("ethereum")
@Service
@Slf4j
@RequiredArgsConstructor
public class EthService implements DistributedStorageService {
    private final Web3j web3;
    private final String CONTRACT_ADDRESS = "0x5FbDB2315678afecb367f032d93F642f64180aa3";
    private final String ACCOUNT_PRIVATE_KEY = "0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80";

    @Override
    public String getRegistrySnapshotHashByRequestId(Long requestId) {
        log.info("[ETH] Getting registry snapshot hash for request id: {}", requestId);
        DocumentRegistry document = DocumentRegistry.load(CONTRACT_ADDRESS, web3, Credentials.create(ACCOUNT_PRIVATE_KEY), new DefaultGasProvider());
        try {
            String snapshotHash = document.getDocumentsForRequest(String.valueOf(requestId)).send();
            log.info("[ETH] Fetched snapshot hash: {}", snapshotHash);
            return snapshotHash;
        } catch (Exception e) {
            log.error("[ETH] Error fetching snapshot hash: {}", e.getMessage());
            return "";
        }
    }

    @Override
    public void persistRegistrySnapshot(Long requestId, String snapshotHash) {
        log.info("[ETH] Persisting registry snapshot hash for request id: {}", requestId);
        DocumentRegistry document = DocumentRegistry.load(CONTRACT_ADDRESS, web3, Credentials.create(ACCOUNT_PRIVATE_KEY), new DefaultGasProvider());
        try {
            document.addDocument(String.valueOf(requestId), snapshotHash).send();
            log.info("[ETH] Persisted snapshot hash: {}", snapshotHash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
