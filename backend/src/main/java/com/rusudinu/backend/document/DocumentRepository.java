package com.rusudinu.backend.document;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DocumentRepository extends JpaRepository<Document, Long> {
	List<Document> findByGeneralSecretariatCommentIsNull();

	List<Document> findByLegislativeCouncilCommentIsNull();

	@Query("""
				SELECT d FROM Document d
				WHERE d.economicAndSocialCouncilComment IS NULL
			"""
	)
	List<Document> findByEconomicAndSocialCouncilCommentIsNull();

	List<Document> findByTransportCommitteeCommentIsNull();

	List<Document> findByPoliticsCommitteeCommentIsNull();

	List<Document> findByDecidingSpecialtyCommissionDocumentNameIsNull();
	List<Document> findByDecidingSpecialtyCommissionDocumentNameIsNotNull();

	// countdownAutoApproval must be false
	// and plenarySessionFinished must also be false
	@Query("""
				SELECT d FROM Document d
				WHERE d.countdownAutoApproval = false
				AND d.plenarySessionFinished = false
			"""
	)
	List<Document> findDocumentsThatNeedVote();
}
