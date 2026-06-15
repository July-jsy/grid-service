<script setup>
import { onMounted, reactive, ref } from 'vue'
import request from '../api'

const rows = ref([])
const show = ref(false)
const form = reactive({ username: '', displayName: '', password: 'user123', role: '普通用户', phone: '', status: '启用' })
async function load() { rows.value = await request.get('/users') }
async function save() { await request.post('/users', form); show.value = false; await load() }
async function remove(id) { if (confirm('确定删除该用户吗？')) { await request.delete(`/users/${id}`); await load() } }
onMounted(load)
</script>

<template>
  <section class="panel"><div class="panel-head"><div><p class="eyebrow">SYSTEM MANAGEMENT</p><h3>用户与权限管理</h3></div><button class="primary" @click="show = true">新增用户</button></div>
    <div class="table-wrap"><table><thead><tr><th>用户名</th><th>姓名</th><th>角色</th><th>联系电话</th><th>状态</th><th>操作</th></tr></thead><tbody><tr v-for="row in rows" :key="row.id"><td>{{ row.username }}</td><td>{{ row.displayName }}</td><td><span class="tag">{{ row.role }}</span></td><td>{{ row.phone }}</td><td>{{ row.status }}</td><td><button class="text-btn danger" @click="remove(row.id)">删除</button></td></tr></tbody></table></div>
  </section>
  <div v-if="show" class="modal-mask" @click.self="show = false"><form class="modal" @submit.prevent="save"><div class="panel-head"><h3>新增用户</h3><button type="button" class="ghost" @click="show = false">关闭</button></div><label>用户名<input v-model="form.username" required /></label><label>姓名<input v-model="form.displayName" required /></label><label>初始密码<input v-model="form.password" type="password" minlength="6" required /></label><label>角色<select v-model="form.role"><option>管理员</option><option>普通用户</option></select></label><label>联系电话<input v-model="form.phone" /></label><button class="primary wide">保存</button></form></div>
</template>
