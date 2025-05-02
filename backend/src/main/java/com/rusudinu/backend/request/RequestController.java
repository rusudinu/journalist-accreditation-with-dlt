package com.rusudinu.backend.request;

import com.nimbusds.jose.shaded.gson.internal.LinkedTreeMap;
import com.rusudinu.backend.request.dto.RequestWithApprovalStatusDTO;
import com.rusudinu.backend.request.snapshot.RequestSnapshot;
import com.rusudinu.backend.request.snapshot.SnapshotService;
import com.rusudinu.backend.user.User;
import com.rusudinu.backend.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/requests")
@RequiredArgsConstructor
@Slf4j
public class RequestController {
    private final RequestService requestService;
    private final UserService userService;
    private final SnapshotService snapshotService;

    @PostMapping
    public Request createRequest() {
        Authentication authToken = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> attributes = ((JwtAuthenticationToken) authToken).getTokenAttributes();
        String userId = (String) attributes.get("sub");
        User user = userService.findOrCreateByKeycloakId(userId);
        return requestService.createRequest(user);
    }

    @GetMapping("{id}")
    public Request getRequestById(@PathVariable Long id) {
        return requestService.getRequestById(id);
    }

    @GetMapping("verify/{id}")
    public boolean verifyRequestById(@PathVariable Long id) {
        Request request = requestService.getRequestById(id);
        boolean response = snapshotService.verifyRequest(request);
        log.info("Request verification result: {}", response);
        return response;
    }

    @PostMapping("/{requestId}/assign-approval-process/{approvalProcessId}")
//    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Request> assignApprovalProcess(
            @PathVariable Long requestId,
            @PathVariable Long approvalProcessId) {
        return ResponseEntity.ok(requestService.assignApprovalProcess(requestId, approvalProcessId));
    }

}
