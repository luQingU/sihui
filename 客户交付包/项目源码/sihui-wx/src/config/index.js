// 配置文件
const config = {
  // API基础地址配置
  api: {
    // 开发环境
    development: 'http://localhost:8080/api',
    // 生产环境
    production: 'https://your-api-domain.com/api'
  },
  
  // 当前环境
  env: process.env.NODE_ENV || 'development',
  
  // 应用配置
  app: {
    name: '思汇投票',
    version: '1.0.0'
  },
  
  // 请求超时时间（毫秒）
  requestTimeout: 30000,
  
  // 文件上传配置
  upload: {
    maxSize: 10 * 1024 * 1024, // 10MB
    allowedTypes: ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  },
  
  // 缓存配置
  cache: {
    // token缓存key
    tokenKey: 'auth_token',
    // 用户信息缓存key
    userInfoKey: 'user_info',
    // 缓存过期时间（小时）
    expireTime: 24
  },
  
  // 分页配置
  pagination: {
    pageSize: 20,
    maxPageSize: 100
  }
}

// 获取当前环境的API地址
config.baseURL = config.api[config.env] || config.api.development

export default config 