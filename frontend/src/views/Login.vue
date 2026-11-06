<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const form = reactive({
  username: 'baizhan',
  password: 'baizhan123',
  type: 'admin'
})

async function onSubmit() {
  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/')
  } catch (e) {
    // 错误提示由 request 拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <h1 class="login-title">亿级高并发电商平台</h1>
      <p class="login-subtitle">Spring Cloud Alibaba 管理后台</p>
      <el-form :model="form" label-width="0" @submit.prevent="onSubmit">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            size="large"
            show-password
            @keyup.enter="onSubmit"
          />
        </el-form-item>
        <el-form-item>
          <el-radio-group v-model="form.type">
            <el-radio value="admin">运营后台</el-radio>
            <el-radio value="user">C端用户</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-button
          type="primary"
          size="large"
          class="login-btn"
          :loading="loading"
          @click="onSubmit"
        >
          登 录
        </el-button>
      </el-form>
      <p class="login-tip">演示账号：baizhan / baizhan123（运营后台）、user001 / user123（C端）</p>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  background: linear-gradient(135deg, #1f2a48 0%, #2b6cb0 100%);
}
.login-card {
  width: 380px;
  padding: 40px 36px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.25);
}
.login-title {
  margin: 0;
  font-size: 20px;
  text-align: center;
  color: #1f2a48;
}
.login-subtitle {
  margin: 8px 0 24px;
  text-align: center;
  color: #909399;
  font-size: 13px;
}
.login-btn {
  width: 100%;
}
.login-tip {
  margin-top: 16px;
  font-size: 12px;
  color: #a8abb2;
  text-align: center;
}
</style>
