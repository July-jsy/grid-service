<script setup>
import { onMounted, reactive, ref } from 'vue'
import request from '../api'

const tab = ref('resident')
const residents = ref([])
const houses = ref([])
const editing = ref(null)
const error = ref('')
const keyword = ref('')
const rForm = reactive({ name: '', gender: '男', idCard: '', phone: '', gridName: '', gridId: null, address: '', houseId: null, residentType: '常住人口', remark: '' })
const hForm = reactive({ houseCode: '', address: '', building: '', unit: '', roomNumber: '', gridName: '', gridId: null, houseType: '商品房', usageStatus: '自住', ownerName: '', residentCount: 0, longitude: null, latitude: null })

async function load() {
  residents.value = await request.get('/base-info/residents', { params: { keyword: keyword.value } })
  houses.value = await request.get('/base-info/houses', { params: { keyword: keyword.value } })
}
function openResident(row) { editing.value = { type: 'resident', id: row?.id }; Object.assign(rForm, row || { name: '', gender: '男', idCard: '', phone: '', gridName: '', gridId: null, address: '', houseId: null, residentType: '常住人口', remark: '' }); error.value = '' }
function openHouse(row) { editing.value = { type: 'house', id: row?.id }; Object.assign(hForm, row || { houseCode: '', address: '', building: '', unit: '', roomNumber: '', gridName: '', gridId: null, houseType: '商品房', usageStatus: '自住', ownerName: '', residentCount: 0, longitude: null, latitude: null }); error.value = '' }
function close() { editing.value = null }
async function save() {
  try {
    if (editing.value.type === 'resident') {
      if (editing.value.id) await request.put(`/base-info/residents/${editing.value.id}`, rForm)
      else await request.post('/base-info/residents', rForm)
    } else {
      if (editing.value.id) await request.put(`/base-info/houses/${editing.value.id}`, hForm)
      else await request.post('/base-info/houses', hForm)
    }
    close(); await load()
  } catch (e) { error.value = e.message }
}
async function remove(id, type) {
  if (confirm('确定删除该档案吗？')) {
    await request.delete(`/base-info/${type === 'resident' ? 'residents' : 'houses'}/${id}`)
    await load()
  }
}
onMounted(load)
</script>

<template>
  <section class="panel">
    <div class="panel-head"><div><p class="eyebrow">BASE INFORMATION</p><h3>基础信息采集</h3></div><div><button class="primary" @click="openResident()">新增居民</button> <button class="ghost" @click="openHouse()">新增房屋</button></div></div>
    <div class="toolbar"><input v-model="keyword" placeholder="搜索..." @keyup.enter="load" /><button class="ghost" @click="load">查询</button></div>
    <div class="tabs"><button :class="{ active: tab === 'resident' }" @click="tab = 'resident'">居民档案 ({{ residents.length }})</button><button :class="{ active: tab === 'house' }" @click="tab = 'house'">房屋档案 ({{ houses.length }})</button></div>
    <el-table v-if="tab === 'resident'" :data="residents" stripe>
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="gender" label="性别" width="60" />
      <el-table-column prop="idCard" label="身份证号" width="170" />
      <el-table-column prop="residentType" label="类型" width="100"><template #default="{ row }"><el-tag size="small">{{ row.residentType }}</el-tag></template></el-table-column>
      <el-table-column prop="gridName" label="所属网格" />
      <el-table-column prop="address" label="地址" />
      <el-table-column prop="phone" label="电话" width="110" />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openResident(row)">编辑</el-button>
          <el-button type="danger" link size="small" @click="remove(row.id, 'resident')">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-table v-else :data="houses" stripe>
      <el-table-column prop="houseCode" label="编号" width="90" />
      <el-table-column prop="address" label="房屋地址" />
      <el-table-column prop="building" label="楼栋" width="80" />
      <el-table-column prop="unit" label="单元" width="70" />
      <el-table-column prop="roomNumber" label="门牌号" width="80" />
      <el-table-column prop="gridName" label="所属网格" />
      <el-table-column prop="houseType" label="类型" width="80" />
      <el-table-column prop="usageStatus" label="状态" width="80" />
      <el-table-column prop="ownerName" label="户主" />
      <el-table-column prop="residentCount" label="人数" width="60" />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openHouse(row)">编辑</el-button>
          <el-button type="danger" link size="small" @click="remove(row.id, 'house')">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </section>
  <!-- Resident dialog -->
  <el-dialog v-model="!!editing && editing.type === 'resident'" :title="editing?.id ? '编辑居民' : '新增居民'" width="520px" @close="close">
    <el-form :model="rForm" label-width="90px">
      <el-form-item label="姓名"><el-input v-model="rForm.name" /></el-form-item>
      <el-form-item label="性别"><el-select v-model="rForm.gender"><el-option label="男" value="男" /><el-option label="女" value="女" /></el-select></el-form-item>
      <el-form-item label="身份证号"><el-input v-model="rForm.idCard" /></el-form-item>
      <el-form-item label="人员类型"><el-select v-model="rForm.residentType"><el-option label="常住人口" value="常住人口" /><el-option label="流动人口" value="流动人口" /><el-option label="独居老人" value="独居老人" /><el-option label="特殊人群" value="特殊人群" /></el-select></el-form-item>
      <el-form-item label="所属网格"><el-input v-model="rForm.gridName" /></el-form-item>
      <el-form-item label="地址"><el-input v-model="rForm.address" /></el-form-item>
      <el-form-item label="电话"><el-input v-model="rForm.phone" /></el-form-item>
      <el-form-item label="备注"><el-input v-model="rForm.remark" type="textarea" /></el-form-item>
    </el-form>
    <p v-if="error" class="error">{{ error }}</p>
    <template #footer><el-button @click="close">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
  </el-dialog>
  <!-- House dialog -->
  <el-dialog v-model="!!editing && editing.type === 'house'" :title="editing?.id ? '编辑房屋' : '新增房屋'" width="520px" @close="close">
    <el-form :model="hForm" label-width="90px">
      <el-form-item label="房屋编号"><el-input v-model="hForm.houseCode" /></el-form-item>
      <el-form-item label="地址"><el-input v-model="hForm.address" /></el-form-item>
      <el-form-item label="楼栋"><el-input v-model="hForm.building" /></el-form-item>
      <el-form-item label="单元"><el-input v-model="hForm.unit" /></el-form-item>
      <el-form-item label="门牌号"><el-input v-model="hForm.roomNumber" /></el-form-item>
      <el-form-item label="所属网格"><el-input v-model="hForm.gridName" /></el-form-item>
      <el-form-item label="房屋类型"><el-select v-model="hForm.houseType"><el-option label="商品房" value="商品房" /><el-option label="经济适用房" value="经济适用房" /><el-option label="公租房" value="公租房" /><el-option label="自建房" value="自建房" /></el-select></el-form-item>
      <el-form-item label="使用状态"><el-select v-model="hForm.usageStatus"><el-option label="自住" value="自住" /><el-option label="出租" value="出租" /><el-option label="空置" value="空置" /></el-select></el-form-item>
      <el-form-item label="户主"><el-input v-model="hForm.ownerName" /></el-form-item>
      <el-form-item label="居住人数"><el-input-number v-model="hForm.residentCount" :min="0" /></el-form-item>
      <el-form-item label="经度"><el-input-number v-model="hForm.longitude" :precision="6" /></el-form-item>
      <el-form-item label="纬度"><el-input-number v-model="hForm.latitude" :precision="6" /></el-form-item>
    </el-form>
    <p v-if="error" class="error">{{ error }}</p>
    <template #footer><el-button @click="close">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
  </el-dialog>
</template>
