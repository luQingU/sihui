-- V2__Insert_initial_data.sql
-- 四会项目初始数据插入脚本

-- ================================
-- 1. 初始角色数据
-- ================================

INSERT INTO roles (name, display_name, description, permissions, status) VALUES
('ADMIN', '系统管理员', '拥有系统全部权限的管理员角色', 
 JSON_ARRAY('USER_MANAGEMENT', 'ROLE_MANAGEMENT', 'CONTENT_MANAGEMENT', 'QUESTIONNAIRE_MANAGEMENT', 'SYSTEM_CONFIG'), 
 'ACTIVE'),
('MANAGER', '内容管理员', '负责内容和问卷管理的角色', 
 JSON_ARRAY('CONTENT_MANAGEMENT', 'QUESTIONNAIRE_MANAGEMENT', 'VIEW_REPORTS'), 
 'ACTIVE'),
('USER', '普通用户', '系统的普通用户角色', 
 JSON_ARRAY('VIEW_CONTENT', 'PARTICIPATE_QUESTIONNAIRE', 'AI_CHAT'), 
 'ACTIVE'),
('GUEST', '访客', '未注册用户的访客角色', 
 JSON_ARRAY('VIEW_PUBLIC_CONTENT'), 
 'ACTIVE');

-- ================================
-- 2. 初始管理员用户
-- ================================

-- 创建默认管理员用户 (密码: admin123 - 生产环境请立即修改)
INSERT INTO users (username, email, password_hash, real_name, status, email_verified, created_at, updated_at) VALUES
('admin', 'admin@sihui.com', '$2a$10$8.UnVuG9HHkXjwuBUeXue.CrpJNHKGhqtUHzJhM9oJQcPQdXjlZJ6', '系统管理员', 'ACTIVE', TRUE, NOW(), NOW());

-- 为管理员分配角色
INSERT INTO user_roles (user_id, role_id, granted_at) VALUES
((SELECT id FROM users WHERE username = 'admin'), (SELECT id FROM roles WHERE name = 'ADMIN'), NOW());

-- ================================
-- 3. 系统配置数据
-- ================================

INSERT INTO system_configs (config_key, config_value, value_type, description, is_public) VALUES
('app.name', '四会学习培训系统', 'STRING', '应用名称', TRUE),
('app.version', '1.0.0', 'STRING', '应用版本', TRUE),
('app.description', '四会学习培训管理平台', 'STRING', '应用描述', TRUE),
('app.logo', '/api/files/logo.png', 'STRING', '应用Logo', TRUE),
('app.contact.email', 'support@sihui.com', 'STRING', '联系邮箱', TRUE),
('app.contact.phone', '400-123-4567', 'STRING', '联系电话', TRUE),

-- 问卷配置
('questionnaire.max_questions', '50', 'NUMBER', '问卷最大题目数', FALSE),
('questionnaire.max_options', '10', 'NUMBER', '单题最大选项数', FALSE),
('questionnaire.auto_close_days', '30', 'NUMBER', '问卷自动关闭天数', FALSE),

-- 文件上传配置
('upload.max_file_size', '10485760', 'NUMBER', '最大文件大小(字节)', FALSE),
('upload.allowed_types', '["jpg","jpeg","png","gif","pdf","doc","docx","ppt","pptx","xls","xlsx"]', 'JSON', '允许的文件类型', FALSE),

-- AI聊天配置
('ai.chat.enabled', 'true', 'BOOLEAN', '是否启用AI聊天', FALSE),
('ai.chat.max_history', '20', 'NUMBER', '最大聊天历史记录数', FALSE),
('ai.chat.max_tokens', '1000', 'NUMBER', '最大Token数', FALSE),

-- 安全配置
('security.password.min_length', '6', 'NUMBER', '密码最小长度', FALSE),
('security.password.require_uppercase', 'false', 'BOOLEAN', '密码是否需要大写字母', FALSE),
('security.password.require_lowercase', 'false', 'BOOLEAN', '密码是否需要小写字母', FALSE),
('security.password.require_number', 'false', 'BOOLEAN', '密码是否需要数字', FALSE),
('security.password.require_special', 'false', 'BOOLEAN', '密码是否需要特殊字符', FALSE),

-- 邮件配置
('mail.enabled', 'false', 'BOOLEAN', '是否启用邮件服务', FALSE),
('mail.smtp.host', '', 'STRING', 'SMTP服务器地址', FALSE),
('mail.smtp.port', '587', 'NUMBER', 'SMTP服务器端口', FALSE),
('mail.smtp.username', '', 'STRING', 'SMTP用户名', FALSE),
('mail.from.address', 'noreply@sihui.com', 'STRING', '发件人邮箱', FALSE),
('mail.from.name', '四会学习培训系统', 'STRING', '发件人名称', FALSE);

-- ================================
-- 4. 示例分类数据
-- ================================

