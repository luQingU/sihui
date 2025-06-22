package com.vote.sihuibackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 问卷统计响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionnaireStatsResponse {

    private Long questionnaireId;
    private String title;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;

    // 基础统计
    private Long totalResponses;
    private Long completedResponses;
    private Long questionCount;
    private Double completionRate;
    private Integer averageDurationSeconds;

    // 时间统计
    private LocalDateTime firstResponseAt;
    private LocalDateTime lastResponseAt;

    // 问题统计
    private List<QuestionStatsDto> questionStats;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionStatsDto {
        private Long questionId;
        private String title;
        private String type;
        private Boolean required;
        private Long responseCount;
        private Double responseRate;

        // 选择题统计
        private List<OptionStatsDto> optionStats;

        // 评分题统计
        private Double averageRating;
        private Map<Integer, Long> ratingDistribution;

        // 文本题样本
        private List<String> textSamples;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OptionStatsDto {
        private Long optionId;
        private String text;
        private Long count;
        private Double percentage;
    }
}