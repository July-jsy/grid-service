<script setup>
import { computed, onMounted, ref } from 'vue'
import request from '../api'

const data = ref({})
const loading = ref(true)
const statusItems = computed(() => Object.entries(data.value.statusDistribution || {}))
const maxStatus = computed(() => Math.max(1, ...statusItems.value.map(([, value]) => value)))

onMounted(async () => {
  try { data.value = await request.get('/dashboard') } finally { loading.value = false }
})
</script>

<template>
  <div v-if="loading" class="loading">正在加载数据...</div>
  <template v-else>
    <section class="hero">
      <div><p class="eyebrow">今日治理概况</p><h1>让服务沉到网格，让问题止于社区</h1></div>
      <div class="hero-rate"><strong>{{ data.completionRate }}%</strong><span>事件办结率</span></div>
    </section>
    <section class="metric-grid">
      <article class="metric"><span>网格总数</span><strong>{{ data.gridCount }}</strong><small>个责任网格</small></article>
      <article class="metric"><span>服务居民</span><strong>{{ data.residentCount }}</strong><small>人基础档案</small></article>
      <article class="metric"><span>事件总数</span><strong>{{ data.eventCount }}</strong><small>件治理事件</small></article>
      <article class="metric"><span>服务事项</span><strong>{{ data.serviceCount }}</strong><small>项便民服务</small></article>
    </section>
    <section class="panel-grid">
      <article class="panel">
        <div class="panel-head"><h3>事件处置进度</h3><span>实时统计</span></div>
        <div v-for="[name, value] in statusItems" :key="name" class="bar-row">
          <span>{{ name }}</span><div class="bar"><i :style="{ width: `${value / maxStatus * 100}%` }"></i></div><strong>{{ value }}</strong>
        </div>
      </article>
      <article class="panel accent-panel">
        <p class="eyebrow">工作提示</p>
        <h3>网格工作要落细，居民诉求要闭环</h3>
        <p>及时响应待处理事件，完成现场处置后推进核查结案。</p>
        <RouterLink class="primary link-button" to="/events">查看事件列表</RouterLink>
      </article>
    </section>
  </template>
</template>
