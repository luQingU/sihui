// API接口模块
import request from '@/utils/request.js'

// 认证相关接口
export const authApi = {
  // 登录
  login(data) {
    return request.post('/auth/login', data)
  },
  
  // 登出
  logout() {
    return request.post('/auth/logout')
  },
  
  // 获取当前用户信息
  getUserInfo() {
    return request.get('/auth/user-info')
  },
  
  // 修改密码
  changePassword(data) {
    return request.post('/auth/change-password', data)
  }
}

// 问卷相关接口
export const questionnaireApi = {
  // 获取问卷列表
  getList(params) {
    return request.get('/questionnaires', params)
  },
  
  // 获取问卷详情
  getDetail(id) {
    return request.get(`/questionnaires/${id}`)
  },
  
  // 创建问卷
  create(data) {
    return request.post('/questionnaires', data)
  },
  
  // 更新问卷
  update(id, data) {
    return request.put(`/questionnaires/${id}`, data)
  },
  
  // 删除问卷
  delete(id) {
    return request.delete(`/questionnaires/${id}`)
  },
  
  // 发布问卷
  publish(id) {
    return request.post(`/questionnaires/${id}/publish`)
  },
  
  // 关闭问卷
  close(id) {
    return request.post(`/questionnaires/${id}/close`)
  },
  
  // 获取问卷统计数据
  getStatistics(id) {
    return request.get(`/questionnaires/${id}/statistics`)
  }
}

// 答案相关接口
export const answerApi = {
  // 提交答案
  submit(data) {
    return request.post('/answers', data)
  },
  
  // 获取用户的答题记录
  getUserAnswers(params) {
    return request.get('/answers/user', params)
  },
  
  // 获取答题详情
  getDetail(id) {
    return request.get(`/answers/${id}`)
  }
}

// AI聊天相关接口
export const chatApi = {
  // 获取聊天会话列表
  getSessions(params) {
    return request.get('/chat/sessions', params)
  },
  
  // 创建新会话
  createSession(data) {
    return request.post('/chat/sessions', data)
  },
  
  // 获取会话消息历史
  getMessages(sessionId, params) {
    return request.get(`/chat/sessions/${sessionId}/messages`, params)
  },
  
  // 发送消息
  sendMessage(sessionId, data) {
    return request.post(`/chat/sessions/${sessionId}/messages`, data)
  },
  
  // 删除会话
  deleteSession(sessionId) {
    return request.delete(`/chat/sessions/${sessionId}`)
  },
  
  // 清空会话消息
  clearMessages(sessionId) {
    return request.delete(`/chat/sessions/${sessionId}/messages`)
  }
}

// 培训相关接口
export const trainingApi = {
  // 获取培训列表
  getList(params) {
    return request.get('/trainings', params)
  },
  
  // 获取培训详情
  getDetail(id) {
    return request.get(`/trainings/${id}`)
  },
  
  // 报名培训
  enroll(id) {
    return request.post(`/trainings/${id}/enroll`)
  },
  
  // 取消报名
  cancelEnroll(id) {
    return request.post(`/trainings/${id}/cancel-enroll`)
  },
  
  // 获取我的培训记录
  getMyTrainings(params) {
    return request.get('/trainings/my', params)
  }
}

// 学习进度相关接口
export const progressApi = {
  // 获取学习进度
  getProgress(params) {
    return request.get('/learning/progress', params)
  },
  
  // 更新学习进度
  updateProgress(data) {
    return request.post('/learning/progress', data)
  },
  
  // 获取学习统计
  getStatistics() {
    return request.get('/learning/statistics')
  }
}

// 文件上传接口
export const fileApi = {
  // 上传图片
  uploadImage(filePath, formData = {}) {
    return request.upload('/files/upload/image', filePath, formData)
  },
  
  // 上传文件
  uploadFile(filePath, formData = {}) {
    return request.upload('/files/upload/file', filePath, formData)
  }
}

// 通用接口
export const commonApi = {
  // 获取系统配置
  getConfig() {
    return request.get('/common/config')
  },
  
  // 获取轮播图
  getBanners() {
    return request.get('/common/banners')
  },
  
  // 获取公告列表
  getNotices(params) {
    return request.get('/common/notices', params)
  }
}

// 导出所有API
export default {
  auth: authApi,
  questionnaire: questionnaireApi,
  answer: answerApi,
  chat: chatApi,
  training: trainingApi,
  progress: progressApi,
  file: fileApi,
  common: commonApi
} 