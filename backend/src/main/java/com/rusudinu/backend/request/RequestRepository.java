package com.rusudinu.backend.request;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByUserIdAndStatusIn(Long userId, List<RequestStatus> statusFilter);

    List<Request> findAllByStatusIn(List<RequestStatus> statusFilter);

}
