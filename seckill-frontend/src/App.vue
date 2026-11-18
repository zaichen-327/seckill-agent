<template>
  <div id="app">
    <header class="topbar">
      <span class="brand">⚡ 秒杀系统</span>
      <div v-if="showLayout" class="search-box">
        <input v-model="ui.searchQuery" placeholder="搜索活动名称..." @input="onSearchInput" />
      </div>
      <div class="top-right">
        <template v-if="auth.username">
          <span class="hello">你好，{{ auth.username }}</span>
          <a href="#" @click.prevent="logout">退出</a>
        </template>
        <router-link v-else to="/login">登录</router-link>
      </div>
    </header>
    <div class="layout">
      <aside v-if="showLayout" class="sidebar">
        <nav>
          <router-link to="/">📋 活动列表</router-link>
          <router-link v-if="isAdmin" to="/admin/activities">🛠️ 活动管理</router-link>
          <router-link v-if="isAdmin" to="/ops">🤖 运维 Agent</router-link>
        </nav>
        <div class="side-bottom">
          <p>当前日期<br />{{ today }}</p>
        </div>
      </aside>
      <main class="main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { auth, clearLogin } from './store/auth'
import { ui } from './store/ui'

const router = useRouter()
const route = useRoute()
// 只有管理员才显示"运维 Agent"入口
const isAdmin = computed(() => auth.role === 'ADMIN')
// 登录/注册页不显示侧边栏和搜索框
const showLayout = computed(() => !['/login', '/register'].includes(route.path))
const today = computed(() => new Date().toLocaleDateString('zh-CN'))

function logout() {
  clearLogin()
  router.push('/login')
}

// 在顶栏搜索时，如果不在列表页，先跳回列表页再筛选
function onSearchInput() {
  if (route.path !== '/') {
    router.push('/')
  }
}
</script>
