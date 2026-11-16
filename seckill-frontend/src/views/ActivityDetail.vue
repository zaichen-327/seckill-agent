<template>
  <div class="page">
    <div v-if="activity" class="card activity-card">
      <div class="banner" :style="{ background: bannerColor(activity) }">
        <span class="banner-tag">秒杀</span>
        <h3>{{ activity.name }}</h3>
      </div>
      <div class="card-body">
        <p><span class="price">¥{{ activity.seckillPrice }}</span> <span class="origin-price">¥{{ activity.originalPrice }}</span></p>
        <p>库存：{{ activity.availableStock }} / {{ activity.initialStock }}</p>
        <div class="stock-bar"><div :style="{ width: stockPercent + '%' }"></div></div>
        <p>时间：{{ activity.startTime }} ~ {{ activity.endTime }}</p>
        <p>
          <span class="badge" :class="[statusCls, { live: status === '进行中' }]">{{ status }}</span>
        </p>

        <div v-if="countdown" class="countdown">{{ countdown }}</div>

        <button v-if="!started" :disabled="!canSeckill" @click="startSeckill">参与秒杀</button>
        <p v-if="!canSeckill && !started">当前不能秒杀（{{ status }}）</p>

        <div v-if="step === 'captcha'">
          <p>请输入验证码：</p>
          <div class="captcha-box">
            <img :src="'data:image/png;base64,' + captchaImage" alt="验证码" @click="startSeckill" title="看不清？点一下换一张" />
            <input v-model="verifyCode" placeholder="验证码" />
            <button @click="getToken">确认</button>
          </div>
        </div>

        <p v-if="step === 'doing'">秒杀提交中...</p>
        <p v-if="step === 'polling'">排队中，正在查询结果...</p>
        <p v-if="result" class="result">结果：{{ result }}</p>
      </div>
    </div>
    <p v-else>加载中...</p>
    <p><router-link to="/">← 返回列表</router-link></p>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getActivity } from '../api/activity'
import { getCaptcha, getSeckillToken, doSeckill, getSeckillResult } from '../api/seckill'

const route = useRoute()
const router = useRouter()
const activityId = route.params.id

const activity = ref(null)
const started = ref(false)
const step = ref('') // '' | 'captcha' | 'doing' | 'polling' | 'done'
const captchaId = ref('')
const captchaImage = ref('')
const verifyCode = ref('')
const result = ref('')
const now = ref(new Date())
let timer = null
let countdownTimer = null

const palette = ['#2563eb', '#7c3aed', '#ea580c', '#0d9488', '#dc2626', '#1d4ed8', '#0891b2', '#b45309']
function bannerColor(a) {
  return palette[(a.id || 0) % palette.length]
}

const status = computed(() => {
  if (!activity.value) return ''
  const a = activity.value
  const start = new Date(a.startTime.replace(' ', 'T'))
  const end = new Date(a.endTime.replace(' ', 'T'))
  if (now.value < start) return '未开始'
  if (now.value > end) return '已结束'
  if (a.availableStock <= 0) return '已售罄'
  return '进行中'
})
const statusCls = computed(() => {
  if (status.value === '进行中') return 'green'
  if (status.value === '未开始') return 'amber'
  if (status.value === '已售罄') return 'red'
  return 'gray'
})
const canSeckill = computed(() => status.value === '进行中')
const stockPercent = computed(() => {
  if (!activity.value || !activity.value.initialStock) return 0
  return Math.round((activity.value.availableStock / activity.value.initialStock) * 100)
})

// 倒计时：未开始→距开始，进行中→距结束（每秒更新）
const countdown = computed(() => {
  if (!activity.value) return ''
  let target = null
  let label = ''
  if (status.value === '未开始') {
    target = new Date(activity.value.startTime.replace(' ', 'T'))
    label = '距开始 '
  } else if (status.value === '进行中') {
    target = new Date(activity.value.endTime.replace(' ', 'T'))
    label = '距结束 '
  } else {
    return ''
  }
  const diff = target - now.value
  if (diff <= 0) return ''
  const d = Math.floor(diff / 86400000)
  const h = Math.floor((diff % 86400000) / 3600000)
  const m = Math.floor((diff % 3600000) / 60000)
  const s = Math.floor((diff % 60000) / 1000)
  const pad = (n) => String(n).padStart(2, '0')
  return label + (d > 0 ? d + '天 ' : '') + pad(h) + ':' + pad(m) + ':' + pad(s)
})

onMounted(async () => {
  countdownTimer = setInterval(() => { now.value = new Date() }, 1000)
  try {
    activity.value = await getActivity(activityId)
  } catch (e) {
    // 失败提示已由请求拦截器弹出
  }
})

onUnmounted(() => {
  if (timer) clearTimeout(timer)
  if (countdownTimer) clearInterval(countdownTimer)
})

// 点"参与秒杀"：先拿验证码
async function startSeckill() {
  if (!localStorage.getItem('token')) {
    alert('请先登录')
    router.push('/login')
    return
  }
  started.value = true
  try {
    const data = await getCaptcha(activityId)
    captchaId.value = data.captchaId
    captchaImage.value = data.image
    step.value = 'captcha'
  } catch (e) {
    started.value = false
  }
}

// 填完验证码：拿秒杀 token，再提交秒杀
async function getToken() {
  if (!verifyCode.value) {
    alert('请输入验证码')
    return
  }
  try {
    const token = await getSeckillToken(activityId, captchaId.value, verifyCode.value)
    step.value = 'doing'
    await doSeckill(activityId, token)
    // 提交成功（已受理），开始轮询结果
    step.value = 'polling'
    pollResult()
  } catch (e) {
    // 验证码错了或 token 失效：重新拿验证码
    step.value = 'captcha'
    verifyCode.value = ''
    startSeckill()
  }
}

// 每 2 秒查一次结果，直到不是"排队中"
async function pollResult() {
  try {
    const r = await getSeckillResult(activityId)
    if (r === '排队中') {
      timer = setTimeout(pollResult, 2000)
    } else {
      result.value = r
      step.value = 'done'
    }
  } catch (e) {
    result.value = '查询失败，请刷新重试'
    step.value = 'done'
  }
}
</script>
