<template>
  <view class="container">
    <!-- 用户欢迎信息 -->
    <view class="welcome-card">
      <text class="welcome-text">欢迎使用四会学习平台，<text class="user-name">{{ userInfo.name || '学员' }}</text>！</text>
      <text class="welcome-desc">智能AI问答 + 四会培训资源，让学习更高效</text>
    </view>

    <!-- 四大主要功能入口 -->
    <view class="function-grid">
      <view class="function-item" @click="navigateTo('/pages/training/training')">
        <uni-icons type="medal-filled" size="40" color="#2979ff"></uni-icons>
        <text class="function-text">四会培训</text>
        <text class="function-desc">视频课程·文档资料</text>
      </view>
      
      <view class="function-item" @click="navigateTo('/pages/chat/ai-chat')">
        <uni-icons type="chat-filled" size="40" color="#9c27b0"></uni-icons>
        <text class="function-text">AI智能问答</text>
        <text class="function-desc">DeepSeek·知识检索</text>
      </view>
      
      <view class="function-item" @click="navigateTo('/pages/questionnaire/questionnaire')">
        <uni-icons type="list" size="40" color="#18bc37"></uni-icons>
        <text class="function-text">问卷调研</text>
        <text class="function-desc">数据收集·分析报告</text>
      </view>
      
      <view class="function-item" @click="navigateTo('/pages/learning/progress')">
        <uni-icons type="calendar-filled" size="40" color="#ff9800"></uni-icons>
        <text class="function-text">学习记录</text>
        <text class="function-desc">进度跟踪·历史记录</text>
      </view>
    </view>

    <!-- 底部占位 -->
    <view class="bottom-safe"></view>

    <!-- 自定义底部导航栏 -->
    <TabBar :current="0"></TabBar>
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
        name: '张三'
      }
    }
  },
  onLoad() {
    // 检查登录状态
    this.checkLoginStatus()
  },
  methods: {
    navigateTo(url) {
      uni.navigateTo({
        url: url
      })
    },
    checkLoginStatus() {
      // 检查用户登录状态
      const token = uni.getStorageSync('token')
      if (!token) {
        console.log('用户未登录')
      } else {
        // 加载用户信息
        const userInfo = uni.getStorageSync('userInfo')
        if (userInfo) {
          this.userInfo = userInfo
        }
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.container {
  @include page-container();
}

.welcome-card {
  @include card-style($space-lg);
  margin-bottom: $space-xl;
}

.welcome-text {
  font-size: $font-size-lg;
  color: $text-color-primary;
  display: block;
  margin-bottom: $space-sm;
}

.user-name {
  font-weight: 500;
  color: $primary-color;
}

.welcome-desc {
  font-size: $font-size-md;
  color: $text-color-secondary;
  display: block;
}

.function-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $space-lg;
}

.function-item {
  @include card-style($space-xl);
  @include flex-column();
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  
  &:active {
    transform: scale(0.98);
    box-shadow: $shadow-md;
  }
}

.function-text {
  font-size: $font-size-lg;
  color: $text-color-primary;
  font-weight: 500;
  margin-top: $space-md;
  text-align: center;
}

.function-desc {
  font-size: $font-size-sm;
  color: $text-color-secondary;
  margin-top: $space-xs;
  text-align: center;
}

.bottom-safe {
  @include bottom-safe();
}
</style>
