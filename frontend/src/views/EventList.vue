<script setup>
import { onMounted, reactive, ref } from 'vue'
import request from '../api'
const user = JSON.parse(localStorage.getItem('grid-user') || '{}')
const isAdmin = user.role === '管理员'
const rows = ref([]); const status = ref(''); const show = ref(false); const error = ref('')
const form = reactive({ title: '', category: '', gridName: '', reporter: '居民上报', description: '', imageUrl: '' })
const statuses = ['待处理', '处理中', '已办结']
async function load() { rows.value = await request.get('/events', { params: { status: status.value } }) }
async function save() { try { await request.post('/events', form); show.value = false; await load() } catch(e) { error.value = e.message } }
async function change(row, value) { await request.put(`/events/${row.id}/status`, { status: value }); await load() }
async function upload(event) {
  const file = event.target.files?.[0]
  if (!file) return
  const data = new FormData()
  data.append('file', file)
  try {
    const result = await request.post('/uploads/images', data)
    form.imageUrl = result.url
  } catch (e) { error.value = e.message }
}
function format(value) { return value?.replace('T', ' ').slice(0, 16) }
onMounted(load)
</script>
<template>
  <section class="panel">
    <div class="panel-head"><div><p class="eyebrow">EVENT CENTER</p><h3>{{ isAdmin ? '事件闭环处置' : '我的上报事件' }}</h3></div><button class="primary" @click="show = true">上报事件</button></div>
    <div class="toolbar"><select v-model="status" @change="load"><option value="">全部状态</option><option v-for="item in statuses" :key="item">{{ item }}</option></select></div>
    <div class="event-list"><article v-for="row in rows" :key="row.id" class="event-card"><div><span class="tag">{{ row.category }}</span><small>{{ row.code }}</small><h3>{{ row.title }}</h3><p>{{ row.description }}</p><img v-if="row.imageUrl" class="event-image" :src="row.imageUrl" :alt="row.title" /><span class="muted">{{ row.gridName }} · {{ row.reporter }} · {{ format(row.createdAt) }}</span></div><div class="event-action"><span :class="['status', `status-${row.status}`]">{{ row.status }}</span><select v-if="isAdmin" :value="row.status" @change="change(row, $event.target.value)"><option v-for="item in statuses" :key="item">{{ item }}</option></select></div></article></div>
  </section>
  <div v-if="show" class="modal-mask" @click.self="show = false"><form class="modal" @submit.prevent="save"><div class="panel-head"><h3>上报事件</h3><button type="button" class="ghost" @click="show = false">关闭</button></div><label>事件标题<input v-model="form.title" required /></label><label>事件分类<select v-model="form.category" required><option disabled value="">请选择</option><option>城市管理</option><option>公共安全</option><option>环境卫生</option><option>矛盾纠纷</option><option>民生服务</option></select></label><label>所属网格<input v-model="form.gridName" required /></label><label v-if="isAdmin">事件来源<input v-model="form.reporter" /></label><label>现场图片<input type="file" accept="image/jpeg,image/png,image/webp" @change="upload" /></label><img v-if="form.imageUrl" class="event-image" :src="form.imageUrl" alt="现场图片预览" /><label>详细描述<textarea v-model="form.description"></textarea></label><p v-if="error" class="error">{{ error }}</p><button class="primary wide">提交并自动派单</button></form></div>
</template>
