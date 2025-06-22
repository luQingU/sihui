package com.vote.sihuibackend.repository;

import com.vote.sihuibackend.entity.Questionnaire;
import com.vote.sihuibackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Long> {

        List<Questionnaire> findByStatus(Questionnaire.QuestionnaireStatus status);

        Page<Questionnaire> findByCreatedBy(User createdBy, Pageable pageable);

        Page<Questionnaire> findByCreatedByAndStatus(User createdBy, Questionnaire.QuestionnaireStatus status,
                        Pageable pageable);

        @Query("SELECT q FROM Questionnaire q WHERE q.title LIKE %:title%")
        Page<Questionnaire> findByTitleContaining(@Param("title") String title, Pageable pageable);

        @Query("SELECT q FROM Questionnaire q WHERE q.status = 'PUBLISHED' AND (q.endTime IS NULL OR q.endTime > :now)")
        List<Questionnaire> findPublishedAndActive(@Param("now") LocalDateTime now);

        @Query("SELECT q FROM Questionnaire q WHERE q.status = 'PUBLISHED' " +
                        "AND (q.startTime IS NULL OR q.startTime <= :now) " +
                        "AND (q.endTime IS NULL OR q.endTime > :now)")
        List<Questionnaire> findAvailableForUser(@Param("now") LocalDateTime now);

        Optional<Questionnaire> findByPublishUrl(String publishUrl);

        @Query("SELECT COUNT(q) FROM Questionnaire q WHERE q.createdBy = :user")
        Long countByCreatedBy(@Param("user") User user);

        @Query("SELECT COUNT(qr) FROM QuestionnaireResponse qr WHERE qr.questionnaire = :questionnaire")
        Long countResponsesByQuestionnaire(@Param("questionnaire") Questionnaire questionnaire);
}