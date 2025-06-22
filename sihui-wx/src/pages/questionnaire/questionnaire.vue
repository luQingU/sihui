<template>
  <view class="container">
    <!-- 下拉刷新提示 -->
    <view class="refresh-tip">下拉刷新以获取最新问卷</view>
    
    <!-- 问卷列表 -->
    <scroll-view 
      class="questionnaire-list" 
      scroll-y 
      refresher-enabled 
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onRefresh"
    >
      <view class="questionnaire-item" v-for="item in questionnaireList" :key="item.id" @click="goToQuestionnaire(item)">
        <view class="questionnaire-content">
          <text class="questionnaire-title">{{ item.title }}</text>
          <text class="questionnaire-desc">{{ item.description }}</text>
          <view class="questionnaire-meta">
            <view class="status-tag" :class="item.status">
              <uni-icons :type="getStatusIcon(item.status)" size="12" :color="getStatusColor(item.status)"></uni-icons>
              <text class="status-text">{{ getStatusText(item.status) }}</text>
            </view>
            <view class="time-info">
              <uni-icons type="clock" size="12" color="#999999"></uni-icons>
              <text class="time-text">预计{{ item.estimatedTime }}分钟</text>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 空状态 -->
      <view class="empty-state" v-if="questionnaireList.length === 0 && !isLoading">
        <uni-icons type="list" size="60" color="#cccccc"></uni-icons>
        <text class="empty-text">暂无问卷</text>
      </view>
    </scroll-view>

    <!-- 底部安全区域 -->
    <view class="bottom-safe"></view>
    
    <!-- 底部导航栏 -->
    <TabBar :current="3"></TabBar>
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
      questionnaireList: [],
      isLoading: false,
      isRefreshing: false
    }
  },
  onLoad() {
    this.loadQuestionnaireList()
  },
  methods: {
    async loadQuestionnaireList() {
      this.isLoading = true
      
      try {
        const data = await this.fetchQuestionnaireList()
        this.questionnaireList = data
      } catch (error) {
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      } finally {
        this.isLoading = false
        this.isRefreshing = false
      }
    },
    
    fetchQuestionnaireList() {
      return new Promise((resolve) => {
        setTimeout(() => {
          const mockData = [
            {
              id: 1,
              title: '四会培训效果评估调查',
              description: '请对刚刚完成的四会培训（会听、会说、会做、会教）进行效果评估，您的反馈对我们改进培训质量非常重要。',
              status: 'pending',
              estimatedTime: 5,
              category: 'training_evaluation'
            },
            {
              id: 2,
              title: '客户服务满意度调研',
              description: '您对最近接受的客户服务满意吗？请帮助我们了解服务质量，持续改进服务水平。',
              status: 'completed',
              estimatedTime: 8,
              category: 'service_quality'
            },
            {
              id: 3,
              title: '新培训内容需求调研',
              description: '我们正在规划新的培训课程，希望了解您在工作中还需要哪些技能培训和知识补充。',
              status: 'pending',
              estimatedTime: 10,
              category: 'training_demand'
            },
            {
              id: 4,
              title: 'AI问答系统使用体验反馈',
              description: '您使用AI智能问答系统的体验如何？请告诉我们您的使用感受和改进建议。',
              status: 'pending',
              estimatedTime: 6,
              category: 'product_feedback'
            }
          ]
          resolve(mockData)
        }, 800)
      })
    },
    
    onRefresh() {
      this.isRefreshing = true
      this.loadQuestionnaireList()
    },
    
    goToQuestionnaire(item) {
      if (item.status === 'completed') {
        uni.showToast({
          title: '您已完成该问卷',
          icon: 'none'
        })
        return
      }
      
      uni.navigateTo({
        url: `/pages/questionnaire/fill?id=${item.id}`
      })
    },
    
    getStatusIcon(status) {
      const statusMap = {
        completed: 'checkmarkempty',
        pending: 'clock'
      }
      return statusMap[status] || 'clock'
    },
    
    getStatusColor(status) {
      const colorMap = {
        completed: '#18bc37',
        pending: '#2979ff'
      }
      return colorMap[status] || '#2979ff'
    },
    
    getStatusText(status) {
      const textMap = {
        completed: '已完成',
        pending: '待填写'
      }
      return textMap[status] || '待填写'
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f8f8f8;
}

.refresh-tip {
  text-align: center;
  padding: 32rpx;
  color: #999999;
  font-size: 28rpx;
  background-color: #ffffff;
  border-bottom: 2rpx solid #f0f0f0;
}

.questionnaire-list {
  height: calc(100vh - 100rpx);
  padding: 24rpx;
}

.questionnaire-item {
  background-color: #ffffff;
  border-radius: 24rpx;
  margin-bottom: 32rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.questionnaire-item:active {
  transform: scale(0.98);
}

.questionnaire-content {
  padding: 32rpx;
}

.questionnaire-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #333333;
  display: block;
  margin-bottom: 16rpx;
}

.questionnaire-desc {
  font-size: 28rpx;
  color: #666666;
  display: block;
  margin-bottom: 24rpx;
  line-height: 1.5;
}

.questionnaire-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-tag {
  display: flex;
  align-items: center;
  padding: 8rpx 16rpx;
  border-radius: 12rpx;
  
  &.completed {
    background-color: #e8f5e8;
  }
  
  &.pending {
    background-color: #e3f2fd;
  }
}

.status-text {
  font-size: 24rpx;
  font-weight: 500;
  margin-left: 8rpx;
}

.time-info {
  display: flex;
  align-items: center;
}

.time-text {
  font-size: 24rpx;
  color: #999999;
  margin-left: 8rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 48rpx;
}

.empty-text {
  color: #999999;
  font-size: 32rpx;
  margin-top: 32rpx;
}

.bottom-safe {
  height: calc(100rpx + env(safe-area-inset-bottom));
  background-color: transparent;
}
</style> 