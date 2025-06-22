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
        <view class="course-badge">
          <view class="type-badge" :class="course.type">
            <uni-icons :type="course.type === 'video' ? 'videocam' : 'document'" size="12" color="#ffffff"></uni-icons>
            <text class="badge-text">{{ course.type === 'video' ? '视频' : '文档' }}</text>
          </view>
        </view>
        <view class="course-info">
          <text class="course-title">{{ course.title }}</text>
          <text class="course-desc">{{ course.description }}</text>
          <view class="course-meta">
            <view class="meta-item">
              <uni-icons type="clock" size="12" color="#999999"></uni-icons>
              <text class="meta-text">{{ course.duration }}分钟</text>
            </view>
            <view class="meta-item">
              <uni-icons type="person" size="12" color="#999999"></uni-icons>
              <text class="meta-text">{{ course.studentCount }}人已学</text>
            </view>
            <view class="meta-item" v-if="course.hasDocument">
              <uni-icons type="document" size="12" color="#2979ff"></uni-icons>
              <text class="meta-text doc-text">配套文档</text>
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

    <!-- 底部安全区域 -->
    <view class="bottom-safe"></view>
    
    <!-- 底部导航栏 -->
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
              title: '四会培训：会听 - 客户需求分析',
              description: '学习如何有效倾听客户需求，掌握专业的沟通技巧，提升客户满意度。',
              cover: '/static/course-placeholder.jpg',
              duration: 24,
              studentCount: 156,
              type: 'video', // video, document
              hasDocument: true
            },
            {
              id: 2,
              title: '四会培训：会说 - 产品介绍技巧',
              description: '掌握产品介绍的核心要点，学会用简洁有力的语言打动客户。',
              cover: '/static/course-placeholder.jpg',
              duration: 18,
              studentCount: 142,
              type: 'video',
              hasDocument: true
            },
            {
              id: 3,
              title: '四会培训：会做 - 业务操作实务',
              description: '系统学习业务操作流程，掌握规范的业务处理方法和技巧。',
              cover: '/static/course-placeholder.jpg',
              duration: 36,
              studentCount: 178,
              type: 'document',
              hasDocument: true
            },
            {
              id: 4,
              title: '四会培训：会教 - 知识传授方法',
              description: '学习如何有效传授知识给客户，提升培训和指导能力。',
              cover: '/static/course-placeholder.jpg',
              duration: 30,
              studentCount: 123,
              type: 'video',
              hasDocument: true
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
  position: relative;
}

.course-item:active {
  transform: scale(0.98);
}

.course-image {
  width: 100%;
  height: 300rpx;
  background-color: #f0f0f0;
}

.course-badge {
  position: absolute;
  top: 16rpx;
  right: 16rpx;
}

.type-badge {
  padding: 8rpx 12rpx;
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &.video {
    background-color: #2979ff;
  }
  
  &.document {
    background-color: #18bc37;
  }
}

.badge-text {
  font-size: 20rpx;
  color: #ffffff;
  margin-left: 4rpx;
  font-weight: 500;
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

.doc-text {
  color: #2979ff;
  font-weight: 500;
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