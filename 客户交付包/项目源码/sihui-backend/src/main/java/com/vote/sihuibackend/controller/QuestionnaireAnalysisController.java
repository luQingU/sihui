package com.vote.sihuibackend.controller;

import com.vote.sihuibackend.dto.QuestionnaireStatsResponse;
import com.vote.sihuibackend.entity.User;
import com.vote.sihuibackend.service.QuestionnaireAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

/**
 * 问卷数据分析控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/questionnaires/{questionnaireId}/analysis")
@RequiredArgsConstructor
public class QuestionnaireAnalysisController {

    private final QuestionnaireAnalysisService analysisService;

    /**
     * 获取问卷统计报告
     */
    @GetMapping("/stats")
    public ResponseEntity<QuestionnaireStatsResponse> getQuestionnaireStats(
            @PathVariable Long questionnaireId,
            @AuthenticationPrincipal User currentUser) {

        log.info("User {} requesting stats for questionnaire {}",
                currentUser.getUsername(), questionnaireId);

        QuestionnaireStatsResponse stats = analysisService.generateQuestionnaireStats(questionnaireId, currentUser);
        return ResponseEntity.ok(stats);
    }

    /**
     * 获取问卷回答时间分布
     */
    @GetMapping("/time-distribution")
    public ResponseEntity<Map<String, Long>> getResponseTimeDistribution(
            @PathVariable Long questionnaireId,
            @AuthenticationPrincipal User currentUser) {

        log.info("User {} requesting time distribution for questionnaire {}",
                currentUser.getUsername(), questionnaireId);

        // 暂时返回空Map，需要完善实现
        return ResponseEntity.ok(Collections.emptyMap());
    }

    /**
     * 导出问卷数据为CSV
     */
    @GetMapping("/export/csv")
    public ResponseEntity<String> exportToCsv(
            @PathVariable Long questionnaireId,
            @AuthenticationPrincipal User currentUser) {

        log.info("User {} exporting CSV for questionnaire {}",
                currentUser.getUsername(), questionnaireId);

        try {
            String csvData = analysisService.exportQuestionnaireDataToCsv(questionnaireId, currentUser);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "questionnaire_" + questionnaireId + "_data.csv");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvData);
        } catch (Exception e) {
            log.error("Error exporting CSV", e);
            return ResponseEntity.badRequest()
                    .body("导出失败: " + e.getMessage());
        }
    }

    /**
     * 导出统计报告为PDF
     */
    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportToPdf(
            @PathVariable Long questionnaireId,
            @AuthenticationPrincipal User currentUser) {

        log.info("User {} exporting PDF for questionnaire {}",
                currentUser.getUsername(), questionnaireId);

        try {
            byte[] pdfData = analysisService.exportStatisticsReportToPdf(questionnaireId, currentUser);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "questionnaire_" + questionnaireId + "_report.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfData);
        } catch (UnsupportedOperationException e) {
            log.warn("PDF export not implemented yet");
            return ResponseEntity.status(501).build(); // Not Implemented
        } catch (Exception e) {
            log.error("Error exporting PDF", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 异常处理
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Invalid argument in analysis API: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(Collections.singletonMap("error", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericError(Exception e) {
        log.error("Unexpected error in analysis API", e);
        return ResponseEntity.internalServerError()
                .body(Collections.singletonMap("error", "服务器内部错误"));
    }
}