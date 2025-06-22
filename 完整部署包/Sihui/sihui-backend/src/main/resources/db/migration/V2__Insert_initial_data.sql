-- V2__Insert_initial_data.sql
-- 四会项目初始数据插入脚本

-- ================================
-- 1. 系统配置初始数据
-- ================================

-- 系统基础配置
INSERT INTO system_configs (config_key, config_value, value_type, description, is_public) VALUES
('system.name', '四会项目管理系统', 'STRING', '系统名称', TRUE),
('system.version', '1.0.0', 'STRING', '系统版本', TRUE),
('system.description', '四会项目全栈应用管理系统', 'STRING', '系统描述', TRUE),
('system.copyright', '© 2025 四会项目', 'STRING', '版权信息', TRUE),
('system.maintenance', 'false', 'BOOLEAN', '系统维护模式', FALSE),
('system.registration_enabled', 'true', 'BOOLEAN', '是否允许用户注册', FALSE),
('system.email_verification_required', 'true', 'BOOLEAN', '是否需要邮箱验证', FALSE);

-- 文件上传配置
INSERT INTO system_configs (config_key, config_value, value_type, description, is_public) VALUES
('upload.max_file_size', '10485760', 'NUMBER', '最大文件上传大小(字节)', FALSE),
('upload.allowed_types', '["image/jpeg", "image/png", "image/gif", "application/pdf", "text/plain"]', 'JSON', '允许的文件类型', FALSE),
('upload.path', '/uploads', 'STRING', '文件上传路径', FALSE);

-- 投票系统配置
INSERT INTO system_configs (config_key, config_value, value_type, description, is_public) VALUES
('poll.default_duration_days', '7', 'NUMBER', '投票默认持续天数', FALSE),
('poll.max_options', '10', 'NUMBER', '投票最大选项数', FALSE),
('poll.allow_anonymous', 'true', 'BOOLEAN', '是否允许匿名投票', FALSE);

-- ================================
-- 2. 角色权限初始数据
-- ================================

-- 基础角色
INSERT INTO roles (name, display_name, description, permissions, status) VALUES
('SUPER_ADMIN', '超级管理员', '系统超级管理员，拥有所有权限', 
 '{"users": ["create", "read", "update", "delete"], "roles": ["create", "read", "update", "delete"], "articles": ["create", "read", "update", "delete"], "categories": ["create", "read", "update", "delete"], "polls": ["create", "read", "update", "delete"], "system": ["config", "audit"]}', 
 'ACTIVE'),

('ADMIN', '管理员', '系统管理员，拥有大部分管理权限', 
 '{"users": ["read", "update"], "articles": ["create", "read", "update", "delete"], "categories": ["create", "read", "update", "delete"], "polls": ["create", "read", "update", "delete"]}', 
 'ACTIVE'),

('EDITOR', '编辑员', '内容编辑员，负责内容管理', 
 '{"articles": ["create", "read", "update"], "categories": ["read"], "polls": ["create", "read", "update"]}', 
 'ACTIVE'),

('USER', '普通用户', '系统普通用户，基础功能权限', 
 '{"articles": ["read"], "polls": ["read", "vote"]}', 
 'ACTIVE');

-- ================================
-- 3. 默认管理员用户
-- ================================

-- 创建默认超级管理员用户
-- 密码: admin123 (实际使用时应该使用安全的密码哈希)
INSERT INTO users (username, email, password_hash, real_name, status, email_verified, created_at) VALUES
('admin', 'admin@sihui.com', '$2a$10$J7Z1YK8XzWnqV7nF4K9X.eH8Bg7JcU6vVx3zH2E9mA1qL4pY6sN8S', '系统管理员', 'ACTIVE', TRUE, NOW());

-- 分配超级管理员角色
INSERT INTO user_roles (user_id, role_id, granted_at) VALUES
((SELECT id FROM users WHERE username = 'admin'), (SELECT id FROM roles WHERE name = 'SUPER_ADMIN'), NOW());

