<script setup>
import { onMounted, reactive, ref, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../api'

const rows = ref([])
const keyword = ref('')
const editing = ref(null)
const error = ref('')
const mapRef = ref(null)
const drawing = ref(false)
let map = null
let mouseTool = null
let polygon = null
let polygonPath = []

const emptyForm = () => ({
  code: '', name: '', community: '', description: '', staffName: '', staffPhone: '',
  residentCount: 0, longitude: null, latitude: null, fencePoints: null
})
const form = reactive(emptyForm())

async function load() { rows.value = await request.get('/grids', { params: { keyword: keyword.value } }) }

function open(row) {
  editing.value = row?.id || 0
  Object.assign(form, row || emptyForm())
  polygonPath = []
  error.value = ''
  if (form.fencePoints) {
    try { polygonPath = JSON.parse(form.fencePoints) } catch {}
  }
  nextTick(loadMap)
}

function close() {
  editing.value = null
  destroyMap()
}

async function save() {
  form.fencePoints = polygonPath.length > 0 ? JSON.stringify(polygonPath) : null
  try {
    if (editing.value) await request.put(`/grids/${editing.value}`, form)
    else await request.post('/grids', form)
    ElMessage.success(editing.value ? '网格已更新' : '网格已创建')
    close(); await load()
  } catch (e) { error.value = e.message }
}

async function remove(id) {
  if (confirm('确定删除该网格吗？')) {
    await request.delete(`/grids/${id}`)
    ElMessage.success('已删除')
    await load()
  }
}

// ===== 地图圈选 =====
function loadAMapScript() {
  return new Promise((resolve) => {
    if (window.AMap) { resolve(); return }
    const script = document.createElement('script')
    script.src = 'https://webapi.amap.com/maps?v=2.0&key=请替换为高德JS_API_Key&plugin=AMap.MouseTool,AMap.PolygonEditor'
    script.onload = resolve
    document.head.appendChild(script)
  })
}

async function loadMap() {
  if (!mapRef.value) return
  await loadAMapScript()
  // 清空容器残留 DOM，避免 AMap 重建冲突
  mapRef.value.innerHTML = ''
  const center = form.longitude ? [form.longitude, form.latitude] : [116.397428, 39.909204]
  map = new AMap.Map(mapRef.value, { zoom: 15, center })

  // 还原已有围栏
  if (polygonPath.length > 0) {
    polygon = new AMap.Polygon({ path: polygonPath, strokeColor: '#17614f', strokeWeight: 3, fillColor: '#17614f22', fillOpacity: 0.3 })
    map.add(polygon)
    map.setFitView([polygon])
  }
}

function startDraw() {
  if (!map) return
  drawing.value = true
  // 每次绘制前重建 mouseTool，避免监听器累积
  if (mouseTool) { mouseTool.close(true) }
  mouseTool = new AMap.MouseTool(map)
  mouseTool.polygon({ strokeColor: '#e5b455', strokeWeight: 3, fillColor: '#e5b45522', fillOpacity: 0.3 })
  mouseTool.on('draw', (event) => {
    polygonPath = event.obj.getPath().map(p => [p.lng, p.lat])
    form.fencePoints = JSON.stringify(polygonPath)
    if (polygon) { map.remove(polygon) }
    polygon = event.obj
    drawing.value = false
    ElMessage.success('围栏已圈选完成，可继续编辑')
  })
}

function clearFence() {
  if (polygon) { map.remove(polygon); polygon = null }
  polygonPath = []
  form.fencePoints = null
  ElMessage.info('围栏已清除')
}

function destroyMap() {
  // mouseTool 在绘制完成后会自动关闭，先置空避免 close 报错
  mouseTool = null
  if (map) {
    try { map.destroy() } catch (_) { /* AMap 内部 DOM 清理报错可忽略 */ }
    map = null
  }
  polygon = null
}

onMounted(load)
</script>

<template>
  <section class="panel">
    <div class="panel-head">
      <div><p class="eyebrow">GRID MANAGEMENT</p><h3>网格责任清单</h3></div>
      <button class="primary" @click="open()">新增网格</button>
    </div>
    <div class="toolbar">
      <input v-model="keyword" placeholder="搜索网格或社区" @keyup.enter="load" />
      <button class="ghost" @click="load">查询</button>
    </div>
    <el-table :data="rows" stripe style="width:100%">
      <el-table-column prop="code" label="编号" width="100" />
      <el-table-column prop="name" label="网格名称" />
      <el-table-column prop="community" label="所属社区" />
      <el-table-column prop="staffName" label="网格员" />
      <el-table-column prop="staffPhone" label="联系电话" />
      <el-table-column prop="residentCount" label="居民数" width="80" />
      <el-table-column label="围栏" width="70">
        <template #default="{ row }"><span v-if="row.fencePoints">📍</span></template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="open(row)">编辑</el-button>
          <el-button type="danger" link size="small" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </section>

  <el-dialog :model-value="editing !== null"
    :title="editing ? '编辑网格' : '新增网格'" width="750px" @close="close" top="2vh">
    <el-form :model="form" label-width="90px">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="网格编号"><el-input v-model="form.code" /></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="网格名称"><el-input v-model="form.name" /></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="所属社区"><el-input v-model="form.community" /></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="网格员"><el-input v-model="form.staffName" /></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系电话"><el-input v-model="form.staffPhone" /></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="居民数量"><el-input-number v-model="form.residentCount" :min="0" style="width:100%" /></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="中心经度"><el-input-number v-model="form.longitude" :precision="6" style="width:100%" /></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="中心纬度"><el-input-number v-model="form.latitude" :precision="6" style="width:100%" /></el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="范围描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
        </el-col>
      </el-row>

      <!-- 地图圈选区域 -->
      <el-form-item label="网格围栏">
        <div style="width:100%">
          <div style="margin-bottom:8px;display:flex;gap:8px">
            <el-button type="warning" size="small" :disabled="drawing" @click="startDraw">
              {{ drawing ? '绘制中...' : (polygonPath.length > 0 ? '重新圈选' : '开始圈选') }}
            </el-button>
            <el-button v-if="polygonPath.length > 0" size="small" @click="clearFence">清除围栏</el-button>
            <span v-if="polygonPath.length > 0" style="line-height:32px;color:#17614f;font-size:13px">
              已圈选 {{ polygonPath.length }} 个围栏拐点
            </span>
            <span v-else style="line-height:32px;color:#999;font-size:13px">
              点击"开始圈选"后在地图上点击绘制多边形，双击完成
            </span>
          </div>
          <div ref="mapRef" style="width:100%;height:350px;border-radius:10px;border:1px solid #dce7e0"></div>
        </div>
      </el-form-item>
    </el-form>
    <p v-if="error" class="error">{{ error }}</p>
    <template #footer>
      <el-button @click="close">取消</el-button>
      <el-button type="primary" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>
