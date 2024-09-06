package com.rusudinu.backend.request.snapshot;


import com.rusudinu.backend.request.RequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@Entity
@AllArgsConstructor
@RequiredArgsConstructor
public class RequestSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long requestId;
    private byte[] documentHash;
    private RequestStatus status;
    @Column(length = 1000)
    private String previousSnapshotHash;
}
