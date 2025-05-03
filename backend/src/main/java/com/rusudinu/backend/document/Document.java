package com.rusudinu.backend.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.util.List;
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
	/*
	REGISTRATION PARLIMENT
	general secretariat, legislative council, economic and social council
	 */
	private String generalSecretariatComment;
	private String legislativeCouncilComment;
	private String economicAndSocialCouncilComment;
	/*
	AMENDMENTS / OPINIONS
	advisory committee, budget committee, public administration
	 */
	private String legalCommitteeComment;
	private String budgetCommitteeComment;
	private String publicAdministrationComment;

	/*
	DECIDING SPECIALTY COMMISSION
	 */
	private String decidingSpecialtyCommissionDocumentName;

	private ZonedDateTime debateAndApprovalStartDate;

	// list of strings (YES, NO, ABSTAIN)
	@Column(name = "vote_results", columnDefinition = "TEXT[]")
	private List<String> debateAndApprovalPlenarySessionVoteResults;

	Boolean debateAndApprovalInPlenarySession; // true / false
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@CreationTimestamp
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private ZonedDateTime createdDate;
	private boolean isDeleted = false;
	private String storedDocumentName;
}
