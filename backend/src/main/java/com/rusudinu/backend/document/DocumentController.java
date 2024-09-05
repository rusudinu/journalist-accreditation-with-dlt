package com.rusudinu.backend.document;

import com.rusudinu.backend.user.User;
import com.rusudinu.backend.user.UserService;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService documentService;
    private final UserService userService;


    @PostMapping
    @PreAuthorize("hasAnyAuthority('MINISTRY', 'JOURNALIST')")
    public Document uploadDocument(@RequestParam("file") MultipartFile file, @RequestParam(defaultValue = "request") String status, @RequestParam Optional<Long> uploadedForUserWithId) {
        Authentication authToken = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> attributes = ((JwtAuthenticationToken) authToken).getTokenAttributes();
        String userId = (String) attributes.get("sub");
        User user = userService.findOrCreateByKeycloakId(userId);
        return documentService.uploadDocument(file, user, status, uploadedForUserWithId);
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
