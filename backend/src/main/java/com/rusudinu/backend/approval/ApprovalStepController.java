package com.rusudinu.backend.approval;

import com.rusudinu.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/approval-steps")
public class ApprovalStepController {
    private final ApprovalStepService approvalStepService;

    @GetMapping("/process/{approvalProcessId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<ApprovalStep>> getStepsByApprovalProcessId(@PathVariable Long approvalProcessId) {
        return ResponseEntity.ok(approvalStepService.getStepsByApprovalProcessId(approvalProcessId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApprovalStep> getStepById(@PathVariable Long id) {
        return ResponseEntity.ok(approvalStepService.getStepById(id));
    }

    @PostMapping("/process/{approvalProcessId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApprovalStep> createStep(
            @PathVariable Long approvalProcessId,
            @RequestBody ApprovalStep step) {
        return ResponseEntity.ok(approvalStepService.createStep(approvalProcessId, step));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApprovalStep> updateStep(
            @PathVariable Long id,
            @RequestBody ApprovalStep step) {
        return ResponseEntity.ok(approvalStepService.updateStep(id, step));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteStep(@PathVariable Long id) {
        approvalStepService.deleteStep(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> updateStepStatus(
            @PathVariable Long id,
            @RequestBody ApprovalStepStatus status) {
        approvalStepService.updateStepStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/random-reviewers")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<User>> getRandomReviewers(@RequestParam int count) {
        return ResponseEntity.ok(approvalStepService.getRandomReviewers(count));
    }
}
