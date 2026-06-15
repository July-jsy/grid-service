<script setup>
import { onMounted, reactive, ref } from 'vue'
import request from '../api'
const user = JSON.parse(localStorage.getItem('grid-user') || '{}')
const isAdmin = user.role === '管理员'
const rows = ref([]); const applications = ref([]); const show = ref(false); const applyShow = ref(false); const error = ref('')
const form = reactive({ name: '', category: '', department: '', deadlineDays: 5, materials: '', status: '启用' })
const applyForm = reactive({ itemName: '', applicant: user.displayName || '', phone: '', materialsNote: '' })
async function load() { rows.value = await request.get('/service-items'); applications.value = await request.get('/service-applications') }
async function save() { try { await request.post('/service-items', form); show.value = false; await load() } catch(e) { error.value = e.message } }
async function apply() { try { await request.post('/service-applications', applyForm); applyShow.value = false; await load() } catch(e) { error.value = e.message } }
async function changeApply(row, status) { await request.put(`/service-applications/${row.id}/status`, { status }); await load() }
async function remove(id) { if (confirm('确定删除该服务事项吗？')) { await request.delete(`/service-items/${id}`); await load() } }
onMounted(load)
</script>
<template>
  <section class="panel">
    <div class="panel-head"><div><p class="eyebrow">PUBLIC SERVICE</p><h3>便民服务事项</h3></div><div><button class="ghost" @click="applyShow = true">在线申办</button> <button v-if="isAdmin" class="primary" @click="show = true">新增事项</button></div></div>
    <div class="card-list"><article v-for="row in rows" :key="row.id" class="service-card"><span class="tag">{{ row.category }}</span><h3>{{ row.name }}</h3><p>{{ row.department }}</p><p class="muted">办理时限：{{ row.deadlineDays }} 个工作日</p><p class="materials">所需材料：{{ row.materials }}</p><div><span class="status">{{ row.status }}</span><button v-if="isAdmin" class="text-btn danger" @click="remove(row.id)">删除</button></div></article></div>
  </section>
  <section class="panel section-gap"><div class="panel-head"><h3>{{ isAdmin ? '服务申请管理' : '我的申请进度' }}</h3><span>{{ applications.length }} 条申请</span></div><div class="table-wrap"><table><thead><tr><th>申请编号</th><th>事项</th><th>申请人</th><th>联系电话</th><th>状态</th><th v-if="isAdmin">流转</th></tr></thead><tbody><tr v-for="row in applications" :key="row.id"><td>{{ row.code }}</td><td>{{ row.itemName }}</td><td>{{ row.applicant }}</td><td>{{ row.phone }}</td><td><span class="status">{{ row.status }}</span></td><td v-if="isAdmin"><select :value="row.status" @change="changeApply(row, $event.target.value)"><option>待受理</option><option>办理中</option><option>已办结</option></select></td></tr></tbody></table></div></section>
  <div v-if="show && isAdmin" class="modal-mask" @click.self="show = false"><form class="modal" @submit.prevent="save"><div class="panel-head"><h3>新增服务事项</h3><button type="button" class="ghost" @click="show = false">关闭</button></div><label>事项名称<input v-model="form.name" required /></label><label>服务分类<input v-model="form.category" required /></label><label>办理部门<input v-model="form.department" /></label><label>办理时限<input v-model.number="form.deadlineDays" type="number" min="1" /></label><label>所需材料<textarea v-model="form.materials"></textarea></label><p v-if="error" class="error">{{ error }}</p><button class="primary wide">保存</button></form></div>
  <div v-if="applyShow" class="modal-mask" @click.self="applyShow = false"><form class="modal" @submit.prevent="apply"><div class="panel-head"><h3>在线申办</h3><button type="button" class="ghost" @click="applyShow = false">关闭</button></div><label v-if="isAdmin">申请人<input v-model="applyForm.applicant" required /></label><label>申请事项<select v-model="applyForm.itemName" required><option disabled value="">请选择</option><option v-for="row in rows" :key="row.id">{{ row.name }}</option></select></label><label>联系电话<input v-model="applyForm.phone" /></label><label>材料说明<textarea v-model="applyForm.materialsNote"></textarea></label><button class="primary wide">提交申请</button></form></div>
</template>
