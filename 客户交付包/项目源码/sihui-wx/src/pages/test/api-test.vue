<template>
  <view class="test-container">
    <view class="test-header">
      <text class="title">API测试页面</text>
      <text class="subtitle">当前环境: {{ currentEnv }}</text>
      <text class="subtitle">API地址: {{ baseURL }}</text>
    </view>
    
    <view class="test-section">
      <view class="section-title">配置信息</view>
      <view class="info-item" v-for="(item, index) in configInfo" :key="index">
        <text class="label">{{ item.label }}:</text>
        <text class="value">{{ item.value }}</text>
      </view>
    </view>
    
    <view class="test-section">
      <view class="section-title">API测试</view>
      
      <view class="test-item">
        <button class="test-btn" @click="testLogin">测试登录接口</button>
        <view v-if="loginResult" class="result">
          <text class="result-title">登录结果:</text>
          <text class="result-content">{{ loginResult }}</text>
        </view>
      </view>
      
      <view class="test-item">
        <button class="test-btn" @click="testGetUserInfo">获取用户信息</button>
        <view v-if="userInfoResult" class="result">
          <text class="result-title">用户信息:</text>
          <text class="result-content">{{ userInfoResult }}</text>
        </view>
      </view>
      
      <view class="test-item">
        <button class="test-btn" @click="testGetQuestionnaires">获取问卷列表</button>
        <view v-if="questionnairesResult" class="result">
          <text class="result-title">问卷列表:</text>
          <text class="result-content">{{ questionnairesResult }}</text>
        </view>
      </view>
      
      <view class="test-item">
        <button class="test-btn" @click="testUploadImage">测试图片上传</button>
        <view v-if="uploadResult" class="result">
          <text class="result-title">上传结果:</text>
          <text class="result-content">{{ uploadResult }}</text>
        </view>
      </view>
    </view>
    
    <view class="test-section">
      <view class="section-title">缓存信息</view>
      <view class="cache-item">
        <text class="label">Token:</text>
        <text class="value">{{ token || '未登录' }}</text>
      </view>
      <button class="clear-btn" @click="clearCache">清除缓存</button>
    </view>
  </view>
</template>

<script>
import api from '@/api/index.js'
import config from '@/config/index.js'

export default {
  data() {
    return {
      currentEnv: config.env,
      baseURL: config.baseURL,
      loginResult: '',
      userInfoResult: '',
      questionnairesResult: '',
      uploadResult: '',
      token: ''
    }
  },
  
  computed: {
    configInfo() {
      return [
        { label: '应用名称', value: config.app.name },
        { label: '应用版本', value: config.app.version },
        { label: '请求超时', value: config.requestTimeout + 'ms' },
        { label: '文件大小限制', value: (config.upload.maxSize / 1024 / 1024) + 'MB' },
        { label: '分页大小', value: config.pagination.pageSize + '条' }
      ]
    }
  },
  
  onShow() {
    this.loadCacheInfo()
  },
  
  methods: {
    // 加载缓存信息
    loadCacheInfo() {
      this.token = uni.getStorageSync(config.cache.tokenKey) || ''
    },
    
    // 测试登录
    async testLogin() {
      try {
        const result = await api.auth.login({
          username: 'test',
          password: '123456'
        })
        this.loginResult = JSON.stringify(result, null, 2)
        this.loadCacheInfo()
        uni.showToast({
          title: '登录成功',
          icon: 'success'
        })
      } catch (error) {
        this.loginResult = `错误: ${error.message || error}`
      }
    },
    
    // 测试获取用户信息
    async testGetUserInfo() {
      try {
        const result = await api.auth.getUserInfo()
        this.userInfoResult = JSON.stringify(result, null, 2)
      } catch (error) {
        this.userInfoResult = `错误: ${error.message || error}`
      }
    },
    
    // 测试获取问卷列表
    async testGetQuestionnaires() {
      try {
        const result = await api.questionnaire.getList({
          page: 1,
          pageSize: 10
        })
        this.questionnairesResult = JSON.stringify(result, null, 2)
      } catch (error) {
        this.questionnairesResult = `错误: ${error.message || error}`
      }
    },
    
    // 测试图片上传
    async testUploadImage() {
      uni.chooseImage({
        count: 1,
        success: async (res) => {
          try {
            const result = await api.file.uploadImage(res.tempFilePaths[0], {
              type: 'test'
            })
            this.uploadResult = JSON.stringify(result, null, 2)
            uni.showToast({
              title: '上传成功',
              icon: 'success'
            })
          } catch (error) {
            this.uploadResult = `错误: ${error.message || error}`
          }
        }
      })
    },
    
    // 清除缓存
    clearCache() {
      uni.removeStorageSync(config.cache.tokenKey)
      uni.removeStorageSync(config.cache.userInfoKey)
      this.token = ''
      uni.showToast({
        title: '缓存已清除',
        icon: 'success'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.test-container {
  padding: 30rpx;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.test-header {
  background: #fff;
  padding: 30rpx;
  border-radius: 20rpx;
  margin-bottom: 30rpx;
  
  .title {
    display: block;
    font-size: 36rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 20rpx;
  }
  
  .subtitle {
    display: block;
    font-size: 28rpx;
    color: #666;
    margin-bottom: 10rpx;
  }
}

.test-section {
  background: #fff;
  padding: 30rpx;
  border-radius: 20rpx;
  margin-bottom: 30rpx;
  
  .section-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 30rpx;
    padding-bottom: 20rpx;
    border-bottom: 1rpx solid #eee;
  }
}

.info-item {
  display: flex;
  padding: 15rpx 0;
  
  .label {
    width: 200rpx;
    color: #666;
    font-size: 28rpx;
  }
  
  .value {
    flex: 1;
    color: #333;
    font-size: 28rpx;
  }
}

.test-item {
  margin-bottom: 30rpx;
  
  .test-btn {
    width: 100%;
    height: 80rpx;
    line-height: 80rpx;
    background: #667eea;
    color: #fff;
    border-radius: 10rpx;
    font-size: 28rpx;
    border: none;
    
    &::after {
      border: none;
    }
  }
  
  .result {
    margin-top: 20rpx;
    padding: 20rpx;
    background: #f5f5f5;
    border-radius: 10rpx;
    
    .result-title {
      display: block;
      font-size: 26rpx;
      color: #666;
      margin-bottom: 10rpx;
    }
    
    .result-content {
      display: block;
      font-size: 24rpx;
      color: #333;
      white-space: pre-wrap;
      word-break: break-all;
    }
  }
}

.cache-item {
  display: flex;
  padding: 20rpx 0;
  
  .label {
    width: 150rpx;
    color: #666;
    font-size: 28rpx;
  }
  
  .value {
    flex: 1;
    color: #333;
    font-size: 28rpx;
    word-break: break-all;
  }
}

.clear-btn {
  width: 100%;
  height: 80rpx;
  line-height: 80rpx;
  background: #ff4444;
  color: #fff;
  border-radius: 10rpx;
  font-size: 28rpx;
  border: none;
  margin-top: 30rpx;
  
  &::after {
    border: none;
  }
}
</style> 