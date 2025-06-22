-- V1__Create_core_tables.sql
-- 四会项目核心数据库表结构创建脚本

-- ================================
-- 1. 用户管理模块
-- ================================

-- 用户表
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱地址',
    phone VARCHAR(20) UNIQUE COMMENT '手机号码',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    real_name VARCHAR(100) COMMENT '真实姓名',
    avatar_url VARCHAR(500) COMMENT '头像URL',
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE' COMMENT '用户状态',
    email_verified BOOLEAN DEFAULT FALSE COMMENT '邮箱验证状态',
    phone_verified BOOLEAN DEFAULT FALSE COMMENT '手机验证状态',
    last_login_at TIMESTAMP COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_phone (phone),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) COMMENT='用户基础信息表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 角色表
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称',
    display_name VARCHAR(100) NOT NULL COMMENT '角色显示名称',
    description TEXT COMMENT '角色描述',
    permissions JSON COMMENT '权限配置',
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE' COMMENT '角色状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_name (name),
    INDEX idx_status (status)
) COMMENT='角色权限表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 用户角色关联表
CREATE TABLE user_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
    granted_by BIGINT COMMENT '授权人ID',
    
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id),
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (granted_by) REFERENCES users(id) ON DELETE SET NULL
) COMMENT='用户角色关联表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================
-- 2. 内容管理模块
-- ================================

-- 分类表
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    slug VARCHAR(100) NOT NULL UNIQUE COMMENT '分类标识',
    description TEXT COMMENT '分类描述',
    parent_id BIGINT COMMENT '父分类ID',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE' COMMENT '分类状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_slug (slug),
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status),
    INDEX idx_sort_order (sort_order),
    
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
) COMMENT='内容分类表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 文章表
CREATE TABLE articles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '文章ID',
    title VARCHAR(200) NOT NULL COMMENT '文章标题',
    slug VARCHAR(200) NOT NULL UNIQUE COMMENT '文章标识',
    summary TEXT COMMENT '文章摘要',
    content LONGTEXT NOT NULL COMMENT '文章内容',
    featured_image VARCHAR(500) COMMENT '特色图片URL',
    category_id BIGINT COMMENT '分类ID',
    author_id BIGINT NOT NULL COMMENT '作者ID',
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') DEFAULT 'DRAFT' COMMENT '文章状态',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    like_count INT DEFAULT 0 COMMENT '点赞次数',
    is_featured BOOLEAN DEFAULT FALSE COMMENT '是否推荐',
    published_at TIMESTAMP COMMENT '发布时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_slug (slug),
    INDEX idx_category_id (category_id),
    INDEX idx_author_id (author_id),
    INDEX idx_status (status),
    INDEX idx_published_at (published_at),
    INDEX idx_is_featured (is_featured),
    
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE RESTRICT
) COMMENT='文章内容表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 附件表
CREATE TABLE attachments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '附件ID',
    filename VARCHAR(255) NOT NULL COMMENT '文件名',
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_url VARCHAR(500) NOT NULL COMMENT '文件访问URL',
    file_size BIGINT NOT NULL COMMENT '文件大小(字节)',
    mime_type VARCHAR(100) NOT NULL COMMENT 'MIME类型',
    file_hash VARCHAR(64) NOT NULL COMMENT '文件哈希值',
    uploaded_by BIGINT NOT NULL COMMENT '上传者ID',
    upload_ip VARCHAR(50) COMMENT '上传IP',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_filename (filename),
    INDEX idx_file_hash (file_hash),
    INDEX idx_uploaded_by (uploaded_by),
    INDEX idx_mime_type (mime_type),
    
    FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE RESTRICT
) COMMENT='文件附件表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================
-- 3. 投票调研模块
-- ================================

