import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import ActivityList from '../views/ActivityList.vue'
import ActivityDetail from '../views/ActivityDetail.vue'
import OpsChat from '../views/OpsChat.vue'

const routes = [
  { path: '/', name: 'ActivityList', component: ActivityList },
  { path: '/login', name: 'Login', component: Login },
  { path: '/register', name: 'Register', component: Register },
  { path: '/activity/:id', name: 'ActivityDetail', component: ActivityDetail },
  { path: '/ops', name: 'OpsChat', component: OpsChat, meta: { requireAdmin: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：/ops 只允许管理员（前端守卫只是体验，后端拦截器才是真正的鉴权）
router.beforeEach((to, from, next) => {
  if (to.meta.requireAdmin) {
    const role = localStorage.getItem('role')
    if (role !== 'ADMIN') {
      alert('需要管理员权限')
      next('/login')
      return
    }
  }
  next()
})

export default router
