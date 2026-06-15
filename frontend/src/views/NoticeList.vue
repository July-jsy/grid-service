<script setup>
import { onMounted, reactive, ref } from 'vue'
import request from '../api'

const user = JSON.parse(localStorage.getItem('grid-user') || '{}')
const isAdmin = user.role === '管理员'
const rows = ref([])
const show = ref(false)
const form = reactive({ title: '', category: '社区通知', content: '' })
async function load() { rows.value = await request.get('/notices') }
async function save() { await request.post('/notices', form); show.value = false; await load() }
async function remove(id) { if (confirm('确定删除该公告吗？')) { await request.delete(`/notices/${id}`); await load() } }
function format(value) { return value?.replace('T', ' ').slice(0, 16) }
onMounted(load)
</script>

<template>
  <section class="panel">
    <div class="panel-head"><div><p class="eyebrow">COMMUNITY NOTICE</p><h3>通知公告</h3></div><button v-if="isAdmin" class="primary" @click="show = true">发布公告</button></div>
    <div class="notice-list"><article v-for="row in rows" :key="row.id" class="notice-card"><div><span class="tag">{{ row.category }}</span><h3>{{ row.title }}</h3><p>{{ row.content }}</p><span class="muted">{{ format(row.publishedAt) }}</span></div><button v-if="isAdmin" class="text-btn danger" @click="remove(row.id)">删除</button></article></div>
  </section>
  <div v-if="show && isAdmin" class="modal-mask" @click.self="show = false"><form class="modal" @submit.prevent="save"><div class="panel-head"><h3>发布公告</h3><button type="button" class="ghost" @click="show = false">关闭</button></div><label>公告标题<input v-model="form.title" required /></label><label>公告分类<select v-model="form.category"><option>社区通知</option><option>便民服务</option><option>政策宣传</option><option>社区活动</option></select></label><label>公告内容<textarea v-model="form.content" required></textarea></label><button class="primary wide">发布</button></form></div>
</template>
