package com.vote.sihuibackend.repository;

import com.vote.sihuibackend.entity.Answer;
import com.vote.sihuibackend.entity.Question;
import com.vote.sihuibackend.entity.Questionnaire;
import com.vote.sihuibackend.entity.QuestionnaireResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

        List<Answer> findByResponse(QuestionnaireResponse questionnaireResponse);

        List<Answer> findByQuestion(Question question);

        @Query("SELECT a FROM Answer a WHERE a.question.questionnaire = :questionnaire")
        List<Answer> findByQuestionnaire(@Param("questionnaire") Questionnaire questionnaire);

        @Query("SELECT COUNT(a) FROM Answer a WHERE a.question = :question")
        Long countByQuestion(@Param("question") Question question);

        @Query("SELECT a.textAnswer, COUNT(a) FROM Answer a WHERE a.question = :question " +
                        "GROUP BY a.textAnswer ORDER BY COUNT(a) DESC")
        List<Object[]> findAnswerFrequencyByQuestion(@Param("question") Question question);

        @Query("SELECT AVG(a.numberAnswer) FROM Answer a WHERE a.question = :question AND a.numberAnswer IS NOT NULL")
        Double findAverageNumericAnswerByQuestion(@Param("question") Question question);

        @Query("SELECT a.textAnswer FROM Answer a WHERE a.question = :question " +
                        "AND a.textAnswer IS NOT NULL AND LENGTH(a.textAnswer) > 0 ORDER BY a.createdAt DESC")
        List<String> findTextAnswersByQuestion(@Param("question") Question question);
}