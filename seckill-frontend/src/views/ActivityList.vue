<template>
  <div class="page">
    <h2>秒杀活动</h2>
    <p class="subtitle">参与秒杀，先到先得，库存有限。</p>

    <div class="stats">
      <div class="stat dark">
        <div class="stat-label">进行中活动</div>
        <div class="stat-num">{{ runningCount }}<span>个</span></div>
        <div class="stat-sub">当前可参与秒杀</div>
      </div>
      <div class="stat">
        <div class="stat-label">活动总数</div>
        <div class="stat-num">{{ activities.length }}<span>个</span></div>
        <div class="stat-sub">共 {{ totalStock }} 件库存</div>
      </div>
      <div class="stat">
        <div class="stat-label">剩余库存</div>
        <div class="stat-num">{{ totalStock }}<span>件</span></div>
        <div class="stat-sub">全部活动剩余</div>
      </div>
      <div class="stat">
        <div class="stat-label">已售罄</div>
        <div class="stat-num">{{ soldOutCount }}<span>个</span></div>
        <div class="stat-sub">库存为 0 的活动</div>
      </div>
    </div>

    <div class="filters">
      <button v-for="f in filters" :key="f.value" :class="['pill', { active: filter === f.value }]" @click="filter = f.value">{{ f.label }}</button>
    </div>

    <p v-if="loading">加载中...</p>
    <p v-if="!loading && filtered.length === 0">没有符合条件的活动</p>
    <div class="grid">
      <div v-for="(a, i) in filtered" :key="a.id" class="card activity-card fade-up" :style="{ animationDelay: (i * 60) + 'ms' }">
        <div class="banner" :style="{ background: bannerColor(a) }">
          <span class="banner-tag">秒杀</span>
          <h3>{{ a.name }}</h3>
        </div>
        <div class="card-body">
          <p><span class="price">¥{{ a.seckillPrice }}</span> <span class="origin-price">¥{{ a.originalPrice }}</span></p>
          <p>库存：{{ a.availableStock }} / {{ a.initialStock }}</p>
          <div class="stock-bar"><div :style="{ width: stockPercent(a) + '%' }"></div></div>
          <p>{{ a.startTime }} ~ {{ a.endTime }}</p>
          <div class="card-foot">
            <span class="badge" :class="[statusOf(a).cls, { live: statusOf(a).text === '进行中' }]">{{ statusOf(a).text }}</span>
            <router-link :to="'/activity/' + a.id">参与秒杀 →</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getActivityList } from '../api/activity'
import { ui } from '../store/ui'

const activities = ref([])
const loading = ref(true)
const filter = ref('all')
const filters = [
  { label: '全部', value: 'all' },
  { label: '进行中', value: 'running' },
  { label: '未开始', value: 'not_started' },
  { label: '已售罄', value: 'soldout' },
  { label: '已结束', value: 'ended' }
]

const palette = ['#2563eb', '#7c3aed', '#ea580c', '#0d9488', '#dc2626', '#1d4ed8', '#0891b2', '#b45309']
function bannerColor(a) {
  return palette[(a.id || 0) % palette.length]
}

function statusOf(a) {
  const now = new Date()
  const start = new Date(a.startTime.replace(' ', 'T'))
  const end = new Date(a.endTime.replace(' ', 'T'))
  if (now < start) return { text: '未开始', cls: 'amber' }
  if (now > end) return { text: '已结束', cls: 'gray' }
  if (a.availableStock <= 0) return { text: '已售罄', cls: 'red' }
  return { text: '进行中', cls: 'green' }
}

function stockPercent(a) {
  if (!a.initialStock) return 0
  return Math.round((a.availableStock / a.initialStock) * 100)
}

const runningCount = computed(() => activities.value.filter(a => statusOf(a).text === '进行中').length)
const soldOutCount = computed(() => activities.value.filter(a => a.availableStock <= 0).length)
const totalStock = computed(() => activities.value.reduce((s, a) => s + (a.availableStock || 0), 0))

const filtered = computed(() => {
  let list = activities.value
  const q = ui.searchQuery.trim()
  if (q) list = list.filter(a => a.name.includes(q))
  if (filter.value === 'running') list = list.filter(a => statusOf(a).text === '进行中')
  if (filter.value === 'not_started') list = list.filter(a => statusOf(a).text === '未开始')
  if (filter.value === 'soldout') list = list.filter(a => a.availableStock <= 0)
  if (filter.value === 'ended') list = list.filter(a => statusOf(a).text === '已结束')
  return list
})

onMounted(async () => {
  try {
    activities.value = await getActivityList()
  } catch (e) {
    // 失败提示已由请求拦截器弹出
  } finally {
    loading.value = false
  }
})
</script>
