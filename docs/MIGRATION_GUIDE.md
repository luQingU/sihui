# 数据库迁移指南

## 概览

本项目使用Flyway进行数据库版本控制和迁移管理。所有的数据库结构变更都通过迁移脚本进行管理。

## 迁移脚本位置

```
sihui-backend/src/main/resources/db/migration/
├── V1__Create_core_tables.sql      # 核心表结构
├── V2__Insert_initial_data.sql     # 初始数据
└── ...                             # 未来的迁移脚本
```

## 命名规范

Flyway迁移脚本遵循以下命名规范：

```
V{version}__{description}.sql
```

示例：
- `V1__Create_core_tables.sql`
- `V2__Insert_initial_data.sql`
- `V3__Add_user_avatar_column.sql`

## 配置说明

### 开发环境配置 (application-dev.properties)

```properties
# Flyway配置
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration
spring.flyway.encoding=UTF-8
spring.flyway.validate-on-migrate=true
```

### JPA配置

```properties
# JPA配置 - 使用validate模式，让Flyway管理DDL
spring.jpa.hibernate.ddl-auto=validate
```

## 使用步骤

### 1. 数据库准备

使用提供的脚本创建数据库：

**Linux/Mac:**
```bash
chmod +x scripts/setup-database.sh
./scripts/setup-database.sh
```

**Windows:**
```powershell
.\scripts\setup-database.ps1
```

### 2. 更新数据库配置

根据实际情况更新 `application-dev.properties` 中的数据库连接信息：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sihui_dev?...
spring.datasource.username=sihui_user
spring.datasource.password=sihui_password
```

### 3. 运行迁移

启动Spring Boot应用时，Flyway会自动运行迁移：

```bash
cd sihui-backend
mvn spring-boot:run
```

### 4. 验证迁移

检查Flyway迁移历史：

```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

## 迁移状态检查

### 查看迁移状态

```bash
mvn flyway:info
```

### 验证迁移

```bash
mvn flyway:validate
```

### 手动运行迁移（如果需要）

```bash
mvn flyway:migrate
```

## 新增迁移脚本

### 1. 创建新的迁移文件

在 `src/main/resources/db/migration/` 目录下创建新文件：

```
V3__Add_new_feature.sql
```

### 2. 编写迁移脚本

```sql
-- V3__Add_new_feature.sql
-- 添加新功能的数据库变更

-- 添加新列
ALTER TABLE users ADD COLUMN phone_verified_at TIMESTAMP NULL COMMENT '手机验证时间';

-- 创建索引
CREATE INDEX idx_users_phone_verified_at ON users(phone_verified_at);

-- 插入新的系统配置
INSERT INTO system_configs (config_key, config_value, value_type, description, is_public) 
VALUES ('feature.phone_verification', 'true', 'BOOLEAN', '是否启用手机验证功能', FALSE);
```

### 3. 测试迁移

```bash
mvn spring-boot:run
```

## 回滚策略

### 注意事项

- Flyway不支持自动回滚
- 生产环境变更需要额外小心
- 建议为重要变更准备回滚脚本

### 手动回滚示例

如果需要回滚某个迁移，需要手动创建回滚脚本：

```sql
-- V3_rollback__Remove_new_feature.sql
-- 回滚V3__Add_new_feature.sql的变更

-- 删除索引
DROP INDEX idx_users_phone_verified_at ON users;

-- 删除列
ALTER TABLE users DROP COLUMN phone_verified_at;

-- 删除配置
DELETE FROM system_configs WHERE config_key = 'feature.phone_verification';
```

## 最佳实践

### 1. 脚本编写原则

- **向前兼容**: 新迁移应该向前兼容
- **原子性**: 每个迁移脚本应该是原子操作
- **幂等性**: 脚本应该可以安全地重复执行
- **测试**: 在开发环境充分测试后再应用到生产环境

### 2. 命名建议

- 使用描述性的名称
- 包含操作类型：Create, Add, Update, Remove等
- 避免过长的描述

### 3. 版本控制

- 所有迁移脚本都应该提交到版本控制
- 不要修改已经执行过的迁移脚本
- 使用增量迁移而不是重新创建

## 故障排除

### 常见问题

1. **迁移失败**
   - 检查SQL语法
   - 验证数据库连接
   - 查看日志文件

2. **版本冲突**
   - 检查是否有重复的版本号
   - 确认迁移脚本的顺序

3. **权限问题**
   - 确认数据库用户有足够的权限
   - 检查DDL权限设置

### 调试命令

```bash
# 查看迁移状态
mvn flyway:info

# 验证迁移脚本
mvn flyway:validate

# 查看应用日志
tail -f logs/spring.log
```

## 生产环境部署

### 1. 备份数据库

```bash
./scripts/backup-database.sh
```

### 2. 停机维护

在生产环境中，重要的迁移可能需要停机维护。

### 3. 分步执行

对于大型变更，考虑分步执行迁移。

### 4. 回滚准备

准备回滚计划和脚本，以备不时之需。 