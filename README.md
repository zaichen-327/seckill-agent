# 电商秒杀系统 + 运维 Agent

独立开发的后端项目：一个可落地的秒杀系统，外加一个帮运营管秒杀的运维 Agent。

- 项目时间：2026.10 – 至今
- 当前状态：秒杀核心链路 + 4 个运维工具接口已完成并通过 Apifox 验证；Python Agent 封装为下一步（Phase 2，11 月 12 日周四开始）

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Java 17、Spring Boot 2.7 |
| 存储 | MySQL、Redis |
| 消息 | RabbitMQ |
| 登录 | JWT |
| Agent | Python、LangChain（Phase 2 待开始） |
| 测试 | JMeter、Apifox、Docker |

## 秒杀核心链路

- **防超卖**：Lua 脚本原子扣减库存 + Redis 预热，库存扣减与下单解耦。
- **异步下单**：RabbitMQ 异步下单，生产者 confirm、消费者手动 ack、死信队列兜底。
- **登录与防刷**：JWT 登录，token 按（活动，用户）隔离；验证码 + 接口限流。
- **结果查询**：`GET /seckill/result/{activityId}`，四态返回：秒杀成功 / 排队中 / 已售罄 / 活动未开始。

## 压测结果

- JMeter 压测峰值 **440 QPS（核心链路，不含验证码和 token 发放），0 超卖**。

## 运维 Agent 工具接口

Agent 通过 HTTP 调用以下接口做运维排障（均走 JWT 鉴权）：

| 接口 | 说明 |
|---|---|
| `GET /ops/metrics` | 运维指标总览。订单统计 1 条 GROUP BY + 库存 1 次 mget，IO 固定 5 次，与活动数无关（亲手抓出的 N+1 已改批量） |
| `GET /ops/activities` | 活动列表，状态与 metrics 同口径：NOT_STARTED / RUNNING / ENDED |
| `GET /ops/orders` | 订单列表，`limit` 为上限而非保证条数 |
| `POST /ops/activities/{id}/publish` | 发布预热活动。接口层 `confirm=true` 确认；重复发布直接拒绝；建活动时清掉该 id 的旧库存 key，避免 id 复用导致脏数据 |

接口统一用 `Result` 返回（`code` 200 成功 / 500 失败，`message` + `data`）。

## 评测集

自建 20 条运维排障评测集（EVAL-001~020），覆盖库存未预热、MQ 积压、JWT 失效等真实故障场景，用于评测 Agent 的任务完成率、工具调用成功率与 token 成本。

## 目录结构

```
├── src/                 # Java 源码
├── jmeter/              # JMeter 压测脚本
├── docker-compose.yml   # 本地中间件（MySQL / Redis / RabbitMQ）
├── .env.example         # 环境变量模板（复制为 .env 后填真实值）
├── mvnw / mvnw.cmd      # Maven Wrapper
└── pom.xml
```

## 本地启动

```bash
cp .env.example .env   # 填好数据库 / Redis / RabbitMQ 的账号密码
docker compose up -d   # 起本地中间件
./mvnw spring-boot:run # 启动后端
```

> 启动命令与端口以本地实际配置为准。
