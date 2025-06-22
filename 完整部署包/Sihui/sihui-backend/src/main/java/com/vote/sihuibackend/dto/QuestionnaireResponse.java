package com.vote.sihuibackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 问卷响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionnaireResponse {

    private Long id;
    private String title;
    private String description;
    private String type;
    private String status;
    private String version;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean isAnonymous;
    private Boolean allowMultipleSubmissions;
    private String themeColor;
    private String layout;
    private Boolean requirePassword;
    private String publishUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;

    // 统计信息
    private Long questionCount;
    private Long responseCount;
    private Integer estimatedTime;

    // 用户是否已完成
    private Boolean userCompleted;

    // 问题列表
    private List<QuestionResponse> questions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionResponse {
        private Long id;
        private String type;
        private String title;
        private String description;
        private Boolean required;
        private Integer sortOrder;
        private String placeholder;
        private String ratingStyle;
        private List<String> fileTypes;
        private ValidationRulesDto validationRules;
        private List<QuestionOptionResponse> options;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class ValidationRulesDto {
            private Integer minLength;
            private Integer maxLength;
            private String pattern;
            private Integer min;
            private Integer max;
            private Integer step;
            private Integer decimals;
            private Integer maxSize;
            private Integer maxFiles;
            private Boolean strict;
            private String phoneFormat;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionOptionResponse {
        private Long id;
        private String text;
        private String image;
        private String value;
        private Integer sortOrder;
    }
}