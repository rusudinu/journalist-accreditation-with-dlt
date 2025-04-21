package com.rusudinu.backend.approval;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Table(name = "approval_steps")
public class ApprovalStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime createdDate;

    private String name;

    private String description;

    private Integer stepOrder;

    private Integer minReviewers;

    private Boolean requiresApproval;

    @Enumerated(EnumType.STRING)
    private ApprovalStepStatus status = ApprovalStepStatus.PENDING;

    @ManyToOne
    @JsonIgnoreProperties("steps")
    private ApprovalProcess approvalProcess;

    @JsonIgnoreProperties("approvalStep")
    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "approvalStep")
    private List<ApprovalReview> reviews;
}
