<script setup>
import { reactive, ref } from 'vue'
import { login, register } from '@/api/auth.js'

const form = reactive({ username: '', password: '' })
const isRegister = ref(false)

async function onSubmit() {
  try {
    const data = await login({ ...form, type: 'user' })
    uni.setStorageSync('token', data.token)
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => uni.switchTab({ url: '/pages/index/index' }), 500)
  } catch (e) {
    /* 错误提示已统一处理 */
  }
}

async function onRegister() {
  try {
    await register({ ...form })
    uni.showToast({ title: '注册成功，请登录', icon: 'success' })
    isRegister = false
  } catch (e) {
    /* ignore */
  }
}
</script>

<template>
  <view class="page">
    <view class="title">{{ isRegister ? '注册账号' : '欢迎回来' }}</view>

    <view class="form">
      <input v-model="form.username" class="input" placeholder="用户名" />
      <input v-model="form.password" class="input" placeholder="密码" password />

      <button v-if="!isRegister" class="btn" @click="onSubmit">登 录</button>
      <button v-else class="btn" @click="onRegister">注 册</button>

      <text class="switch" @click="isRegister = !isRegister">
        {{ isRegister ? '已有账号，去登录' : '没有账号，去注册' }}
      </text>
      <text class="tip">演示账号：user001 / user123</text>
    </view>
  </view>
</template>

<style scoped>
.page {
  padding: 80rpx 48rpx;
}
.title {
  font-size: 48rpx;
  font-weight: 700;
  margin-bottom: 48rpx;
}
.input {
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  font-size: 30rpx;
}
.btn {
  background: linear-gradient(90deg, #2b6cb0, #1f2a48);
  color: #fff;
  border-radius: 44rpx;
  font-size: 32rpx;
  margin-top: 16rpx;
}
.switch {
  display: block;
  text-align: center;
  color: #2b6cb0;
  margin-top: 32rpx;
  font-size: 26rpx;
}
.tip {
  display: block;
  text-align: center;
  color: #909399;
  margin-top: 16rpx;
  font-size: 24rpx;
}
</style>
