<script setup>
import { onMounted, reactive, ref } from 'vue'
import request from '../api'

const rows = ref([])
const keyword = ref('')
const editing = ref(null)
const error = ref('')
const emptyForm = () => ({ code: '', name: '', community: '', description: '', staffName: '', staffPhone: '', residentCount: 0, longitude: null, latitude: null })
const form = reactive(emptyForm())
const page = ref(1)
const pageSize = 10

async function load() { rows.value = await request.get('/grids', { params: { keyword: keyword.value } }) }
function open(row) { editing.value = row?.id || 0; Object.assign(form, row || emptyForm()); error.value = '' }
function close() { editing.value = null }
async function save() {
  try {
    if (editing.value) await request.put(`/grids/${editing.value}`, form)
    else await request.post('/grids', form)
    close(); await load()
  } catch (e) { error.value = e.message }
}
async function remove(id) { if (confirm('确定删除该网格吗？')) { await request.delete(`/grids/${id}`); await load() } }
onMounted(load)
</script>

<template>
  <section class="panel">
    <div class="panel-head"><div><p class="eyebrow">GRID MANAGEMENT</p><h3>网格责任清单</h3></div><button class="primary" @click="open()">新增网格</button></div>
    <div class="toolbar"><input v-model="keyword" placeholder="搜索网格或社区" @keyup.enter="load" /><button class="ghost" @click="load">查询</button></div>
    <el-table :data="rows" stripe style="width:100%" @row-dblclick="open">
      <el-table-column prop="code" label="编号" width="100" />
      <el-table-column prop="name" label="网格名称" />
      <el-table-column prop="community" label="所属社区" />
      <el-table-column prop="staffName" label="网格员" />
      <el-table-column prop="staffPhone" label="联系电话" />
      <el-table-column prop="residentCount" label="居民数" width="80" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="open(row)">编辑</el-button>
          <el-button type="danger" link size="small" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </section>
  <el-dialog v-model="!!editing" :title="editing ? '编辑网格' : '新增网格'" width="520px" @close="close">
    <el-form :model="form" label-width="90px">
      <el-form-item label="网格编号"><el-input v-model="form.code" /></el-form-item>
      <el-form-item label="网格名称"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="所属社区"><el-input v-model="form.community" /></el-form-item>
      <el-form-item label="网格员"><el-input v-model="form.staffName" /></el-form-item>
      <el-form-item label="联系电话"><el-input v-model="form.staffPhone" /></el-form-item>
      <el-form-item label="居民数量"><el-input-number v-model="form.residentCount" :min="0" /></el-form-item>
      <el-form-item label="中心经度"><el-input-number v-model="form.longitude" :precision="6" /></el-form-item>
      <el-form-item label="中心纬度"><el-input-number v-model="form.latitude" :precision="6" /></el-form-item>
      <el-form-item label="范围描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
    </el-form>
    <p v-if="error" class="error">{{ error }}</p>
    <template #footer><el-button @click="close">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
  </el-dialog>
</template>