-- 投票活动表
CREATE TABLE polls (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '投票ID',
    title VARCHAR(200) NOT NULL COMMENT '投票标题',
    description TEXT COMMENT '投票描述',
    poll_type ENUM('SINGLE', 'MULTIPLE', 'TEXT') NOT NULL COMMENT '投票类型：单选/多选/文本',
    max_choices INT DEFAULT 1 COMMENT '最大选择数(多选时)',
    created_by BIGINT NOT NULL COMMENT '创建者ID',
    status ENUM('DRAFT', 'ACTIVE', 'CLOSED', 'ARCHIVED') DEFAULT 'DRAFT' COMMENT '投票状态',
    is_anonymous BOOLEAN DEFAULT FALSE COMMENT '是否匿名投票',
    require_login BOOLEAN DEFAULT TRUE COMMENT '是否需要登录',
    allow_change BOOLEAN DEFAULT FALSE COMMENT '是否允许修改投票',
    result_visible ENUM('IMMEDIATELY', 'AFTER_VOTE', 'AFTER_CLOSE') DEFAULT 'AFTER_VOTE' COMMENT '结果可见性',
    start_at TIMESTAMP COMMENT '开始时间',
    end_at TIMESTAMP COMMENT '结束时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_created_by (created_by),
    INDEX idx_status (status),
    INDEX idx_start_at (start_at),
    INDEX idx_end_at (end_at),
    
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT
) COMMENT='投票活动表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 投票选项表
CREATE TABLE poll_options (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '选项ID',
    poll_id BIGINT NOT NULL COMMENT '投票ID',
    option_text VARCHAR(500) NOT NULL COMMENT '选项文本',
    option_image VARCHAR(500) COMMENT '选项图片URL',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
    vote_count INT DEFAULT 0 COMMENT '投票数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_poll_id (poll_id),
    INDEX idx_sort_order (sort_order),
    
    FOREIGN KEY (poll_id) REFERENCES polls(id) ON DELETE CASCADE
) COMMENT='投票选项表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 投票记录表
CREATE TABLE votes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '投票记录ID',
    poll_id BIGINT NOT NULL COMMENT '投票ID',
    user_id BIGINT COMMENT '投票用户ID(匿名投票时为NULL)',
    option_ids JSON NOT NULL COMMENT '选择的选项IDs',
    text_answer TEXT COMMENT '文本答案(文本投票时)',
    voter_ip VARCHAR(50) COMMENT '投票者IP',
    user_agent TEXT COMMENT '用户代理',
    voted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '投票时间',
    
    INDEX idx_poll_id (poll_id),
    INDEX idx_user_id (user_id),
    INDEX idx_voted_at (voted_at),
    
    FOREIGN KEY (poll_id) REFERENCES polls(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) COMMENT='投票记录表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 投票参与者表
CREATE TABLE poll_participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '参与记录ID',
    poll_id BIGINT NOT NULL COMMENT '投票ID',
    user_id BIGINT COMMENT '参与用户ID',
    invite_code VARCHAR(50) COMMENT '邀请码',
    access_token VARCHAR(100) COMMENT '访问令牌',
    participated_at TIMESTAMP COMMENT '参与时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    UNIQUE KEY uk_poll_user (poll_id, user_id),
    INDEX idx_poll_id (poll_id),
    INDEX idx_user_id (user_id),
    INDEX idx_invite_code (invite_code),
    
    FOREIGN KEY (poll_id) REFERENCES polls(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT='投票参与者表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================
-- 4. 系统管理模块
-- ================================

-- 审计日志表
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_id BIGINT COMMENT '操作用户ID',
    action VARCHAR(100) NOT NULL COMMENT '操作动作',
    resource_type VARCHAR(50) NOT NULL COMMENT '资源类型',
    resource_id VARCHAR(100) COMMENT '资源ID',
    old_values JSON COMMENT '操作前数据',
    new_values JSON COMMENT '操作后数据',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent TEXT COMMENT '用户代理',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_resource_type (resource_type),
    INDEX idx_resource_id (resource_id),
    INDEX idx_created_at (created_at),
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) COMMENT='操作审计日志表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 系统配置表
CREATE TABLE system_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    value_type ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON') DEFAULT 'STRING' COMMENT '值类型',
    description VARCHAR(500) COMMENT '配置描述',
    is_public BOOLEAN DEFAULT FALSE COMMENT '是否公开配置',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_config_key (config_key),
    INDEX idx_is_public (is_public)
) COMMENT='系统配置表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci; 