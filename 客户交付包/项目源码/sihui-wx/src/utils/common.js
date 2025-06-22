// 通用工具类
export default {
  /**
   * 页面导航
   * @param {String} url 目标页面路径
   * @param {String} type 导航类型：navigateTo, redirectTo, reLaunch, switchTab
   */
  navigateTo(url, type = 'navigateTo') {
    if (!url) {
      console.error('导航路径不能为空')
      return
    }
    
    uni[type]({
      url,
      fail: (err) => {
        console.error('页面导航失败:', err)
      }
    })
  },

  /**
   * 显示提示信息
   * @param {String} title 提示文字
   * @param {String} icon 图标类型：success, error, none
   * @param {Number} duration 显示时长
   */
  showToast(title, icon = 'none', duration = 2000) {
    uni.showToast({
      title,
      icon,
      duration
    })
  },

  /**
   * 显示加载中
   * @param {String} title 加载文字
   */
  showLoading(title = '加载中...') {
    uni.showLoading({
      title
    })
  },

  /**
   * 隐藏加载中
   */
  hideLoading() {
    uni.hideLoading()
  },

  /**
   * 显示确认对话框
   * @param {String} title 标题
   * @param {String} content 内容
   * @param {Function} confirmCallback 确认回调
   * @param {Function} cancelCallback 取消回调
   */
  showModal(title, content, confirmCallback, cancelCallback) {
    uni.showModal({
      title,
      content,
      success: (res) => {
        if (res.confirm && confirmCallback) {
          confirmCallback()
        } else if (res.cancel && cancelCallback) {
          cancelCallback()
        }
      }
    })
  },

  /**
   * 本地存储操作
   */
  storage: {
    // 设置存储
    set(key, value) {
      try {
        uni.setStorageSync(key, value)
      } catch (error) {
        console.error('存储设置失败:', error)
      }
    },
    
    // 获取存储
    get(key, defaultValue = null) {
      try {
        return uni.getStorageSync(key) || defaultValue
      } catch (error) {
        console.error('存储获取失败:', error)
        return defaultValue
      }
    },
    
    // 删除存储
    remove(key) {
      try {
        uni.removeStorageSync(key)
      } catch (error) {
        console.error('存储删除失败:', error)
      }
    },
    
    // 清空存储
    clear() {
      try {
        uni.clearStorageSync()
      } catch (error) {
        console.error('存储清空失败:', error)
      }
    }
  },

  /**
   * 格式化日期
   * @param {Date} date 日期对象
   * @param {String} format 格式化字符串
   */
  formatDate(date, format = 'YYYY-MM-DD') {
    if (!date) return ''
    
    const d = new Date(date)
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const hours = String(d.getHours()).padStart(2, '0')
    const minutes = String(d.getMinutes()).padStart(2, '0')
    const seconds = String(d.getSeconds()).padStart(2, '0')
    
    return format
      .replace('YYYY', year)
      .replace('MM', month)
      .replace('DD', day)
      .replace('HH', hours)
      .replace('mm', minutes)
      .replace('ss', seconds)
  },

  /**
   * 防抖函数
   * @param {Function} func 要防抖的函数
   * @param {Number} delay 延迟时间
   */
  debounce(func, delay = 300) {
    let timer = null
    return function(...args) {
      if (timer) clearTimeout(timer)
      timer = setTimeout(() => {
        func.apply(this, args)
      }, delay)
    }
  },

  /**
   * 节流函数
   * @param {Function} func 要节流的函数
   * @param {Number} delay 延迟时间
   */
  throttle(func, delay = 300) {
    let timer = null
    return function(...args) {
      if (!timer) {
        timer = setTimeout(() => {
          func.apply(this, args)
          timer = null
        }, delay)
      }
    }
  },

  /**
   * 验证手机号
   * @param {String} phone 手机号
   */
  isValidPhone(phone) {
    const phoneRegex = /^1[3-9]\d{9}$/
    return phoneRegex.test(phone)
  },

  /**
   * 验证邮箱
   * @param {String} email 邮箱
   */
  isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    return emailRegex.test(email)
  },

  /**
   * 深拷贝
   * @param {*} obj 要拷贝的对象
   */
  deepClone(obj) {
    if (obj === null || typeof obj !== 'object') return obj
    if (obj instanceof Date) return new Date(obj)
    if (obj instanceof Array) return obj.map(item => this.deepClone(item))
    if (typeof obj === 'object') {
      const clonedObj = {}
      for (const key in obj) {
        if (obj.hasOwnProperty(key)) {
          clonedObj[key] = this.deepClone(obj[key])
        }
      }
      return clonedObj
    }
  }
} 