package com.rusudinu.backend.distributedStorage.hyperledger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class HyperledgerRegistry {
	private String id; // the request id
	private String requestSnapshotHash;
}
