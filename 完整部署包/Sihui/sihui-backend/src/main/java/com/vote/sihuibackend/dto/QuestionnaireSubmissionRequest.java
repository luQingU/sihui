package com.vote.sihuibackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 问卷提交请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionnaireSubmissionRequest {

    @NotNull(message = "问卷ID不能为空")
    private Long questionnaireId;

    // 访问密码（如果需要）
    private String accessPassword;

    // 答案列表
    @Valid
    private List<AnswerRequest> answers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnswerRequest {
        @NotNull(message = "问题ID不能为空")
        private Long questionId;

        // 文本答案
        private String textAnswer;

        // 数字答案
        private String numberAnswer;

        // 日期答案
        private String dateAnswer;

        // 选择的选项IDs
        private List<Long> selectedOptions;

        // 文件URL（文件上传题）
        private String fileUrl;
    }
}