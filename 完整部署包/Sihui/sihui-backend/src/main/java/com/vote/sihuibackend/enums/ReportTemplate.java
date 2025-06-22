package com.vote.sihuibackend.enums;

/**
 * 报表模板枚举
 */
public enum ReportTemplate {

    SUMMARY("summary", "摘要报表", "简洁的问卷统计摘要"),
    DETAILED("detailed", "详细报表", "包含所有问题分析的详细报表"),
    EXECUTIVE("executive", "管理层报表", "面向管理层的高层次分析报表"),
    STATISTICAL("statistical", "统计分析报表", "深度统计分析和数据挖掘报表"),
    COMPARISON("comparison", "对比分析报表", "多个问卷或时间段的对比分析"),
    TREND("trend", "趋势分析报表", "时间序列和趋势分析报表");

    private final String code;
    private final String name;
    private final String description;

    ReportTemplate(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public static ReportTemplate fromCode(String code) {
        for (ReportTemplate template : values()) {
            if (template.code.equals(code)) {
                return template;
            }
        }
        throw new IllegalArgumentException("Unknown report template code: " + code);
    }
}