<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../api'

const rows = ref([])
const logs = ref([])
const tab = ref('users')
const editing = ref(null)
const form = reactive({ username: '', displayName: '', password: '', role: '普通用户', phone: '', status: '启用' })
async function load() { rows.value = await request.get('/users'); logs.value = await request.get('/users/logs', { params: { limit: 100 } }) }
function open(row) { editing.value = row?.id || 0; Object.assign(form, row || { username: '', displayName: '', password: '', role: '普通用户', phone: '', status: '启用' }) }
function close() { editing.value = null }
async function save() {
  try {
    if (editing.value) await request.put(`/users/${editing.value}`, form)
    else await request.post('/users', form)
    close(); await load(); ElMessage.success('保存成功')
  } catch (e) { ElMessage.error(e.message) }
}
async function remove(id) { if (confirm('确定删除该用户吗？')) { try { await request.delete(`/users/${id}`); await load() } catch (e) { ElMessage.error(e.message) } } }
function format(value) { return value?.replace('T', ' ').slice(0, 16) }
onMounted(load)
</script>

<template>
  <section class="panel">
    <div class="panel-head"><div><p class="eyebrow">SYSTEM MANAGEMENT</p><h3>系统管理</h3></div><button class="primary" @click="open()">新增用户</button></div>
    <div class="tabs"><button :class="{ active: tab === 'users' }" @click="tab = 'users'">用户管理</button><button :class="{ active: tab === 'logs' }" @click="tab = 'logs'">操作日志</button></div>
    <el-table v-if="tab === 'users'" :data="rows" stripe>
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="displayName" label="姓名" />
      <el-table-column prop="role" label="角色" width="100"><template #default="{ row }"><el-tag :type="row.role === '管理员' ? 'danger' : 'info'" size="small">{{ row.role }}</el-tag></template></el-table-column>
      <el-table-column prop="phone" label="电话" width="120" />
      <el-table-column prop="status" label="状态" width="80" />
      <el-table-column prop="createTime" label="创建时间" width="160"><template #default="{ row }">{{ format(row.createTime) }}</template></el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="open(row)">编辑</el-button>
          <el-button v-if="row.username !== 'admin'" type="danger" link size="small" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-table v-else :data="logs" stripe>
      <el-table-column prop="username" label="用户" width="100" />
      <el-table-column prop="operation" label="操作" />
      <el-table-column prop="module" label="模块" width="100" />
      <el-table-column prop="createTime" label="时间" width="160"><template #default="{ row }">{{ format(row.createTime) }}</template></el-table-column>
    </el-table>
  </section>
  <el-dialog v-model="!!editing" :title="editing ? '编辑用户' : '新增用户'" width="480px" @close="close">
    <el-form :model="form" label-width="90px">
      <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
      <el-form-item label="姓名"><el-input v-model="form.displayName" /></el-form-item>
      <el-form-item label="密码"><el-input v-model="form.password" type="password" :placeholder="editing ? '留空不修改' : '至少6位'" /></el-form-item>
      <el-form-item label="角色"><el-select v-model="form.role" style="width:100%"><el-option label="管理员" value="管理员" /><el-option label="普通用户" value="普通用户" /></el-select></el-form-item>
      <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
      <el-form-item label="状态"><el-select v-model="form.status" style="width:100%"><el-option label="启用" value="启用" /><el-option label="禁用" value="禁用" /></el-select></el-form-item>
    </el-form>
    <template #footer><el-button @click="close">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
  </el-dialog>
</template>
