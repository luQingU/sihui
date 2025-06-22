package com.vote.sihuibackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 问题请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionRequest {

    @NotBlank(message = "问题标题不能为空")
    @Size(max = 500, message = "问题标题长度不能超过500个字符")
    private String title;

    @Size(max = 1000, message = "问题描述长度不能超过1000个字符")
    private String description;

    @NotNull(message = "问题类型不能为空")
    private String type;

    private Boolean required = false;

    private Integer sortOrder = 0;

    @Size(max = 200, message = "占位符文本长度不能超过200个字符")
    private String placeholder;

    private String ratingStyle;

    private List<String> fileTypes;

    private ValidationRulesDto validationRules;

    @Valid
    private List<QuestionOptionRequest> options;

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