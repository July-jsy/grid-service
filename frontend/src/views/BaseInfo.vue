<script setup>
import { onMounted, reactive, ref } from 'vue'
import request from '../api'

const tab = ref('resident')
const residents = ref([])
const houses = ref([])
const show = ref(false)
const resident = reactive({ name: '', gender: '男', phone: '', gridName: '', address: '', type: '常住人口' })
const house = reactive({ address: '', gridName: '', propertyType: '商品房', rentalStatus: '自住', ownerName: '', residentCount: 0 })

async function load() {
  residents.value = await request.get('/base-info/residents')
  houses.value = await request.get('/base-info/houses')
}
async function save() {
  await request.post(tab.value === 'resident' ? '/base-info/residents' : '/base-info/houses', tab.value === 'resident' ? resident : house)
  show.value = false
  await load()
}
async function remove(id) {
  if (confirm('确定删除该档案吗？')) {
    await request.delete(`/base-info/${tab.value === 'resident' ? 'residents' : 'houses'}/${id}`)
    await load()
  }
}
onMounted(load)
</script>

<template>
  <section class="panel">
    <div class="panel-head"><div><p class="eyebrow">BASE INFORMATION</p><h3>基础信息采集</h3></div><button class="primary" @click="show = true">新增档案</button></div>
    <div class="tabs"><button :class="{ active: tab === 'resident' }" @click="tab = 'resident'">居民档案</button><button :class="{ active: tab === 'house' }" @click="tab = 'house'">房屋档案</button></div>
    <div class="table-wrap">
      <table v-if="tab === 'resident'"><thead><tr><th>姓名</th><th>性别</th><th>类型</th><th>所属网格</th><th>地址</th><th>电话</th><th>操作</th></tr></thead><tbody><tr v-for="row in residents" :key="row.id"><td>{{ row.name }}</td><td>{{ row.gender }}</td><td><span class="tag">{{ row.type }}</span></td><td>{{ row.gridName }}</td><td>{{ row.address }}</td><td>{{ row.phone }}</td><td><button class="text-btn danger" @click="remove(row.id)">删除</button></td></tr></tbody></table>
      <table v-else><thead><tr><th>房屋地址</th><th>所属网格</th><th>产权类型</th><th>使用状态</th><th>户主</th><th>居住人数</th><th>操作</th></tr></thead><tbody><tr v-for="row in houses" :key="row.id"><td>{{ row.address }}</td><td>{{ row.gridName }}</td><td>{{ row.propertyType }}</td><td>{{ row.rentalStatus }}</td><td>{{ row.ownerName }}</td><td>{{ row.residentCount }}</td><td><button class="text-btn danger" @click="remove(row.id)">删除</button></td></tr></tbody></table>
    </div>
  </section>
  <div v-if="show" class="modal-mask" @click.self="show = false"><form class="modal" @submit.prevent="save"><div class="panel-head"><h3>新增{{ tab === 'resident' ? '居民' : '房屋' }}档案</h3><button type="button" class="ghost" @click="show = false">关闭</button></div>
    <template v-if="tab === 'resident'"><label>姓名<input v-model="resident.name" required /></label><label>性别<select v-model="resident.gender"><option>男</option><option>女</option></select></label><label>人员类型<input v-model="resident.type" /></label><label>所属网格<input v-model="resident.gridName" required /></label><label>地址<input v-model="resident.address" /></label><label>电话<input v-model="resident.phone" /></label></template>
    <template v-else><label>房屋地址<input v-model="house.address" required /></label><label>所属网格<input v-model="house.gridName" required /></label><label>产权类型<input v-model="house.propertyType" /></label><label>使用状态<input v-model="house.rentalStatus" /></label><label>户主<input v-model="house.ownerName" /></label><label>居住人数<input v-model.number="house.residentCount" type="number" min="0" /></label></template>
    <button class="primary wide">保存</button>
  </form></div>
</template>
