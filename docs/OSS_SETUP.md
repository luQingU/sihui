# 阿里云OSS配置指南

## 前置要求

1. 拥有阿里云账号
2. 已开通对象存储OSS服务
3. 已创建OSS存储桶（Bucket）

## 配置步骤

### 1. 创建OSS存储桶

1. 登录阿里云控制台
2. 进入对象存储OSS服务
3. 创建新的Bucket，建议命名为 `sihui-training-files`
4. 选择合适的地域（建议选择广州）
5. 设置读写权限为"私有"（通过应用程序控制访问）

### 2. 获取访问凭证

1. 在阿里云控制台中，进入"访问控制RAM"
2. 创建新的用户（如果还没有）
3. 为用户添加OSS相关权限：
   - `AliyunOSSFullAccess`（完整权限）
   - 或者创建自定义策略，只授予特定Bucket的权限
4. 获取AccessKey ID和AccessKey Secret

### 3. 配置环境变量

创建 `.env` 文件（在项目根目录），添加以下配置：

```bash
# 阿里云OSS配置
ALIYUN_OSS_ACCESS_KEY_ID=你的AccessKey ID
ALIYUN_OSS_ACCESS_KEY_SECRET=你的AccessKey Secret
ALIYUN_OSS_BUCKET_NAME=sihui-training-files
ALIYUN_OSS_DOMAIN=你的自定义域名(可选)
```

### 4. 应用程序配置

应用程序已经配置了以下默认设置（可在 `application.properties` 中修改）：

- **服务端点**: `https://oss-cn-guangzhou.aliyuncs.com`
- **最大文件大小**: 100MB
- **允许的文件类型**: 
  - 视频：mp4, avi, mov, wmv, flv, mkv
  - 文档：pdf, doc, docx, ppt, pptx, xls, xlsx, txt
  - 图片：jpg, jpeg, png, gif

## 安全建议

1. **使用RAM用户**: 不要使用主账号的AccessKey，创建专门的RAM用户
2. **最小权限原则**: 只授予必要的OSS权限
3. **定期轮换密钥**: 定期更换AccessKey
4. **环境变量管理**: 不要将AccessKey硬编码在代码中
5. **HTTPS传输**: 确保使用HTTPS端点

## 测试配置

启动应用程序后，可以通过以下方式测试OSS配置：

1. 查看应用程序启动日志，确认OSS配置加载成功
2. 使用文件上传API测试文件上传功能
3. 检查OSS控制台中是否有文件成功上传

## 故障排除

### 常见错误

1. **AccessDenied**: 检查AccessKey权限和Bucket权限设置
2. **InvalidBucketName**: 确认Bucket名称正确且存在
3. **SignatureDoesNotMatch**: 检查AccessKey Secret是否正确
4. **RequestTimeout**: 检查网络连接和服务端点设置

### 调试方法

1. 启用应用程序日志调试模式
2. 查看OSS SDK的详细错误信息
3. 使用阿里云控制台的日志服务查看访问日志

## 性能优化

1. **CDN加速**: 配置CDN域名加速文件访问
2. **分片上传**: 对于大文件使用分片上传
3. **图片处理**: 使用OSS的图片处理服务
4. **缓存策略**: 合理设置文件的缓存策略 