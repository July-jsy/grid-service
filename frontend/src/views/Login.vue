<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../api'

const router = useRouter()
const form = reactive({ username: 'admin', password: 'admin123' })
const loading = ref(false)
const error = ref('')

async function login() {
  loading.value = true
  error.value = ''
  try {
    const user = await request.post('/auth/login', form)
    localStorage.setItem('grid-user', JSON.stringify(user))
    router.push('/')
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <section class="login-panel">
      <div class="brand-mark">网</div>
      <p class="eyebrow">SMART COMMUNITY</p>
      <h1>街道办网格化便民服务管理系统</h1>
      <p class="muted">连接社区治理与居民服务，让每一件事都有回应。</p>
      <form class="login-form" @submit.prevent="login">
        <label>用户名<input v-model="form.username" autocomplete="username" /></label>
        <label>密码<input v-model="form.password" type="password" autocomplete="current-password" /></label>
        <p v-if="error" class="error">{{ error }}</p>
        <button class="primary wide" :disabled="loading">{{ loading ? '登录中...' : '进入系统' }}</button>
      </form>
      <p class="login-tip">演示账号：admin / admin123</p>
    </section>
  </main>
</template>
