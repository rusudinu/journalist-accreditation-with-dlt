package com.rusudinu.backend.request;

import com.rusudinu.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<Request> fetchHomePageRequests(Long userId, List<String> rolesList) {
        List<RequestStatus> journalistStatusFilter = List.of(RequestStatus.CREATED, RequestStatus.VALIDATED, RequestStatus.APPROVED, RequestStatus.REJECTED);
        List<RequestStatus> juridicStatusFilter = List.of(RequestStatus.CREATED);
        List<RequestStatus> directorStatusFilter = List.of(RequestStatus.VALIDATED);

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
        } else {
            throw new RuntimeException("User with id " + userId + " has no roles");
        }
    }
}
