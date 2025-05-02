package com.rusudinu.backend.request;

import com.rusudinu.backend.request.dto.RequestWithApprovalStatusDTO;
import com.rusudinu.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RequestService {
    private final RequestRepository requestRepository;

    public Request createRequest(User user) {
        Request request = Request.builder()
                .user(user)
                .build();

        return requestRepository.save(request);
    }

    @Transactional
    public Request assignApprovalProcess(Long requestId, Long approvalProcessId) {
        Request request = requestRepository.findById(requestId).orElseThrow(
                () -> new RuntimeException("Request with id " + requestId + " not found")
        );


        return requestRepository.save(request);
    }

    public Request getRequestById(Long requestId) {
        return requestRepository.findById(requestId).orElseThrow(
                () -> new RuntimeException("Request with id " + requestId + " not found")
        );
    }
}
