import { defineStore } from 'pinia'
import { login as loginApi, check as checkApi } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userId: null,
    username: '',
    type: ''
  }),
  actions: {
    async login(payload) {
      const res = await loginApi(payload)
      this.token = res.data.token
      this.userId = res.data.userId
      this.username = res.data.username
      this.type = res.data.type
      localStorage.setItem('token', res.data.token)
      return res.data
    },
    async check() {
      if (!this.token) return null
      const res = await checkApi()
      if (res.data) {
        this.userId = res.data.userId
        this.username = res.data.username
        this.type = res.data.type
      }
      return res.data
    },
    logout() {
      this.token = ''
      this.userId = null
      this.username = ''
      this.type = ''
      localStorage.removeItem('token')
    }
  }
})
