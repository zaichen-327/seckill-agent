CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    actor_id BIGINT NULL,
    actor_role VARCHAR(32) NULL,
    action VARCHAR(64) NOT NULL,
    target_type VARCHAR(64) NOT NULL,
    target_id BIGINT NULL,
    before_json TEXT NULL,
    after_json TEXT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_audit_target (target_type, target_id),
    KEY idx_audit_actor (actor_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
