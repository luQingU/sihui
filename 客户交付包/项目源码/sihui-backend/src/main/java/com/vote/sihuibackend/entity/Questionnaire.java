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
 * 问卷实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "questionnaires")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Questionnaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "问卷标题不能为空")
    @Size(max = 200, message = "问卷标题长度不能超过200个字符")
    @Column(nullable = false)
    private String title;

    @Size(max = 1000, message = "问卷描述长度不能超过1000个字符")
    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "questionnaire_type", nullable = false)
    private QuestionnaireType type = QuestionnaireType.SURVEY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionnaireStatus status = QuestionnaireStatus.DRAFT;

    @Size(max = 20, message = "版本号长度不能超过20个字符")
    @Column
    private String version = "1.0.0";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @JsonIgnoreProperties("questionnaires")
    private User createdBy;

    // 问卷设置（JSON格式存储）
    @Column(name = "settings", columnDefinition = "JSON")
    private String settings;

    // 发布URL
    @Size(max = 500, message = "发布URL长度不能超过500个字符")
    @Column(name = "publish_url")
    private String publishUrl;

    // 开始时间
    @Column(name = "start_time")
    private LocalDateTime startTime;

    // 结束时间
    @Column(name = "end_time")
    private LocalDateTime endTime;

    // 是否匿名
    @Column(name = "is_anonymous", nullable = false)
    private Boolean isAnonymous = false;

    // 是否允许多次提交
    @Column(name = "allow_multiple_submissions", nullable = false)
    private Boolean allowMultipleSubmissions = false;

    // 主题颜色
    @Size(max = 10, message = "主题颜色长度不能超过10个字符")
    @Column(name = "theme_color")
    private String themeColor = "#007bff";

    // 布局类型
    @Enumerated(EnumType.STRING)
    @Column(name = "layout_type")
    private LayoutType layout = LayoutType.SINGLE;

    // 是否需要密码访问
    @Column(name = "require_password", nullable = false)
    private Boolean requirePassword = false;

    // 访问密码
    @Size(max = 100, message = "访问密码长度不能超过100个字符")
    @Column(name = "access_password")
    private String accessPassword;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    // 问题列表
    @OneToMany(mappedBy = "questionnaire", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("questionnaire")
    private List<Question> questions;

    // 回答列表
    @OneToMany(mappedBy = "questionnaire", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("questionnaire")
    private List<QuestionnaireResponse> responses;

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
     * 问卷类型枚举
     */
    public enum QuestionnaireType {
        SURVEY("调查问卷"),
        FEEDBACK("反馈收集"),
        EVALUATION("评估测试"),
        REGISTRATION("报名登记");

        private final String displayName;

        QuestionnaireType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 问卷状态枚举
     */
    public enum QuestionnaireStatus {
        DRAFT("草稿"),
        SCHEDULED("定时发布"),
        PUBLISHED("已发布"),
        PAUSED("已暂停"),
        COMPLETED("已完成");

        private final String displayName;

        QuestionnaireStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 布局类型枚举
     */
    public enum LayoutType {
        SINGLE("单页显示"),
        PAGED("分页显示"),
        PROGRESSIVE("渐进式显示");

        private final String displayName;

        LayoutType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}