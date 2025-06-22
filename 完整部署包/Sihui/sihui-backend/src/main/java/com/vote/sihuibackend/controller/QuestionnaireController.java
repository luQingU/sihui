package com.vote.sihuibackend.controller;

import com.vote.sihuibackend.dto.*;
import com.vote.sihuibackend.entity.User;
import com.vote.sihuibackend.service.QuestionnaireService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 问卷控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/questionnaires")
@RequiredArgsConstructor
@Validated
public class QuestionnaireController {

    private final QuestionnaireService questionnaireService;

    /**
     * 创建问卷
     */
    @PostMapping
    public ResponseEntity<QuestionnaireResponse> createQuestionnaire(
            @Valid @RequestBody QuestionnaireRequest request,
            @AuthenticationPrincipal User currentUser) {

        log.info("User {} creating questionnaire: {}", currentUser.getUsername(), request.getTitle());

        QuestionnaireResponse response = questionnaireService.createQuestionnaire(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 获取问卷详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<QuestionnaireResponse> getQuestionnaire(@PathVariable Long id) {
        return questionnaireService.getQuestionnaireById(id)
                .map(questionnaire -> ResponseEntity.ok(questionnaire))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 获取用户可参与的问卷列表
     */
    @GetMapping("/available")
    public ResponseEntity<List<QuestionnaireResponse>> getAvailableQuestionnaires(
            @AuthenticationPrincipal User currentUser) {

        List<QuestionnaireResponse> questionnaires = questionnaireService.getAvailableQuestionnaires(currentUser);
        return ResponseEntity.ok(questionnaires);
    }

    /**
     * 获取用户创建的问卷列表
     */
    @GetMapping("/my")
    public ResponseEntity<Page<QuestionnaireResponse>> getUserQuestionnaires(
            @AuthenticationPrincipal User currentUser,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<QuestionnaireResponse> questionnaires = questionnaireService.getUserQuestionnaires(currentUser, pageable);
        return ResponseEntity.ok(questionnaires);
    }

    /**
     * 发布问卷
     */
    @PostMapping("/{id}/publish")
    public ResponseEntity<QuestionnaireResponse> publishQuestionnaire(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        log.info("User {} publishing questionnaire {}", currentUser.getUsername(), id);

        QuestionnaireResponse response = questionnaireService.publishQuestionnaire(id, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * 提交问卷答案
     */
    @PostMapping("/submit")
    public ResponseEntity<QuestionnaireSubmissionResponse> submitQuestionnaire(
            @Valid @RequestBody QuestionnaireSubmissionRequest request,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest httpRequest) {

        String userAgent = httpRequest.getHeader("User-Agent");
        String ipAddress = getClientIpAddress(httpRequest);

        log.info("User {} submitting questionnaire {}",
                currentUser != null ? currentUser.getUsername() : "anonymous",
                request.getQuestionnaireId());

        QuestionnaireSubmissionResponse response = questionnaireService.submitQuestionnaire(
                request, currentUser, userAgent, ipAddress);

        return ResponseEntity.ok(response);
    }

    /**
     * 验证问卷访问权限
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
     * 检查用户是否已完成问卷
     */
    @GetMapping("/{id}/completion-status")
    public ResponseEntity<Map<String, Boolean>> getCompletionStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        boolean completed = questionnaireService.hasUserCompletedQuestionnaire(id, currentUser);
        return ResponseEntity.ok(Collections.singletonMap("completed", completed));
    }

    /**
     * 获取问卷统计信息
     */
    @GetMapping("/{id}/stats")
    public ResponseEntity<QuestionnaireStatsResponse> getQuestionnaireStats(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        QuestionnaireStatsResponse stats = questionnaireService.getQuestionnaireStats(id, currentUser);
        return ResponseEntity.ok(stats);
    }

    /**
     * 异常处理
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Invalid argument: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(Collections.singletonMap("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException e) {
        log.warn("Invalid state: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(Collections.singletonMap("error", e.getMessage()));
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<Map<String, String>> handleUnsupportedOperation(UnsupportedOperationException e) {
        log.warn("Unsupported operation: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Collections.singletonMap("error", "功能暂未实现"));
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