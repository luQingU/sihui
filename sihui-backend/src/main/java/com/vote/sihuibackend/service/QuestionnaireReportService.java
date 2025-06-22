package com.vote.sihuibackend.service;

import com.vote.sihuibackend.entity.User;

import java.util.Map;

/**
 * 问卷报表生成服务接口
 */
public interface QuestionnaireReportService {

    /**
     * 生成问卷统计报表（HTML格式）
     */
    String generateHtmlReport(Long questionnaireId, User user);

    /**
     * 生成问卷统计报表（PDF格式）
     */
    byte[] generatePdfReport(Long questionnaireId, User user);

    /**
     * 生成Excel数据报表
     */
    byte[] generateExcelReport(Long questionnaireId, User user);

    /**
     * 生成问卷摘要报表
     */
    String generateSummaryReport(Long questionnaireId, User user);

    /**
     * 生成详细数据报表
     */
    String generateDetailedReport(Long questionnaireId, User user);

    /**
     * 生成对比报表（多个问卷对比）
     */
    String generateComparisonReport(Long[] questionnaireIds, User user);

    /**
     * 获取报表模板列表
     */
    Map<String, String> getReportTemplates();

    /**
     * 使用指定模板生成报表
     */
    String generateReportWithTemplate(Long questionnaireId, String templateName, User user);
}