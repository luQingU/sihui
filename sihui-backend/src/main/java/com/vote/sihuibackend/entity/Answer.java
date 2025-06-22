package com.vote.sihuibackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 回答实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "answers")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id", nullable = false)
    @JsonIgnoreProperties("answers")
    private QuestionnaireResponse response;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @JsonIgnoreProperties("answers")
    private Question question;

    // 文本答案
    @Column(name = "text_answer", columnDefinition = "TEXT")
    private String textAnswer;

    // 数字答案
    @Column(name = "number_answer")
    private BigDecimal numberAnswer;

    // 日期答案
    @Column(name = "date_answer")
    private LocalDateTime dateAnswer;

    // 选择的选项IDs（JSON格式存储，支持多选）
    @Column(name = "selected_options", columnDefinition = "JSON")
    private String selectedOptions;

    // 文件路径（文件上传题）
    @Column(name = "file_path")
    private String filePath;

    // 文件URL（文件上传题）
    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}