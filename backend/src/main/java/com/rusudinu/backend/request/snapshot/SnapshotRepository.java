package com.rusudinu.backend.request.snapshot;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SnapshotRepository extends JpaRepository<RequestSnapshot, Long> {
    List<RequestSnapshot> findAllByRequestId(Long requestId);

    RequestSnapshot findFirstByRequestIdOrderByIdDesc(Long id);
}
