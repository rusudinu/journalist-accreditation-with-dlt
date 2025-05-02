package com.rusudinu.backend.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rusudinu.backend.comment.Comment;
import com.rusudinu.backend.request.Request;
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

    @JsonIgnoreProperties("documents")
    @ManyToOne(fetch = FetchType.LAZY)
    private Request request;

    /*
    REGISTRATION PARLIMENT
    general secretariat, legislative council, economic and social council
     */
    @JsonIgnoreProperties("document")
    @JsonIgnore
    @OneToOne
    Comment generalSecretariatComment;


    @JsonIgnoreProperties("document")
    @JsonIgnore
    @OneToOne
    Comment legislativeCouncilComment;

    @JsonIgnoreProperties("document")
    @JsonIgnore
    @OneToOne
    Comment economicAndSocialCouncilComment;

    /*
    AMENDMENTS / OPINIONS
    advisory committee, budget committee, public administration
     */
    @JsonIgnoreProperties("document")
    @JsonIgnore
    @OneToOne
    Comment legalCommitteeComment;

    @JsonIgnoreProperties("document")
    @JsonIgnore
    @OneToOne
    Comment budgetCommitteeComment;

    @JsonIgnoreProperties("document")
    @JsonIgnore
    @OneToOne
    Comment publicAdministrationComment;

    @JsonIgnoreProperties("document")
    @JsonIgnore
    @OneToOne
    Comment specialtyCommissionComment;

    Boolean debateAndApprovalInPlenarySession; // true / false
}
