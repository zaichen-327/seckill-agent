# 秒杀系统前端（Vue 3 + Vite）

## 运行

```bash
npm install
npm run dev
```

打开 http://localhost:5173

## 打包上线

```bash
npm run build
```

打包出 `dist/`，用 nginx serve（配置见 `seckill-frontend-nginx.conf`，root 指到 `dist/`）。

## 说明

- 登录分用户 / 管理员两种入口；管理员（role=ADMIN）登录后导航栏出现"运维 Agent"，可进 `/ops` 跟 Agent 对话。
- `/ops` 的聊天功能依赖 FastAPI 服务（agent_api.py，端口 8000），要等切片 6 做好才能用；其他页面（登录、列表、秒杀）只依赖 Java 后端（8080）。
- 后端 Spring Boot 跑在 8080，开发时由 Vite 代理转发（见 vite.config.js）。
