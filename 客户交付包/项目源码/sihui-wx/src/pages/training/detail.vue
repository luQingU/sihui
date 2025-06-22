<template>
  <view class="detail-container">
    <!-- 课程横幅 -->
    <view class="course-banner">
      <image class="banner-image" :src="course.cover" mode="aspectFill"></image>
      <view class="banner-overlay">
        <view class="play-btn" @click="startLearning">
          <uni-icons type="play-filled" size="30" color="#fff"></uni-icons>
        </view>
      </view>
    </view>

    <!-- 课程信息 -->
    <view class="course-info">
      <view class="course-title">{{ course.title }}</view>
      <view class="course-meta">
        <view class="meta-item">
          <uni-icons type="person" size="14" color="#999"></uni-icons>
          <text>{{ course.instructor }}</text>
        </view>
        <view class="meta-item">
          <uni-icons type="clock" size="14" color="#999"></uni-icons>
          <text>{{ course.duration }}</text>
        </view>
        <view class="meta-item">
          <uni-icons type="eye" size="14" color="#999"></uni-icons>
          <text>{{ course.students }}人学习</text>
        </view>
      </view>
      <view class="course-price">
        <text class="price-current">¥{{ course.price }}</text>
        <text class="price-original">¥{{ course.originalPrice }}</text>
      </view>
    </view>

    <!-- 课程简介 -->
    <view class="course-section">
      <view class="section-title">课程简介</view>
      <view class="section-content">
        <text>{{ course.description }}</text>
      </view>
    </view>

    <!-- 底部固定按钮 -->
    <view class="bottom-actions">
      <view class="action-left">
        <view class="price-info">
          <text class="current-price">¥{{ course.price }}</text>
          <text class="original-price">¥{{ course.originalPrice }}</text>
        </view>
      </view>
      <view class="action-right">
        <button class="btn-collect" @click="toggleCollect">
          <uni-icons 
            :type="isCollected ? 'heart-filled' : 'heart'" 
            size="18" 
            :color="isCollected ? '#ff6b6b' : '#999'"
          ></uni-icons>
          {{ isCollected ? '已收藏' : '收藏' }}
        </button>
        <button class="btn-buy" @click="buyCourse">立即学习</button>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      courseId: '',
      isCollected: false,
      course: {
        id: '1',
        title: 'Vue3从入门到精通',
        instructor: '张老师',
        duration: '48小时',
        students: 1258,
        price: 199,
        originalPrice: 299,
        cover: '/static/course-banner-placeholder.jpg',
        description: 'Vue3是目前最新的Vue.js版本，带来了Composition API、更好的TypeScript支持、更小的包体积等众多特性。本课程将从基础语法开始，逐步深入到Vue3的高级特性，帮助您快速掌握Vue3开发技能。'
      }
    }
  },
  
  onLoad(options) {
    if (options.id) {
      this.courseId = options.id
    }
    this.loadCourseDetail()
  },

  methods: {
    // 加载课程详情
    async loadCourseDetail() {
      try {
        // 模拟API请求
        await new Promise(resolve => setTimeout(resolve, 500))
        
        // 检查收藏状态
        const collected = uni.getStorageSync('collected_courses') || []
        this.isCollected = collected.includes(this.courseId)
        
        console.log('课程详情加载完成')
      } catch (error) {
        console.error('加载课程详情失败:', error)
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      }
    },

    // 开始学习
    startLearning() {
      uni.navigateTo({
        url: '/pages/learning/progress'
      })
    },

    // 切换收藏状态
    toggleCollect() {
      let collected = uni.getStorageSync('collected_courses') || []
      
      if (this.isCollected) {
        // 取消收藏
        collected = collected.filter(id => id !== this.courseId)
        this.isCollected = false
        uni.showToast({
          title: '取消收藏',
          icon: 'none'
        })
      } else {
        // 添加收藏
        collected.push(this.courseId)
        this.isCollected = true
        uni.showToast({
          title: '收藏成功',
          icon: 'success'
        })
      }
      
      uni.setStorageSync('collected_courses', collected)
    },

    // 购买课程
    buyCourse() {
      uni.showToast({
        title: '开始学习',
        icon: 'success'
      })
      setTimeout(() => {
        this.startLearning()
      }, 1500)
    }
  }
}
</script>

<style lang="scss" scoped>
.detail-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 120rpx;
}

.course-banner {
  position: relative;
  height: 400rpx;
  
  .banner-image {
    width: 100%;
    height: 100%;
  }
  
  .banner-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.3);
    display: flex;
    align-items: center;
    justify-content: center;
    
    .play-btn {
      width: 120rpx;
      height: 120rpx;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.2);
      backdrop-filter: blur(10rpx);
      display: flex;
      align-items: center;
      justify-content: center;
      border: 2rpx solid rgba(255, 255, 255, 0.3);
      
      &:active {
        transform: scale(0.95);
      }
    }
  }
}

.course-info {
  background-color: #fff;
  padding: 32rpx;
  margin-bottom: 20rpx;
  
  .course-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 20rpx;
    line-height: 1.4;
  }
  
  .course-meta {
    display: flex;
    align-items: center;
    margin-bottom: 24rpx;
    
    .meta-item {
      display: flex;
      align-items: center;
      margin-right: 40rpx;
      font-size: 26rpx;
      color: #666;
      
      text {
        margin-left: 8rpx;
      }
    }
  }
  
  .course-price {
    display: flex;
    align-items: center;
    
    .price-current {
      font-size: 36rpx;
      font-weight: 600;
      color: #ff4757;
      margin-right: 16rpx;
    }
    
    .price-original {
      font-size: 26rpx;
      color: #999;
      text-decoration: line-through;
    }
  }
}

.course-section {
  background-color: #fff;
  margin-bottom: 20rpx;
  
  .section-title {
    padding: 32rpx 32rpx 24rpx;
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
    border-bottom: 1rpx solid #f0f0f0;
  }
  
  .section-content {
    padding: 24rpx 32rpx 32rpx;
    font-size: 28rpx;
    line-height: 1.6;
    color: #666;
  }
}

.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 120rpx;
  background-color: #fff;
  border-top: 1rpx solid #eee;
  display: flex;
  align-items: center;
  padding: 0 32rpx;
  z-index: 100;
  
  .action-left {
    flex: 1;
    
    .price-info {
      .current-price {
        font-size: 32rpx;
        font-weight: 600;
        color: #ff4757;
        margin-right: 12rpx;
      }
      
      .original-price {
        font-size: 24rpx;
        color: #999;
        text-decoration: line-through;
      }
    }
  }
  
  .action-right {
    display: flex;
    align-items: center;
    
    .btn-collect {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 140rpx;
      height: 72rpx;
      background-color: #f8f8f8;
      border: none;
      border-radius: 36rpx;
      font-size: 26rpx;
      color: #666;
      margin-right: 20rpx;
      
      &:active {
        background-color: #eee;
      }
    }
    
    .btn-buy {
      width: 200rpx;
      height: 72rpx;
      background: linear-gradient(135deg, #2979ff, #1e88e5);
      border: none;
      border-radius: 36rpx;
      font-size: 28rpx;
      color: #fff;
      font-weight: 500;
      
      &:active {
        transform: scale(0.98);
      }
    }
  }
}
</style> 