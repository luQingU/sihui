package com.vote.sihuibackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 问卷提交响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionnaireSubmissionResponse {

    private Long responseId;
    private Long questionnaireId;
    private String questionnaireTitle;
    private Boolean isCompleted;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer durationSeconds;
    private String message;
}