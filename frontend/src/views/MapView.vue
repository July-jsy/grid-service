<script setup>
import { onMounted, ref } from 'vue'
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
  script.src = 'https://webapi.amap.com/maps?v=2.0&key=YOUR_AMAP_KEY&plugin=AMap.Marker'
  script.onload = () => {
    map = new AMap.Map(mapContainer.value, { zoom: 14, center: defaultCenter })
    addMarkers()
  }
  document.head.appendChild(script)
}

function addMarkers() {
  // Grid markers (green)
  grids.value.forEach(grid => {
    if (grid.longitude && grid.latitude) {
      const marker = new AMap.Marker({
        position: [grid.longitude, grid.latitude],
        title: grid.name,
        label: { content: grid.name, direction: 'top' },
      })
      marker.setMap(map)
      marker.on('click', () => {
        infoContent.value = `<b>网格：${grid.name}</b><br>社区：${grid.community}<br>网格员：${grid.staffName || '-'}<br>居民数：${grid.residentCount}`
      })
    }
  })

  // Event markers (red)
  events.value.forEach(ev => {
    if (ev.longitude && ev.latitude) {
      const marker = new AMap.Marker({
        position: [ev.longitude, ev.latitude],
        title: ev.title,
        icon: new AMap.Icon({ size: new AMap.Size(24, 24), image: 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10" fill="%23b33b32"/></svg>' }),
      })
      marker.setMap(map)
      marker.on('click', () => {
        infoContent.value = `<b>事件：${ev.title}</b><br>分类：${ev.category}<br>网格：${ev.gridName}<br>状态：${ev.status}<br>地址：${ev.address || '-'}`
      })
    }
  })

  // House markers (blue)
  houses.value.forEach(house => {
    if (house.longitude && house.latitude) {
      const marker = new AMap.Marker({
        position: [house.longitude, house.latitude],
        title: house.address,
        icon: new AMap.Icon({ size: new AMap.Size(20, 20), image: 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10" fill="%232c68a8"/></svg>' }),
      })
      marker.setMap(map)
      marker.on('click', () => {
        infoContent.value = `<b>房屋：${house.address}</b><br>网格：${house.gridName}<br>户主：${house.ownerName || '-'}<br>类型：${house.houseType || '-'}`
      })
    }
  })
}

import { nextTick } from 'vue'
</script>

<template>
  <div v-if="loading" class="loading">正在加载地图数据...</div>
  <template v-else>
    <section class="panel">
      <div class="panel-head"><div><p class="eyebrow">MAP VISUALIZATION</p><h3>地图可视化</h3></div>
        <div>
          <span class="tag" style="margin-right:8px">🟢 {{ grids.length }} 网格</span>
          <span class="tag" style="margin-right:8px">🔴 {{ events.filter(e => e.longitude).length }} 事件</span>
          <span class="tag">🔵 {{ houses.filter(h => h.longitude).length }} 房屋</span>
        </div>
      </div>
      <div ref="mapContainer" style="width:100%;height:600px;border-radius:12px;border:1px solid #dce7e0"></div>
      <div v-if="infoContent" style="margin-top:12px;padding:12px;background:#f7f9f8;border-radius:8px" v-html="infoContent"></div>
      <p class="muted" style="margin-top:12px">提示：请将 YOUR_AMAP_KEY 替换为实际的高德地图 JS API Key。坐标数据可通过网格/房屋/事件管理页面录入。</p>
    </section>
  </template>
</template>
