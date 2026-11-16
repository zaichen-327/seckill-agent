import request from './request'

// 问运维 Agent（要管理员）
// token 由请求拦截器自动从 localStorage 取，放在 Authorization 头里
// 注意：这个接口要等切片 6 的 FastAPI 服务（agent_api.py，端口 8000）做好才能用
// 返回 {answer: '...'}
export function askAgent(question, sessionId) {
  return request.post('/agent/chat', { question, session_id: sessionId })
}
