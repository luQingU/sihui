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
  </view>
</template>

<script>
export default {
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
              title: '用户体验满意度调查',
              description: '您对我们平台的使用体验满意吗？请花几分钟完成此调查。',
              status: 'completed',
              estimatedTime: 5
            },
            {
              id: 2,
              title: '新课程需求调研',
              description: '我们正在规划新课程，您的宝贵意见对我们非常重要！',
              status: 'pending',
              estimatedTime: 8
            },
            {
              id: 3,
              title: '平台功能反馈',
              description: '请告诉我们您希望改进或新增哪些功能。',
              status: 'pending',
              estimatedTime: 10
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
</style> 