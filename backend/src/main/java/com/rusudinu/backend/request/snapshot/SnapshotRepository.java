package com.rusudinu.backend.request.snapshot;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SnapshotRepository extends JpaRepository<RequestSnapshot, Long> {
    RequestSnapshot findFirstByRequestIdOrderByIdDesc(Long id);
}
