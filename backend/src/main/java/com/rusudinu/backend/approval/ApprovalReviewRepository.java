package com.rusudinu.backend.approval;

import com.rusudinu.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalReviewRepository extends JpaRepository<ApprovalReview, Long> {
    List<ApprovalReview> findByApprovalStepId(Long approvalStepId);
    
    List<ApprovalReview> findByApprovalStepIdAndReviewer(Long approvalStepId, User reviewer);
    
    List<ApprovalReview> findByApprovalStepIdAndApproved(Long approvalStepId, Boolean approved);
    
    long countByApprovalStepIdAndApproved(Long approvalStepId, Boolean approved);
}
