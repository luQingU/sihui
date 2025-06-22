# 四会学习平台 - 开发规范

## 项目结构

```
sihui-wx/
├── src/
│   ├── api/                    # API接口管理
│   │   └── index.js           # 统一接口配置
│   ├── components/            # 公共组件
│   │   └── TabBar.vue         # 自定义底部导航栏
│   ├── pages/                 # 页面文件
│   │   ├── index/             # 首页
│   │   ├── login/             # 登录页
│   │   ├── profile/           # 个人中心
│   │   ├── training/          # 培训学习
│   │   ├── learning/          # 学习进度
│   │   ├── questionnaire/     # 问卷调查
│   │   ├── chat/              # AI问答
│   │   └── error/             # 错误页面
│   ├── static/                # 静态资源
│   │   ├── icons/             # 图标文件
│   │   ├── avatar-placeholder.png      # 头像占位图
│   │   ├── course-placeholder.jpg      # 课程封面占位图
│   │   └── course-banner-placeholder.jpg # 课程横幅占位图
│   ├── styles/                # 全局样式
│   │   ├── variables.scss     # 样式变量
│   │   └── mixins.scss        # 样式混入
│   ├── utils/                 # 工具类
│   │   ├── common.js          # 通用工具函数
│   │   └── request.js         # 网络请求工具
│   ├── App.vue                # 应用入口
│   ├── main.js                # 主文件
│   ├── pages.json             # 页面配置（已移除tabBar）
│   └── manifest.json          # 应用配置
└── package.json
```

## 开发规范

### 1. 样式规范

#### 变量使用
- 颜色：使用 `$primary-color`、`$text-color-primary` 等预定义变量
- 间距：使用 `$space-xs`、`$space-sm`、`$space-md` 等
- 字体：使用 `$font-size-sm`、`$font-size-md` 等
- 圆角：使用 `$radius-sm`、`$radius-md` 等

#### Mixins使用
```scss
// 导入样式文件
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

// 使用混入
.container {
  @include page-container();
}

.card {
  @include card-style($space-lg);
}

.button {
  @include button-style($primary-color, #ffffff);
}
```

### 2. 组件规范

#### 图标使用
- 统一使用 `uni-icons` 组件
- 不引入外部图标库
```vue
<uni-icons type="home" size="24" color="#2979ff"></uni-icons>
```

#### 自定义TabBar
- 已移除原生tabBar配置
- 使用自定义TabBar组件：`<TabBar :current="0"></TabBar>`
- 在需要底部导航的页面中引入

### 3. 工具类使用

#### 通用工具 (utils/common.js)
```javascript
import utils from '@/utils/common.js'

// 页面导航
utils.navigateTo('/pages/login/login', 'reLaunch')

// 提示信息
utils.showToast('操作成功', 'success')

// 本地存储
utils.storage.set('token', 'xxx')
const token = utils.storage.get('token')

// 日期格式化
utils.formatDate(new Date(), 'YYYY-MM-DD HH:mm')

// 表单验证
const isValid = utils.isValidPhone('13800138000')
```

#### 网络请求 (utils/request.js)
```javascript
import { userApi } from '@/api/index.js'

// 使用封装好的API
const userInfo = await userApi.getUserInfo()
```

### 4. 图片资源

#### 占位图片
- 头像：`/static/avatar-placeholder.png`
- 课程封面：`/static/course-placeholder.jpg`
- 课程横幅：`/static/course-banner-placeholder.jpg`

#### 使用示例
```vue
<image :src="userInfo.avatar || '/static/avatar-placeholder.png'" mode="aspectFill"></image>
```

### 5. 代码编写规范

#### Vue组件结构
```vue
<template>
  <!-- 模板内容 -->
</template>

<script>
import utils from '@/utils/common.js'

export default {
  name: 'ComponentName',
  components: {},
  props: {},
  data() {
    return {}
  },
  computed: {},
  watch: {},
  onLoad() {},
  onShow() {},
  methods: {}
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';
</style>
```

#### 命名规范
- 组件名：PascalCase（如：`TabBar.vue`）
- 文件名：kebab-case（如：`ai-chat.vue`）
- 变量名：camelCase（如：`userInfo`）
- CSS类名：kebab-case（如：`.user-info`）

### 6. 页面开发指南

#### 页面容器
```vue
<template>
  <view class="container">
    <!-- 页面内容 -->
    
    <!-- 底部安全区 -->
    <view class="bottom-safe"></view>
    
    <!-- TabBar（如需要） -->
    <TabBar :current="0"></TabBar>
  </view>
</template>

<style lang="scss" scoped>
.container {
  @include page-container();
}

.bottom-safe {
  @include bottom-safe();
}
</style>
```

#### 表单处理
```javascript
// 表单验证
validateForm() {
  if (!this.form.username.trim()) {
    utils.showToast('请输入用户名')
    return false
  }
  return true
},

// 表单提交
async handleSubmit() {
  if (!this.validateForm()) return
  
  this.loading = true
  try {
    await api.submit(this.form)
    utils.showToast('提交成功', 'success')
  } catch (error) {
    utils.showToast(error.message || '提交失败')
  } finally {
    this.loading = false
  }
}
```

### 7. 注意事项

1. **不使用外部CDN和CSS库**
2. **图标统一使用uni-icons**
3. **占位图片便于后续替换**
4. **样式统一使用变量和mixins**
5. **工具函数统一封装使用**
6. **API接口统一管理**
7. **错误处理统一规范**

### 8. 开发流程

1. 新建页面时，复制现有页面结构
2. 导入必要的样式文件和工具类
3. 使用预定义的样式变量
4. 统一使用工具类进行页面导航、提示等操作
5. 图片使用占位文件
6. 测试功能完整性

这套规范确保了代码的一致性、可维护性和可扩展性，便于团队协作开发。 