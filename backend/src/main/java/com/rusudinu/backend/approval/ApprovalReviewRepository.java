package com.rusudinu.backend.approval;

import com.rusudinu.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalReviewRepository extends JpaRepository<ApprovalReview, Long> {
    List<ApprovalReview> findByApprovalStepId(Long approvalStepId);

    List<ApprovalReview> findByApprovalStepIdAndReviewer(Long approvalStepId, User reviewer);

    List<ApprovalReview> findByApprovalStepIdAndApproved(Long approvalStepId, Boolean approved);

    long countByApprovalStepIdAndApproved(Long approvalStepId, Boolean approved);

    List<ApprovalReview> findByReviewer(User reviewer);

    @Query("SELECT r FROM Request r JOIN r.approvalProcess ap JOIN ap.steps s JOIN s.reviews rev WHERE rev.reviewer = :reviewer")
    List<com.rusudinu.backend.request.Request> findRequestsByReviewer(@Param("reviewer") User reviewer);
}
