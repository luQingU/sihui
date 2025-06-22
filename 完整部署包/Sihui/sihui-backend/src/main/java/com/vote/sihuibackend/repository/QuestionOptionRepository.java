package com.vote.sihuibackend.repository;

import com.vote.sihuibackend.entity.Question;
import com.vote.sihuibackend.entity.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {

    List<QuestionOption> findByQuestionOrderBySortOrder(Question question);

    List<QuestionOption> findByQuestion(Question question);
}