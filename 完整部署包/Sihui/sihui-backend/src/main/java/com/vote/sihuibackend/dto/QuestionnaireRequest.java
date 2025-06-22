package com.vote.sihuibackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 问卷请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionnaireRequest {

    @NotBlank(message = "问卷标题不能为空")
    @Size(max = 200, message = "问卷标题长度不能超过200个字符")
    private String title;

    @Size(max = 1000, message = "问卷描述长度不能超过1000个字符")
    private String description;

    @NotNull(message = "问卷类型不能为空")
    private String type;

    @Size(max = 20, message = "版本号长度不能超过20个字符")
    private String version;

    // 问卷设置
    private QuestionnaireSettingsDto settings;

    // 开始时间
    private LocalDateTime startTime;

    // 结束时间
    private LocalDateTime endTime;

    // 问题列表
    @Valid
    private List<QuestionRequest> questions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionnaireSettingsDto {
        private Boolean isAnonymous = false;
        private Boolean allowMultipleSubmissions = false;
        private String themeColor = "#007bff";
        private String layout = "single";
        private Boolean requirePassword = false;
        private String accessPassword;
    }
}