# 思汇投票小程序配置说明

## 配置文件结构

### 1. 主配置文件 `/src/config/index.js`

这是小程序的主要配置文件，包含所有全局配置项：

```javascript
{
  // API地址配置
  api: {
    development: 'http://localhost:8080/api',  // 开发环境
    production: 'https://your-api-domain.com/api'  // 生产环境
  },
  
  // 当前环境
  env: 'development',  // 'development' | 'production'
  
  // 应用配置
  app: {
    name: '思汇投票',
    version: '1.0.0'
  },
  
  // 请求超时时间（毫秒）
  requestTimeout: 30000,
  
  // 文件上传配置
  upload: {
    maxSize: 10485760,  // 10MB
    allowedTypes: ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  },
  
  // 缓存配置
  cache: {
    tokenKey: 'auth_token',
    userInfoKey: 'user_info',
    expireTime: 24  // 小时
  },
  
  // 分页配置
  pagination: {
    pageSize: 20,
    maxPageSize: 100
  }
}
```

### 2. 环境变量配置

#### 开发环境 `.env.development`
```
NODE_ENV=development
VUE_APP_API_URL=http://localhost:8080/api
VUE_APP_TITLE=思汇投票-开发环境
```

#### 生产环境 `.env.production`
```
NODE_ENV=production
VUE_APP_API_URL=https://api.sihui-vote.com/api
VUE_APP_TITLE=思汇投票
```

## 修改配置

### 1. 修改API地址

在 `/src/config/index.js` 中修改对应环境的API地址：

```javascript
api: {
  development: 'http://你的开发环境API地址/api',
  production: 'https://你的生产环境API地址/api'
}
```

### 2. 修改请求超时时间

```javascript
requestTimeout: 30000  // 修改为你需要的毫秒数
```

### 3. 修改文件上传限制

```javascript
upload: {
  maxSize: 20 * 1024 * 1024,  // 修改为20MB
  allowedTypes: [
    'image/jpeg',
    'image/png',
    'image/gif',
    'image/webp',
    'application/pdf'  // 添加PDF支持
  ]
}
```

### 4. 修改缓存配置

```javascript
cache: {
  tokenKey: 'your_token_key',  // 自定义token存储键名
  userInfoKey: 'your_user_key',  // 自定义用户信息存储键名
  expireTime: 48  // 修改为48小时
}
```

## 请求工具使用

### 1. 基本使用

```javascript
import api from '@/api/index.js'

// 登录
const loginData = {
  username: 'test',
  password: '123456'
}
const result = await api.auth.login(loginData)

// 获取用户信息
const userInfo = await api.auth.getUserInfo()

// 获取问卷列表
const questionnaires = await api.questionnaire.getList({
  page: 1,
  pageSize: 20
})
```

### 2. 错误处理

请求工具已内置错误处理，会自动显示错误提示。如需自定义处理：

```javascript
try {
  const result = await api.auth.login(loginData)
  // 处理成功结果
} catch (error) {
  // 自定义错误处理
  console.error('登录失败:', error)
  // 可以根据error.code或error.message进行不同处理
}
```

### 3. 文件上传

```javascript
// 选择图片并上传
uni.chooseImage({
  count: 1,
  success: async (res) => {
    const tempFilePath = res.tempFilePaths[0]
    try {
      const result = await api.file.uploadImage(tempFilePath, {
        type: 'avatar'  // 额外的表单数据
      })
      console.log('上传成功:', result)
    } catch (error) {
      console.error('上传失败:', error)
    }
  }
})
```

### 4. 请求拦截

如需添加自定义请求头或参数，可以在调用时传入options：

```javascript
const result = await api.questionnaire.getList(
  { page: 1 },
  {
    header: {
      'X-Custom-Header': 'value'
    }
  }
)
```

## 注意事项

1. **环境切换**：修改 `config/index.js` 中的 `env` 字段或使用环境变量来切换环境
2. **Token管理**：Token会自动从缓存读取并添加到请求头，无需手动处理
3. **401处理**：当收到401响应时，会自动清除登录信息并跳转到登录页
4. **网络错误**：网络连接失败时会自动显示提示，无需额外处理
5. **响应格式**：默认期望后端返回格式为 `{ code: 200/0, data: {}, message: '' }`

## 调试

在开发环境中，可以通过以下方式调试请求：

1. 在请求工具中添加日志：
```javascript
// 在 /src/utils/request.js 的请求拦截器中
console.log('Request:', options)

// 在响应拦截器中
console.log('Response:', response)
```

2. 使用微信开发者工具的Network面板查看请求详情

3. 检查后端日志确认请求是否正确到达 