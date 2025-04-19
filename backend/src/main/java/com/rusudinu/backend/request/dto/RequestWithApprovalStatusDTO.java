package com.rusudinu.backend.request.dto;

import com.rusudinu.backend.approval.ApprovalProcess;
import com.rusudinu.backend.approval.ApprovalStep;
import com.rusudinu.backend.approval.ApprovalStepStatus;
import com.rusudinu.backend.request.Request;
import com.rusudinu.backend.request.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestWithApprovalStatusDTO {
    private Long id;
    private ZonedDateTime createdDate;
    private RequestStatus status;
    private Long userId;
    private String userName;
    private Long approvalProcessId;
    private String approvalProcessName;
    private String currentStepName;
    private Integer currentStepNumber;
    private Integer totalSteps;
    private String progressDisplay; // e.g. "2/3"

    public static RequestWithApprovalStatusDTO fromRequest(Request request) {
        if (request == null) {
            return null;
        }

        RequestWithApprovalStatusDTO dto = new RequestWithApprovalStatusDTO();
        dto.setId(request.getId());
        dto.setCreatedDate(request.getCreatedDate());
        dto.setStatus(request.getStatus());
        
        if (request.getUser() != null) {
            dto.setUserId(request.getUser().getId());
            dto.setUserName(request.getUser().getKeycloakId()); // Using keycloakId as name for now
        }

        ApprovalProcess approvalProcess = request.getApprovalProcess();
        if (approvalProcess != null) {
            dto.setApprovalProcessId(approvalProcess.getId());
            dto.setApprovalProcessName(approvalProcess.getName());
            dto.setTotalSteps(approvalProcess.getSteps().size());

            // Find the current step (first non-approved/completed step)
            ApprovalStep currentStep = null;
            int currentStepNumber = 0;
            int completedSteps = 0;

            for (ApprovalStep step : approvalProcess.getSteps()) {
                if (step.getStatus() == ApprovalStepStatus.APPROVED || 
                    step.getStatus() == ApprovalStepStatus.COMPLETED) {
                    completedSteps++;
                } else if (currentStep == null) {
                    currentStep = step;
                    currentStepNumber = step.getStepOrder();
                }
            }

            // If all steps are completed, use the last step
            if (currentStep == null && !approvalProcess.getSteps().isEmpty()) {
                currentStep = approvalProcess.getSteps().get(approvalProcess.getSteps().size() - 1);
                currentStepNumber = currentStep.getStepOrder();
            }

            if (currentStep != null) {
                dto.setCurrentStepName(currentStep.getName());
                dto.setCurrentStepNumber(currentStepNumber);
            }

            dto.setProgressDisplay(completedSteps + "/" + dto.getTotalSteps());
        }

        return dto;
    }
}
