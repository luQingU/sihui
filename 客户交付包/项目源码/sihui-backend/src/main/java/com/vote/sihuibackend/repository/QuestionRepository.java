package com.vote.sihuibackend.repository;

import com.vote.sihuibackend.entity.Question;
import com.vote.sihuibackend.entity.Questionnaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByQuestionnaireOrderBySortOrder(Questionnaire questionnaire);

    @Query("SELECT COUNT(q) FROM Question q WHERE q.questionnaire = :questionnaire")
    Long countByQuestionnaire(@Param("questionnaire") Questionnaire questionnaire);
}