import request from './request'

// 拿验证码（要登录），返回 {captchaId, image}
export function getCaptcha(activityId) {
  return request.get('/seckill/captcha', { params: { activityId } })
}

// 拿秒杀 token（要登录），返回 token 字符串
export function getSeckillToken(activityId, captchaId, verifyCode) {
  return request.get('/seckill/token/' + activityId, {
    params: { captchaId, verifyCode }
  })
}

// 秒杀（要登录）
export function doSeckill(activityId, token) {
  return request.post('/seckill/doSeckill/' + activityId + '/' + encodeURIComponent(token))
}

// 查秒杀结果（要登录），返回 "秒杀成功" / "排队中" / "已售罄" / "活动未开始"
export function getSeckillResult(activityId) {
  return request.get('/seckill/result/' + activityId)
}
