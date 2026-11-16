import { reactive } from 'vue'

// 登录状态：一个 reactive 对象，所有页面共用
// 不用 Pinia，这么小一个状态没必要
export const auth = reactive({
  token: localStorage.getItem('token') || '',
  role: localStorage.getItem('role') || '',
  username: localStorage.getItem('username') || ''
})

// 登录成功后调用：data = {token, role, username}
export function setLogin(data) {
  auth.token = data.token
  auth.role = data.role
  auth.username = data.username
  localStorage.setItem('token', data.token)
  localStorage.setItem('role', data.role)
  localStorage.setItem('username', data.username)
}

// 退出登录
export function clearLogin() {
  auth.token = ''
  auth.role = ''
  auth.username = ''
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  localStorage.removeItem('username')
}