-- ================================
-- 4. 内容分类初始数据
-- ================================

-- 创建基础内容分类
INSERT INTO categories (name, slug, description, sort_order, status) VALUES
('公告通知', 'announcements', '系统公告和重要通知', 1, 'ACTIVE'),
('新闻动态', 'news', '最新新闻和动态信息', 2, 'ACTIVE'),
('政策文件', 'policies', '相关政策文件和规定', 3, 'ACTIVE'),
('常见问题', 'faq', '常见问题解答', 4, 'ACTIVE'),
('帮助文档', 'help', '系统使用帮助文档', 5, 'ACTIVE');

-- ================================
-- 5. 示例文章内容
-- ================================

-- 创建欢迎文章
INSERT INTO articles (title, slug, summary, content, category_id, author_id, status, is_featured, published_at, created_at) VALUES
('欢迎使用四会项目管理系统', 'welcome-to-sihui-system', '欢迎使用我们的全新管理系统，本文将为您介绍系统的基本功能和使用方法。',
'# 欢迎使用四会项目管理系统

欢迎您使用四会项目管理系统！本系统是一个功能完善的全栈应用，旨在为您提供高效、便捷的管理体验。

## 系统特色

- **用户管理**: 完善的用户注册、登录和权限管理
- **内容管理**: 灵活的文章发布和分类管理
- **投票调研**: 强大的投票和民意调研功能
- **系统监控**: 完整的操作审计和系统配置

## 快速开始

1. 使用您的账号登录系统
2. 根据您的角色权限，访问相应的功能模块
3. 如有疑问，请查看帮助文档或联系管理员

感谢您的使用！',
(SELECT id FROM categories WHERE slug = 'announcements'),
(SELECT id FROM users WHERE username = 'admin'),
'PUBLISHED', TRUE, NOW(), NOW());

-- 创建系统说明文章
INSERT INTO articles (title, slug, summary, content, category_id, author_id, status, published_at, created_at) VALUES
('系统功能介绍', 'system-features', '详细介绍系统的各项功能模块和使用方法。',
'# 系统功能介绍

## 主要功能模块

### 1. 用户管理
- 用户注册和登录
- 角色权限分配
- 个人信息管理

### 2. 内容管理
- 文章发布和编辑
- 分类管理
- 文件上传

### 3. 投票系统
- 创建投票活动
- 参与投票
- 结果统计

### 4. 系统管理
- 系统配置
- 操作日志
- 数据统计

更多详细信息请参阅相关文档。',
(SELECT id FROM categories WHERE slug = 'help'),
(SELECT id FROM users WHERE username = 'admin'),
'PUBLISHED', NOW(), NOW());

-- ================================
-- 6. 示例投票活动
-- ================================

-- 创建示例投票
INSERT INTO polls (title, description, poll_type, created_by, status, is_anonymous, require_login, result_visible, start_at, end_at, created_at) VALUES
('系统功能优先级调研', '为了更好地改进系统，我们希望了解您认为最重要的功能模块是什么？', 'SINGLE',
(SELECT id FROM users WHERE username = 'admin'), 'ACTIVE', FALSE, TRUE, 'AFTER_VOTE',
NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), NOW());

-- 添加投票选项
INSERT INTO poll_options (poll_id, option_text, sort_order) VALUES
((SELECT id FROM polls WHERE title = '系统功能优先级调研'), '用户管理功能', 1),
((SELECT id FROM polls WHERE title = '系统功能优先级调研'), '内容管理功能', 2),
((SELECT id FROM polls WHERE title = '系统功能优先级调研'), '投票调研功能', 3),
((SELECT id FROM polls WHERE title = '系统功能优先级调研'), '数据统计功能', 4),
((SELECT id FROM polls WHERE title = '系统功能优先级调研'), '系统安全功能', 5); 