package com.rusudinu.backend.approval;

import com.rusudinu.backend.config.KeycloakClient;
import com.rusudinu.backend.user.User;
import com.rusudinu.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalReviewService {
    private final ApprovalReviewRepository approvalReviewRepository;
    private final ApprovalStepRepository approvalStepRepository;
    private final ApprovalStepService approvalStepService;
    private final UserRepository userRepository;
    private final KeycloakClient keycloakClient;

    public List<ApprovalReview> getReviewsByStepId(Long stepId) {
        return approvalReviewRepository.findByApprovalStepId(stepId);
    }

    public ApprovalReview getReviewById(Long id) {
        return approvalReviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Approval review not found with id: " + id));
    }

    @Transactional
    public ApprovalReview createReview(Long stepId, String reviewerKeycloakId, ApprovalReview review) {
        ApprovalStep step = approvalStepRepository.findById(stepId)
                .orElseThrow(() -> new RuntimeException("Approval step not found with id: " + stepId));

        // Find the user by their database ID
        User reviewer = userRepository.findByKeycloakId(reviewerKeycloakId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + reviewerKeycloakId));

        // Check if the reviewer has already submitted a review for this step
        List<ApprovalReview> existingReviews = approvalReviewRepository.findByApprovalStepIdAndReviewer(stepId, reviewer);
        if (!existingReviews.isEmpty()) {
            throw new RuntimeException("Reviewer has already submitted a review for this step");
        }

        // Set the step and reviewer for the review
        review.setApprovalStep(step);
        review.setReviewer(reviewer);

        // For comment-only steps, set approved to null
        if (!step.getRequiresApproval()) {
            review.setApproved(null);
        }

        // Save the review
        ApprovalReview savedReview = approvalReviewRepository.save(review);

        // Check if the step is complete
        approvalStepService.checkStepCompletion(stepId);

        return savedReview;
    }

    @Transactional
    public ApprovalReview updateReview(Long id, ApprovalReview review) {
        ApprovalReview existingReview = getReviewById(id);

        // Only allow updating the comment and approved status
        existingReview.setComment(review.getComment());

        // Only update approved status if the step requires approval
        if (existingReview.getApprovalStep().getRequiresApproval()) {
            existingReview.setApproved(review.getApproved());
        }

        // Save the updated review
        ApprovalReview savedReview = approvalReviewRepository.save(existingReview);

        // Check if the step is complete
        approvalStepService.checkStepCompletion(existingReview.getApprovalStep().getId());

        return savedReview;
    }

    @Transactional
    public void deleteReview(Long id) {
        ApprovalReview review = getReviewById(id);
        Long stepId = review.getApprovalStep().getId();

        approvalReviewRepository.delete(review);

        // Check if the step status needs to be updated
        approvalStepService.checkStepCompletion(stepId);
    }

    public List<ApprovalReview> getReviewsByStepIdAndReviewer(Long stepId, Long reviewerId) {
        // Find the user by their database ID
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + reviewerId));

        return approvalReviewRepository.findByApprovalStepIdAndReviewer(stepId, reviewer);
    }
}
