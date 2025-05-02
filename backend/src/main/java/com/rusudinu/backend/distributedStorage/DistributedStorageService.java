package com.rusudinu.backend.distributedStorage;

public interface DistributedStorageService {
	String getRegistrySnapshotHashByRequestId(Long requestId);

	void persistRegistrySnapshot(Long requestId, String snapshotHash);

	String getCommentHashByCommentKey(String key);

	void persistCommentHash(String key, String commentHash);
}
