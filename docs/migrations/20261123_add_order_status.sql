-- 已有数据库执行一次；新库由 schema.sql 直接创建 status 字段。
-- 旧订单默认视为已完成，避免历史数据被误标为待支付。
ALTER TABLE seckill_order
    ADD COLUMN status VARCHAR(32) NOT NULL DEFAULT 'COMPLETED' COMMENT '订单状态' AFTER user_id;

ALTER TABLE seckill_order
    ALTER COLUMN status SET DEFAULT 'PENDING_PAYMENT';

CREATE INDEX idx_order_status_create_time ON seckill_order (status, create_time);
