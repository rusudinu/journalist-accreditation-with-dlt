package com.rusudinu.backend.hyperledger;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HyperledgerRegistryService {
    private final HyperledgerSnapshotRepositoryClient hyperledgerSnapshotRepositoryClient;

    public HyperledgerRegistry getHyperledgerRegistryByRegistryId(Long requestId) {
        return hyperledgerSnapshotRepositoryClient.getHyperledgerRegistryByRegistryId(requestId);
    }

    public void saveOrUpdateHyperledgerRegistry(HyperledgerRegistry hyperledgerRegistry) {
        hyperledgerSnapshotRepositoryClient.saveOrUpdateHyperledgerRegistry(hyperledgerRegistry);
    }
}
