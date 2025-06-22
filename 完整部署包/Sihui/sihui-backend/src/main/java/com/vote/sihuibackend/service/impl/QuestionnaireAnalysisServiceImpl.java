package com.vote.sihuibackend.service.impl;

import com.vote.sihuibackend.dto.QuestionnaireStatsResponse;
import com.vote.sihuibackend.entity.*;
import com.vote.sihuibackend.repository.*;
import com.vote.sihuibackend.service.QuestionnaireAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 问卷数据分析服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionnaireAnalysisServiceImpl implements QuestionnaireAnalysisService {

    private final QuestionnaireRepository questionnaireRepository;
    private final QuestionRepository questionRepository;
    private final QuestionnaireResponseRepository responseRepository;
    private final AnswerRepository answerRepository;
    private final QuestionOptionRepository questionOptionRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "questionnaireStats", key = "#questionnaireId + '_' + #user.id")
    public QuestionnaireStatsResponse generateQuestionnaireStats(Long questionnaireId, User user) {
        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new IllegalArgumentException("问卷不存在"));

        // 验证用户权限
        if (!questionnaire.getCreatedBy().getId().equals(user.getId())) {
            throw new IllegalArgumentException("无权限查看此问卷统计");
        }

        // 基础统计
        Long totalResponses = questionnaireRepository.countResponsesByQuestionnaire(questionnaire);
        Long completedResponses = responseRepository.countByQuestionnaireAndIsCompleted(questionnaire, true);
        Long questionCount = questionRepository.countByQuestionnaire(questionnaire);

        double completionRate = calculateCompletionRate(questionnaire);
        int averageDuration = calculateAverageDuration(questionnaire);

        // 时间统计
        LocalDateTime firstResponseAt = responseRepository.findFirstResponseTime(questionnaire);
        LocalDateTime lastResponseAt = responseRepository.findLastResponseTime(questionnaire);

        // 问题统计
        List<Question> questions = questionRepository.findByQuestionnaireOrderBySortOrder(questionnaire);
        List<QuestionnaireStatsResponse.QuestionStatsDto> questionStats = questions.stream()
                .map(this::analyzeQuestionStats)
                .collect(Collectors.toList());

        return QuestionnaireStatsResponse.builder()
                .questionnaireId(questionnaire.getId())
                .title(questionnaire.getTitle())
                .status(questionnaire.getStatus().name())
                .createdAt(questionnaire.getCreatedAt())
                .publishedAt(questionnaire.getPublishedAt())
                .totalResponses(totalResponses)
                .completedResponses(completedResponses)
                .questionCount(questionCount)
                .completionRate(completionRate)
                .averageDurationSeconds(averageDuration)
                .firstResponseAt(firstResponseAt)
                .lastResponseAt(lastResponseAt)
                .questionStats(questionStats)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionnaireStatsResponse.QuestionStatsDto analyzeQuestionStats(Question question) {
        Long responseCount = answerRepository.countByQuestion(question);
        Long totalPossibleResponses = questionnaireRepository
                .countResponsesByQuestionnaire(question.getQuestionnaire());

        double responseRate = totalPossibleResponses > 0 ? (double) responseCount / totalPossibleResponses * 100 : 0;

        QuestionnaireStatsResponse.QuestionStatsDto.QuestionStatsDtoBuilder builder = QuestionnaireStatsResponse.QuestionStatsDto
                .builder()
                .questionId(question.getId())
                .title(question.getTitle())
                .type(question.getType().name())
                .required(question.getRequired())
                .responseCount(responseCount)
                .responseRate(responseRate);

        // 根据问题类型分析不同的统计数据
        switch (question.getType()) {
            case RADIO:
            case CHECKBOX:
            case SELECT:
                builder.optionStats(analyzeOptionStats(question));
                break;
            case RATING:
            case SCALE:
                Map<String, Object> ratingStats = analyzeRatingStats(question);
                builder.averageRating((Double) ratingStats.get("average"));
                builder.ratingDistribution((Map<Integer, Long>) ratingStats.get("distribution"));
                break;
            case TEXT:
            case TEXTAREA:
                builder.textSamples(getTextAnswerSamples(question, 10));
                break;
        }

        return builder.build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionnaireStatsResponse.OptionStatsDto> analyzeOptionStats(Question question) {
        List<QuestionOption> options = questionOptionRepository.findByQuestionOrderBySortOrder(question);
        List<Object[]> answerFrequency = answerRepository.findAnswerFrequencyByQuestion(question);
        Long totalAnswers = answerRepository.countByQuestion(question);

        Map<String, Long> frequencyMap = answerFrequency.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]));

        return options.stream()
                .map(option -> {
                    Long count = frequencyMap.getOrDefault(option.getId().toString(), 0L);
                    double percentage = totalAnswers > 0 ? (double) count / totalAnswers * 100 : 0;

                    return QuestionnaireStatsResponse.OptionStatsDto.builder()
                            .optionId(option.getId())
                            .text(option.getText())
                            .count(count)
                            .percentage(percentage)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> analyzeRatingStats(Question question) {
        Double average = answerRepository.findAverageNumericAnswerByQuestion(question);

        // 计算评分分布
        List<Object[]> answerFrequency = answerRepository.findAnswerFrequencyByQuestion(question);
        Map<Integer, Long> distribution = answerFrequency.stream()
                .filter(row -> isNumeric((String) row[0]))
                .collect(Collectors.toMap(
                        row -> Integer.parseInt((String) row[0]),
                        row -> (Long) row[1]));

        Map<String, Object> result = new HashMap<>();
        result.put("average", average);
        result.put("distribution", distribution);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getTextAnswerSamples(Question question, int maxSamples) {
        List<String> allAnswers = answerRepository.findTextAnswersByQuestion(question);
        return allAnswers.stream()
                .filter(answer -> answer != null && !answer.trim().isEmpty())
                .limit(maxSamples)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public double calculateCompletionRate(Questionnaire questionnaire) {
        Long totalResponses = questionnaireRepository.countResponsesByQuestionnaire(questionnaire);
        Long completedResponses = responseRepository.countByQuestionnaireAndIsCompleted(questionnaire, true);

        return totalResponses > 0 ? (double) completedResponses / totalResponses * 100 : 0;
    }

    @Override
    @Transactional(readOnly = true)
    public int calculateAverageDuration(Questionnaire questionnaire) {
        List<com.vote.sihuibackend.entity.QuestionnaireResponse> responses = responseRepository
                .findByQuestionnaireAndIsCompleted(questionnaire, true);

        OptionalDouble average = responses.stream()
                .filter(response -> response.getStartedAt() != null && response.getCompletedAt() != null)
                .mapToLong(response -> ChronoUnit.SECONDS.between(response.getStartedAt(), response.getCompletedAt()))
                .average();

        return (int) average.orElse(0);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getResponseTimeDistribution(Questionnaire questionnaire) {
        List<com.vote.sihuibackend.entity.QuestionnaireResponse> responses = responseRepository
                .findByQuestionnaireAndIsCompleted(questionnaire, true);

        return responses.stream()
                .filter(response -> response.getCompletedAt() != null)
                .collect(Collectors.groupingBy(
                        response -> response.getCompletedAt().toLocalDate().toString(),
                        Collectors.counting()));
    }

    @Override
    @Transactional(readOnly = true)
    public String exportQuestionnaireDataToCsv(Long questionnaireId, User user) {
        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new IllegalArgumentException("问卷不存在"));

        // 验证用户权限
        if (!questionnaire.getCreatedBy().getId().equals(user.getId())) {
            throw new IllegalArgumentException("无权限导出此问卷数据");
        }

        StringBuilder csv = new StringBuilder();

        // CSV头部
        csv.append("回答ID,用户ID,开始时间,完成时间,IP地址,用户代理");

        List<Question> questions = questionRepository.findByQuestionnaireOrderBySortOrder(questionnaire);
        for (Question question : questions) {
            csv.append(",").append(escapeCSV(question.getTitle()));
        }
        csv.append("\n");

        // CSV数据
        List<com.vote.sihuibackend.entity.QuestionnaireResponse> responses = responseRepository
                .findByQuestionnaire(questionnaire);

        for (com.vote.sihuibackend.entity.QuestionnaireResponse response : responses) {
            csv.append(response.getId()).append(",");
            csv.append(response.getUser() != null ? response.getUser().getId() : "匿名").append(",");
            csv.append(response.getStartedAt()).append(",");
            csv.append(response.getCompletedAt()).append(",");
            csv.append(escapeCSV(response.getRespondentIp())).append(",");
            csv.append(escapeCSV(response.getUserAgent()));

            // 添加答案数据
            List<Answer> answers = answerRepository.findByResponse(response);
            Map<Long, String> answerMap = answers.stream()
                    .collect(Collectors.toMap(
                            answer -> answer.getQuestion().getId(),
                            answer -> answer.getTextAnswer() != null ? answer.getTextAnswer() : ""));

            for (Question question : questions) {
                String answerText = answerMap.getOrDefault(question.getId(), "");
                csv.append(",").append(escapeCSV(answerText));
            }
            csv.append("\n");
        }

        return csv.toString();
    }

    @Override
    public byte[] exportStatisticsReportToPdf(Long questionnaireId, User user) {
        // PDF导出功能需要引入PDF库，这里先返回空实现
        throw new UnsupportedOperationException("PDF导出功能暂未实现");
    }

    // 辅助方法
    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String escapeCSV(String value) {
        if (value == null)
            return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}