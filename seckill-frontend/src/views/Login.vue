<template>
  <div class="auth-card">
    <h2>登录</h2>
    <div class="tabs">
      <button :class="{ active: mode === 'user' }" @click="mode = 'user'">用户登录</button>
      <button :class="{ active: mode === 'admin' }" @click="mode = 'admin'">管理员登录</button>
    </div>
    <form @submit.prevent="handleLogin">
      <label>用户名</label>
      <input v-model="username" placeholder="请输入用户名" />
      <label>密码</label>
      <input v-model="password" type="password" placeholder="请输入密码" />
      <button type="submit">登录</button>
    </form>
    <p class="error" v-if="errorMsg">{{ errorMsg }}</p>
    <p class="switch">没有账号？<router-link to="/register">去注册</router-link></p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api/user'
import { setLogin } from '../store/auth'

const router = useRouter()
const mode = ref('user') // 'user' 用户登录，'admin' 管理员登录
const username = ref('')
const password = ref('')
const errorMsg = ref('')

async function handleLogin() {
  errorMsg.value = ''
  if (!username.value || !password.value) {
    errorMsg.value = '请输入用户名和密码'
    return
  }
  try {
    // data = {token, role, username}
    const data = await login(username.value, password.value)
    // 管理员入口：账号必须是 ADMIN，不然不让进
    if (mode.value === 'admin' && data.role !== 'ADMIN') {
      errorMsg.value = '该账号不是管理员，请用管理员账号登录'
      return
    }
    setLogin(data)
    if (data.role === 'ADMIN' && mode.value === 'admin') {
      router.push('/ops')
    } else {
      router.push('/')
    }
  } catch (e) {
    // 失败提示（比如用户名或密码错误）已由请求拦截器弹出
  }
}
</script>
