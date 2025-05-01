package com.rusudinu.backend.request.snapshot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.rusudinu.backend.distributedStorage.DistributedStorageService;
import com.rusudinu.backend.hash.HashService;
import com.rusudinu.backend.request.Request;
import com.rusudinu.backend.request.RequestStatus;
import com.rusudinu.backend.request.vc.VerifiableCredentialService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotService {
	private final DistributedStorageService distributedStorageService;
	private final HashService hashService;
	private final SnapshotRepository snapshotRepository;
	private final VerifiableCredentialService verifiableCredentialService;


	@SneakyThrows
	public void createAndPersistRequestSnapshot(RequestStatus status, Long requestId, String documentUniqueName) {
		RequestSnapshot snapshot = RequestSnapshot.builder().requestId(requestId)
				.documentHash(hashService.hashDocument(documentUniqueName)).status(status)
				.previousSnapshotHash(distributedStorageService.getRegistrySnapshotHashByRequestId(requestId)).build();

		snapshot = snapshotRepository.save(snapshot);

		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json = ow.writeValueAsString(snapshot); byte[] hash = hashService.hashString(json);

		String newSnapshotHash = Base64.getEncoder().encodeToString(hash);
		distributedStorageService.persistRegistrySnapshot(requestId, newSnapshotHash);

		log.info("Persisted snapshot hash: {}", newSnapshotHash);
		log.info("Persisted snapshot status: {}", status);
		if (status == RequestStatus.APPROVED) {
			verifiableCredentialService.createVerifiableCredentialFromDocumentHash(newSnapshotHash, requestId);
		}
	}

	@SneakyThrows
	public boolean verifyRequest(Request request) {
		log.info("Verifying request with ID: {}", request.getId());
		String snapshotHash = distributedStorageService.getRegistrySnapshotHashByRequestId(request.getId());
		RequestSnapshot snapshot = snapshotRepository.findFirstByRequestIdOrderByIdDesc(request.getId());

		if (snapshot == null || snapshotHash == null || snapshotHash.trim().isEmpty()) {
			return false;
		}

		byte[] documentHash = hashService.hashDocument(request.getDocuments().get(request.getDocuments().size() - 1)
				.getStoredDocumentName());

		// Document hash does not match the one potentially stored on the blockchain
		if (!Arrays.equals(documentHash, snapshot.getDocumentHash())) {
			return false;
		}

		// rebuild the snapshot such that we can compare with the one stored on the blockchain
		RequestSnapshot rebuiltSnapshot = RequestSnapshot.builder().id(snapshot.getId()).requestId(request.getId())
				.documentHash(documentHash).status(request.getStatus())
				.previousSnapshotHash(snapshot.getPreviousSnapshotHash()).build();

		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json = ow.writeValueAsString(rebuiltSnapshot);

		byte[] hash = hashService.hashString(json);

		// if this is false means that the hash was
		// modified in the database hence
		// the request was altered
		return Arrays.equals(hash, Base64.getDecoder().decode(snapshotHash));
	}
}
