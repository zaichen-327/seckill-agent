<template>
  <div class="page">
    <h2>活动管理</h2>
    <p class="subtitle">仅管理员可见：新建活动、发布预热。新建只写数据库，发布后才写 Redis 库存。</p>

    <!-- 新建活动表单 -->
    <div class="card admin-form">
      <h3>新建活动</h3>
      <div class="form-grid">
        <div>
          <label>活动名称</label>
          <input v-model="form.name" placeholder="例如：iPhone 15 Pro 秒杀" />
        </div>
        <div>
          <label>商品 ID</label>
          <input v-model="form.productId" type="number" min="1" placeholder="例如：1" />
        </div>
        <div>
          <label>原价（元）</label>
          <input v-model="form.originalPrice" type="number" min="0" step="0.01" placeholder="例如：8999" />
        </div>
        <div>
          <label>秒杀价（元）</label>
          <input v-model="form.seckillPrice" type="number" min="0" step="0.01" placeholder="例如：4999" />
        </div>
        <div>
          <label>初始库存（件）</label>
          <input v-model="form.initialStock" type="number" min="1" placeholder="例如：100" @input="form.availableStock = form.initialStock" />
        </div>
        <div>
          <label>可售库存（件）</label>
          <input v-model="form.availableStock" type="number" min="1" placeholder="默认等于初始库存" />
        </div>
        <div>
          <label>开始时间</label>
          <input v-model="form.startTime" type="datetime-local" />
        </div>
        <div>
          <label>结束时间</label>
          <input v-model="form.endTime" type="datetime-local" />
        </div>
      </div>
      <button :disabled="creating" @click="onCreate">{{ creating ? '创建中...' : '创建活动' }}</button>
      <p class="tip">创建成功后活动出现在下方列表里，还需要点「发布预热」才会写 Redis 库存、开始秒杀。</p>
    </div>

    <!-- 活动列表 -->
    <p v-if="loading">加载中...</p>
    <p v-if="!loading && activities.length === 0">还没有活动，先在上面新建一个吧</p>
    <div v-if="!loading && activities.length > 0" class="card">
      <table class="admin-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>活动名称</th>
            <th>秒杀价</th>
            <th>库存</th>
            <th>状态</th>
            <th>时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="a in activities" :key="a.id">
            <td>{{ a.id }}</td>
            <td>{{ a.name }}</td>
            <td><span class="price">¥{{ a.seckillPrice }}</span> <span class="origin-price">¥{{ a.originalPrice }}</span></td>
            <td>{{ a.availableStock }} / {{ a.initialStock }}</td>
            <td><span class="badge" :class="statusCls(a.status)">{{ statusText(a.status) }}</span></td>
            <td class="time-cell">{{ a.startTime }}<br />~ {{ a.endTime }}</td>
            <td>
              <button class="btn-small" :disabled="a.status === 'ENDED' || isPublished(a) || publishingId === a.id" @click="onPublish(a)">
                {{ isPublished(a) ? '已预热' : (publishingId === a.id ? '发布中...' : '发布预热') }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAdminActivities, getMetrics, createActivity, publishActivity } from '../api/admin'

const activities = ref([])
const loading = ref(true)
const creating = ref(false)
const publishingId = ref(null)
// 活动 id → redisStock（null = 没发布过，数字 = 已发布过，含 0）
const publishedMap = ref(new Map())

const form = ref({
  name: '',
  productId: '',
  originalPrice: '',
  seckillPrice: '',
  initialStock: '',
  availableStock: '',
  startTime: '',
  endTime: ''
})

// 后端状态码 → 中文
function statusText(status) {
  if (status === 'NOT_STARTED') return '未开始'
  if (status === 'RUNNING') return '进行中'
  if (status === 'ENDED') return '已结束'
  return status || '未知'
}

function statusCls(status) {
  if (status === 'NOT_STARTED') return 'amber'
  if (status === 'RUNNING') return 'green'
  if (status === 'ENDED') return 'gray'
  return 'gray'
}

// 是否已发布预热：redisStock 不是 null 就是发布过
// 注意不能写 !redisStock，因为卖光后 redisStock 是 0，也是已发布
function isPublished(a) {
  const stock = publishedMap.value.get(a.id)
  return stock !== null && stock !== undefined
}

// datetime-local 的值是 "2026-11-18T15:30"，后端要 "2026-11-18 15:30:00"
function toBackendTime(value) {
  if (!value) return ''
  return value.replace('T', ' ') + ':00'
}

async function loadList() {
  loading.value = true
  try {
    // 活动列表和运维指标一起拉；指标挂了也不影响列表显示
    const [list, metrics] = await Promise.all([
      getAdminActivities(),
      getMetrics().catch(() => null)
    ])
    activities.value = list
    const map = new Map()
    if (metrics && metrics.activities) {
      for (const m of metrics.activities) {
        map.set(m.activityId, m.redisStock)
      }
    }
    publishedMap.value = map
  } catch (e) {
    // 失败提示已由请求拦截器弹出
  } finally {
    loading.value = false
  }
}

async function onCreate() {
  if (!form.value.name || !form.value.productId || !form.value.originalPrice
    || !form.value.seckillPrice || !form.value.initialStock
    || !form.value.startTime || !form.value.endTime) {
    alert('请把表单填完整')
    return
  }
  if (new Date(form.value.endTime) <= new Date(form.value.startTime)) {
    alert('结束时间必须晚于开始时间')
    return
  }
  creating.value = true
  try {
    await createActivity({
      name: form.value.name,
      productId: Number(form.value.productId),
      originalPrice: Number(form.value.originalPrice),
      seckillPrice: Number(form.value.seckillPrice),
      initialStock: Number(form.value.initialStock),
      availableStock: Number(form.value.availableStock || form.value.initialStock),
      startTime: toBackendTime(form.value.startTime),
      endTime: toBackendTime(form.value.endTime)
    })
    alert('活动创建成功，记得点「发布预热」')
    form.value = { name: '', productId: '', originalPrice: '', seckillPrice: '', initialStock: '', availableStock: '', startTime: '', endTime: '' }
    await loadList()
  } catch (e) {
    // 失败提示已由请求拦截器弹出
  } finally {
    creating.value = false
  }
}

async function onPublish(a) {
  if (!confirm('确定要发布预热活动「' + a.name + '」吗？发布后库存 ' + a.availableStock + ' 件将写入 Redis，开始秒杀。')) {
    return
  }
  publishingId.value = a.id
  try {
    const result = await publishActivity(a.id)
    alert('发布成功，库存 ' + (result && result.stock) + ' 件已写入 Redis')
    await loadList()
  } catch (e) {
    // 已发布 / 未确认等失败提示已由请求拦截器弹出
  } finally {
    publishingId.value = null
  }
}

onMounted(loadList)
</script>
