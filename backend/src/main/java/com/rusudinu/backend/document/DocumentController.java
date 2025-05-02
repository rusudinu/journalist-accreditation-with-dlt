package com.rusudinu.backend.document;

import com.rusudinu.backend.comment.CommentDTO;
import com.rusudinu.backend.request.RequestStatus;
import com.rusudinu.backend.request.snapshot.SnapshotService;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService documentService;
    private final SnapshotService snapshotService;

    @PostMapping
//    @PreAuthorize("hasAnyAuthority('MINISTRY', 'JOURNALIST')")
    public Document uploadDocument(@RequestParam("file") MultipartFile file, @RequestParam RequestStatus status, @RequestParam Long requestId) {
        Document document = documentService.uploadDocument(file, status, requestId);
        snapshotService.createAndPersistRequestSnapshot(status, requestId, document.getStoredDocumentName());
        return document;
    }

    @GetMapping("/need-review")
    public List<DocumentDTO> getNeedReviewDocuments() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(authentication.getName());
        return new ArrayList<>();
    }

    @GetMapping("{storedDocumentName}")
    public ResponseEntity<byte[]> getDocument(@PathVariable String storedDocumentName) {
        byte[] documentContent = documentService.getDocument(storedDocumentName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline().filename(storedDocumentName).build());
        headers.set("X-Frame-Options", "SAMEORIGIN");

        return new ResponseEntity<>(documentContent, headers, HttpStatus.OK);
    }

    @GetMapping("/{documentId}/with-comments")
//    @PreAuthorize("hasAnyAuthority('MINISTRY', 'JOURNALIST', 'DEPUTY')")
    public ResponseEntity<DocumentDTO> getDocumentWithComments(@PathVariable Long documentId) {
        return ResponseEntity.ok(documentService.getDocumentWithComments(documentId));
    }

    @PostMapping("/{documentId}/comments")
//    @PreAuthorize("hasAnyAuthority('MINISTRY', 'JOURNALIST', 'DEPUTY')")
    public ResponseEntity<CommentDTO> addCommentToDocument(
            @PathVariable Long documentId,
            @RequestBody CommentDTO commentDTO) {
        CommentDTO savedComment = documentService.addCommentToDocument(documentId, commentDTO);

        // After adding a comment, create a new snapshot to update the hash in the blockchain
        Document document = documentService.getDocumentById(documentId);
        snapshotService.createAndPersistRequestSnapshot(
                document.getRequest().getStatus(),
                document.getRequest().getId(),
                document.getStoredDocumentName());

        return ResponseEntity.ok(savedComment);
    }

    @GetMapping("/{documentId}/comments")
//    @PreAuthorize("hasAnyAuthority('MINISTRY', 'JOURNALIST', 'DEPUTY')")
    public ResponseEntity<List<CommentDTO>> getCommentsForDocument(@PathVariable Long documentId) {
        return ResponseEntity.ok(documentService.getCommentsForDocument(documentId));
    }
}
