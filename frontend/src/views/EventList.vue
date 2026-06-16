<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../api'
const user = JSON.parse(sessionStorage.getItem('grid-user') || '{}')
const isAdmin = user.role === '管理员'
const rows = ref([]); const status = ref(''); const show = ref(false); const error = ref(''); const gridList = ref([])
const form = reactive({ title: '', category: '', gridName: '', reporter: '居民上报', description: '', imageUrl: '', address: '', longitude: null, latitude: null })
const statuses = ['待处理', '处理中', '已办结']
async function load() { rows.value = await request.get('/events', { params: { status: status.value } }) }
async function openDialog() {
  Object.assign(form, { title: '', category: '', gridName: '', reporter: '居民上报', description: '', imageUrl: '', address: '', longitude: null, latitude: null })
  error.value = ''
  show.value = true
  gridList.value = await request.get('/grids')
}
async function save() { try { await request.post('/events', form); show.value = false; await load(); ElMessage.success('事件上报成功') } catch(e) { error.value = e.message } }
async function change(row, value) { await request.put(`/events/${row.id}/status`, { status: value }); await load(); ElMessage.success('状态已更新') }
async function remove(id) { if (confirm('确定删除该事件吗？')) { await request.delete(`/events/${id}`); await load() } }
async function upload(event) {
  const file = event.target.files?.[0]
  if (!file) return
  const data = new FormData()
  data.append('file', file)
  try { const result = await request.post('/uploads/images', data); form.imageUrl = result.url } catch (e) { error.value = e.message }
}
function format(value) { return value?.replace('T', ' ').slice(0, 16) }
onMounted(load)
</script>
<template>
  <section class="panel">
    <div class="panel-head"><div><p class="eyebrow">EVENT CENTER</p><h3>{{ isAdmin ? '事件闭环处置' : '我的上报事件' }}</h3></div><button class="primary" @click="openDialog">上报事件</button></div>
    <div class="toolbar"><el-select v-model="status" clearable placeholder="全部状态" @change="load" style="width:150px"><el-option v-for="item in statuses" :key="item" :label="item" :value="item" /></el-select></div>
    <div class="event-list"><article v-for="row in rows" :key="row.id" class="event-card">
      <div style="flex:1">
        <span class="tag">{{ row.category }}</span><small>{{ row.code }}</small>
        <h3>{{ row.title }}</h3>
        <p>{{ row.description }}</p>
        <p v-if="row.address" class="muted">📍 {{ row.address }}</p>
        <img v-if="row.imageUrl" class="event-image" :src="row.imageUrl" :alt="row.title" />
        <span class="muted">{{ row.gridName }} · {{ row.reporter }} · {{ format(row.createdAt) }}</span>
        <p v-if="row.handleResult" class="muted">处理结果：{{ row.handleResult }}</p>
      </div>
      <div class="event-action">
        <span :class="['status', `status-${row.status}`]">{{ row.status }}</span>
        <el-select v-if="isAdmin" :model-value="row.status" size="small" @change="change(row, $event)" style="width:100px">
          <el-option v-for="item in statuses" :key="item" :label="item" :value="item" />
        </el-select>
        <el-button v-if="isAdmin" type="danger" link size="small" @click="remove(row.id)">删除</el-button>
      </div>
    </article></div>
  </section>
  <el-dialog v-model="show" title="上报事件" width="540px">
    <el-form :model="form" label-width="90px">
      <el-form-item label="事件标题"><el-input v-model="form.title" /></el-form-item>
      <el-form-item label="事件分类">
        <el-select v-model="form.category" style="width:100%"><el-option disabled value="">请选择</el-option><el-option label="城市管理" value="城市管理" /><el-option label="公共安全" value="公共安全" /><el-option label="环境卫生" value="环境卫生" /><el-option label="矛盾纠纷" value="矛盾纠纷" /><el-option label="民生服务" value="民生服务" /></el-select>
      </el-form-item>
      <el-form-item label="所属网格">
        <el-select v-model="form.gridName" style="width:100%" filterable placeholder="请选择网格">
          <el-option v-for="g in gridList" :key="g.id" :label="g.name" :value="g.name" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="isAdmin" label="事件来源"><el-input v-model="form.reporter" /></el-form-item>
      <el-form-item label="事件地址"><el-input v-model="form.address" /></el-form-item>
      <el-form-item label="现场图片"><input type="file" accept="image/jpeg,image/png,image/webp" @change="upload" /></el-form-item>
      <img v-if="form.imageUrl" class="event-image" :src="form.imageUrl" alt="现场图片预览" />
      <el-form-item label="详细描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
    </el-form>
    <p v-if="error" class="error">{{ error }}</p>
    <template #footer><el-button @click="show = false">取消</el-button><el-button type="primary" @click="save">提交并自动派单</el-button></template>
  </el-dialog>
</template>
