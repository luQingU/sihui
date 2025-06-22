package com.vote.sihuibackend.dto;

import com.vote.sihuibackend.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import javax.validation.constraints.Pattern;
import java.util.Set;

/**
 * 用户更新请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {

    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(max = 20, message = "手机号长度不能超过20个字符")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Size(max = 100, message = "真实姓名长度不能超过100个字符")
    private String realName;

    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    private String avatarUrl;

    /**
     * 用户状态
     */
    private User.UserStatus status;

    /**
     * 邮箱验证状态
     */
    private Boolean emailVerified;

    /**
     * 手机验证状态
     */
    private Boolean phoneVerified;

    /**
     * 角色ID集合
     */
    private Set<Long> roleIds;
}