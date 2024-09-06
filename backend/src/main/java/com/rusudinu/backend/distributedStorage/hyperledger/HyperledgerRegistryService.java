package com.rusudinu.backend.distributedStorage.hyperledger;

import com.rusudinu.backend.distributedStorage.DistributedStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("hyperledger")
@Service
@Slf4j
@RequiredArgsConstructor
public class HyperledgerRegistryService implements DistributedStorageService {
    private final HyperledgerSnapshotRepositoryClient hyperledgerSnapshotRepositoryClient;

    @Override
    public String getRegistrySnapshotHashByRequestId(Long requestId) {
        log.info("[HYP] Getting registry snapshot hash for request id: {}", requestId);
        HyperledgerRegistry hyperledgerRegistry;
        try {
            hyperledgerRegistry = hyperledgerSnapshotRepositoryClient.getHyperledgerRegistryByRegistryId(requestId);
        } catch (Exception e) {
            log.error("[HYP] Error fetching snapshot hash: {}", e.getMessage());
            return "";
        }
        if (hyperledgerRegistry == null) {
            log.info("[HYP] No snapshot hash found for request id: {}", requestId);
            return "";
        }
        log.info("[HYP] Fetched snapshot hash: {}", hyperledgerRegistry.getRequestSnapshotHash());
        return hyperledgerRegistry.getRequestSnapshotHash();
    }

    @Override
    public void persistRegistrySnapshot(Long requestId, String snapshotHash) {
        log.info("[HYP] Persisting registry snapshot hash for request id: {}", requestId);
        HyperledgerRegistry hyperledgerRegistry = HyperledgerRegistry.builder()
                .id(String.valueOf(requestId))
                .requestSnapshotHash(snapshotHash)
                .build();
        log.info("[HYP] Persisting snapshot registry: {}", hyperledgerRegistry);
        hyperledgerSnapshotRepositoryClient.saveOrUpdateHyperledgerRegistry(hyperledgerRegistry);
        log.info("[HYP] Persisted snapshot hash: {}", snapshotHash);
    }
}
