CREATE DATABASE IF NOT EXISTS seckill_db;
USE seckill_db;

DROP TABLE IF EXISTS seckill_activity;

CREATE TABLE seckill_activity (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    product_id BIGINT NOT NULL,
    original_price DECIMAL(19,2) NOT NULL,
    seckill_price DECIMAL(19,2) NOT NULL,
    initial_stock INT NOT NULL,
    available_stock INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS seckill_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    # 避免联合索引时，会因为找不到而查找全表，通过使用维护索引的微小成本来降低大批量sql的请求
    KEY 'idx_activity_id' ('activity_id'),
    UNIQUE KEY uk_user_activity (user_id, activity_id)
);

INSERT INTO seckill_activity (id, name, product_id, original_price, seckill_price, initial_stock, available_stock, start_time, end_time)
VALUES (1, 'iPhone 15 Pro', 1001, 9999.00, 4999.00, 100, 100, DATE_ADD(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 25 HOUR));

-- Activity 2: Started activity (starts from current time)
INSERT INTO seckill_activity (id, name, product_id, original_price, seckill_price, initial_stock, available_stock, start_time, end_time)
VALUES (2, '小米 14', 1002, 3999.00, 1999.00, 50, 50, NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY));

-- Activity 3: Sold out activity
INSERT INTO seckill_activity (id, name, product_id, original_price, seckill_price, initial_stock, available_stock, start_time, end_time)
VALUES (3, 'MacBook Pro (已售罄)', 1003, 12999.00, 6999.00, 30, 0, NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY));