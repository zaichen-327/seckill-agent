import axios from 'axios'

const request = axios.create({
  baseURL: '',
  timeout: 10000
})

// 请求拦截器：自动带 JWT
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = 'Bearer ' + token
  }
  return config
})

// 响应拦截器：拆 Result（code 200 取 data，500 弹提示并拒绝）
request.interceptors.response.use(
  (response) => {
    const res = response.data
    // Java 后端 Result 形状：{code, message, data}
    if (res && typeof res.code !== 'undefined') {
      if (res.code === 200) {
        return res.data
      }
      alert(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    // 其他形状（比如 FastAPI 直接返回 {answer}），原样返回
    return res
  },
  (error) => {
    alert('网络错误：' + error.message)
    return Promise.reject(error)
  }
)

export default request
