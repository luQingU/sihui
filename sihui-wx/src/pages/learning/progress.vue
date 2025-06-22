<template>
  <view class="container">
    <!-- 学习概览 -->
    <view class="overview-card">
      <text class="overview-title">总学习概览</text>
      <view class="progress-bar">
        <text class="progress-label">总完成度:</text>
        <view class="progress-track">
          <view class="progress-fill" :style="{width: totalProgress + '%'}"></view>
        </view>
        <text class="progress-text">{{ totalProgress }}%</text>
      </view>
      <view class="stats">
        <text class="stat-text">已学习课程: {{ completedCourses }}门</text>
        <text class="stat-text">已完成课程: {{ finishedCourses }}门</text>
      </view>
    </view>

    <!-- 课程进度列表 -->
    <view class="course-section">
      <text class="section-title">我的课程进度</text>
      
      <view class="course-item" v-for="course in courseProgress" :key="course.id">
        <text class="course-name">{{ course.name }}</text>
        <view class="course-progress">
          <text class="progress-label">进度:</text>
          <view class="progress-track">
            <view class="progress-fill" :style="{width: course.progress + '%', backgroundColor: course.progressColor}"></view>
          </view>
          <text class="progress-text" :style="{color: course.progressColor}">{{ course.progress }}%</text>
        </view>
        <button class="continue-btn" :class="course.progress === 0 ? 'start-btn' : ''" @click="continueLearning(course)">
          {{ course.progress === 0 ? '开始学习' : '继续学习' }}
        </button>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      totalProgress: 75,
      completedCourses: 5,
      finishedCourses: 3,
      courseProgress: [
        {
          id: 1,
          name: '前端开发进阶训练营',
          progress: 90,
          progressColor: '#18bc37'
        },
        {
          id: 2,
          name: '人工智能基础与应用',
          progress: 40,
          progressColor: '#f3a73f'
        },
        {
          id: 3,
          name: '产品经理实战训练',
          progress: 0,
          progressColor: '#cccccc'
        }
      ]
    }
  },
  methods: {
    continueLearning(course) {
      if (course.progress === 0) {
        uni.showToast({
          title: '开始学习',
          icon: 'success'
        })
      } else {
        uni.showToast({
          title: '继续学习',
          icon: 'success'
        })
      }
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

.overview-card {
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 48rpx 32rpx;
  margin-bottom: 48rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.1);
}

.overview-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #333333;
  display: block;
  margin-bottom: 32rpx;
}

.progress-bar {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.progress-label {
  font-size: 28rpx;
  color: #666666;
  margin-right: 16rpx;
  min-width: 120rpx;
}

.progress-track {
  flex: 1;
  height: 16rpx;
  background-color: #f0f0f0;
  border-radius: 8rpx;
  margin-right: 16rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background-color: #2979ff;
  border-radius: 8rpx;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 28rpx;
  font-weight: 600;
  color: #2979ff;
  min-width: 80rpx;
  text-align: right;
}

.stats {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.stat-text {
  font-size: 24rpx;
  color: #999999;
}

.course-section {
  margin-bottom: 32rpx;
}

.section-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #333333;
  display: block;
  margin-bottom: 24rpx;
}

.course-item {
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.1);
}

.course-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #333333;
  display: block;
  margin-bottom: 24rpx;
}

.course-progress {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.continue-btn {
  width: 100%;
  height: 72rpx;
  background-color: #e3f2fd;
  color: #2979ff;
  border: none;
  border-radius: 12rpx;
  font-size: 28rpx;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
}

.start-btn {
  background-color: #2979ff;
  color: #ffffff;
}

.continue-btn:active {
  opacity: 0.8;
}
</style> 