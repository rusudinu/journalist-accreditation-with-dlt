package com.rusudinu.backend.approval;

import com.rusudinu.backend.request.Request;
import com.rusudinu.backend.request.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalProcessService {
    private final ApprovalProcessRepository approvalProcessRepository;
    private final ApprovalStepRepository approvalStepRepository;
    private final RequestRepository requestRepository;

    public List<ApprovalProcess> getAllApprovalProcesses() {
        return approvalProcessRepository.findAllByOrderByCreatedDateDesc();
    }

    public ApprovalProcess getApprovalProcessById(Long id) {
        return approvalProcessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Approval process not found with id: " + id));
    }

    @Transactional
    public ApprovalProcess createApprovalProcess(ApprovalProcess approvalProcess) {
        // Save the approval process first
        ApprovalProcess savedProcess = approvalProcessRepository.save(approvalProcess);
        
        // If steps are provided, set the approval process for each step and save them
        if (approvalProcess.getSteps() != null && !approvalProcess.getSteps().isEmpty()) {
            List<ApprovalStep> steps = new ArrayList<>();
            for (int i = 0; i < approvalProcess.getSteps().size(); i++) {
                ApprovalStep step = approvalProcess.getSteps().get(i);
                step.setApprovalProcess(savedProcess);
                step.setStepOrder(i + 1); // Set the step order based on the list order
                steps.add(approvalStepRepository.save(step));
            }
            savedProcess.setSteps(steps);
        }
        
        return savedProcess;
    }

    @Transactional
    public ApprovalProcess updateApprovalProcess(Long id, ApprovalProcess approvalProcess) {
        ApprovalProcess existingProcess = getApprovalProcessById(id);
        
        existingProcess.setName(approvalProcess.getName());
        existingProcess.setDescription(approvalProcess.getDescription());
        
        return approvalProcessRepository.save(existingProcess);
    }

    @Transactional
    public void deleteApprovalProcess(Long id) {
        ApprovalProcess process = getApprovalProcessById(id);
        
        // Check if the process is assigned to any requests
        List<Request> requests = process.getRequests();
        if (requests != null && !requests.isEmpty()) {
            throw new RuntimeException("Cannot delete approval process that is assigned to requests");
        }
        
        approvalProcessRepository.delete(process);
    }

    @Transactional
    public void assignApprovalProcessToRequest(Long approvalProcessId, Long requestId) {
        ApprovalProcess approvalProcess = getApprovalProcessById(approvalProcessId);
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found with id: " + requestId));
        
        request.setApprovalProcess(approvalProcess);
        requestRepository.save(request);
    }
}
