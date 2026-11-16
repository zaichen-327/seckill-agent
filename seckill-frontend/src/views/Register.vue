<template>
  <div class="auth-card">
    <h2>注册</h2>
    <form @submit.prevent="handleRegister">
      <label>用户名</label>
      <input v-model="username" placeholder="请输入用户名" />
      <label>密码</label>
      <input v-model="password" type="password" placeholder="至少 6 位" />
      <label>确认密码</label>
      <input v-model="confirmPassword" type="password" placeholder="再输一次密码" />
      <button type="submit">注册</button>
    </form>
    <p class="error" v-if="errorMsg">{{ errorMsg }}</p>
    <p class="switch">已有账号？<router-link to="/login">去登录</router-link></p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../api/user'

const router = useRouter()
const username = ref('')
const password = ref('')
const confirmPassword = ref('')
const errorMsg = ref('')

async function handleRegister() {
  errorMsg.value = ''
  if (!username.value || !password.value) {
    errorMsg.value = '请输入用户名和密码'
    return
  }
  if (password.value.length < 6) {
    errorMsg.value = '密码至少 6 位'
    return
  }
  if (password.value !== confirmPassword.value) {
    errorMsg.value = '两次密码不一致'
    return
  }
  try {
    await register(username.value, password.value)
    alert('注册成功，去登录吧')
    router.push('/login')
  } catch (e) {
    // 失败提示已由请求拦截器弹出
  }
}
</script>
