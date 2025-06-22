<template>
  <view class="container">
    <!-- 下拉刷新提示 -->
    <view class="refresh-tip">下拉刷新以获取最新课程</view>
    
    <!-- 课程列表 -->
    <scroll-view 
      class="course-list" 
      scroll-y 
      refresher-enabled 
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onRefresh"
    >
      <view class="course-item" v-for="course in courseList" :key="course.id" @click="goToCourseDetail(course)">
        <image class="course-image" :src="course.cover" mode="aspectFill"></image>
        <view class="course-info">
          <text class="course-title">{{ course.title }}</text>
          <text class="course-desc">{{ course.description }}</text>
          <view class="course-meta">
            <view class="meta-item">
              <uni-icons type="clock" size="12" color="#999999"></uni-icons>
              <text class="meta-text">{{ course.duration }}课时</text>
            </view>
            <view class="meta-item">
              <uni-icons type="person" size="12" color="#999999"></uni-icons>
              <text class="meta-text">{{ course.studentCount }}人已学</text>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 空状态 -->
      <view class="empty-state" v-if="courseList.length === 0 && !isLoading">
        <uni-icons type="medal" size="60" color="#cccccc"></uni-icons>
        <text class="empty-text">暂无课程</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      courseList: [],
      isLoading: false,
      isRefreshing: false
    }
  },
  onLoad() {
    this.loadCourseList()
  },
  methods: {
    async loadCourseList() {
      this.isLoading = true
      
      try {
        const data = await this.fetchCourseList()
        this.courseList = data
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
    
    fetchCourseList() {
      return new Promise((resolve) => {
        setTimeout(() => {
          const mockData = [
            {
              id: 1,
              title: '前端开发进阶训练营',
              description: '深入学习React、Vue、Angular等主流框架，掌握前端工程化与性能优化。',
              cover: '/static/course-placeholder.jpg',
              duration: 48,
              studentCount: 120
            },
            {
              id: 2,
              title: '人工智能基础与应用',
              description: '从零开始学习Python、机器学习算法、深度学习框架TensorFlow和PyTorch。',
              cover: '/static/course-placeholder.jpg',
              duration: 60,
              studentCount: 85
            },
            {
              id: 3,
              title: '产品经理实战训练',
              description: '学习产品生命周期管理、用户需求分析、原型设计与敏捷开发流程。',
              cover: '/static/course-placeholder.jpg',
              duration: 32,
              studentCount: 150
            }
          ]
          resolve(mockData)
        }, 1000)
      })
    },
    
    onRefresh() {
      this.isRefreshing = true
      this.loadCourseList()
    },
    
    goToCourseDetail(course) {
      uni.navigateTo({
        url: `/pages/training/detail?id=${course.id}`
      })
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

.course-list {
  height: calc(100vh - 100rpx);
  padding: 24rpx;
}

.course-item {
  background-color: #ffffff;
  border-radius: 24rpx;
  margin-bottom: 32rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.course-item:active {
  transform: scale(0.98);
}

.course-image {
  width: 100%;
  height: 300rpx;
  background-color: #f0f0f0;
}

.course-info {
  padding: 32rpx;
}

.course-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #333333;
  display: block;
  margin-bottom: 16rpx;
}

.course-desc {
  font-size: 28rpx;
  color: #666666;
  display: block;
  margin-bottom: 24rpx;
  line-height: 1.5;
}

.course-meta {
  display: flex;
  justify-content: space-between;
}

.meta-item {
  display: flex;
  align-items: center;
}

.meta-text {
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