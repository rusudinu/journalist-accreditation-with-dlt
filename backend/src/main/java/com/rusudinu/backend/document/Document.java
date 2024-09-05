package com.rusudinu.backend.document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rusudinu.backend.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Entity
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "documents")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private ZonedDateTime createdDate;

    private boolean isDeleted = false;
    private String storedDocumentName;
    private String status; // request, approve, deny

    @JsonIgnoreProperties("documents")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private Long uploadedByUserId;
}
