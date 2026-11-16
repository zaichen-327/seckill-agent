<template>
  <div class="page">
    <h2>运维 Agent</h2>
    <div class="chat">
      <div class="messages">
        <div v-for="(m, i) in messages" :key="i" :class="['msg', m.role]">
          <strong>{{ m.role === 'user' ? '我' : 'Agent' }}</strong>{{ m.content }}
        </div>
        <p v-if="thinking">Agent 思考中...</p>
        <p v-if="messages.length === 0 && !thinking" class="tip">试着问：现在有没有超卖？活动 3 的库存为什么是 null？</p>
      </div>
      <div class="input-row">
        <input v-model="question" placeholder="问 Agent，比如：现在有没有超卖？" @keyup.enter="send" />
        <button @click="send" :disabled="thinking">发送</button>
        <button class="secondary" @click="newChat">新对话</button>
      </div>
    </div>
    <p class="tip">注意：聊天功能要等 FastAPI 服务（agent_api.py，端口 8000）做好才能用。</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { askAgent } from '../api/agent'

const messages = ref([])
const question = ref('')
const thinking = ref(false)
// 会话 id：一个浏览器一个对话，点"新对话"就换一个
const sessionId = ref('sess-' + Date.now() + '-' + Math.floor(Math.random() * 10000))

async function send() {
  const q = question.value.trim()
  if (!q || thinking.value) return
  messages.value.push({ role: 'user', content: q })
  question.value = ''
  thinking.value = true
  try {
    // data = {answer: '...'}
    const data = await askAgent(q, sessionId.value)
    messages.value.push({ role: 'agent', content: data.answer || '（没有返回答案）' })
  } catch (e) {
    messages.value.push({ role: 'agent', content: '调用失败，请确认 Agent 服务（8000 端口）已启动。' })
  } finally {
    thinking.value = false
  }
}

function newChat() {
  messages.value = []
  sessionId.value = 'sess-' + Date.now() + '-' + Math.floor(Math.random() * 10000)
}
</script>
