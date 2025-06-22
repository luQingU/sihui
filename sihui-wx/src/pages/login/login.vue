<template>
  <view class="login-container">
    <view class="login-header">
      <image class="logo" src="/static/logo.png" mode="aspectFit"></image>
      <text class="app-name">{{ appName }}</text>
    </view>
    
    <view class="login-form">
      <view class="form-item">
        <uni-icons type="person" size="20" color="#999"></uni-icons>
        <input 
          v-model="formData.username" 
          type="text" 
          placeholder="请输入用户名"
          placeholder-class="placeholder"
          @confirm="handleLogin"
        />
      </view>
      
      <view class="form-item">
        <uni-icons type="locked" size="20" color="#999"></uni-icons>
        <input 
          v-model="formData.password" 
          type="password" 
          placeholder="请输入密码"
          placeholder-class="placeholder"
          @confirm="handleLogin"
        />
      </view>
      
      <button 
        class="login-btn" 
        type="primary" 
        :loading="loading"
        :disabled="!canSubmit"
        @click="handleLogin"
      >
        {{ loading ? '登录中...' : '登录' }}
      </button>
      
      <view class="login-tips">
        <text class="tip-text">忘记密码？请联系管理员</text>
      </view>
    </view>
    
    <view class="login-footer">
      <text class="footer-text">© 2024 思汇投票 版权所有</text>
    </view>
  </view>
</template>

<script>
import api from '@/api/index.js'
import config from '@/config/index.js'

export default {
  data() {
    return {
      appName: config.app.name,
      formData: {
        username: '',
        password: ''
      },
      loading: false
    }
  },
  
  computed: {
    canSubmit() {
      return this.formData.username.trim() && this.formData.password.trim() && !this.loading
    }
  },
  
  onLoad() {
    // 检查是否已登录
    this.checkLoginStatus()
  },
  
  methods: {
    // 检查登录状态
    async checkLoginStatus() {
      const token = uni.getStorageSync(config.cache.tokenKey)
      if (token) {
        // 验证token是否有效
        try {
          await api.auth.getUserInfo()
          // token有效，直接跳转到首页
          uni.reLaunch({
            url: '/pages/index/index'
          })
        } catch (error) {
          // token无效，清除缓存
          uni.removeStorageSync(config.cache.tokenKey)
          uni.removeStorageSync(config.cache.userInfoKey)
        }
      }
    },
    
    // 处理登录
    async handleLogin() {
      if (!this.canSubmit) return
      
      // 表单验证
      if (!this.formData.username.trim()) {
        uni.showToast({
          title: '请输入用户名',
          icon: 'none'
        })
        return
      }
      
      if (!this.formData.password.trim()) {
        uni.showToast({
          title: '请输入密码',
          icon: 'none'
        })
        return
      }
      
      this.loading = true
      
      try {
        // 调用登录接口
        const loginResult = await api.auth.login({
          username: this.formData.username.trim(),
          password: this.formData.password.trim()
        })
        
        // 保存token
        if (loginResult.token) {
          uni.setStorageSync(config.cache.tokenKey, loginResult.token)
        }
        
        // 获取用户信息
        const userInfo = await api.auth.getUserInfo()
        
        // 保存用户信息
        uni.setStorageSync(config.cache.userInfoKey, userInfo)
        
        // 显示登录成功提示
        uni.showToast({
          title: '登录成功',
          icon: 'success'
        })
        
        // 延迟跳转到首页
        setTimeout(() => {
          uni.reLaunch({
            url: '/pages/index/index'
          })
        }, 1500)
        
      } catch (error) {
        console.error('登录失败:', error)
        // 错误提示已由请求工具处理
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  padding: 0 40rpx;
}

.login-header {
  margin-top: 120rpx;
  text-align: center;
  
  .logo {
    width: 160rpx;
    height: 160rpx;
    border-radius: 50%;
    box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.2);
  }
  
  .app-name {
    display: block;
    margin-top: 30rpx;
    font-size: 36rpx;
    font-weight: bold;
    color: #ffffff;
    letter-spacing: 2rpx;
  }
}

.login-form {
  margin-top: 80rpx;
  background: #ffffff;
  border-radius: 20rpx;
  padding: 60rpx 40rpx;
  box-shadow: 0 20rpx 40rpx rgba(0, 0, 0, 0.1);
  
  .form-item {
    display: flex;
    align-items: center;
    height: 100rpx;
    border-bottom: 1rpx solid #f0f0f0;
    margin-bottom: 40rpx;
    
    &:last-of-type {
      margin-bottom: 60rpx;
    }
    
    input {
      flex: 1;
      height: 100%;
      margin-left: 20rpx;
      font-size: 32rpx;
    }
    
    .placeholder {
      color: #999999;
      font-size: 30rpx;
    }
  }
  
  .login-btn {
    width: 100%;
    height: 90rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    border-radius: 45rpx;
    font-size: 32rpx;
    color: #ffffff;
    margin-bottom: 40rpx;
    
    &::after {
      border: none;
    }
    
    &[disabled] {
      background: #cccccc;
    }
  }
  
  .login-tips {
    text-align: center;
    
    .tip-text {
      font-size: 26rpx;
      color: #999999;
    }
  }
}

.login-footer {
  position: fixed;
  bottom: 60rpx;
  left: 0;
  right: 0;
  text-align: center;
  
  .footer-text {
    font-size: 24rpx;
    color: rgba(255, 255, 255, 0.8);
  }
}
</style> 