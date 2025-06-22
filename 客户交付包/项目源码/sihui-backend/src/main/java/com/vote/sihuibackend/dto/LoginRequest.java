package com.vote.sihuibackend.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 登录请求 DTO
 */
@Data
public class LoginRequest {

    @NotBlank(message = "用户名或邮箱不能为空")
    @Size(min = 3, max = 50, message = "用户名或邮箱长度必须在3-50个字符之间")
    private String usernameOrEmail;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    private String password;
}