import request from './request'

// 管理员活动列表（GET /ops/activities，带状态）
export function getAdminActivities() {
  return request.get('/ops/activities')
}

// 运维指标（GET /ops/metrics），用里面的 redisStock 判断活动是否已发布预热
// redisStock 为 null = 没发布过；是数字（含 0）= 已发布过
export function getMetrics() {
  return request.get('/ops/metrics')
}

// 管理员新建活动（POST /ops/activities，只写 MySQL，不写 Redis）
export function createActivity(data) {
  return request.post('/ops/activities', data)
}

// 管理员发布预热（POST /ops/activities/{id}/publish，confirm=true）
export function publishActivity(id) {
  return request.post('/ops/activities/' + id + '/publish', { confirm: true })
}
