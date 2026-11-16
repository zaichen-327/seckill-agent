import request from './request'

// 活动列表（不用登录）
export function getActivityList() {
  return request.get('/activity/list')
}

// 活动详情（不用登录）
export function getActivity(id) {
  return request.get('/activity/' + id)
}
