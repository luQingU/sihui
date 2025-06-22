package com.vote.sihuibackend.service;

import com.vote.sihuibackend.dto.QuestionnaireStatsResponse;
import com.vote.sihuibackend.entity.Question;
import com.vote.sihuibackend.entity.Questionnaire;
import com.vote.sihuibackend.entity.User;

import java.util.List;
import java.util.Map;

/**
 * 问卷数据分析服务接口
 */
public interface QuestionnaireAnalysisService {

    /**
     * 生成问卷统计报告
     */
    QuestionnaireStatsResponse generateQuestionnaireStats(Long questionnaireId, User user);

    /**
     * 分析单个问题的统计数据
     */
    QuestionnaireStatsResponse.QuestionStatsDto analyzeQuestionStats(Question question);

    /**
     * 分析选择题选项统计
     */
    List<QuestionnaireStatsResponse.OptionStatsDto> analyzeOptionStats(Question question);

    /**
     * 计算评分题平均分和分布
     */
    Map<String, Object> analyzeRatingStats(Question question);

    /**
     * 获取文本题答案样本
     */
    List<String> getTextAnswerSamples(Question question, int maxSamples);

    /**
     * 计算问卷完成率
     */
    double calculateCompletionRate(Questionnaire questionnaire);

    /**
     * 计算平均答题时长
     */
    int calculateAverageDuration(Questionnaire questionnaire);

    /**
     * 获取问卷回答时间分布
     */
    Map<String, Long> getResponseTimeDistribution(Questionnaire questionnaire);

    /**
     * 导出问卷数据为CSV
     */
    String exportQuestionnaireDataToCsv(Long questionnaireId, User user);

    /**
     * 导出问卷统计报告为PDF
     */
    byte[] exportStatisticsReportToPdf(Long questionnaireId, User user);
}