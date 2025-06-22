package com.vote.sihuibackend.enums;

/**
 * 系统权限枚举
 */
public enum Permission {

    // 用户管理权限
    USER_CREATE("user:create", "创建用户"),
    USER_READ("user:read", "查看用户"),
    USER_VIEW("user:view", "查看用户"),
    USER_UPDATE("user:update", "更新用户"),
    USER_EDIT("user:edit", "编辑用户"),
    USER_DELETE("user:delete", "删除用户"),
    USER_BATCH_DELETE("user:batch_delete", "批量删除用户"),
    USER_ASSIGN_ROLE("user:assign_role", "分配用户角色"),
    USER_REMOVE_ROLE("user:remove_role", "移除用户角色"),
    USER_SEARCH("user:search", "搜索用户"),
    USER_STATUS_UPDATE("user:status_update", "更新用户状态"),
    USER_VIEW_SELF("user:view_self", "查看自己信息"),
    USER_EDIT_SELF("user:edit_self", "编辑自己信息"),

    // 角色管理权限
    ROLE_CREATE("role:create", "创建角色"),
    ROLE_READ("role:read", "查看角色"),
    ROLE_VIEW("role:view", "查看角色"),
    ROLE_UPDATE("role:update", "更新角色"),
    ROLE_EDIT("role:edit", "编辑角色"),
    ROLE_DELETE("role:delete", "删除角色"),
    ROLE_ASSIGN_PERMISSION("role:assign_permission", "分配角色权限"),

    // 投票管理权限
    POLL_CREATE("poll:create", "创建投票"),
    POLL_READ("poll:read", "查看投票"),
    POLL_UPDATE("poll:update", "更新投票"),
    POLL_DELETE("poll:delete", "删除投票"),
    POLL_PUBLISH("poll:publish", "发布投票"),
    POLL_CLOSE("poll:close", "关闭投票"),
    POLL_RESULT_VIEW("poll:result_view", "查看投票结果"),
    POLL_VOTE("poll:vote", "参与投票"),

    // 内容管理权限
    CONTENT_CREATE("content:create", "创建内容"),
    CONTENT_READ("content:read", "查看内容"),
    CONTENT_VIEW("content:view", "查看内容"),
    CONTENT_UPDATE("content:update", "更新内容"),
    CONTENT_EDIT("content:edit", "编辑内容"),
    CONTENT_DELETE("content:delete", "删除内容"),
    CONTENT_PUBLISH("content:publish", "发布内容"),
    ARTICLE_CREATE("article:create", "创建文章"),
    ARTICLE_READ("article:read", "查看文章"),
    ARTICLE_UPDATE("article:update", "更新文章"),
    ARTICLE_DELETE("article:delete", "删除文章"),
    ARTICLE_PUBLISH("article:publish", "发布文章"),

    // 分类管理权限
    CATEGORY_CREATE("category:create", "创建分类"),
    CATEGORY_READ("category:read", "查看分类"),
    CATEGORY_UPDATE("category:update", "更新分类"),
    CATEGORY_DELETE("category:delete", "删除分类"),

    // 系统管理权限
    SYSTEM_CONFIG("system:config", "系统配置"),
    SYSTEM_LOG("system:log", "系统日志"),
    SYSTEM_MONITOR("system:monitor", "系统监控"),
    SYSTEM_AUDIT("system:audit", "系统审计"),
    SYSTEM_ADMIN("system:admin", "系统管理员"),

    // 文件管理权限
    FILE_UPLOAD("file:upload", "文件上传"),
    FILE_DELETE("file:delete", "文件删除"),
    FILE_DOWNLOAD("file:download", "文件下载"),

    // 特殊权限
    ADMIN_ALL("admin:all", "管理员全部权限"),
    SELF_PROFILE("self:profile", "管理个人资料");

    private final String code;
    private final String description;

    Permission(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据权限代码获取权限枚举
     */
    public static Permission fromCode(String code) {
        for (Permission permission : values()) {
            if (permission.getCode().equals(code)) {
                return permission;
            }
        }
        throw new IllegalArgumentException("无效的权限代码: " + code);
    }

    /**
     * 检查权限代码是否有效
     */
    public static boolean isValidCode(String code) {
        for (Permission permission : values()) {
            if (permission.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
}