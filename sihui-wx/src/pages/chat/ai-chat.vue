<template>
  <view class="container">
    <!-- 聊天消息区域 -->
    <scroll-view class="chat-area" scroll-y :scroll-top="scrollTop" scroll-with-animation>
      <view class="message-list">
        <!-- AI欢迎消息 -->
        <view class="message-item">
          <view class="message-ai">
            <view class="avatar-ai">
              <uni-icons type="chat" size="16" color="#ffffff"></uni-icons>
            </view>
            <view class="message-content">
              <text class="message-text">您好！我是四会学习平台的AI助手，具备以下能力：

🎯 四会培训知识问答
📚 基于培训文档的专业解答
🔍 实时联网搜索最新信息
💬 上下文记忆对话

有任何关于四会培训（会听、会说、会做、会教）的问题都可以问我哦～</text>
            </view>
          </view>
        </view>
        
        <!-- 用户消息示例 -->
        <view class="message-item">
          <view class="message-user">
            <view class="message-content">
              <text class="message-text">什么是前端框架？React和Vue有什么区别？</text>
            </view>
            <view class="avatar-user">
              <uni-icons type="person" size="16" color="#ffffff"></uni-icons>
            </view>
          </view>
        </view>
        
        <!-- AI回复示例 -->
        <view class="message-item">
          <view class="message-ai">
            <view class="avatar-ai">
              <uni-icons type="chat" size="16" color="#ffffff"></uni-icons>
            </view>
            <view class="message-content">
              <text class="message-text">前端框架是用于构建用户界面的工具集。React和Vue的主要区别：

React:
• 由Facebook开发
• 使用JSX语法
• 生态系统更丰富

Vue:
• 学习曲线更平缓
• 模板语法更直观
• 中文文档完善</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 快速回复建议 -->
    <view class="quick-replies">
      <scroll-view class="quick-scroll" scroll-x>
        <view class="quick-item" @click="sendQuickReply('什么是四会培训？')">
          <text class="quick-text">什么是四会培训？</text>
        </view>
        <view class="quick-item" @click="sendQuickReply('如何提升客户沟通技巧？')">
          <text class="quick-text">如何提升沟通技巧？</text>
        </view>
        <view class="quick-item" @click="sendQuickReply('业务操作规范有哪些？')">
          <text class="quick-text">业务操作规范</text>
        </view>
        <view class="quick-item" @click="sendQuickReply('客户培训方法推荐')">
          <text class="quick-text">客户培训方法</text>
        </view>
      </scroll-view>
    </view>

    <!-- 输入区域 -->
    <view class="input-area">
      <view class="input-wrapper">
        <button class="attach-btn" @click="showAttachMenu">
          <uni-icons type="plus" size="20" color="#999999"></uni-icons>
        </button>
        
        <input 
          class="input-field"
          type="text"
          placeholder="输入您的问题..."
          v-model="inputText"
          @confirm="sendMessage"
          confirm-type="send"
        />
        
        <button class="send-btn" @click="sendMessage" :disabled="!inputText.trim()">
          <uni-icons type="paperplane" size="20" :color="inputText.trim() ? '#2979ff' : '#cccccc'"></uni-icons>
        </button>
        
        <button class="voice-btn" @click="startVoiceInput">
          <uni-icons type="mic" size="20" color="#999999"></uni-icons>
        </button>
      </view>
    </view>

    <!-- 底部导航栏 -->
    <TabBar :current="2"></TabBar>
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
      inputText: '',
      scrollTop: 0
    }
  },
  methods: {
    sendMessage() {
      if (!this.inputText.trim()) return
      
      uni.showToast({
        title: '消息发送功能开发中',
        icon: 'none'
      })
      
      this.inputText = ''
    },
    
    sendQuickReply(reply) {
      this.inputText = reply
      this.sendMessage()
    },
    
    showAttachMenu() {
      uni.showActionSheet({
        itemList: ['相册', '拍照', '文件'],
        success: (res) => {
          uni.showToast({
            title: '功能开发中',
            icon: 'none'
          })
        }
      })
    },
    
    startVoiceInput() {
      uni.showToast({
        title: '语音输入功能开发中',
        icon: 'none'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  height: calc(100vh - 100rpx - env(safe-area-inset-bottom));
  display: flex;
  flex-direction: column;
  background-color: #f8f8f8;
}

.chat-area {
  flex: 1;
  padding: 24rpx 0;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
  padding: 0 24rpx;
}

.message-item {
  display: flex;
  align-items: flex-start;
}

.message-ai {
  display: flex;
  align-items: flex-start;
  max-width: 80%;
  margin-left: 16rpx;
}

.message-user {
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  max-width: 80%;
  margin-left: auto;
  margin-right: 16rpx;
}

.avatar-ai,
.avatar-user {
  width: 64rpx;
  height: 64rpx;
  border-radius: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar-ai {
  background: linear-gradient(135deg, #9c27b0 0%, #673ab7 100%);
  margin-right: 24rpx;
}

.avatar-user {
  background-color: #2979ff;
  margin-left: 16rpx;
}

.message-content {
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 24rpx 32rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.1);
  max-width: 100%;
}

.message-user .message-content {
  background: linear-gradient(135deg, #2979ff 0%, #1976d2 100%);
}

.message-text {
  font-size: 30rpx;
  line-height: 1.5;
  color: #333333;
  white-space: pre-wrap;
}

.message-user .message-text {
  color: #ffffff;
}

.quick-replies {
  padding: 24rpx;
  background-color: #ffffff;
  border-top: 2rpx solid #f0f0f0;
}

.quick-scroll {
  height: 80rpx;
}

.quick-item {
  display: inline-block;
  background-color: #f0f0f0;
  color: #666666;
  padding: 16rpx 32rpx;
  border-radius: 40rpx;
  margin-right: 16rpx;
  white-space: nowrap;
}

.quick-item:active {
  background-color: #e0e0e0;
}

.quick-text {
  font-size: 24rpx;
}

.input-area {
  background-color: #ffffff;
  padding: 24rpx;
  border-top: 2rpx solid #f0f0f0;
}

.input-wrapper {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.attach-btn,
.send-btn,
.voice-btn {
  width: 72rpx;
  height: 72rpx;
  border: none;
  background-color: #f8f8f8;
  border-radius: 36rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.input-field {
  flex: 1;
  height: 72rpx;
  padding: 0 24rpx;
  background-color: #f8f8f8;
  border: 2rpx solid #f0f0f0;
  border-radius: 36rpx;
  font-size: 30rpx;
  color: #333333;
}

.input-field:focus {
  border-color: #2979ff;
  outline: none;
}
</style> 