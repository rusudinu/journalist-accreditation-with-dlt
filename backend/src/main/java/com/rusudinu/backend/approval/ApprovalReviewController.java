package com.rusudinu.backend.approval;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/approval-reviews")
public class ApprovalReviewController {
    private final ApprovalReviewService approvalReviewService;

    @GetMapping("/step/{stepId}")
    public ResponseEntity<List<ApprovalReview>> getReviewsByStepId(@PathVariable Long stepId) {
        return ResponseEntity.ok(approvalReviewService.getReviewsByStepId(stepId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApprovalReview> getReviewById(@PathVariable Long id) {
        return ResponseEntity.ok(approvalReviewService.getReviewById(id));
    }

    @PostMapping("/step/{stepId}/reviewer/{reviewerId}")
    public ResponseEntity<ApprovalReview> createReview(
            @PathVariable Long stepId,
            @PathVariable Long reviewerId,
            @RequestBody ApprovalReview review) {
        return ResponseEntity.ok(approvalReviewService.createReview(stepId, reviewerId, review));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApprovalReview> updateReview(
            @PathVariable Long id,
            @RequestBody ApprovalReview review) {
        return ResponseEntity.ok(approvalReviewService.updateReview(id, review));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        approvalReviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/step/{stepId}/reviewer/{reviewerId}")
    public ResponseEntity<List<ApprovalReview>> getReviewsByStepIdAndReviewer(
            @PathVariable Long stepId,
            @PathVariable Long reviewerId) {
        return ResponseEntity.ok(approvalReviewService.getReviewsByStepIdAndReviewer(stepId, reviewerId));
    }
}
