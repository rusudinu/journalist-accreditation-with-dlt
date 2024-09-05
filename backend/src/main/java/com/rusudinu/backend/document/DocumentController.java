package com.rusudinu.backend.document;

import com.rusudinu.backend.request.RequestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService documentService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('MINISTRY', 'JOURNALIST')")
    public Document uploadDocument(@RequestParam("file") MultipartFile file, @RequestParam RequestStatus status, @RequestParam Long requestId) {
        return documentService.uploadDocument(file, status, requestId);
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

    @GetMapping("/only-ministry")
    @PreAuthorize("hasAuthority('MINISTRY')")
    public String testMinistry() {
        return "hello ministry";
    }

    @GetMapping("/only-journalist")
    @PreAuthorize("hasAuthority('JOURNALIST')")
    public String testJournalist() {
        return "hello journalist";
    }

    @GetMapping("/journalist-and-ministry")
    @PreAuthorize("hasAnyAuthority('MINISTRY', 'JOURNALIST')")
    public String testJournalistAndMinistry() {
        return "hello journalist and ministry";
    }
}
