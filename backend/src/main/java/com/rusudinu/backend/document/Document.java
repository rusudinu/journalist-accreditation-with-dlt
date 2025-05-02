package com.rusudinu.backend.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;

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

    @CreationTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime createdDate;

    private boolean isDeleted = false;
    private String storedDocumentName;

    /*
    REGISTRATION PARLIMENT
    general secretariat, legislative council, economic and social council
     */
    String generalSecretariatComment;
    String legislativeCouncilComment;
    String economicAndSocialCouncilComment;

    /*
    AMENDMENTS / OPINIONS
    advisory committee, budget committee, public administration
     */
    String legalCommitteeComment;
    String budgetCommitteeComment;
    String publicAdministrationComment;

    String specialtyCommissionComment;

    Boolean debateAndApprovalInPlenarySession; // true / false
}
