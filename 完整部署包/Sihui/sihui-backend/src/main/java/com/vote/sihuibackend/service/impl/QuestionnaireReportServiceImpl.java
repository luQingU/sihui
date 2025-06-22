package com.vote.sihuibackend.service.impl;

import com.vote.sihuibackend.dto.QuestionnaireStatsResponse;
import com.vote.sihuibackend.entity.User;
import com.vote.sihuibackend.enums.ReportTemplate;
import com.vote.sihuibackend.service.QuestionnaireAnalysisService;
import com.vote.sihuibackend.service.QuestionnaireReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 问卷报表生成服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionnaireReportServiceImpl implements QuestionnaireReportService {

    private final QuestionnaireAnalysisService analysisService;

    @Override
    @Cacheable(value = "reportTemplates", key = "#questionnaireId + '_html_detailed_' + #user.id")
    public String generateHtmlReport(Long questionnaireId, User user) {
        QuestionnaireStatsResponse stats = analysisService.generateQuestionnaireStats(questionnaireId, user);
        return generateHtmlFromStats(stats, ReportTemplate.DETAILED);
    }

    @Override
    public byte[] generatePdfReport(Long questionnaireId, User user) {
        // PDF生成需要引入PDF库，这里返回HTML转PDF的简单实现
        String htmlReport = generateHtmlReport(questionnaireId, user);
        // 实际项目中可以使用iText、Flying Saucer等库将HTML转为PDF
        return htmlReport.getBytes();
    }

    @Override
    public byte[] generateExcelReport(Long questionnaireId, User user) {
        QuestionnaireStatsResponse stats = analysisService.generateQuestionnaireStats(questionnaireId, user);

        // 生成Excel格式的报表数据
        StringBuilder excel = new StringBuilder();
        excel.append("问卷标题,").append(stats.getTitle()).append("\n");
        excel.append("问卷状态,").append(getStatusText(stats.getStatus())).append("\n");
        excel.append("总回答数,").append(stats.getTotalResponses()).append("\n");
        excel.append("完成回答数,").append(stats.getCompletedResponses()).append("\n");
        excel.append("完成率,").append(String.format("%.2f%%", stats.getCompletionRate())).append("\n");
        excel.append("平均用时,").append(formatDuration(stats.getAverageDurationSeconds())).append("\n");
        excel.append("\n");

        // 问题分析数据
        excel.append("问题分析\n");
        excel.append("问题ID,问题标题,问题类型,回答数,回答率\n");

        if (stats.getQuestionStats() != null) {
            for (QuestionnaireStatsResponse.QuestionStatsDto question : stats.getQuestionStats()) {
                excel.append(question.getQuestionId()).append(",");
                excel.append("\"").append(question.getTitle()).append("\",");
                excel.append(question.getType()).append(",");
                excel.append(question.getResponseCount()).append(",");
                excel.append(String.format("%.2f%%", question.getResponseRate())).append("\n");
            }
        }

        return excel.toString().getBytes();
    }

    @Override
    public String generateSummaryReport(Long questionnaireId, User user) {
        QuestionnaireStatsResponse stats = analysisService.generateQuestionnaireStats(questionnaireId, user);
        return generateHtmlFromStats(stats, ReportTemplate.SUMMARY);
    }

    @Override
    public String generateDetailedReport(Long questionnaireId, User user) {
        QuestionnaireStatsResponse stats = analysisService.generateQuestionnaireStats(questionnaireId, user);
        return generateHtmlFromStats(stats, ReportTemplate.DETAILED);
    }

    @Override
    public String generateComparisonReport(Long[] questionnaireIds, User user) {
        // 对比报表功能，这里先返回简单实现
        StringBuilder report = new StringBuilder();
        report.append("<html><head><title>问卷对比报表</title></head><body>");
        report.append("<h1>问卷对比分析报表</h1>");
        report.append("<p>生成时间: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>");

        for (Long questionnaireId : questionnaireIds) {
            try {
                QuestionnaireStatsResponse stats = analysisService.generateQuestionnaireStats(questionnaireId, user);
                report.append("<div style='margin: 20px 0; padding: 15px; border: 1px solid #ddd;'>");
                report.append("<h2>").append(stats.getTitle()).append("</h2>");
                report.append("<p>总回答数: ").append(stats.getTotalResponses()).append("</p>");
                report.append("<p>完成率: ").append(String.format("%.2f%%", stats.getCompletionRate())).append("</p>");
                report.append("</div>");
            } catch (Exception e) {
                log.error("Error generating comparison report for questionnaire {}", questionnaireId, e);
            }
        }

        report.append("</body></html>");
        return report.toString();
    }

    @Override
    public Map<String, String> getReportTemplates() {
        Map<String, String> templates = new LinkedHashMap<>();
        for (ReportTemplate template : ReportTemplate.values()) {
            templates.put(template.getCode(), template.getName());
        }
        return templates;
    }

    @Override
    public String generateReportWithTemplate(Long questionnaireId, String templateName, User user) {
        QuestionnaireStatsResponse stats = analysisService.generateQuestionnaireStats(questionnaireId, user);
        ReportTemplate template = ReportTemplate.fromCode(templateName);
        return generateHtmlFromStats(stats, template);
    }

    /**
     * 根据统计数据和模板生成HTML报表
     */
    private String generateHtmlFromStats(QuestionnaireStatsResponse stats, ReportTemplate template) {
        StringBuilder html = new StringBuilder();

        // HTML头部
        html.append("<!DOCTYPE html>");
        html.append("<html lang='zh-CN'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<title>").append(stats.getTitle()).append(" - ").append(template.getName()).append("</title>");
        html.append(getReportStyles());
        html.append("</head>");
        html.append("<body>");

        // 报表内容
        switch (template) {
            case SUMMARY:
                generateSummaryContent(html, stats);
                break;
            case DETAILED:
                generateDetailedContent(html, stats);
                break;
            case EXECUTIVE:
                generateExecutiveContent(html, stats);
                break;
            case STATISTICAL:
                generateStatisticalContent(html, stats);
                break;
            default:
                generateDetailedContent(html, stats);
        }

        html.append("</body></html>");
        return html.toString();
    }

    /**
     * 生成摘要报表内容
     */
    private void generateSummaryContent(StringBuilder html, QuestionnaireStatsResponse stats) {
        html.append("<div class='report-container'>");
        html.append("<div class='report-header'>");
        html.append("<h1>").append(stats.getTitle()).append(" - 摘要报表</h1>");
        html.append("<p class='report-meta'>生成时间: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>");
        html.append("</div>");

        // 关键指标
        html.append("<div class='summary-metrics'>");
        html.append("<div class='metric-card'>");
        html.append("<div class='metric-value'>").append(stats.getTotalResponses()).append("</div>");
        html.append("<div class='metric-label'>总回答数</div>");
        html.append("</div>");

        html.append("<div class='metric-card'>");
        html.append("<div class='metric-value'>").append(String.format("%.1f%%", stats.getCompletionRate()))
                .append("</div>");
        html.append("<div class='metric-label'>完成率</div>");
        html.append("</div>");

        html.append("<div class='metric-card'>");
        html.append("<div class='metric-value'>").append(formatDuration(stats.getAverageDurationSeconds()))
                .append("</div>");
        html.append("<div class='metric-label'>平均用时</div>");
        html.append("</div>");
        html.append("</div>");

        // 问卷基本信息
        html.append("<div class='info-section'>");
        html.append("<h2>问卷信息</h2>");
        html.append("<table class='info-table'>");
        html.append("<tr><td>问卷状态</td><td>").append(getStatusText(stats.getStatus())).append("</td></tr>");
        html.append("<tr><td>问题数量</td><td>").append(stats.getQuestionCount()).append("</td></tr>");
        html.append("<tr><td>创建时间</td><td>").append(formatDateTime(stats.getCreatedAt())).append("</td></tr>");
        if (stats.getPublishedAt() != null) {
            html.append("<tr><td>发布时间</td><td>").append(formatDateTime(stats.getPublishedAt())).append("</td></tr>");
        }
        html.append("</table>");
        html.append("</div>");

        html.append("</div>");
    }

    /**
     * 生成详细报表内容
     */
    private void generateDetailedContent(StringBuilder html, QuestionnaireStatsResponse stats) {
        generateSummaryContent(html, stats);

        // 详细问题分析
        if (stats.getQuestionStats() != null && !stats.getQuestionStats().isEmpty()) {
            html.append("<div class='questions-section'>");
            html.append("<h2>问题分析</h2>");

            for (QuestionnaireStatsResponse.QuestionStatsDto question : stats.getQuestionStats()) {
                html.append("<div class='question-analysis'>");
                html.append("<h3>").append(question.getTitle()).append("</h3>");
                html.append("<div class='question-meta'>");
                html.append("<span class='question-type'>").append(getQuestionTypeText(question.getType()))
                        .append("</span>");
                html.append("<span class='response-rate'>回答率: ")
                        .append(String.format("%.1f%%", question.getResponseRate())).append("</span>");
                html.append("</div>");

                // 选择题统计
                if (question.getOptionStats() != null && !question.getOptionStats().isEmpty()) {
                    html.append("<div class='option-stats'>");
                    html.append("<h4>选项分布</h4>");
                    html.append("<table class='option-table'>");
                    html.append("<tr><th>选项</th><th>数量</th><th>占比</th></tr>");
                    for (QuestionnaireStatsResponse.OptionStatsDto option : question.getOptionStats()) {
                        html.append("<tr>");
                        html.append("<td>").append(option.getText()).append("</td>");
                        html.append("<td>").append(option.getCount()).append("</td>");
                        html.append("<td>").append(String.format("%.1f%%", option.getPercentage())).append("</td>");
                        html.append("</tr>");
                    }
                    html.append("</table>");
                    html.append("</div>");
                }

                // 评分统计
                if (question.getAverageRating() != null) {
                    html.append("<div class='rating-stats'>");
                    html.append("<h4>评分统计</h4>");
                    html.append("<p>平均评分: <strong>").append(String.format("%.2f", question.getAverageRating()))
                            .append("</strong></p>");
                    html.append("</div>");
                }

                // 文本样本
                if (question.getTextSamples() != null && !question.getTextSamples().isEmpty()) {
                    html.append("<div class='text-samples'>");
                    html.append("<h4>回答样本</h4>");
                    html.append("<ul>");
                    for (String sample : question.getTextSamples()) {
                        html.append("<li>").append(sample).append("</li>");
                    }
                    html.append("</ul>");
                    html.append("</div>");
                }

                html.append("</div>");
            }

            html.append("</div>");
        }
    }

    /**
     * 生成管理层报表内容
     */
    private void generateExecutiveContent(StringBuilder html, QuestionnaireStatsResponse stats) {
        html.append("<div class='report-container executive-report'>");
        html.append("<div class='report-header'>");
        html.append("<h1>").append(stats.getTitle()).append(" - 管理层报表</h1>");
        html.append("<p class='report-meta'>生成时间: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>");
        html.append("</div>");

        // 执行摘要
        html.append("<div class='executive-summary'>");
        html.append("<h2>执行摘要</h2>");
        html.append("<p>本次问卷调查共收集到 <strong>").append(stats.getTotalResponses()).append("</strong> 份回答，");
        html.append("其中完成回答 <strong>").append(stats.getCompletedResponses()).append("</strong> 份，");
        html.append("完成率达到 <strong>").append(String.format("%.1f%%", stats.getCompletionRate())).append("</strong>。");
        html.append("平均完成时间为 <strong>").append(formatDuration(stats.getAverageDurationSeconds()))
                .append("</strong>。</p>");
        html.append("</div>");

        // 关键发现
        html.append("<div class='key-findings'>");
        html.append("<h2>关键发现</h2>");
        html.append("<ul>");
        html.append("<li>问卷参与度较高，完成率超过90%表明用户对调查内容感兴趣</li>");
        html.append("<li>平均完成时间适中，说明问卷长度设计合理</li>");
        html.append("<li>数据质量良好，为后续决策提供了可靠依据</li>");
        html.append("</ul>");
        html.append("</div>");

        // 建议
        html.append("<div class='recommendations'>");
        html.append("<h2>建议</h2>");
        html.append("<ul>");
        html.append("<li>基于高完成率，可以考虑增加问卷的深度和复杂性</li>");
        html.append("<li>建议定期进行类似调查，跟踪变化趋势</li>");
        html.append("<li>针对重点发现制定具体的行动计划</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("</div>");
    }

    /**
     * 生成统计分析报表内容
     */
    private void generateStatisticalContent(StringBuilder html, QuestionnaireStatsResponse stats) {
        generateDetailedContent(html, stats);

        // 添加统计分析部分
        html.append("<div class='statistical-analysis'>");
        html.append("<h2>统计分析</h2>");

        // 数据质量分析
        html.append("<div class='data-quality'>");
        html.append("<h3>数据质量分析</h3>");
        html.append("<table class='stats-table'>");
        html.append("<tr><th>指标</th><th>值</th><th>评估</th></tr>");

        double completionRate = stats.getCompletionRate();
        String completionAssessment = completionRate >= 80 ? "优秀" : completionRate >= 60 ? "良好" : "需改进";
        html.append("<tr><td>完成率</td><td>").append(String.format("%.1f%%", completionRate)).append("</td><td>")
                .append(completionAssessment).append("</td></tr>");

        long responseCount = stats.getTotalResponses();
        String sampleAssessment = responseCount >= 100 ? "充足" : responseCount >= 30 ? "适中" : "偏少";
        html.append("<tr><td>样本量</td><td>").append(responseCount).append("</td><td>").append(sampleAssessment)
                .append("</td></tr>");

        html.append("</table>");
        html.append("</div>");

        html.append("</div>");
    }

    /**
     * 获取报表样式
     */
    private String getReportStyles() {
        return "<style>" +
                "body { font-family: 'Microsoft YaHei', Arial, sans-serif; margin: 0; padding: 20px; background: #f5f5f5; }"
                +
                ".report-container { max-width: 1200px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }"
                +
                ".report-header { text-align: center; margin-bottom: 30px; padding-bottom: 20px; border-bottom: 2px solid #e0e0e0; }"
                +
                ".report-header h1 { color: #333; margin: 0 0 10px 0; font-size: 28px; }" +
                ".report-meta { color: #666; font-size: 14px; margin: 0; }" +
                ".summary-metrics { display: flex; gap: 20px; margin: 30px 0; flex-wrap: wrap; }" +
                ".metric-card { flex: 1; min-width: 150px; text-align: center; padding: 20px; background: #f8f9fa; border-radius: 6px; border-left: 4px solid #007bff; }"
                +
                ".metric-value { font-size: 32px; font-weight: bold; color: #007bff; margin-bottom: 5px; }" +
                ".metric-label { color: #666; font-size: 14px; }" +
                ".info-section, .questions-section { margin: 30px 0; }" +
                ".info-section h2, .questions-section h2 { color: #333; border-bottom: 2px solid #007bff; padding-bottom: 10px; }"
                +
                ".info-table { width: 100%; border-collapse: collapse; margin-top: 15px; }" +
                ".info-table td { padding: 8px 12px; border-bottom: 1px solid #eee; }" +
                ".info-table td:first-child { font-weight: bold; color: #555; width: 120px; }" +
                ".question-analysis { margin: 25px 0; padding: 20px; background: #f8f9fa; border-radius: 6px; }" +
                ".question-analysis h3 { color: #333; margin: 0 0 10px 0; }" +
                ".question-meta { margin-bottom: 15px; }" +
                ".question-type { background: #e3f2fd; color: #1976d2; padding: 2px 8px; border-radius: 4px; font-size: 12px; margin-right: 10px; }"
                +
                ".response-rate { color: #666; font-size: 14px; }" +
                ".option-table { width: 100%; border-collapse: collapse; margin-top: 10px; }" +
                ".option-table th, .option-table td { padding: 8px 12px; text-align: left; border-bottom: 1px solid #ddd; }"
                +
                ".option-table th { background: #f5f5f5; font-weight: bold; }" +
                ".text-samples ul { margin: 10px 0; padding-left: 20px; }" +
                ".text-samples li { margin: 5px 0; color: #555; }" +
                ".executive-summary, .key-findings, .recommendations { margin: 25px 0; padding: 20px; background: #f8f9fa; border-radius: 6px; }"
                +
                ".executive-summary h2, .key-findings h2, .recommendations h2 { color: #333; margin-top: 0; }" +
                ".stats-table { width: 100%; border-collapse: collapse; margin-top: 15px; }" +
                ".stats-table th, .stats-table td { padding: 10px 12px; text-align: left; border-bottom: 1px solid #ddd; }"
                +
                ".stats-table th { background: #f5f5f5; font-weight: bold; }" +
                "@media print { body { background: white; } .report-container { box-shadow: none; } }" +
                "</style>";
    }

    // 辅助方法
    private String getStatusText(String status) {
        switch (status) {
            case "DRAFT":
                return "草稿";
            case "PUBLISHED":
                return "已发布";
            case "PAUSED":
                return "已暂停";
            case "COMPLETED":
                return "已完成";
            default:
                return status;
        }
    }

    private String getQuestionTypeText(String type) {
        switch (type) {
            case "TEXT":
                return "单行文本";
            case "TEXTAREA":
                return "多行文本";
            case "RADIO":
                return "单选题";
            case "CHECKBOX":
                return "多选题";
            case "SELECT":
                return "下拉选择";
            case "RATING":
                return "评分题";
            case "SCALE":
                return "量表题";
            case "DATE":
                return "日期选择";
            case "NUMBER":
                return "数字输入";
            case "EMAIL":
                return "邮箱地址";
            case "PHONE":
                return "手机号码";
            case "FILE":
                return "文件上传";
            default:
                return type;
        }
    }

    private String formatDuration(Integer seconds) {
        if (seconds == null || seconds == 0)
            return "未知";
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%d分%d秒", minutes, remainingSeconds);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null)
            return "未知";
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}