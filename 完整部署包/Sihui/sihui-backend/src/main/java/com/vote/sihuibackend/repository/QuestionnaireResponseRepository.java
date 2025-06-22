package com.vote.sihuibackend.repository;

import com.vote.sihuibackend.entity.QuestionnaireResponse;
import com.vote.sihuibackend.entity.Questionnaire;
import com.vote.sihuibackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public interface QuestionnaireResponseRepository extends JpaRepository<QuestionnaireResponse, Long> {

        List<QuestionnaireResponse> findByQuestionnaire(Questionnaire questionnaire);

        List<QuestionnaireResponse> findByQuestionnaireAndUser(Questionnaire questionnaire, User user);

        @Query("SELECT COUNT(r) FROM QuestionnaireResponse r WHERE r.questionnaire = :questionnaire")
        Long countByQuestionnaire(@Param("questionnaire") Questionnaire questionnaire);

        @Query("SELECT COUNT(r) FROM QuestionnaireResponse r WHERE r.questionnaire = :questionnaire AND r.isCompleted = true")
        Long countCompletedByQuestionnaire(@Param("questionnaire") Questionnaire questionnaire);

        Page<QuestionnaireResponse> findByQuestionnaireAndIsCompleted(Questionnaire questionnaire, Boolean isCompleted,
                        Pageable pageable);

        Optional<QuestionnaireResponse> findByQuestionnaireAndUserAndIsCompleted(Questionnaire questionnaire, User user,
                        Boolean isCompleted);

        List<QuestionnaireResponse> findByQuestionnaireAndIsCompleted(Questionnaire questionnaire, boolean isCompleted);

        @Query("SELECT COUNT(qr) FROM QuestionnaireResponse qr WHERE qr.questionnaire = :questionnaire AND qr.isCompleted = :isCompleted")
        Long countByQuestionnaireAndIsCompleted(
                        @Param("questionnaire") Questionnaire questionnaire,
                        @Param("isCompleted") boolean isCompleted);

        @Query("SELECT MIN(qr.startedAt) FROM QuestionnaireResponse qr WHERE qr.questionnaire = :questionnaire")
        LocalDateTime findFirstResponseTime(@Param("questionnaire") Questionnaire questionnaire);

        @Query("SELECT MAX(qr.completedAt) FROM QuestionnaireResponse qr WHERE qr.questionnaire = :questionnaire")
        LocalDateTime findLastResponseTime(@Param("questionnaire") Questionnaire questionnaire);
}