package com.rusudinu.backend.approval;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/approval-processes")
public class ApprovalProcessController {
    private final ApprovalProcessService approvalProcessService;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<ApprovalProcess>> getAllApprovalProcesses() {
        return ResponseEntity.ok(approvalProcessService.getAllApprovalProcesses());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApprovalProcess> getApprovalProcessById(@PathVariable Long id) {
        return ResponseEntity.ok(approvalProcessService.getApprovalProcessById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApprovalProcess> createApprovalProcess(@RequestBody ApprovalProcess approvalProcess) {
        return ResponseEntity.ok(approvalProcessService.createApprovalProcess(approvalProcess));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApprovalProcess> updateApprovalProcess(
            @PathVariable Long id,
            @RequestBody ApprovalProcess approvalProcess) {
        return ResponseEntity.ok(approvalProcessService.updateApprovalProcess(id, approvalProcess));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteApprovalProcess(@PathVariable Long id) {
        approvalProcessService.deleteApprovalProcess(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{approvalProcessId}/assign-to-request/{requestId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> assignApprovalProcessToRequest(
            @PathVariable Long approvalProcessId,
            @PathVariable Long requestId) {
        approvalProcessService.assignApprovalProcessToRequest(approvalProcessId, requestId);
        return ResponseEntity.ok().build();
    }
}
