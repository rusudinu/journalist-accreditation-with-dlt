package com.rusudinu.backend.distributedStorage;

public interface DistributedStorageService {
    String getRegistrySnapshotHashByRequestId(Long requestId);

    void persistRegistrySnapshot(Long requestId, String snapshotHash);

    String getCommentHashByRequestId(Long requestId);

    void persistCommentHash(Long requestId, String commentHash);
}
