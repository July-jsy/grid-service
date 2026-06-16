<script setup>
import { onMounted, reactive, ref } from 'vue'
import request from '../api'
const user = JSON.parse(sessionStorage.getItem('grid-user') || '{}')
const isAdmin = user.role === '管理员'
const rows = ref([]); const applications = ref([]); const show = ref(false); const applyShow = ref(false); const editing = ref(null); const error = ref('')
const form = reactive({ name: '', category: '', department: '', description: '', requiredMaterials: '', process: '', timeLimit: '', status: '启用' })
const applyForm = reactive({ itemName: '', applicantName: user.displayName || '', applicantPhone: '', content: '', serviceItemId: null })
async function load() { rows.value = await request.get('/service-items'); applications.value = await request.get('/service-applications') }
function open(row) { editing.value = row?.id || 0; Object.assign(form, row || { name: '', category: '', department: '', description: '', requiredMaterials: '', process: '', timeLimit: '', status: '启用' }); error.value = '' }
function close() { editing.value = null }
async function save() {
  try {
    if (editing.value) await request.put(`/service-items/${editing.value}`, form)
    else await request.post('/service-items', form)
    close(); await load()
  } catch(e) { error.value = e.message }
}
async function apply() { try { await request.post('/service-applications', applyForm); applyShow.value = false; await load() } catch(e) { error.value = e.message } }
async function changeApply(row, status) { await request.put(`/service-applications/${row.id}/status`, { status }); await load() }
async function remove(id) { if (confirm('确定删除该服务事项吗？')) { await request.delete(`/service-items/${id}`); await load() } }
onMounted(load)
</script>
<template>
  <section class="panel">
    <div class="panel-head"><div><p class="eyebrow">PUBLIC SERVICE</p><h3>便民服务事项</h3></div><div><button class="ghost" @click="applyShow = true">在线申办</button> <button v-if="isAdmin" class="primary" @click="open()">新增事项</button></div></div>
    <div class="card-list"><article v-for="row in rows" :key="row.id" class="service-card">
      <span class="tag">{{ row.category }}</span>
      <h3>{{ row.name }}</h3>
      <p>{{ row.department }}</p>
      <p class="muted">办理时限：{{ row.timeLimit }}</p>
      <p class="muted">{{ row.description }}</p>
      <p class="materials">所需材料：{{ row.requiredMaterials }}</p>
      <div>
        <span class="status">{{ row.status === '启用' ? '启用中' : '已停用' }}</span>
        <div v-if="isAdmin">
          <button class="text-btn" @click="open(row)">编辑</button>
          <button class="text-btn danger" @click="remove(row.id)">删除</button>
        </div>
      </div>
    </article></div>
  </section>
  <section class="panel section-gap"><div class="panel-head"><h3>{{ isAdmin ? '服务申请管理' : '我的申请进度' }}</h3><span>{{ applications.length }} 条申请</span></div>
    <el-table :data="applications" stripe>
      <el-table-column prop="code" label="编号" width="110" />
      <el-table-column prop="itemName" label="事项" />
      <el-table-column prop="applicantName" label="申请人" width="100" />
      <el-table-column prop="applicantPhone" label="电话" width="110" />
      <el-table-column prop="status" label="状态" width="100"><template #default="{ row }"><el-tag :type="row.status === '已办结' ? 'success' : row.status === '已驳回' ? 'danger' : 'warning'" size="small">{{ row.status }}</el-tag></template></el-table-column>
      <el-table-column v-if="isAdmin" label="流转" width="200">
        <template #default="{ row }">
          <el-select :model-value="row.status" size="small" @change="changeApply(row, $event)">
            <el-option label="待受理" value="待受理" /><el-option label="办理中" value="办理中" /><el-option label="已办结" value="已办结" /><el-option label="已驳回" value="已驳回" />
          </el-select>
        </template>
      </el-table-column>
    </el-table>
  </section>
  <!-- Service item dialog -->
  <el-dialog :model-value="editing !== null" :title="editing ? '编辑服务事项' : '新增服务事项'" width="520px" @close="close">
    <el-form :model="form" label-width="90px">
      <el-form-item label="事项名称"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="服务分类"><el-input v-model="form.category" /></el-form-item>
      <el-form-item label="办理部门"><el-input v-model="form.department" /></el-form-item>
      <el-form-item label="事项描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
      <el-form-item label="办理时限"><el-input v-model="form.timeLimit" placeholder="如：15个工作日" /></el-form-item>
      <el-form-item label="所需材料"><el-input v-model="form.requiredMaterials" type="textarea" /></el-form-item>
      <el-form-item label="办理流程"><el-input v-model="form.process" type="textarea" /></el-form-item>
      <el-form-item label="状态"><el-select v-model="form.status"><el-option label="启用" value="启用" /><el-option label="停用" value="停用" /></el-select></el-form-item>
    </el-form>
    <p v-if="error" class="error">{{ error }}</p>
    <template #footer><el-button @click="close">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
  </el-dialog>
  <!-- Apply dialog -->
  <el-dialog v-model="applyShow" title="在线申办" width="500px">
    <el-form :model="applyForm" label-width="90px">
      <el-form-item v-if="isAdmin" label="申请人"><el-input v-model="applyForm.applicantName" /></el-form-item>
      <el-form-item label="申请事项">
        <el-select v-model="applyForm.itemName" style="width:100%"><el-option disabled value="">请选择</el-option><el-option v-for="row in rows" :key="row.id" :label="row.name" :value="row.name" /></el-select>
      </el-form-item>
      <el-form-item label="联系电话"><el-input v-model="applyForm.applicantPhone" /></el-form-item>
      <el-form-item label="申请详情"><el-input v-model="applyForm.content" type="textarea" /></el-form-item>
    </el-form>
    <template #footer><el-button @click="applyShow = false">取消</el-button><el-button type="primary" @click="apply">提交申请</el-button></template>
  </el-dialog>
</template>
