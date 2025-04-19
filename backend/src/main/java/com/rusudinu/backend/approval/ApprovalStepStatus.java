package com.rusudinu.backend.approval;

public enum ApprovalStepStatus {
    PENDING, // Step is waiting for reviews
    IN_PROGRESS, // Step has some reviews but not enough
    APPROVED, // Step has enough approvals
    REJECTED, // Step has been rejected
    COMPLETED // Step is completed (for comment-only steps)
}
