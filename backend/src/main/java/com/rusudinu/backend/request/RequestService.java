package com.rusudinu.backend.request;

import com.rusudinu.backend.approval.ApprovalProcess;
import com.rusudinu.backend.approval.ApprovalProcessService;
import com.rusudinu.backend.approval.ApprovalStep;
import com.rusudinu.backend.approval.ApprovalStepStatus;
import com.rusudinu.backend.request.dto.RequestWithApprovalStatusDTO;
import com.rusudinu.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestService {
    private final RequestRepository requestRepository;
    private final ApprovalProcessService approvalProcessService;

    public Request createRequest(User user) {
        Request request = Request.builder()
                .user(user)
                .status(RequestStatus.CREATED)
                .build();

        return requestRepository.save(request);
    }

    @Transactional
    public Request updateRequestStatus(Long requestId, RequestStatus status) {
        Request request = requestRepository.findById(requestId).orElseThrow(
                () -> new RuntimeException("Request with id " + requestId + " not found")
        );

        request.setStatus(status);

        // If the request is being approved or rejected, check if it has an approval process
        if ((status == RequestStatus.APPROVED || status == RequestStatus.REJECTED) && request.getApprovalProcess() != null) {
            // Check if all steps in the approval process are completed
            ApprovalProcess approvalProcess = request.getApprovalProcess();
            boolean allStepsCompleted = true;

            for (ApprovalStep step : approvalProcess.getSteps()) {
                ApprovalStepStatus stepStatus = step.getStatus();

                // If any step is not approved/completed, the request cannot be approved
                if (status == RequestStatus.APPROVED && 
                    (stepStatus != ApprovalStepStatus.APPROVED && stepStatus != ApprovalStepStatus.COMPLETED)) {
                    allStepsCompleted = false;
                    break;
                }

                // If any step is rejected, the request should be rejected
                if (stepStatus == ApprovalStepStatus.REJECTED) {
                    request.setStatus(RequestStatus.REJECTED);
                    return requestRepository.save(request);
                }
            }

            // If not all steps are completed, the request cannot be approved
            if (status == RequestStatus.APPROVED && !allStepsCompleted) {
                throw new RuntimeException("Cannot approve request. Not all approval steps are completed.");
            }
        }

        return requestRepository.save(request);
    }

    @Transactional
    public Request assignApprovalProcess(Long requestId, Long approvalProcessId) {
        Request request = requestRepository.findById(requestId).orElseThrow(
                () -> new RuntimeException("Request with id " + requestId + " not found")
        );

        ApprovalProcess approvalProcess = approvalProcessService.getApprovalProcessById(approvalProcessId);
        request.setApprovalProcess(approvalProcess);

        return requestRepository.save(request);
    }

    public Request getRequestById(Long requestId) {
        return requestRepository.findById(requestId).orElseThrow(
                () -> new RuntimeException("Request with id " + requestId + " not found")
        );
    }

    public List<Request> fetchHomePageRequests(Long userId, List<String> rolesList) {
        List<RequestStatus> journalistStatusFilter = List.of(RequestStatus.CREATED, RequestStatus.VALIDATED, RequestStatus.APPROVED, RequestStatus.REJECTED);
        List<RequestStatus> juridicStatusFilter = List.of(RequestStatus.CREATED);
        List<RequestStatus> directorStatusFilter = List.of(RequestStatus.VALIDATED);
        List<RequestStatus> adminStatusFilter = List.of(RequestStatus.CREATED, RequestStatus.VALIDATED, RequestStatus.APPROVED, RequestStatus.REJECTED);

        List<RequestStatus> statusFilter;

        if (rolesList.contains("JOURNALIST")) {
            statusFilter = journalistStatusFilter;
            return requestRepository.findAllByUserIdAndStatusIn(userId, statusFilter);
        } else if (rolesList.contains("JURIDIC")) {
            statusFilter = juridicStatusFilter;
            return requestRepository.findAllByStatusIn(statusFilter);
        } else if (rolesList.contains("DIRECTOR")) {
            statusFilter = directorStatusFilter;
            return requestRepository.findAllByStatusIn(statusFilter);
        } else if (rolesList.contains("ADMIN")) {
            statusFilter = adminStatusFilter;
            return requestRepository.findAllByStatusIn(statusFilter);
        } else {
            throw new RuntimeException("User with id " + userId + " has no roles");
        }
    }

    public List<Request> getRequestsWithoutApprovalProcess() {
        return requestRepository.findByApprovalProcessIsNull();
    }

    public List<RequestWithApprovalStatusDTO> getRequestsWithApprovalProcess() {
        List<Request> requests = requestRepository.findByApprovalProcessIsNotNull();
        return requests.stream()
                .map(RequestWithApprovalStatusDTO::fromRequest)
                .collect(Collectors.toList());
    }
}
