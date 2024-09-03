package com.rusudinu.backend.document;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService documentService;


    @PostMapping
    @PreAuthorize("hasAnyAuthority('MINISTRY', 'JOURNALIST')")
    public String uploadDocument(@RequestParam("file") MultipartFile file) {
        Authentication authToken = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> attributes = ((JwtAuthenticationToken) authToken).getTokenAttributes();
        System.out.println(attributes);
        System.out.println((String) attributes.get("preferred_username"));
        return documentService.uploadDocument(file);
    }

    @GetMapping("/only-ministry")
    @PreAuthorize("hasAuthority('MINISTRY')")
    public String testMinistry() {
        Authentication authToken = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> attributes = ((JwtAuthenticationToken) authToken).getTokenAttributes();
        System.out.println(attributes);
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
