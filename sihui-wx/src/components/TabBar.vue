<template>
  <view class="tab-bar">
    <view 
      v-for="(item, index) in tabList" 
      :key="index"
      class="tab-item"
      :class="{ active: currentTab === index }"
      @click="switchTab(index)"
    >
      <uni-icons 
        :type="item.icon" 
        :size="26" 
        :color="currentTab === index ? primaryColor : inactiveColor"
      ></uni-icons>
      <text class="tab-text" :class="{ active: currentTab === index }">{{ item.text }}</text>
    </view>
  </view>
</template>

<script>
export default {
  name: 'TabBar',
  props: {
    current: {
      type: Number,
      default: 0
    }
  },
  data() {
    return {
      currentTab: 0,
      primaryColor: '#2979ff',
      inactiveColor: '#999999',
      tabList: [
        {
          icon: 'home',
          text: '首页',
          path: '/pages/index/index'
        },
        {
          icon: 'person',
          text: '我的',
          path: '/pages/profile/profile'
        }
      ]
    }
  },
  watch: {
    current: {
      handler(newVal) {
        this.currentTab = newVal
      },
      immediate: true
    }
  },
  methods: {
    switchTab(index) {
      if (this.currentTab === index) return
      
      const targetPage = this.tabList[index].path
      uni.reLaunch({
        url: targetPage
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100rpx;
  background-color: $bg-color-primary;
  border-top: 1rpx solid $border-color-light;
  display: flex;
  align-items: center;
  z-index: $z-index-tabbar;
  padding-bottom: env(safe-area-inset-bottom);
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $space-xs 0;
  transition: all 0.3s ease;
  
  &:active {
    background-color: $bg-color-secondary;
  }
}

.tab-text {
  font-size: $font-size-xs;
  color: $text-color-tertiary;
  margin-top: 4rpx;
  transition: color 0.3s ease;
  
  &.active {
    color: $primary-color;
    font-weight: 500;
  }
}
</style> 