package com.rusudinu.backend.approval;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalProcessRepository extends JpaRepository<ApprovalProcess, Long> {
    List<ApprovalProcess> findAllByOrderByCreatedDateDesc();
}
