package com.rusudinu.backend.distributedStorage.hyperledger;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "hyperledger-snapshot-service", url = "http://localhost:3000")
public interface HyperledgerSnapshotRepositoryClient {
	@GetMapping("/registry/{requestId}")
	HyperledgerRegistry getHyperledgerRegistryByRegistryId(@PathVariable("requestId") Long requestId);

	@PostMapping("/registry")
	void saveOrUpdateHyperledgerRegistry(HyperledgerRegistry hyperledgerRegistry);
}
