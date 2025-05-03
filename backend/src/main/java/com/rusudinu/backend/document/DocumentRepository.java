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

	List<Document> findByLegalCommitteeCommentIsNull();

	List<Document> findByBudgetCommitteeCommentIsNull();

	List<Document> findByPublicAdministrationCommentIsNull();

	List<Document> findByDecidingSpecialtyCommissionDocumentNameIsNull();
	List<Document> findByDecidingSpecialtyCommissionDocumentNameIsNotNull();
}
