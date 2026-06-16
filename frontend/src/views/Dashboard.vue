<script setup>
import { computed, onMounted, ref, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '../api'

const data = ref({})
const loading = ref(true)
const statusItems = computed(() => Object.entries(data.value.statusDistribution || {}))
const maxStatus = computed(() => Math.max(1, ...statusItems.value.map(([, value]) => value)))
const chartRef = ref(null)
const categoryChartRef = ref(null)

onMounted(async () => {
  try { data.value = await request.get('/dashboard') } finally { loading.value = false }
  await nextTick()
  watch(data, () => { renderCharts() }, { immediate: true })
})

function renderCharts() {
  if (chartRef.value) {
    const chart = echarts.init(chartRef.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: statusItems.value.map(([k]) => k) },
      yAxis: { type: 'value' },
      series: [{ data: statusItems.value.map(([, v]) => v), type: 'bar', itemStyle: { color: '#17614f' } }],
    })
  }
  if (categoryChartRef.value) {
    const chart = echarts.init(categoryChartRef.value)
    const categories = Object.entries(data.value.categoryDistribution || {})
    chart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie', radius: ['40%', '70%'],
        data: categories.map(([name, value]) => ({ name, value })),
        label: { show: true, formatter: '{b}: {c}' },
      }],
    })
  }
}
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
    <section class="metric-grid">
      <article class="metric"><span>待处理</span><strong>{{ data.pendingCount }}</strong><small>件</small></article>
      <article class="metric"><span>处理中</span><strong>{{ data.processingCount }}</strong><small>件</small></article>
      <article class="metric"><span>已办结</span><strong>{{ data.completedCount }}</strong><small>件</small></article>
      <article class="metric"><span>通知公告</span><strong>{{ data.noticeCount }}</strong><small>条</small></article>
    </section>
    <section class="panel-grid">
      <article class="panel">
        <div class="panel-head"><h3>事件处置进度</h3><span>实时统计</span></div>
        <div ref="chartRef" style="height:260px"></div>
      </article>
      <article class="panel">
        <div class="panel-head"><h3>事件分类分布</h3><span>按类型统计</span></div>
        <div ref="categoryChartRef" style="height:260px"></div>
      </article>
    </section>
    <section class="panel-grid" style="grid-template-columns:1fr;margin-top:20px">
      <article class="panel accent-panel">
        <p class="eyebrow">工作提示</p>
        <h3>网格工作要落细，居民诉求要闭环</h3>
        <p>及时响应待处理事件，完成现场处置后推进核查结案。</p>
        <RouterLink class="primary link-button" to="/events">查看事件列表</RouterLink>
      </article>
    </section>
  </template>
</template>
