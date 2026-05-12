-- 用户账户表：微信 openid 与 手机/邮箱 凭证可并存；执行前请确认库名。
-- 若已有旧表结构（无 phone/email/password_hash），请执行：db/alter-user-account-credentials.sql
-- 若表不存在，本脚本可整表创建。

CREATE TABLE IF NOT EXISTS user_account (
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    open_id         VARCHAR(64)  NULL COMMENT '微信 openid',
    phone           VARCHAR(20)  NULL COMMENT '手机号',
    email           VARCHAR(128) NULL COMMENT '邮箱',
    password_hash   VARCHAR(255) NULL COMMENT 'BCrypt 密码',
    nickname        VARCHAR(64)  NULL,
    avatar_url      VARCHAR(512) NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_open_id (open_id),
    UNIQUE KEY uk_user_phone (phone),
    UNIQUE KEY uk_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
