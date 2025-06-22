package com.vote.sihuibackend.service;

import com.vote.sihuibackend.dto.*;
import com.vote.sihuibackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 问卷服务接口
 */
public interface QuestionnaireService {

    /**
     * 创建问卷
     */
    QuestionnaireResponse createQuestionnaire(QuestionnaireRequest request, User creator);

    /**
     * 更新问卷
     */
    QuestionnaireResponse updateQuestionnaire(Long id, QuestionnaireRequest request, User user);

    /**
     * 根据ID获取问卷
     */
    Optional<QuestionnaireResponse> getQuestionnaireById(Long id);

    /**
     * 获取用户可参与的问卷列表
     */
    List<QuestionnaireResponse> getAvailableQuestionnaires(User user);

    /**
     * 分页获取用户创建的问卷
     */
    Page<QuestionnaireResponse> getUserQuestionnaires(User user, Pageable pageable);

    /**
     * 发布问卷
     */
    QuestionnaireResponse publishQuestionnaire(Long id, User user);

    /**
     * 暂停问卷
     */
    QuestionnaireResponse pauseQuestionnaire(Long id, User user);

    /**
     * 恢复问卷
     */
    QuestionnaireResponse resumeQuestionnaire(Long id, User user);

    /**
     * 完成问卷
     */
    QuestionnaireResponse completeQuestionnaire(Long id, User user);

    /**
     * 删除问卷
     */
    void deleteQuestionnaire(Long id, User user);

    /**
     * 提交问卷答案
     */
    QuestionnaireSubmissionResponse submitQuestionnaire(QuestionnaireSubmissionRequest request, User user,
            String userAgent, String ipAddress);

    /**
     * 验证问卷访问权限
     */
    boolean validateQuestionnaireAccess(Long questionnaireId, String accessPassword);

    /**
     * 检查用户是否已完成问卷
     */
    boolean hasUserCompletedQuestionnaire(Long questionnaireId, User user);

    /**
     * 获取问卷统计信息
     */
    QuestionnaireStatsResponse getQuestionnaireStats(Long id, User user);
}