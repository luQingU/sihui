package com.vote.sihuibackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 问题实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "questions")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questionnaire_id", nullable = false)
    @JsonIgnoreProperties("questions")
    private Questionnaire questionnaire;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType type;

    @NotBlank(message = "问题标题不能为空")
    @Size(max = 500, message = "问题标题长度不能超过500个字符")
    @Column(nullable = false)
    private String title;

    @Size(max = 1000, message = "问题描述长度不能超过1000个字符")
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean required = false;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    // 占位符文本
    @Size(max = 200, message = "占位符文本长度不能超过200个字符")
    @Column
    private String placeholder;

    // 评分样式（仅评分题使用）
    @Enumerated(EnumType.STRING)
    @Column(name = "rating_style")
    private RatingStyle ratingStyle;

    // 文件类型（仅文件上传题使用）
    @Column(name = "file_types", columnDefinition = "JSON")
    private String fileTypes;

    // 验证规则（JSON格式存储）
    @Column(name = "validation_rules", columnDefinition = "JSON")
    private String validationRules;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 选项列表
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("question")
    private List<QuestionOption> options;

    // 回答列表
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("question")
    private List<Answer> answers;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 问题类型枚举
     */
    public enum QuestionType {
        TEXT("单行文本"),
        TEXTAREA("多行文本"),
        RADIO("单选题"),
        CHECKBOX("多选题"),
        SELECT("下拉选择"),
        RATING("评分题"),
        SCALE("量表题"),
        DATE("日期选择"),
        NUMBER("数字输入"),
        EMAIL("邮箱地址"),
        PHONE("手机号码"),
        FILE("文件上传");

        private final String displayName;

        QuestionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 评分样式枚举
     */
    public enum RatingStyle {
        STAR("星级"),
        NUMBER("数字"),
        EMOJI("表情");

        private final String displayName;

        RatingStyle(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}