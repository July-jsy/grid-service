<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../api'

const user = JSON.parse(localStorage.getItem('grid-user') || '{}')
const isAdmin = user.role === '管理员'
const rows = ref([])
const editing = ref(null)
const form = reactive({ title: '', type: '社区通知', content: '', status: '已发布' })
async function load() { rows.value = await request.get('/notices') }
function open(row) { editing.value = row?.id || 0; Object.assign(form, row || { title: '', type: '社区通知', content: '', status: '已发布' }) }
function close() { editing.value = null }
async function save() {
  if (editing.value) await request.put(`/notices/${editing.value}`, form)
  else await request.post('/notices', form)
  close(); await load(); ElMessage.success(editing.value ? '公告已更新' : '公告已发布')
}
async function remove(id) { if (confirm('确定删除该公告吗？')) { await request.delete(`/notices/${id}`); await load() } }
function format(value) { return value?.replace('T', ' ').slice(0, 16) }
onMounted(load)
</script>

<template>
  <section class="panel">
    <div class="panel-head"><div><p class="eyebrow">COMMUNITY NOTICE</p><h3>通知公告</h3></div><button v-if="isAdmin" class="primary" @click="open()">发布公告</button></div>
    <div class="notice-list"><article v-for="row in rows" :key="row.id" class="notice-card">
      <div style="flex:1">
        <span class="tag">{{ row.type }}</span>
        <h3>{{ row.title }}</h3>
        <p>{{ row.content }}</p>
        <span class="muted">{{ format(row.publishedAt) }} · {{ row.status }}</span>
      </div>
      <div v-if="isAdmin" class="event-action">
        <button class="text-btn" @click="open(row)">编辑</button>
        <button class="text-btn danger" @click="remove(row.id)">删除</button>
      </div>
    </article></div>
  </section>
  <el-dialog v-model="!!editing" :title="editing ? '编辑公告' : '发布公告'" width="520px" @close="close">
    <el-form :model="form" label-width="90px">
      <el-form-item label="公告标题"><el-input v-model="form.title" /></el-form-item>
      <el-form-item label="公告分类">
        <el-select v-model="form.type" style="width:100%"><el-option label="社区通知" value="社区通知" /><el-option label="便民服务" value="便民服务" /><el-option label="政策宣传" value="政策宣传" /><el-option label="社区活动" value="社区活动" /></el-select>
      </el-form-item>
      <el-form-item label="公告内容"><el-input v-model="form.content" type="textarea" :rows="4" /></el-form-item>
      <el-form-item label="发布状态">
        <el-select v-model="form.status" style="width:100%"><el-option label="已发布" value="已发布" /><el-option label="草稿" value="草稿" /></el-select>
      </el-form-item>
    </el-form>
    <template #footer><el-button @click="close">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
  </el-dialog>
</template>
