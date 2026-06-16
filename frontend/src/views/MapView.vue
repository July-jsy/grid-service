<script setup>
import { nextTick, onMounted, ref } from 'vue'
import request from '../api'

const loading = ref(true)
const grids = ref([])
const events = ref([])
const houses = ref([])
const infoContent = ref('')
const mapContainer = ref(null)

let map = null
const defaultCenter = [116.397428, 39.909204]

onMounted(async () => {
  try {
    const [g, e, h] = await Promise.all([
      request.get('/grids'),
      request.get('/events'),
      request.get('/base-info/houses'),
    ])
    grids.value = g; events.value = e; houses.value = h
  } finally { loading.value = false }
  await nextTick()
  initMap()
})

function initMap() {
  const script = document.createElement('script')
  script.src = 'https://webapi.amap.com/maps?v=2.0&key=请替换为高德JS_API_Key&plugin=AMap.Marker,AMap.PolygonEditor'
  script.onload = () => {
    map = new AMap.Map(mapContainer.value, { zoom: 14, center: defaultCenter })
    addMarkers()
  }
  document.head.appendChild(script)
}

function addMarkers() {
  let hasFence = false

  // 网格围栏（多边形）+ 标记
  grids.value.forEach(grid => {
    let fencePath = null
    if (grid.fencePoints) {
      try { fencePath = JSON.parse(grid.fencePoints) } catch {}
    }

    if (fencePath && fencePath.length >= 3) {
      // 有围栏 → 画半透明多边形
      const polygon = new AMap.Polygon({
        path: fencePath,
        strokeColor: '#17614f',
        strokeWeight: 3,
        strokeOpacity: 0.8,
        fillColor: '#17614f',
        fillOpacity: 0.15,
      })
      polygon.setMap(map)
      polygon.on('click', () => {
        infoContent.value = `<b>网格：${grid.name}</b><br>社区：${grid.community}<br>网格员：${grid.staffName || '-'}<br>居民数：${grid.residentCount}<br>围栏拐点：${fencePath.length} 个`
      })

      // 多边形中心加文字标签
      const center = getPolygonCenter(fencePath)
      const labelMarker = new AMap.Marker({
        position: center,
        label: { content: grid.name, direction: 'center', offset: [0, 0] },
        icon: new AMap.Icon({
          size: [1, 1],
          image: 'data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7',
        }),
      })
      labelMarker.setMap(map)
      hasFence = true
    } else if (grid.longitude && grid.latitude) {
      // 无围栏 → 点标记
      const marker = new AMap.Marker({
        position: [grid.longitude, grid.latitude],
        title: grid.name,
      })
      marker.setMap(map)
      marker.on('click', () => {
        infoContent.value = `<b>网格：${grid.name}</b><br>社区：${grid.community}<br>网格员：${grid.staffName || '-'}<br>居民数：${grid.residentCount}`
      })
    }
  })

  if (hasFence) map.setFitView()

  // 事件标记（红点）
  events.value.forEach(ev => {
    if (ev.longitude && ev.latitude) {
      const marker = new AMap.Marker({
        position: [ev.longitude, ev.latitude],
        title: ev.title,
        icon: new AMap.Icon({ size: new AMap.Size(20, 20), image: 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10" fill="%23b33b32"/></svg>' }),
      })
      marker.setMap(map)
      marker.on('click', () => {
        infoContent.value = `<b>事件：${ev.title}</b><br>分类：${ev.category}<br>网格：${ev.gridName}<br>状态：${ev.status}<br>地址：${ev.address || '-'}`
      })
    }
  })

  // 房屋标记（蓝点）
  houses.value.forEach(house => {
    if (house.longitude && house.latitude) {
      const marker = new AMap.Marker({
        position: [house.longitude, house.latitude],
        title: house.address,
        icon: new AMap.Icon({ size: new AMap.Size(16, 16), image: 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10" fill="%232c68a8"/></svg>' }),
      })
      marker.setMap(map)
      marker.on('click', () => {
        infoContent.value = `<b>房屋：${house.address}</b><br>网格：${house.gridName}<br>户主：${house.ownerName || '-'}<br>类型：${house.houseType || '-'}`
      })
    }
  })
}

// 计算多边形中心点
function getPolygonCenter(path) {
  let lng = 0, lat = 0
  path.forEach(([l, t]) => { lng += l; lat += t })
  return [lng / path.length, lat / path.length]
}
</script>

<template>
  <div v-if="loading" class="loading">正在加载地图数据...</div>
  <template v-else>
    <section class="panel">
      <div class="panel-head"><div><p class="eyebrow">MAP VISUALIZATION</p><h3>地图可视化</h3></div>
        <div>
          <span class="tag" style="margin-right:8px">🟩 {{ grids.filter(g => g.fencePoints).length }} 围栏</span>
          <span class="tag" style="margin-right:8px">📍 {{ grids.filter(g => !g.fencePoints && g.longitude).length }} 网格点</span>
          <span class="tag" style="margin-right:8px">🔴 {{ events.filter(e => e.longitude).length }} 事件</span>
          <span class="tag">🔵 {{ houses.filter(h => h.longitude).length }} 房屋</span>
        </div>
      </div>
      <div ref="mapContainer" style="width:100%;height:600px;border-radius:12px;border:1px solid #dce7e0"></div>
      <div v-if="infoContent" style="margin-top:12px;padding:12px;background:#f7f9f8;border-radius:8px" v-html="infoContent"></div>
      <p class="muted" style="margin-top:12px">坐标数据可通过网格/房屋/事件管理页面录入。围栏在网格管理中圈选后自动显示。</p>
    </section>
  </template>
</template>
