// 网络请求工具类
import config from '@/config/index.js'

// 请求拦截器
const requestInterceptor = (options) => {
  // 从缓存获取token
  const token = uni.getStorageSync(config.cache.tokenKey)
  
  // 设置请求头
  options.header = {
    'Content-Type': 'application/json',
    ...options.header
  }
  
  // 如果有token，添加到请求头
  if (token) {
    options.header['Authorization'] = `Bearer ${token}`
  }
  
  // 设置超时时间
  options.timeout = options.timeout || config.requestTimeout
  
  return options
}

// 响应拦截器
const responseInterceptor = (response, resolve, reject) => {
  const { statusCode, data } = response
  
  if (statusCode >= 200 && statusCode < 300) {
    // 请求成功
    if (data.code === 200 || data.code === 0) {
      resolve(data.data || data)
    } else {
      // 业务错误
      uni.showToast({
        title: data.message || '请求失败',
        icon: 'none'
      })
      reject(data)
    }
  } else if (statusCode === 401) {
    // 未授权，清除token并跳转到登录页
    uni.removeStorageSync(config.cache.tokenKey)
    uni.removeStorageSync(config.cache.userInfoKey)
    uni.showToast({
      title: '请重新登录',
      icon: 'none'
    })
    setTimeout(() => {
      uni.reLaunch({
        url: '/pages/login/login'
      })
    }, 1500)
    reject(new Error('未授权'))
  } else {
    // HTTP错误
    const errorMsg = getHttpErrorMessage(statusCode)
    uni.showToast({
      title: errorMsg,
      icon: 'none'
    })
    reject(new Error(errorMsg))
  }
}

// 获取HTTP错误信息
const getHttpErrorMessage = (statusCode) => {
  const messages = {
    400: '请求参数错误',
    403: '没有权限访问',
    404: '请求的资源不存在',
    500: '服务器错误',
    502: '网关错误',
    503: '服务不可用'
  }
  return messages[statusCode] || `请求失败(${statusCode})`
}

// 封装请求方法
const request = (options) => {
  return new Promise((resolve, reject) => {
    // 应用请求拦截器
    const finalOptions = requestInterceptor({
      ...options,
      url: options.url.startsWith('http') ? options.url : config.baseURL + options.url,
      fail: (error) => {
        // 网络错误
        if (error.errMsg.includes('request:fail')) {
          uni.showToast({
            title: '网络连接失败',
            icon: 'none'
          })
        }
        reject(error)
      },
      success: (response) => {
        // 应用响应拦截器
        responseInterceptor(response, resolve, reject)
      }
    })
    
    // 发起请求
    uni.request(finalOptions)
  })
}

// 导出请求方法
export default {
  // GET请求
  get(url, params = {}, options = {}) {
    return request({
      url,
      method: 'GET',
      data: params,
      ...options
    })
  },
  
  // POST请求
  post(url, data = {}, options = {}) {
    return request({
      url,
      method: 'POST',
      data,
      ...options
    })
  },
  
  // PUT请求
  put(url, data = {}, options = {}) {
    return request({
      url,
      method: 'PUT',
      data,
      ...options
    })
  },
  
  // DELETE请求
  delete(url, params = {}, options = {}) {
    return request({
      url,
      method: 'DELETE',
      data: params,
      ...options
    })
  },
  
  // 文件上传
  upload(url, filePath, formData = {}, options = {}) {
    return new Promise((resolve, reject) => {
      const token = uni.getStorageSync(config.cache.tokenKey)
      
      uni.uploadFile({
        url: config.baseURL + url,
        filePath,
        name: 'file',
        formData,
        header: {
          'Authorization': token ? `Bearer ${token}` : '',
          ...options.header
        },
        timeout: config.requestTimeout,
        success: (response) => {
          try {
            const data = JSON.parse(response.data)
            if (response.statusCode === 200 && (data.code === 200 || data.code === 0)) {
              resolve(data.data || data)
            } else {
              uni.showToast({
                title: data.message || '上传失败',
                icon: 'none'
              })
              reject(data)
            }
          } catch (e) {
            reject(new Error('响应数据解析失败'))
          }
        },
        fail: (error) => {
          uni.showToast({
            title: '上传失败',
            icon: 'none'
          })
          reject(error)
        }
      })
    })
  }
}

// 导出配置，方便其他地方使用
export { config } 