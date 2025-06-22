package com.vote.sihuibackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 问卷回答实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "questionnaire_responses")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class QuestionnaireResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questionnaire_id", nullable = false)
    @JsonIgnoreProperties("responses")
    private Questionnaire questionnaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties("responses")
    private User user;

    // 回答者IP地址
    @Column(name = "respondent_ip", length = 50)
    private String respondentIp;

    // 用户代理
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    // 是否完成
    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    // 开始时间
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    // 完成时间
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // 用时（秒）
    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 具体回答列表
    @OneToMany(mappedBy = "response", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("response")
    private List<Answer> answers;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}