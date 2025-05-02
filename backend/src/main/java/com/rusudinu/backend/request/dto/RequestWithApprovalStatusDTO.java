package com.rusudinu.backend.request.dto;

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
            // Use name if available, otherwise use keycloakId
            String userName = request.getUser().getName();
            if (userName == null || userName.isEmpty()) {
                userName = request.getUser().getKeycloakId();
            }
            dto.setUserName(userName);
        }

        return dto;
    }
}
