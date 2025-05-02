package com.rusudinu.backend.document;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
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

    @PostMapping
//    @PreAuthorize("hasAnyAuthority('MINISTRY', 'JOURNALIST')")
    public Document uploadDocument(@RequestParam("file") MultipartFile file, @RequestParam Long documentId) {
		return documentService.uploadDocument(file, documentId);
    }

    @PostMapping("/create-document")
//    @PreAuthorize("hasAnyAuthority('MINISTRY', 'JOURNALIST')")
    public Document createDocument() {
        return documentService.createDocument();
    }

    @GetMapping("/need-review")
    public List<Document> getNeedReviewDocuments() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return documentService.getNeedReviewDocuments(authentication.getName());
    }

    @GetMapping("{documentId}")
    public Document getDocumentById(@PathVariable Long documentId) {
       return documentService.getDocumentById(documentId);
    }

    @GetMapping("/download/{storedDocumentName}")
    public ResponseEntity<byte[]> getDocument(@PathVariable String storedDocumentName) {
        byte[] documentContent = documentService.getDocument(storedDocumentName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline().filename(storedDocumentName).build());
        headers.set("X-Frame-Options", "SAMEORIGIN");

        return new ResponseEntity<>(documentContent, headers, HttpStatus.OK);
    }
}
