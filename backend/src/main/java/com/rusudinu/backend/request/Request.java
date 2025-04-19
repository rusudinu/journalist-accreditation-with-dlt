package com.rusudinu.backend.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rusudinu.backend.approval.ApprovalProcess;
import com.rusudinu.backend.document.Document;
import com.rusudinu.backend.request.vc.VerifiableCredentialEntity;
import com.rusudinu.backend.user.User;
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
@Table(name = "requests")
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime createdDate;

    // IF STATUS IS CREATED SHOW TO JURIDIC
    // IF STATUS IS VALIDATED SHOW TO DIRECTOR
    // IF STATUS IS APPROVED OR DENIED THE REQUEST IS CLOSED
    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    @JsonIgnoreProperties("requests")
    @ManyToOne
    private User user;

    @JsonIgnoreProperties("request")
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "request")
    private List<Document> documents;

    @ManyToOne
    @JsonIgnoreProperties("requests")
    private ApprovalProcess approvalProcess;
}
