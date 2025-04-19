package com.rusudinu.backend.approval;

import com.rusudinu.backend.config.KeycloakClient;
import com.rusudinu.backend.user.User;
import com.rusudinu.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApprovalStepService {
    private final ApprovalStepRepository approvalStepRepository;
    private final ApprovalProcessRepository approvalProcessRepository;
    private final ApprovalReviewRepository approvalReviewRepository;
    private final UserRepository userRepository;
    private final KeycloakClient keycloakClient;

    public List<ApprovalStep> getStepsByApprovalProcessId(Long approvalProcessId) {
        return approvalStepRepository.findByApprovalProcessIdOrderByStepOrder(approvalProcessId);
    }

    public ApprovalStep getStepById(Long id) {
        return approvalStepRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Approval step not found with id: " + id));
    }

    @Transactional
    public ApprovalStep createStep(Long approvalProcessId, ApprovalStep step) {
        ApprovalProcess approvalProcess = approvalProcessRepository.findById(approvalProcessId)
                .orElseThrow(() -> new RuntimeException("Approval process not found with id: " + approvalProcessId));

        // Set the approval process for the step
        step.setApprovalProcess(approvalProcess);

        // Set the step order to be the next in sequence
        List<ApprovalStep> existingSteps = getStepsByApprovalProcessId(approvalProcessId);
        step.setStepOrder(existingSteps.size() + 1);

        // Set the initial status
        step.setStatus(ApprovalStepStatus.PENDING);

        return approvalStepRepository.save(step);
    }

    @Transactional
    public ApprovalStep updateStep(Long id, ApprovalStep step) {
        ApprovalStep existingStep = getStepById(id);

        existingStep.setName(step.getName());
        existingStep.setDescription(step.getDescription());
        existingStep.setMinReviewers(step.getMinReviewers());
        existingStep.setRequiresApproval(step.getRequiresApproval());

        return approvalStepRepository.save(existingStep);
    }

    @Transactional
    public void deleteStep(Long id) {
        ApprovalStep step = getStepById(id);

        // Check if the step has any reviews
        List<ApprovalReview> reviews = approvalReviewRepository.findByApprovalStepId(id);
        if (reviews != null && !reviews.isEmpty()) {
            throw new RuntimeException("Cannot delete step that has reviews");
        }

        approvalStepRepository.delete(step);

        // Reorder the remaining steps
        List<ApprovalStep> remainingSteps = getStepsByApprovalProcessId(step.getApprovalProcess().getId());
        for (int i = 0; i < remainingSteps.size(); i++) {
            ApprovalStep remainingStep = remainingSteps.get(i);
            remainingStep.setStepOrder(i + 1);
            approvalStepRepository.save(remainingStep);
        }
    }

    @Transactional
    public void updateStepStatus(Long id, ApprovalStepStatus status) {
        ApprovalStep step = getStepById(id);
        step.setStatus(status);
        approvalStepRepository.save(step);
    }

    public List<User> getRandomReviewers(int count) {
        // Get all users from Keycloak
        List<User> allUsers = keycloakClient.getAllUsers();

        // Shuffle the list to get random users
        Collections.shuffle(allUsers);

        // Return the requested number of users or all if count is greater than available users
        return allUsers.stream()
                .limit(count)
                .collect(Collectors.toList());
    }

    @Transactional
    public void checkStepCompletion(Long stepId) {
        ApprovalStep step = getStepById(stepId);

        // If the step is already approved or rejected, no need to check
        if (step.getStatus() == ApprovalStepStatus.APPROVED || 
            step.getStatus() == ApprovalStepStatus.REJECTED ||
            step.getStatus() == ApprovalStepStatus.COMPLETED) {
            return;
        }

        // Get all reviews for this step
        List<ApprovalReview> reviews = approvalReviewRepository.findByApprovalStepId(stepId);

        // If there are no reviews yet, the step is still pending
        if (reviews.isEmpty()) {
            step.setStatus(ApprovalStepStatus.PENDING);
            approvalStepRepository.save(step);
            return;
        }

        // If this is a comment-only step (doesn't require approval)
        if (!step.getRequiresApproval()) {
            // If we have at least the minimum number of reviews, mark as completed
            if (reviews.size() >= step.getMinReviewers()) {
                step.setStatus(ApprovalStepStatus.COMPLETED);
                approvalStepRepository.save(step);
            } else {
                step.setStatus(ApprovalStepStatus.IN_PROGRESS);
                approvalStepRepository.save(step);
            }
            return;
        }

        // For approval steps, count the number of approvals
        long approvalCount = approvalReviewRepository.countByApprovalStepIdAndApproved(stepId, true);

        // If we have enough approvals, mark the step as approved
        if (approvalCount >= step.getMinReviewers()) {
            step.setStatus(ApprovalStepStatus.APPROVED);
            approvalStepRepository.save(step);
            return;
        }

        // If any reviewer has rejected, mark the step as rejected
        List<ApprovalReview> rejections = approvalReviewRepository.findByApprovalStepIdAndApproved(stepId, false);
        if (!rejections.isEmpty()) {
            step.setStatus(ApprovalStepStatus.REJECTED);
            approvalStepRepository.save(step);
            return;
        }

        // Otherwise, the step is in progress
        step.setStatus(ApprovalStepStatus.IN_PROGRESS);
        approvalStepRepository.save(step);
    }
}
