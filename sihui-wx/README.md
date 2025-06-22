# 四会学习平台

基于 uniapp + Vue3 的多端学习平台，支持小程序和H5。

## 功能特性

### 🏠 首页
- 用户欢迎信息展示
- 四大主要功能入口：培训学习、问卷调查、AI问答、个人中心

### 🔐 用户认证
- 登录页面（支持用户名/手机号登录）
- 登录状态管理
- 模拟登录（用户名：admin，密码：123456）

### 👤 个人中心
- 用户信息展示
- 功能菜单导航
- 退出登录

### 📚 培训学习
- 课程列表展示
- 下拉刷新功能
- 课程详情页面
- 学习进度管理

### 📝 问卷调查
- 问卷列表显示
- 问卷填写界面
- 支持单选、多选、文本输入
- 表单验证和提交

### 🤖 AI智能问答
- 聊天界面
- 快速回复建议
- 消息发送功能（开发中）

### ❌ 错误处理
- 网络异常页面
- 友好的错误提示

## 技术栈

- **框架**: uniapp
- **前端**: Vue3 + JavaScript
- **样式**: SCSS
- **图标**: uni-icons
- **平台**: 微信小程序 + H5

## 项目结构

```
sihui-wx/
├── src/
│   ├── pages/
│   │   ├── index/           # 首页
│   │   ├── login/           # 登录页
│   │   ├── profile/         # 个人中心
│   │   ├── training/        # 培训学习
│   │   ├── learning/        # 学习进度
│   │   ├── questionnaire/   # 问卷调查
│   │   ├── chat/           # AI问答
│   │   └── error/          # 错误页面
│   ├── static/             # 静态资源
│   ├── uni_modules/        # uni组件
│   ├── App.vue            # 应用入口
│   ├── main.js            # 主文件
│   ├── pages.json         # 页面配置
│   └── manifest.json      # 应用配置
└── package.json
```

## 开发运行

### 安装依赖
```bash
npm install
```

### 开发调试
```bash
# H5开发
npm run dev:h5

# 微信小程序开发
npm run dev:mp-weixin
```

### 构建打包
```bash
# H5构建
npm run build:h5

# 微信小程序构建  
npm run build:mp-weixin
```

## 页面路由

- `/pages/index/index` - 首页
- `/pages/login/login` - 登录页
- `/pages/profile/profile` - 个人中心
- `/pages/training/training` - 培训学习
- `/pages/training/detail` - 课程详情
- `/pages/learning/progress` - 学习进度
- `/pages/questionnaire/questionnaire` - 问卷列表
- `/pages/questionnaire/fill` - 问卷填写
- `/pages/chat/ai-chat` - AI问答
- `/pages/error/network` - 网络异常

## 模拟数据

当前使用 Mock 数据进行开发，包括：
- 用户信息
- 课程列表
- 问卷数据
- AI问答响应

## 待完善功能

1. **后端接口对接** - 替换 Mock 数据为真实 API
2. **图片资源** - 添加真实的课程图片、用户头像等
3. **AI问答功能** - 接入真实的AI服务
4. **支付功能** - 课程购买支付
5. **视频播放** - 课程视频学习
6. **离线缓存** - 支持离线学习
7. **推送通知** - 学习提醒、课程更新等

## 注意事项

1. 项目使用了 uni-icons 组件库，已在 `uni_modules` 中配置
2. 所有样式使用 rpx 单位，自适应不同屏幕尺寸
3. 图片使用占位图片，实际开发时需要替换
4. 登录功能为模拟实现，实际需要对接真实认证系统 