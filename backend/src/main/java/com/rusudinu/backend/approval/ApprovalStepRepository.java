package com.rusudinu.backend.approval;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, Long> {
    List<ApprovalStep> findByApprovalProcessIdOrderByStepOrder(Long approvalProcessId);
    
    List<ApprovalStep> findByApprovalProcessIdAndStatus(Long approvalProcessId, ApprovalStepStatus status);
}
