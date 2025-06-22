package com.vote.sihuibackend.controller;

import com.vote.sihuibackend.entity.User;
import com.vote.sihuibackend.service.QuestionnaireReportService;
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
 * 问卷报表控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/questionnaires/{questionnaireId}/reports")
@RequiredArgsConstructor
public class QuestionnaireReportController {

    private final QuestionnaireReportService reportService;

    /**
     * 生成HTML报表
     */
    @GetMapping("/html")
    public ResponseEntity<String> generateHtmlReport(
            @PathVariable Long questionnaireId,
            @RequestParam(defaultValue = "detailed") String template,
            @AuthenticationPrincipal User currentUser) {

        log.info("User {} generating HTML report for questionnaire {} with template {}",
                currentUser.getUsername(), questionnaireId, template);

        try {
            String htmlReport = reportService.generateReportWithTemplate(questionnaireId, template, currentUser);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(htmlReport);
        } catch (Exception e) {
            log.error("Error generating HTML report", e);
            return ResponseEntity.badRequest()
                    .body("<html><body><h1>报表生成失败</h1><p>" + e.getMessage() + "</p></body></html>");
        }
    }

    /**
     * 生成PDF报表
     */
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> generatePdfReport(
            @PathVariable Long questionnaireId,
            @AuthenticationPrincipal User currentUser) {

        log.info("User {} generating PDF report for questionnaire {}",
                currentUser.getUsername(), questionnaireId);

        try {
            byte[] pdfData = reportService.generatePdfReport(questionnaireId, currentUser);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "questionnaire_" + questionnaireId + "_report.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfData);
        } catch (Exception e) {
            log.error("Error generating PDF report", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 生成Excel报表
     */
    @GetMapping("/excel")
    public ResponseEntity<byte[]> generateExcelReport(
            @PathVariable Long questionnaireId,
            @AuthenticationPrincipal User currentUser) {

        log.info("User {} generating Excel report for questionnaire {}",
                currentUser.getUsername(), questionnaireId);

        try {
            byte[] excelData = reportService.generateExcelReport(questionnaireId, currentUser);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(
                    MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "questionnaire_" + questionnaireId + "_data.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (Exception e) {
            log.error("Error generating Excel report", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 生成摘要报表
     */
    @GetMapping("/summary")
    public ResponseEntity<String> generateSummaryReport(
            @PathVariable Long questionnaireId,
            @AuthenticationPrincipal User currentUser) {

        log.info("User {} generating summary report for questionnaire {}",
                currentUser.getUsername(), questionnaireId);

        try {
            String summaryReport = reportService.generateSummaryReport(questionnaireId, currentUser);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(summaryReport);
        } catch (Exception e) {
            log.error("Error generating summary report", e);
            return ResponseEntity.badRequest()
                    .body("<html><body><h1>报表生成失败</h1><p>" + e.getMessage() + "</p></body></html>");
        }
    }

    /**
     * 生成对比报表
     */
    @PostMapping("/comparison")
    public ResponseEntity<String> generateComparisonReport(
            @RequestBody Long[] questionnaireIds,
            @AuthenticationPrincipal User currentUser) {

        log.info("User {} generating comparison report for questionnaires {}",
                currentUser.getUsername(), questionnaireIds);

        try {
            String comparisonReport = reportService.generateComparisonReport(questionnaireIds, currentUser);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(comparisonReport);
        } catch (Exception e) {
            log.error("Error generating comparison report", e);
            return ResponseEntity.badRequest()
                    .body("<html><body><h1>对比报表生成失败</h1><p>" + e.getMessage() + "</p></body></html>");
        }
    }

    /**
     * 获取可用的报表模板
     */
    @GetMapping("/templates")
    public ResponseEntity<Map<String, String>> getReportTemplates() {
        try {
            Map<String, String> templates = reportService.getReportTemplates();
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            log.error("Error getting report templates", e);
            return ResponseEntity.ok(Collections.emptyMap());
        }
    }

    /**
     * 预览报表
     */
    @GetMapping("/preview")
    public ResponseEntity<String> previewReport(
            @PathVariable Long questionnaireId,
            @RequestParam(defaultValue = "summary") String template,
            @AuthenticationPrincipal User currentUser) {

        log.info("User {} previewing report for questionnaire {} with template {}",
                currentUser.getUsername(), questionnaireId, template);

        try {
            String reportPreview = reportService.generateReportWithTemplate(questionnaireId, template, currentUser);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(reportPreview);
        } catch (Exception e) {
            log.error("Error generating report preview", e);
            return ResponseEntity.badRequest()
                    .body("<html><body><h1>报表预览失败</h1><p>" + e.getMessage() + "</p></body></html>");
        }
    }

    /**
     * 异常处理
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Invalid argument in report API: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(Collections.singletonMap("error", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericError(Exception e) {
        log.error("Unexpected error in report API", e);
        return ResponseEntity.internalServerError()
                .body(Collections.singletonMap("error", "服务器内部错误"));
    }
}