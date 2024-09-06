package com.rusudinu.backend.request.snapshot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.rusudinu.backend.document.DocumentService;
import com.rusudinu.backend.eth.EthService;
import com.rusudinu.backend.hash.HashService;
import com.rusudinu.backend.request.Request;
import com.rusudinu.backend.request.RequestStatus;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class SnapshotService {
    private final EthService ethService;
    private final HashService hashService;
    private final SnapshotRepository snapshotRepository;
    private final DocumentService documentService;


    @SneakyThrows
    public void createAndPersistRequestSnapshot(RequestStatus status, Long requestId, String documentUniqueName) {
        RequestSnapshot snapshot = RequestSnapshot.builder().requestId(requestId).documentHash(hashService.hashDocument(documentUniqueName)).status(status).previousSnapshotHash(ethService.getSnapshotHash(requestId)).build();

        snapshot = snapshotRepository.save(snapshot);

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(snapshot);
        System.out.println(json);

        byte[] hash = hashService.hashString(json);

        String newSnapshotHash = Base64.getEncoder().encodeToString(hash);
        System.out.println(newSnapshotHash);

        ethService.saveDocumentSignature(requestId, newSnapshotHash);
    }

    @SneakyThrows
    public boolean verifyRequest(Request request) {
        String snapshotHash = ethService.getSnapshotHash(request.getId());
        RequestSnapshot snapshot = snapshotRepository.findFirstByRequestIdOrderByIdDesc(request.getId());

        if (snapshot == null || snapshotHash == null || snapshotHash.trim().isEmpty()) {
            return false;
        }

        byte[] documentHash = hashService.hashDocument(request.getDocuments().get(request.getDocuments().size() - 1).getStoredDocumentName());

        // Document hash does not match the one potentially stored on the blockchain
        if (!Arrays.equals(documentHash, snapshot.getDocumentHash())) {
            return false;
        }

        // rebuild the snapshot such that we can compare with the one stored on the blockchain
        RequestSnapshot rebuiltSnapshot = RequestSnapshot.builder().id(snapshot.getId()).requestId(request.getId()).documentHash(documentHash).status(request.getStatus()).previousSnapshotHash(snapshot.getPreviousSnapshotHash()).build();

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(rebuiltSnapshot);

        byte[] hash = hashService.hashString(json);

        // if this is false means that the hash was
        // modified in the database hence
        // the request was altered
        return Arrays.equals(hash, Base64.getDecoder().decode(snapshotHash));
    }
}
