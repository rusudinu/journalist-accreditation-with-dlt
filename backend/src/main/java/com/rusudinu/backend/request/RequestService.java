package com.rusudinu.backend.request;

import com.rusudinu.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestService {
    private final RequestRepository requestRepository;

    public Request createRequest(User user) {
        Request request = Request.builder()
                .user(user)
                .status(RequestStatus.CREATED)
                .build();

        return requestRepository.save(request);
    }

    public Request updateRequestStatus(Long requestId, RequestStatus status) {
        Request request = requestRepository.findById(requestId).orElseThrow(
                () -> new RuntimeException("Request with id " + requestId + " not found")
        );

        request.setStatus(status);

        return requestRepository.save(request);
    }

    public Request getRequestById(Long requestId) {
        return requestRepository.findById(requestId).orElseThrow(
                () -> new RuntimeException("Request with id " + requestId + " not found")
        );
    }
}
