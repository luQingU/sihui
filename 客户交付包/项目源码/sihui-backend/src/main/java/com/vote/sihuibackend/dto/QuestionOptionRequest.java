package com.vote.sihuibackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 问题选项请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionOptionRequest {

    @NotBlank(message = "选项文本不能为空")
    @Size(max = 500, message = "选项文本长度不能超过500个字符")
    private String text;

    @Size(max = 500, message = "选项图片URL长度不能超过500个字符")
    private String image;

    @Size(max = 100, message = "选项值长度不能超过100个字符")
    private String value;

    private Integer sortOrder = 0;
}