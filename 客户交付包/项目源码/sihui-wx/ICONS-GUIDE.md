# 图标使用指南

本项目统一使用 `uni-icons` 图标组件，以下是各功能模块的图标选择和使用说明。

## 主要功能图标

### 🏠 首页功能入口

| 功能 | 图标类型 | 颜色 | 说明 |
|------|----------|------|------|
| 培训学习 | `medal` | `#2979ff` | 使用奖章图标，象征学习成就 |
| 问卷调查 | `list` | `#18bc37` | 使用列表图标，表示问卷条目 |
| AI问答 | `chat` | `#9c27b0` | 使用聊天图标，表示对话交流 |
| 我的学习 | `calendar` | `#ff9800` | 使用日历图标，表示学习计划 |

### 👤 个人中心菜单

| 功能 | 图标类型 | 颜色 | 说明 |
|------|----------|------|------|
| 我的课程 | `medal-filled` | `#2979ff` | 使用实心奖章，表示已获得的课程 |
| 我的问卷 | `list` | `#18bc37` | 与首页保持一致 |
| 设置 | `settings` | `#666666` | 使用设置图标，表示系统配置 |
| 关于我们 | `help` | `#666666` | 使用帮助图标，表示帮助支持 |

### 📚 培训学习页面

| 场景 | 图标类型 | 颜色 | 说明 |
|------|----------|------|------|
| 空状态显示 | `medal` | `#cccccc` | 与功能入口保持一致 |
| 课时信息 | `clock` | `#999999` | 使用时钟图标，表示时间 |
| 学员信息 | `person` | `#999999` | 使用人员图标，表示学员数量 |

### 📖 课程详情页面

| 功能 | 图标类型 | 颜色 | 说明 |
|------|----------|------|------|
| 播放按钮 | `play-filled` | `#fff` | 使用播放图标，表示开始学习 |
| 讲师信息 | `person` | `#999` | 使用人员图标，表示讲师 |
| 课程时长 | `clock` | `#999` | 使用时钟图标，表示时长 |
| 学习人数 | `eye` | `#999` | 使用眼睛图标，表示观看次数 |
| 收藏功能 | `heart` / `heart-filled` | `#999` / `#ff6b6b` | 空心/实心爱心表示收藏状态 |

### 🔄 导航与操作图标

| 功能 | 图标类型 | 颜色 | 说明 |
|------|----------|------|------|
| 首页导航 | `home` | 动态 | TabBar中的首页图标 |
| 个人中心导航 | `person` | 动态 | TabBar中的个人图标 |
| 右箭头 | `arrow-right` | `#cccccc` | 列表项的导航箭头 |
| 编辑功能 | `gear` | `#2979ff` | 个人信息编辑图标 |
| 系统设置 | `settings` | `#666666` | 应用设置配置图标 |
| 帮助支持 | `help` | `#666666` | 帮助、关于我们图标 |

## 图标使用规范

### 1. 基本语法
```vue
<uni-icons type="图标名称" size="大小" color="颜色"></uni-icons>
```

### 2. 尺寸规范
- **大图标（功能入口）**: `size="40"`
- **中等图标（列表项）**: `size="20"`
- **小图标（元信息）**: `size="12"` - `size="16"`
- **空状态图标**: `size="60"`

### 3. 颜色规范
```scss
// 主要功能色
$primary-color: #2979ff    // 主色调
$success-color: #18bc37    // 成功/确认
$warning-color: #ff9800    // 警告/提醒
$info-color: #9c27b0       // 信息/特殊功能

// 中性色
$text-color-primary: #333333     // 主要文字
$text-color-secondary: #666666   // 次要文字
$text-color-tertiary: #999999    // 辅助文字
$text-color-light: #cccccc       // 浅色文字
```

### 4. 语义化选择

#### 学习相关
- `medal` / `medal-filled` - 学习成就、课程奖励
- `star` / `star-filled` - 评分、收藏、推荐
- `calendar` - 学习计划、进度安排

#### 交互相关
- `chat` / `chat-filled` - 聊天、问答、交流
- `heart` / `heart-filled` - 收藏、喜欢
- `eye` / `eye-filled` - 查看、浏览

#### 信息展示
- `person` / `person-filled` - 用户、讲师、学员
- `clock` - 时间、时长、截止日期
- `list` - 列表、目录、问卷

#### 操作功能
- `gear` / `gear-filled` - 编辑、配置
- `settings` / `settings-filled` - 系统设置、偏好设置
- `help` / `help-filled` - 帮助、支持、关于
- `arrow-right` - 导航、跳转
- `play-filled` - 播放、开始

## 使用示例

### 功能卡片
```vue
<view class="function-item" @click="navigateTo('/pages/training/training')">
  <uni-icons type="medal" size="40" color="#2979ff"></uni-icons>
  <text class="function-text">培训学习</text>
</view>
```

### 列表项
```vue
<view class="menu-item" @click="navigateTo('/pages/learning/progress')">
  <uni-icons type="medal-filled" size="20" color="#2979ff"></uni-icons>
  <text class="menu-text">我的课程</text>
  <uni-icons type="arrow-right" size="14" color="#cccccc"></uni-icons>
</view>
```

### 状态切换
```vue
<uni-icons 
  :type="isCollected ? 'heart-filled' : 'heart'" 
  size="18" 
  :color="isCollected ? '#ff6b6b' : '#999'"
></uni-icons>
```

### 元信息展示
```vue
<view class="meta-item">
  <uni-icons type="clock" size="12" color="#999999"></uni-icons>
  <text class="meta-text">{{ course.duration }}课时</text>
</view>
```

### 设置菜单
```vue
<!-- 系统设置 -->
<view class="menu-item" @click="showSettings">
  <uni-icons type="settings" size="20" color="#666666"></uni-icons>
  <text class="menu-text">设置</text>
  <uni-icons type="arrow-right" size="14" color="#cccccc"></uni-icons>
</view>

<!-- 帮助支持 -->
<view class="menu-item" @click="showAbout">
  <uni-icons type="help" size="20" color="#666666"></uni-icons>
  <text class="menu-text">关于我们</text>
  <uni-icons type="arrow-right" size="14" color="#cccccc"></uni-icons>
</view>
```

## 注意事项

1. **保持一致性**: 相同功能在不同页面应使用相同图标
2. **语义明确**: 图标选择应与功能含义匹配
3. **尺寸适配**: 根据使用场景选择合适的图标尺寸
4. **颜色规范**: 遵循设计系统的颜色规范
5. **无障碍**: 图标应配合文字说明，确保易读性

## 可用图标列表

uni-icons 组件包含以下常用图标：

**导航类**: `home`, `arrow-right`, `arrow-left`, `arrow-up`, `arrow-down`, `back`, `forward`

**用户类**: `person`, `person-filled`, `personadd`, `personadd-filled`

**功能类**: `gear`, `gear-filled`, `settings`, `settings-filled`, `help`, `help-filled`, `info`, `info-filled`

**媒体类**: `play-filled`, `videocam`, `videocam-filled`, `image`, `image-filled`

**交互类**: `heart`, `heart-filled`, `star`, `star-filled`, `chat`, `chat-filled`

**学习类**: `medal`, `medal-filled`, `calendar`, `calendar-filled`, `list`, `book`

**工具类**: `clock`, `eye`, `eye-filled`, `search`, `refresh`, `download`

更多图标请参考：`src/uni_modules/uni-icons/components/uni-icons/uniicons_file_vue.js` 