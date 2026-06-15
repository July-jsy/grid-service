<script setup>
import { onMounted, reactive, ref } from 'vue'
import request from '../api'

const rows = ref([])
const keyword = ref('')
const editing = ref(null)
const error = ref('')
const emptyForm = () => ({ code: '', name: '', community: '', description: '', staffName: '', staffPhone: '', residentCount: 0 })
const form = reactive(emptyForm())

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
    <div class="table-wrap"><table><thead><tr><th>编号</th><th>网格名称</th><th>所属社区</th><th>网格员</th><th>联系电话</th><th>居民数</th><th>操作</th></tr></thead>
      <tbody><tr v-for="row in rows" :key="row.id"><td>{{ row.code }}</td><td><strong>{{ row.name }}</strong></td><td>{{ row.community }}</td><td>{{ row.staffName }}</td><td>{{ row.staffPhone }}</td><td>{{ row.residentCount }}</td><td><button class="text-btn" @click="open(row)">编辑</button><button class="text-btn danger" @click="remove(row.id)">删除</button></td></tr></tbody>
    </table></div>
  </section>
  <div v-if="editing !== null" class="modal-mask" @click.self="close"><form class="modal" @submit.prevent="save"><div class="panel-head"><h3>{{ editing ? '编辑网格' : '新增网格' }}</h3><button type="button" class="ghost" @click="close">关闭</button></div>
    <label>网格编号<input v-model="form.code" required /></label><label>网格名称<input v-model="form.name" required /></label><label>所属社区<input v-model="form.community" required /></label><label>网格员<input v-model="form.staffName" /></label><label>联系电话<input v-model="form.staffPhone" /></label><label>居民数量<input v-model.number="form.residentCount" type="number" min="0" /></label><label>范围描述<textarea v-model="form.description"></textarea></label><p v-if="error" class="error">{{ error }}</p><button class="primary wide">保存</button>
  </form></div>
</template>
