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

/**
 * 问题选项实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "question_options")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class QuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @JsonIgnoreProperties("options")
    private Question question;

    @NotBlank(message = "选项文本不能为空")
    @Size(max = 500, message = "选项文本长度不能超过500个字符")
    @Column(name = "option_text", nullable = false)
    private String text;

    @Size(max = 500, message = "选项图片URL长度不能超过500个字符")
    @Column(name = "option_image")
    private String image;

    @Size(max = 100, message = "选项值长度不能超过100个字符")
    @Column(name = "option_value")
    private String value;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

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