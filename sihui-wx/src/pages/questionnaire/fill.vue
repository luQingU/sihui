<template>
  <view class="container">
    <!-- 问卷头部信息 -->
    <view class="questionnaire-header">
      <text class="questionnaire-title">新课程需求调研</text>
      <text class="questionnaire-desc">感谢您参与本次调研，您的意见将帮助我们提供更好的课程内容。</text>
    </view>

    <!-- 问题列表 -->
    <view class="questions-list">
      <!-- 单选题 -->
      <view class="question-item">
        <text class="question-title">1. 您最感兴趣的课程类型是？</text>
        <view class="options-list">
          <view class="option-item" v-for="option in question1Options" :key="option.value" @click="selectSingleOption(1, option.value)">
            <view class="radio" :class="{active: answers.q1 === option.value}">
              <view class="radio-dot" v-if="answers.q1 === option.value"></view>
            </view>
            <text class="option-text">{{ option.label }}</text>
          </view>
        </view>
      </view>

      <!-- 多选题 -->
      <view class="question-item">
        <text class="question-title">2. 您希望通过何种形式学习？(可多选)</text>
        <view class="options-list">
          <view class="option-item" v-for="option in question2Options" :key="option.value" @click="selectMultipleOption(2, option.value)">
            <view class="checkbox" :class="{active: answers.q2.includes(option.value)}">
              <uni-icons v-if="answers.q2.includes(option.value)" type="checkmarkempty" size="16" color="#ffffff"></uni-icons>
            </view>
            <text class="option-text">{{ option.label }}</text>
          </view>
        </view>
      </view>

      <!-- 文本题 -->
      <view class="question-item">
        <text class="question-title">3. 您对现有课程内容有什么建议或期望？</text>
        <textarea 
          class="textarea"
          placeholder="请在这里输入您的宝贵意见..."
          v-model="answers.q3"
          maxlength="500"
        />
        <text class="char-count">{{ answers.q3.length }}/500</text>
      </view>
    </view>

    <!-- 提交按钮 -->
    <view class="submit-section">
      <button class="submit-btn" @click="submitQuestionnaire" :disabled="!canSubmit">
        提交问卷
      </button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      questionnaireId: '',
      answers: {
        q1: '',
        q2: [],
        q3: ''
      },
      question1Options: [
        { value: 'frontend', label: '前端开发' },
        { value: 'backend', label: '后端开发' },
        { value: 'ai', label: '人工智能' },
        { value: 'design', label: '产品/设计' }
      ],
      question2Options: [
        { value: 'video', label: '视频教程' },
        { value: 'live', label: '直播课程' },
        { value: 'project', label: '实战项目' },
        { value: 'offline', label: '线下研讨' }
      ]
    }
  },
  computed: {
    canSubmit() {
      return this.answers.q1 && this.answers.q2.length > 0 && this.answers.q3.trim()
    }
  },
  onLoad(options) {
    this.questionnaireId = options.id
  },
  methods: {
    selectSingleOption(questionId, value) {
      if (questionId === 1) {
        this.answers.q1 = value
      }
    },
    
    selectMultipleOption(questionId, value) {
      if (questionId === 2) {
        const index = this.answers.q2.indexOf(value)
        if (index > -1) {
          this.answers.q2.splice(index, 1)
        } else {
          this.answers.q2.push(value)
        }
      }
    },
    
    async submitQuestionnaire() {
      if (!this.canSubmit) {
        uni.showToast({
          title: '请完成所有必答题',
          icon: 'none'
        })
        return
      }
      
      uni.showLoading({
        title: '提交中...'
      })
      
      try {
        // 模拟提交
        await this.mockSubmit()
        
        uni.hideLoading()
        uni.showToast({
          title: '提交成功',
          icon: 'success'
        })
        
        setTimeout(() => {
          uni.navigateBack()
        }, 1500)
        
      } catch (error) {
        uni.hideLoading()
        uni.showToast({
          title: '提交失败',
          icon: 'none'
        })
      }
    },
    
    mockSubmit() {
      return new Promise((resolve) => {
        setTimeout(() => {
          console.log('提交答案:', this.answers)
          resolve()
        }, 1500)
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
  padding-bottom: 160rpx;
}

.questionnaire-header {
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 48rpx 32rpx;
  margin-bottom: 32rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.1);
}

.questionnaire-title {
  font-size: 40rpx;
  font-weight: bold;
  color: #333333;
  display: block;
  margin-bottom: 24rpx;
}

.questionnaire-desc {
  font-size: 28rpx;
  color: #666666;
  line-height: 1.5;
  display: block;
}

.questions-list {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.question-item {
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.1);
}

.question-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333333;
  display: block;
  margin-bottom: 32rpx;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 16rpx 0;
  cursor: pointer;
}

.radio {
  width: 40rpx;
  height: 40rpx;
  border: 4rpx solid #e0e0e0;
  border-radius: 50%;
  margin-right: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  
  &.active {
    border-color: #2979ff;
  }
}

.radio-dot {
  width: 20rpx;
  height: 20rpx;
  background-color: #2979ff;
  border-radius: 50%;
}

.checkbox {
  width: 40rpx;
  height: 40rpx;
  border: 4rpx solid #e0e0e0;
  border-radius: 8rpx;
  margin-right: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  
  &.active {
    border-color: #2979ff;
    background-color: #2979ff;
  }
}

.option-text {
  font-size: 30rpx;
  color: #333333;
  flex: 1;
}

.textarea {
  width: 100%;
  min-height: 200rpx;
  padding: 24rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 16rpx;
  font-size: 30rpx;
  color: #333333;
  line-height: 1.5;
  background-color: #ffffff;
  box-sizing: border-box;
  resize: none;
}

.textarea:focus {
  border-color: #2979ff;
  outline: none;
}

.char-count {
  display: block;
  text-align: right;
  font-size: 24rpx;
  color: #999999;
  margin-top: 16rpx;
}

.submit-section {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #ffffff;
  padding: 24rpx;
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.1);
}

.submit-btn {
  width: 100%;
  height: 96rpx;
  background-color: #2979ff;
  color: #ffffff;
  border: none;
  border-radius: 16rpx;
  font-size: 36rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.submit-btn:disabled {
  background-color: #cccccc;
}

.submit-btn:not(:disabled):active {
  background-color: #1976d2;
}
</style> 