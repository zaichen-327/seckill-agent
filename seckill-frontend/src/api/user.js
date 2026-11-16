import request from './request'

// 注册
export function register(username, password) {
  return request.post('/user/register', { username, password })
}

// 登录，返回 {token, role, username}
export function login(username, password) {
  return request.post('/user/login', { username, password })
}
