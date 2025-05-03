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

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	private ZonedDateTime debateAndApprovalStartDate;

	// the deputy chamber president will set this to false
	// if he presses the 'Start plenary session' button,
	// we no longer start the 15-minute countdown
	// which will auto-approve the document
	private boolean countdownAutoApproval = true;

	// list of strings (YES, NO, ABSTAIN)
	@Column(columnDefinition = "TEXT[]")
	private List<String> debateAndApprovalPlenarySessionVoteResults;

	// this will mark the document as finished
	private boolean plenarySessionFinished = false;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@CreationTimestamp
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private ZonedDateTime createdDate;
	private boolean isDeleted = false;
	private String storedDocumentName;
	private String lawName;
}
