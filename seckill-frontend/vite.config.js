import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 开发时：前端跑在 5173，接口由 Vite 代理转发，解决跨域
// 上线时：nginx 做同样的代理（见 seckill-frontend-nginx.conf）
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      // Java 后端（8080）
      '/user': 'http://localhost:8080',
      '/activity': 'http://localhost:8080',
      '/seckill': 'http://localhost:8080',
      '/ops': 'http://localhost:8080',
      // Python Agent 服务（8000，切片 6 做好后才有）
      // 注意：FastAPI 里路由是 /chat，没有 /agent 前缀，所以代理要把 /agent 去掉再转发
      // 不去掉的话，8000 收到的是 /agent/chat，找不到路由就报 404
      '/agent': {
        target: 'http://localhost:8000',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/agent/, '')
      }
    }
  }
})