INSERT INTO categories (name, slug, description, sort_order, status) VALUES
('学习资料', 'learning-materials', '各类学习培训资料', 1, 'ACTIVE'),
('政策文件', 'policy-documents', '相关政策文件和规定', 2, 'ACTIVE'),
('培训视频', 'training-videos', '培训教学视频资源', 3, 'ACTIVE'),
('考试题库', 'exam-questions', '考试练习题库', 4, 'ACTIVE'),
('通知公告', 'announcements', '重要通知和公告', 5, 'ACTIVE');

-- ================================
-- 5. 示例问卷数据
-- ================================

-- 创建示例问卷
INSERT INTO questionnaires (title, description, status, questionnaire_type, created_by, start_date, end_date, created_at, updated_at) VALUES
('四会学习培训系统满意度调查', '请您对我们的学习培训系统进行评价，您的反馈对我们很重要。', 'ACTIVE', 'SURVEY', 
 (SELECT id FROM users WHERE username = 'admin'), 
 NOW(), 
 DATE_ADD(NOW(), INTERVAL 30 DAY), 
 NOW(), 
 NOW());

-- 获取问卷ID
SET @questionnaire_id = LAST_INSERT_ID();

-- 插入问卷题目
INSERT INTO questions (questionnaire_id, question_text, question_type, is_required, sort_order, created_at) VALUES
(@questionnaire_id, '您对系统的整体满意度如何？', 'SINGLE_CHOICE', TRUE, 1, NOW()),
(@questionnaire_id, '您认为系统的哪些功能最有用？（可多选）', 'MULTIPLE_CHOICE', TRUE, 2, NOW()),
(@questionnaire_id, '您希望系统增加哪些新功能？', 'TEXT', FALSE, 3, NOW()),
(@questionnaire_id, '您会向朋友推荐这个系统吗？', 'SINGLE_CHOICE', TRUE, 4, NOW());

-- 为选择题插入选项
INSERT INTO question_options (question_id, option_text, sort_order) VALUES
-- 第1题选项
((SELECT id FROM questions WHERE questionnaire_id = @questionnaire_id AND sort_order = 1), '非常满意', 1),
((SELECT id FROM questions WHERE questionnaire_id = @questionnaire_id AND sort_order = 1), '满意', 2),
((SELECT id FROM questions WHERE questionnaire_id = @questionnaire_id AND sort_order = 1), '一般', 3),
((SELECT id FROM questions WHERE questionnaire_id = @questionnaire_id AND sort_order = 1), '不满意', 4),
((SELECT id FROM questions WHERE questionnaire_id = @questionnaire_id AND sort_order = 1), '非常不满意', 5),

-- 第2题选项
((SELECT id FROM questions WHERE questionnaire_id = @questionnaire_id AND sort_order = 2), '问卷调查功能', 1),
((SELECT id FROM questions WHERE questionnaire_id = @questionnaire_id AND sort_order = 2), 'AI智能聊天', 2),
((SELECT id FROM questions WHERE questionnaire_id = @questionnaire_id AND sort_order = 2), '学习资料管理', 3),
((SELECT id FROM questions WHERE questionnaire_id = @questionnaire_id AND sort_order = 2), '用户权限管理', 4),
((SELECT id FROM questions WHERE questionnaire_id = @questionnaire_id AND sort_order = 2), '数据统计分析', 5),

-- 第4题选项
((SELECT id FROM questions WHERE questionnaire_id = @questionnaire_id AND sort_order = 4), '一定会推荐', 1),
((SELECT id FROM questions WHERE questionnaire_id = @questionnaire_id AND sort_order = 4), '可能会推荐', 2),
((SELECT id FROM questions WHERE questionnaire_id = @questionnaire_id AND sort_order = 4), '不确定', 3),
((SELECT id FROM questions WHERE questionnaire_id = @questionnaire_id AND sort_order = 4), '不会推荐', 4);

-- ================================
-- 6. 审计日志示例
-- ================================

INSERT INTO audit_logs (user_id, action, resource_type, resource_id, new_values, ip_address, created_at) VALUES
((SELECT id FROM users WHERE username = 'admin'), 'CREATE', 'USER', '1', '{"username":"admin","email":"admin@sihui.com"}', '127.0.0.1', NOW()),
((SELECT id FROM users WHERE username = 'admin'), 'CREATE', 'QUESTIONNAIRE', '1', '{"title":"四会学习培训系统满意度调查"}', '127.0.0.1', NOW());

-- ================================
-- 完成消息
-- ================================

-- 插入系统初始化完成标记
INSERT INTO system_configs (config_key, config_value, value_type, description, is_public) VALUES
('system.initialized', 'true', 'BOOLEAN', '系统是否已初始化', FALSE),
('system.init_time', NOW(), 'STRING', '系统初始化时间', FALSE),
('system.init_version', '1.0.0', 'STRING', '初始化时的系统版本', FALSE); 