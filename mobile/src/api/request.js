import { API_BASE } from '@/config/index.js'

/**
 * 统一请求封装（uni.request）
 * 约定后端响应体：{ code, message, data }
 */
export function request(options) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')
    uni.request({
      url: API_BASE + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...(options.header || {})
      },
      success: (res) => {
        const body = res.data
        if (res.statusCode === 200 && body && body.code === 200) {
          resolve(body.data)
        } else if (res.statusCode === 401 || (body && body.code === 401)) {
          uni.removeStorageSync('token')
          uni.showToast({ title: '请重新登录', icon: 'none' })
          uni.navigateTo({ url: '/pages/login/login' })
          reject(body)
        } else {
          uni.showToast({ title: (body && body.message) || '请求失败', icon: 'none' })
          reject(body)
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络异常', icon: 'none' })
        reject(err)
      }
    })
  })
}

export const get = (url, data) => request({ url, method: 'GET', data })
export const post = (url, data) => request({ url, method: 'POST', data })
