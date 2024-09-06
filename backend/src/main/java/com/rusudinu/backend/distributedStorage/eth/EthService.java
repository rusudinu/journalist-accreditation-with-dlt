package com.rusudinu.backend.distributedStorage.eth;

import com.rusudinu.backend.distributedStorage.DistributedStorageService;
import com.rusudinu.backend.distributedStorage.eth.model.DocumentRegistry;
import com.rusudinu.backend.distributedStorage.hyperledger.HyperledgerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${journalist-accreditation.contract-address}")
    private String contractAddress;
    @Value("${journalist-accreditation.ministry-account-key}")
    private String ministryAccountPrivateKey;

    @Override
    public String getRegistrySnapshotHashByRequestId(Long requestId) {
        log.info("[ETH] Getting registry snapshot hash for request id: {}", requestId);
        DocumentRegistry document = DocumentRegistry.load(contractAddress, web3, Credentials.create(ministryAccountPrivateKey), new DefaultGasProvider());
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
        DocumentRegistry document = DocumentRegistry.load(contractAddress, web3, Credentials.create(ministryAccountPrivateKey), new DefaultGasProvider());
        try {
            document.addDocument(String.valueOf(requestId), snapshotHash).send();
            log.info("[ETH] Persisted snapshot hash: {}", snapshotHash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
