package com.vote.sihuibackend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vote.sihuibackend.dto.*;
import com.vote.sihuibackend.entity.*;
import com.vote.sihuibackend.repository.*;
import com.vote.sihuibackend.service.QuestionnaireService;
import com.vote.sihuibackend.service.QuestionnaireAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 问卷服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionnaireServiceImpl implements QuestionnaireService {

    private final QuestionnaireRepository questionnaireRepository;
    private final QuestionRepository questionRepository;
    private final QuestionnaireResponseRepository responseRepository;
    private final QuestionnaireAnalysisService questionnaireAnalysisService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public com.vote.sihuibackend.dto.QuestionnaireResponse createQuestionnaire(QuestionnaireRequest request,
            User creator) {
        try {
            Questionnaire questionnaire = Questionnaire.builder()
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .type(Questionnaire.QuestionnaireType.valueOf(request.getType().toUpperCase()))
                    .version(request.getVersion() != null ? request.getVersion() : "1.0.0")
                    .createdBy(creator)
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .build();

            // 设置问卷配置
            if (request.getSettings() != null) {
                questionnaire.setIsAnonymous(request.getSettings().getIsAnonymous());
                questionnaire.setAllowMultipleSubmissions(request.getSettings().getAllowMultipleSubmissions());
                questionnaire.setThemeColor(request.getSettings().getThemeColor());
                questionnaire
                        .setLayout(Questionnaire.LayoutType.valueOf(request.getSettings().getLayout().toUpperCase()));
                questionnaire.setRequirePassword(request.getSettings().getRequirePassword());
                questionnaire.setAccessPassword(request.getSettings().getAccessPassword());

                // 将设置序列化为JSON
                String settingsJson = objectMapper.writeValueAsString(request.getSettings());
                questionnaire.setSettings(settingsJson);
            }

            questionnaire = questionnaireRepository.save(questionnaire);

            // 创建问题
            if (request.getQuestions() != null) {
                createQuestions(questionnaire, request.getQuestions());
            }

            return convertToResponse(questionnaire, null);
        } catch (JsonProcessingException e) {
            log.error("Error serializing questionnaire settings", e);
            throw new RuntimeException("创建问卷失败", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<com.vote.sihuibackend.dto.QuestionnaireResponse> getQuestionnaireById(Long id) {
        return questionnaireRepository.findById(id)
                .map(questionnaire -> convertToResponse(questionnaire, null));
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.vote.sihuibackend.dto.QuestionnaireResponse> getAvailableQuestionnaires(User user) {
        LocalDateTime now = LocalDateTime.now();
        List<Questionnaire> questionnaires = questionnaireRepository.findAvailableForUser(now);

        return questionnaires.stream()
                .map(q -> convertToResponse(q, user))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<com.vote.sihuibackend.dto.QuestionnaireResponse> getUserQuestionnaires(User user, Pageable pageable) {
        Page<Questionnaire> questionnaires = questionnaireRepository.findByCreatedBy(user, pageable);
        return questionnaires.map(q -> convertToResponse(q, user));
    }

    @Override
    @Transactional
    public com.vote.sihuibackend.dto.QuestionnaireResponse publishQuestionnaire(Long id, User user) {
        Questionnaire questionnaire = getQuestionnaireByIdAndUser(id, user);

        if (questionnaire.getStatus() != Questionnaire.QuestionnaireStatus.DRAFT) {
            throw new IllegalStateException("只有草稿状态的问卷才能发布");
        }

        questionnaire.setStatus(Questionnaire.QuestionnaireStatus.PUBLISHED);
        questionnaire.setPublishedAt(LocalDateTime.now());

        // 生成发布URL
        questionnaire.setPublishUrl("/questionnaire/public/" + questionnaire.getId());

        questionnaire = questionnaireRepository.save(questionnaire);
        return convertToResponse(questionnaire, user);
    }

    @Override
    @Transactional
    public QuestionnaireSubmissionResponse submitQuestionnaire(
            QuestionnaireSubmissionRequest request, User user, String userAgent, String ipAddress) {

        Questionnaire questionnaire = questionnaireRepository.findById(request.getQuestionnaireId())
                .orElseThrow(() -> new IllegalArgumentException("问卷不存在"));

        // 验证问卷状态
        if (questionnaire.getStatus() != Questionnaire.QuestionnaireStatus.PUBLISHED) {
            throw new IllegalStateException("问卷未发布或已关闭");
        }

        // 验证时间范围
        LocalDateTime now = LocalDateTime.now();
        if (questionnaire.getStartTime() != null && now.isBefore(questionnaire.getStartTime())) {
            throw new IllegalStateException("问卷尚未开始");
        }
        if (questionnaire.getEndTime() != null && now.isAfter(questionnaire.getEndTime())) {
            throw new IllegalStateException("问卷已结束");
        }

        // 验证访问密码
        if (questionnaire.getRequirePassword() &&
                !Objects.equals(questionnaire.getAccessPassword(), request.getAccessPassword())) {
            throw new IllegalArgumentException("访问密码错误");
        }

        // 检查是否允许多次提交
        if (!questionnaire.getAllowMultipleSubmissions() && user != null) {
            boolean hasCompleted = hasUserCompletedQuestionnaire(questionnaire.getId(), user);
            if (hasCompleted) {
                throw new IllegalStateException("您已经完成过此问卷");
            }
        }

        // 创建回答记录
        com.vote.sihuibackend.entity.QuestionnaireResponse response = com.vote.sihuibackend.entity.QuestionnaireResponse
                .builder()
                .questionnaire(questionnaire)
                .user(user)
                .respondentIp(ipAddress)
                .userAgent(userAgent)
                .isCompleted(true)
                .startedAt(now)
                .completedAt(now)
                .build();

        response = responseRepository.save(response);

        // 保存具体答案
        saveAnswers(response, request.getAnswers());

        return QuestionnaireSubmissionResponse.builder()
                .responseId(response.getId())
                .questionnaireId(questionnaire.getId())
                .questionnaireTitle(questionnaire.getTitle())
                .isCompleted(true)
                .startedAt(response.getStartedAt())
                .completedAt(response.getCompletedAt())
                .durationSeconds(0)
                .message("问卷提交成功，感谢您的参与！")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateQuestionnaireAccess(Long questionnaireId, String accessPassword) {
        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElse(null);

        if (questionnaire == null || !questionnaire.getRequirePassword()) {
            return true;
        }

        return Objects.equals(questionnaire.getAccessPassword(), accessPassword);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserCompletedQuestionnaire(Long questionnaireId, User user) {
        if (user == null) {
            return false;
        }

        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElse(null);

        if (questionnaire == null) {
            return false;
        }

        return responseRepository.findByQuestionnaireAndUserAndIsCompleted(questionnaire, user, true)
                .isPresent();
    }

    // 辅助方法
    private Questionnaire getQuestionnaireByIdAndUser(Long id, User user) {
        Questionnaire questionnaire = questionnaireRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("问卷不存在"));

        if (!questionnaire.getCreatedBy().getId().equals(user.getId())) {
            throw new IllegalArgumentException("无权限操作此问卷");
        }

        return questionnaire;
    }

    private void createQuestions(Questionnaire questionnaire, List<QuestionRequest> questionRequests) {
        for (int i = 0; i < questionRequests.size(); i++) {
            QuestionRequest qr = questionRequests.get(i);

            Question question = Question.builder()
                    .questionnaire(questionnaire)
                    .type(Question.QuestionType.valueOf(qr.getType().toUpperCase()))
                    .title(qr.getTitle())
                    .description(qr.getDescription())
                    .required(qr.getRequired())
                    .sortOrder(qr.getSortOrder() != null ? qr.getSortOrder() : i)
                    .placeholder(qr.getPlaceholder())
                    .build();

            if (qr.getRatingStyle() != null) {
                question.setRatingStyle(Question.RatingStyle.valueOf(qr.getRatingStyle().toUpperCase()));
            }

            try {
                if (qr.getFileTypes() != null) {
                    question.setFileTypes(objectMapper.writeValueAsString(qr.getFileTypes()));
                }
                if (qr.getValidationRules() != null) {
                    question.setValidationRules(objectMapper.writeValueAsString(qr.getValidationRules()));
                }
            } catch (JsonProcessingException e) {
                log.error("Error serializing question data", e);
            }

            question = questionRepository.save(question);

            // 创建选项
            if (qr.getOptions() != null) {
                createQuestionOptions(question, qr.getOptions());
            }
        }
    }

    private void createQuestionOptions(Question question, List<QuestionOptionRequest> optionRequests) {
        // 这里需要创建 QuestionOptionRepository 和相关逻辑
        // 暂时省略具体实现
    }

    private void saveAnswers(com.vote.sihuibackend.entity.QuestionnaireResponse response,
            List<QuestionnaireSubmissionRequest.AnswerRequest> answerRequests) {
        // 这里需要创建 AnswerRepository 和相关逻辑
        // 暂时省略具体实现
    }

    private com.vote.sihuibackend.dto.QuestionnaireResponse convertToResponse(Questionnaire questionnaire, User user) {
        // 计算统计信息
        Long questionCount = questionRepository.countByQuestionnaire(questionnaire);
        Long responseCount = questionnaireRepository.countResponsesByQuestionnaire(questionnaire);

        // 检查用户是否已完成
        Boolean userCompleted = user != null ? hasUserCompletedQuestionnaire(questionnaire.getId(), user) : false;

        return com.vote.sihuibackend.dto.QuestionnaireResponse.builder()
                .id(questionnaire.getId())
                .title(questionnaire.getTitle())
                .description(questionnaire.getDescription())
                .type(questionnaire.getType().name())
                .status(questionnaire.getStatus().name())
                .version(questionnaire.getVersion())
                .startTime(questionnaire.getStartTime())
                .endTime(questionnaire.getEndTime())
                .isAnonymous(questionnaire.getIsAnonymous())
                .allowMultipleSubmissions(questionnaire.getAllowMultipleSubmissions())
                .themeColor(questionnaire.getThemeColor())
                .layout(questionnaire.getLayout() != null ? questionnaire.getLayout().name() : null)
                .requirePassword(questionnaire.getRequirePassword())
                .publishUrl(questionnaire.getPublishUrl())
                .createdAt(questionnaire.getCreatedAt())
                .updatedAt(questionnaire.getUpdatedAt())
                .publishedAt(questionnaire.getPublishedAt())
                .questionCount(questionCount)
                .responseCount(responseCount)
                .estimatedTime(Math.max(1, questionCount.intValue() * 30 / 60)) // 每题30秒估算
                .userCompleted(userCompleted)
                .build();
    }

    // 未实现的方法 - 需要根据具体需求补充
    @Override
    public com.vote.sihuibackend.dto.QuestionnaireResponse updateQuestionnaire(Long id, QuestionnaireRequest request,
            User user) {
        throw new UnsupportedOperationException("暂未实现");
    }

    @Override
    public com.vote.sihuibackend.dto.QuestionnaireResponse pauseQuestionnaire(Long id, User user) {
        throw new UnsupportedOperationException("暂未实现");
    }

    @Override
    public com.vote.sihuibackend.dto.QuestionnaireResponse resumeQuestionnaire(Long id, User user) {
        throw new UnsupportedOperationException("暂未实现");
    }

    @Override
    public com.vote.sihuibackend.dto.QuestionnaireResponse completeQuestionnaire(Long id, User user) {
        throw new UnsupportedOperationException("暂未实现");
    }

    @Override
    public void deleteQuestionnaire(Long id, User user) {
        throw new UnsupportedOperationException("暂未实现");
    }

    @Override
    public QuestionnaireStatsResponse getQuestionnaireStats(Long id, User user) {
        return questionnaireAnalysisService.generateQuestionnaireStats(id, user);
    }
}