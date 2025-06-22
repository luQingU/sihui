package com.vote.sihuibackend.controller;

import com.vote.sihuibackend.dto.QuestionnaireResponse;
import com.vote.sihuibackend.dto.QuestionnaireSubmissionRequest;
import com.vote.sihuibackend.dto.QuestionnaireSubmissionResponse;
import com.vote.sihuibackend.entity.User;
import com.vote.sihuibackend.service.QuestionnaireService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 公开问卷访问控制器
 * 用于小程序端和匿名用户访问问卷
 */
@Slf4j
@RestController
@RequestMapping("/api/public/questionnaires")
@RequiredArgsConstructor
public class PublicQuestionnaireController {

    private final QuestionnaireService questionnaireService;

    /**
     * 获取可用的问卷列表（小程序端）
     */
    @GetMapping
    public ResponseEntity<List<QuestionnaireResponse>> getAvailableQuestionnaires(
            @AuthenticationPrincipal User currentUser) {

        List<QuestionnaireResponse> questionnaires = questionnaireService.getAvailableQuestionnaires(currentUser);
        return ResponseEntity.ok(questionnaires);
    }

    /**
     * 获取问卷详情（小程序端）
     */
    @GetMapping("/{id}")
    public ResponseEntity<QuestionnaireResponse> getQuestionnaire(@PathVariable Long id) {
        return questionnaireService.getQuestionnaireById(id)
                .map(questionnaire -> ResponseEntity.ok(questionnaire))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 提交问卷答案（小程序端）
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<QuestionnaireSubmissionResponse> submitQuestionnaire(
            @PathVariable Long id,
            @Valid @RequestBody QuestionnaireSubmissionRequest request,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest httpRequest) {

        // 确保请求中的问卷ID与路径参数一致
        request.setQuestionnaireId(id);

        String userAgent = httpRequest.getHeader("User-Agent");
        String ipAddress = getClientIpAddress(httpRequest);

        log.info("Public submission for questionnaire {} from user {}",
                id, currentUser != null ? currentUser.getUsername() : "anonymous");

        QuestionnaireSubmissionResponse response = questionnaireService.submitQuestionnaire(
                request, currentUser, userAgent, ipAddress);

        return ResponseEntity.ok(response);
    }

    /**
     * 验证问卷访问权限（小程序端）
     */
    @PostMapping("/{id}/validate-access")
    public ResponseEntity<Map<String, Boolean>> validateAccess(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request) {

        String accessPassword = request != null ? request.get("accessPassword") : null;
        boolean isValid = questionnaireService.validateQuestionnaireAccess(id, accessPassword);

        return ResponseEntity.ok(Collections.singletonMap("valid", isValid));
    }

    /**
     * 检查用户是否已完成问卷（小程序端）
     */
    @GetMapping("/{id}/completion-status")
    public ResponseEntity<Map<String, Boolean>> getCompletionStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        boolean completed = questionnaireService.hasUserCompletedQuestionnaire(id, currentUser);
        return ResponseEntity.ok(Collections.singletonMap("completed", completed));
    }

    /**
     * 异常处理
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Invalid argument in public API: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(Collections.singletonMap("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException e) {
        log.warn("Invalid state in public API: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(Collections.singletonMap("error", e.getMessage()));
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}