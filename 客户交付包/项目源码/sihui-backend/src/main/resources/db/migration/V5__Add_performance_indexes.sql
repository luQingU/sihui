-- 性能优化：添加数据库索引
-- 创建时间：2024-01-01
-- 目的：提升数据库查询性能

-- 用户表索引优化
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);
CREATE INDEX IF NOT EXISTS idx_users_status_email_verified ON users(status, email_verified);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);
CREATE INDEX IF NOT EXISTS idx_users_last_login_at ON users(last_login_at);

-- 文档表索引优化
CREATE INDEX IF NOT EXISTS idx_documents_status ON documents(status);
CREATE INDEX IF NOT EXISTS idx_documents_category ON documents(category);
CREATE INDEX IF NOT EXISTS idx_documents_file_type ON documents(file_type);
CREATE INDEX IF NOT EXISTS idx_documents_uploader_id ON documents(uploader_id);
CREATE INDEX IF NOT EXISTS idx_documents_status_category ON documents(status, category);
CREATE INDEX IF NOT EXISTS idx_documents_status_created_at ON documents(status, created_at);
CREATE INDEX IF NOT EXISTS idx_documents_category_created_at ON documents(category, created_at);
CREATE INDEX IF NOT EXISTS idx_documents_is_public ON documents(is_public);
CREATE INDEX IF NOT EXISTS idx_documents_parent_id ON documents(parent_id);
CREATE INDEX IF NOT EXISTS idx_documents_original_filename_uploader ON documents(original_filename, uploader_id, status);

-- 角色表索引优化
CREATE INDEX IF NOT EXISTS idx_roles_name ON roles(name);
CREATE INDEX IF NOT EXISTS idx_roles_status ON roles(status);

-- 用户角色关联表索引优化
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON user_roles(role_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_user_role ON user_roles(user_id, role_id);

-- 问卷表索引优化
CREATE INDEX IF NOT EXISTS idx_questionnaires_created_by ON questionnaires(created_by);
CREATE INDEX IF NOT EXISTS idx_questionnaires_status ON questionnaires(status);
CREATE INDEX IF NOT EXISTS idx_questionnaires_created_at ON questionnaires(created_at);

-- 问题表索引优化
CREATE INDEX IF NOT EXISTS idx_questions_questionnaire_id ON questions(questionnaire_id);
CREATE INDEX IF NOT EXISTS idx_questions_sort_order ON questions(sort_order);
CREATE INDEX IF NOT EXISTS idx_questions_questionnaire_sort ON questions(questionnaire_id, sort_order);

-- 问题选项表索引优化
CREATE INDEX IF NOT EXISTS idx_question_options_question_id ON question_options(question_id);
CREATE INDEX IF NOT EXISTS idx_question_options_sort_order ON question_options(sort_order);

-- 问卷回答表索引优化
CREATE INDEX IF NOT EXISTS idx_questionnaire_responses_questionnaire_id ON questionnaire_responses(questionnaire_id);
CREATE INDEX IF NOT EXISTS idx_questionnaire_responses_user_id ON questionnaire_responses(user_id);
CREATE INDEX IF NOT EXISTS idx_questionnaire_responses_is_completed ON questionnaire_responses(is_completed);
CREATE INDEX IF NOT EXISTS idx_questionnaire_responses_questionnaire_user ON questionnaire_responses(questionnaire_id, user_id);
CREATE INDEX IF NOT EXISTS idx_questionnaire_responses_completed_at ON questionnaire_responses(completed_at);

-- 答案表索引优化
CREATE INDEX IF NOT EXISTS idx_answers_questionnaire_response_id ON answers(questionnaire_response_id);
CREATE INDEX IF NOT EXISTS idx_answers_question_id ON answers(question_id);

-- 聊天会话表索引优化（如果存在）
CREATE INDEX IF NOT EXISTS idx_chat_sessions_user_id ON chat_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_chat_sessions_session_id ON chat_sessions(session_id);
CREATE INDEX IF NOT EXISTS idx_chat_sessions_created_at ON chat_sessions(created_at);

-- 聊天消息表索引优化（如果存在）
CREATE INDEX IF NOT EXISTS idx_chat_messages_session_id ON chat_messages(session_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_sequence_number ON chat_messages(sequence_number);
CREATE INDEX IF NOT EXISTS idx_chat_messages_session_sequence ON chat_messages(session_id, sequence_number);
CREATE INDEX IF NOT EXISTS idx_chat_messages_created_at ON chat_messages(created_at);

-- 复合索引优化（针对常用查询组合）
CREATE INDEX IF NOT EXISTS idx_users_status_created_at ON users(status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_documents_status_view_count ON documents(status, view_count DESC);
CREATE INDEX IF NOT EXISTS idx_documents_category_status_created_at ON documents(category, status, created_at DESC);

-- 全文搜索索引（MySQL 5.7+支持）
-- ALTER TABLE documents ADD FULLTEXT(title, content, keywords);

-- 统计信息更新（可选，根据数据库版本）
-- ANALYZE TABLE users, documents, questionnaires, questions, questionnaire_responses;