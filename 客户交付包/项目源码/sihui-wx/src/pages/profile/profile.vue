<template>
  <view class="container">
    <!-- 用户信息卡片 -->
    <view class="profile-card">
      <image class="avatar" :src="userInfo.avatar || '/static/avatar-placeholder.png'" mode="aspectFill"></image>
      <view class="user-info">
        <text class="username">{{ userInfo.name || '张三' }}</text>
        <text class="user-id">用户ID: {{ userInfo.id || '123456' }}</text>
        <text class="user-email">邮箱: {{ userInfo.email || 'zhangsan@example.com' }}</text>
      </view>
      <uni-icons type="gear" size="20" color="#2979ff" @click="editProfile"></uni-icons>
    </view>

    <!-- 功能菜单列表 -->
    <view class="menu-list">
      <view class="menu-item" @click="navigateTo('/pages/learning/progress')">
        <uni-icons type="medal-filled" size="20" color="#2979ff"></uni-icons>
        <text class="menu-text">我的课程</text>
        <uni-icons type="arrow-right" size="14" color="#cccccc"></uni-icons>
      </view>
      
      <view class="menu-item" @click="navigateTo('/pages/questionnaire/questionnaire')">
        <uni-icons type="list" size="20" color="#18bc37"></uni-icons>
        <text class="menu-text">我的问卷</text>
        <uni-icons type="arrow-right" size="14" color="#cccccc"></uni-icons>
      </view>
      
      <view class="menu-item" @click="showSettings">
        <uni-icons type="settings" size="20" color="#666666"></uni-icons>
        <text class="menu-text">设置</text>
        <uni-icons type="arrow-right" size="14" color="#cccccc"></uni-icons>
      </view>
      
      <view class="menu-item" @click="showAbout">
        <uni-icons type="help" size="20" color="#666666"></uni-icons>
        <text class="menu-text">关于我们</text>
        <uni-icons type="arrow-right" size="14" color="#cccccc"></uni-icons>
      </view>
    </view>

    <!-- 退出登录按钮 -->
    <button class="logout-btn" @click="handleLogout">退出登录</button>

    <!-- 底部占位 -->
    <view class="bottom-safe"></view>

    <!-- 自定义底部导航栏 -->
    <TabBar :current="1"></TabBar>
  </view>
</template>

<script>
import TabBar from '@/components/TabBar.vue'

export default {
  components: {
    TabBar
  },
  data() {
    return {
      userInfo: {
        name: '',
        id: '',
        email: '',
        avatar: ''
      }
    }
  },
  onLoad() {
    this.loadUserInfo()
  },
  onShow() {
    this.loadUserInfo()
  },
  methods: {
    loadUserInfo() {
      const userInfo = uni.getStorageSync('userInfo')
      if (userInfo) {
        this.userInfo = userInfo
      }
    },
    
    navigateTo(url) {
      uni.navigateTo({
        url: url
      })
    },
    
    editProfile() {
      uni.showToast({
        title: '编辑功能开发中',
        icon: 'none'
      })
    },
    
    showSettings() {
      uni.showToast({
        title: '设置功能开发中',
        icon: 'none'
      })
    },
    
    showAbout() {
      uni.showToast({
        title: '关于我们功能开发中',
        icon: 'none'
      })
    },
    
    handleLogout() {
      uni.showModal({
        title: '确认退出',
        content: '确定要退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            // 清除登录信息
            uni.removeStorageSync('token')
            uni.removeStorageSync('userInfo')
            
            uni.showToast({
              title: '已退出登录',
              icon: 'success'
            })
            
            setTimeout(() => {
              uni.reLaunch({
                url: '/pages/login/login'
              })
            }, 1500)
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f8f8f8;
  padding: 24rpx;
}

.profile-card {
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 48rpx 32rpx;
  margin-bottom: 48rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.1);
}

.avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 80rpx;
  margin-right: 32rpx;
  background-color: #f0f0f0;
  border: 4rpx solid #e0e0e0;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 40rpx;
  font-weight: 600;
  color: #333333;
  margin-bottom: 16rpx;
}

.user-id,
.user-email {
  font-size: 28rpx;
  color: #666666;
  margin-bottom: 8rpx;
}

.menu-list {
  background-color: #ffffff;
  border-radius: 24rpx;
  overflow: hidden;
  margin-bottom: 48rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.1);
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 40rpx 32rpx;
  border-bottom: 2rpx solid #f0f0f0;
  transition: background-color 0.3s ease;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:active {
  background-color: #f8f8f8;
}

.menu-text {
  flex: 1;
  font-size: 32rpx;
  color: #333333;
  font-weight: 500;
  margin-left: 32rpx;
}

.logout-btn {
  width: 100%;
  height: 96rpx;
  background-color: #e43d33;
  color: #ffffff;
  border: none;
  border-radius: 16rpx;
  font-size: 36rpx;
  font-weight: 600;
  margin-top: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logout-btn:active {
  background-color: #d32f2f;
}

.bottom-safe {
  height: 100rpx;
  padding-bottom: env(safe-area-inset-bottom);
}
</style> 