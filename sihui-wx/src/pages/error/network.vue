<template>
  <view class="container">
    <!-- 错误图标和信息 -->
    <view class="error-content">
      <view class="error-icon">
        <uni-icons type="refresh" size="80" color="#cccccc"></uni-icons>
      </view>
      
      <text class="error-title">网络连接异常</text>
      <text class="error-desc">请检查您的网络连接，或稍后重试。如果问题持续存在，请联系客服。</text>
      
      <!-- 操作按钮 -->
      <view class="action-buttons">
        <button class="reload-btn" @click="reloadPage">重新加载</button>
        <button class="home-btn" @click="goHome">返回首页</button>
      </view>
      
      <!-- 客服联系方式 -->
      <view class="contact-info">
        <text class="contact-text">遇到问题？</text>
        <text class="contact-link" @click="contactService">联系客服</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  methods: {
    reloadPage() {
      // 重新加载当前页面
      uni.showLoading({
        title: '加载中...'
      })
      
      setTimeout(() => {
        uni.hideLoading()
        uni.showToast({
          title: '加载成功',
          icon: 'success'
        })
      }, 1500)
    },
    
    goHome() {
      uni.reLaunch({
        url: '/pages/index/index'
      })
    },
    
    contactService() {
      uni.showModal({
        title: '联系客服',
        content: '客服电话：400-123-4567\n工作时间：9:00-18:00',
        showCancel: false,
        confirmText: '知道了'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f8f8f8;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48rpx;
}

.error-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  max-width: 600rpx;
  width: 100%;
}

.error-icon {
  width: 200rpx;
  height: 200rpx;
  background: linear-gradient(135deg, #f0f0f0 0%, #e0e0e0 100%);
  border-radius: 100rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 48rpx;
  box-shadow: inset 0 4rpx 8rpx rgba(0, 0, 0, 0.1);
}

.error-title {
  font-size: 40rpx;
  font-weight: 600;
  color: #333333;
  margin-bottom: 24rpx;
  display: block;
}

.error-desc {
  font-size: 28rpx;
  color: #666666;
  line-height: 1.6;
  margin-bottom: 64rpx;
  display: block;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  width: 100%;
  margin-bottom: 48rpx;
}

.reload-btn,
.home-btn {
  width: 100%;
  height: 96rpx;
  border: none;
  border-radius: 16rpx;
  font-size: 32rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.reload-btn {
  background-color: #2979ff;
  color: #ffffff;
}

.reload-btn:active {
  background-color: #1976d2;
}

.home-btn {
  background-color: #f0f0f0;
  color: #666666;
}

.home-btn:active {
  background-color: #e0e0e0;
}

.contact-info {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.contact-text {
  font-size: 24rpx;
  color: #999999;
}

.contact-link {
  font-size: 24rpx;
  color: #2979ff;
  text-decoration: underline;
}
</style> 