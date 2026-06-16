<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../api'

const route = useRoute()
const router = useRouter()
const title = computed(() => route.meta.title || '管理系统')
const user = JSON.parse(sessionStorage.getItem('grid-user') || '{}')
const isAdmin = computed(() => user.role === '管理员')

async function logout() {
  try { await request.post('/auth/logout') } finally {
    sessionStorage.removeItem('grid-user')
    router.push('/login')
  }
}
</script>

<template>
  <div class="shell">
    <aside class="sidebar">
      <div class="logo"><span>网</span><strong>智慧街道</strong></div>
      <nav>
        <RouterLink v-if="isAdmin" to="/" exact-active-class="active">📊 数据总览</RouterLink>
        <RouterLink v-if="isAdmin" to="/grids">🗺️ 网格管理</RouterLink>
        <RouterLink v-if="isAdmin" to="/base-info">👥 基础信息</RouterLink>
        <RouterLink to="/services">{{ isAdmin ? '📋 便民服务管理' : '📋 在线申办' }}</RouterLink>
        <RouterLink to="/events">{{ isAdmin ? '🔔 事件处置' : '🔔 我的事件' }}</RouterLink>
        <RouterLink to="/notices">📢 通知公告</RouterLink>
        <RouterLink v-if="isAdmin" to="/map">📍 地图可视化</RouterLink>
        <RouterLink v-if="isAdmin" to="/users">⚙️ 系统管理</RouterLink>
        <RouterLink to="/profile">👤 个人中心</RouterLink>
      </nav>
      <div class="sidebar-foot">社区治理 · 服务民生</div>
    </aside>
    <main class="main">
      <header class="topbar">
        <div><p class="eyebrow">GRID SERVICE CENTER</p><h2>{{ title }}</h2></div>
        <div class="user"><span>{{ user.displayName || '管理员' }}</span><button class="ghost" @click="logout">退出</button></div>
      </header>
      <div class="content"><RouterView /></div>
    </main>
  </div>
</template>
