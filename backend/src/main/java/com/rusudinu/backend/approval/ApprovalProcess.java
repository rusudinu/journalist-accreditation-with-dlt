package com.rusudinu.backend.approval;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rusudinu.backend.request.Request;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Entity
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "approval_processes")
public class ApprovalProcess {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime createdDate;

    private String name;

    private String description;

    @JsonIgnoreProperties("approvalProcess")
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "approvalProcess")
    private List<ApprovalStep> steps;

    @JsonIgnoreProperties("approvalProcess")
    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "approvalProcess")
    private List<Request> requests;
}
