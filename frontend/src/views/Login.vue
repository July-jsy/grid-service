<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../api'

const router = useRouter()
const form = reactive({ username: 'admin', password: 'admin123' })
const registerForm = reactive({ username: '', displayName: '', phone: '', password: '', confirmPassword: '' })
const mode = ref('login')
const loading = ref(false)
const error = ref('')
const message = ref('')

async function login() {
  loading.value = true
  error.value = ''
  try {
    const user = await request.post('/auth/login', form)
    localStorage.setItem('grid-user', JSON.stringify(user))
    router.push(user.role === '管理员' ? '/' : '/services')
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function register() {
  error.value = ''
  message.value = ''
  if (registerForm.password !== registerForm.confirmPassword) {
    error.value = '两次输入的密码不一致'
    return
  }
  loading.value = true
  try {
    await request.post('/auth/register', registerForm)
    form.username = registerForm.username
    form.password = registerForm.password
    message.value = '注册成功，请登录'
    mode.value = 'login'
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function switchMode(value) {
  mode.value = value
  error.value = ''
  message.value = ''
}
</script>

<template>
  <main class="login-page">
    <section class="login-panel">
      <div class="brand-mark">网</div>
      <p class="eyebrow">SMART COMMUNITY</p>
      <h1>街道办网格化便民服务管理系统</h1>
      <p class="muted">连接社区治理与居民服务，让每一件事都有回应。</p>
      <div class="login-tabs"><button :class="{ active: mode === 'login' }" @click="switchMode('login')">账号登录</button><button :class="{ active: mode === 'register' }" @click="switchMode('register')">用户注册</button></div>
      <form v-if="mode === 'login'" class="login-form" @submit.prevent="login">
        <label>用户名<input v-model="form.username" autocomplete="username" /></label>
        <label>密码<input v-model="form.password" type="password" autocomplete="current-password" /></label>
        <p v-if="error" class="error">{{ error }}</p>
        <p v-if="message" class="success">{{ message }}</p>
        <button class="primary wide" :disabled="loading">{{ loading ? '登录中...' : '进入系统' }}</button>
      </form>
      <form v-else class="login-form" @submit.prevent="register">
        <label>用户名<input v-model.trim="registerForm.username" minlength="3" autocomplete="username" required /></label>
        <label>姓名<input v-model.trim="registerForm.displayName" required /></label>
        <label>联系电话<input v-model.trim="registerForm.phone" /></label>
        <label>密码<input v-model="registerForm.password" type="password" minlength="6" autocomplete="new-password" required /></label>
        <label>确认密码<input v-model="registerForm.confirmPassword" type="password" minlength="6" autocomplete="new-password" required /></label>
        <p v-if="error" class="error">{{ error }}</p>
        <button class="primary wide" :disabled="loading">{{ loading ? '注册中...' : '注册普通用户' }}</button>
      </form>
      <p v-if="mode === 'login'" class="login-tip">管理员：admin / admin123<br />普通用户：user / user123</p>
    </section>
  </main>
</template>
