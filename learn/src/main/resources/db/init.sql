-- ============================================================
-- PostgreSQL 建表语句
-- 数据库：demo
-- ============================================================

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL       PRIMARY KEY,
    username    VARCHAR(50)     NOT NULL UNIQUE,
    email       VARCHAR(100)    NOT NULL,
    phone       VARCHAR(20),
    nickname    VARCHAR(100),
    avatar_url  TEXT,
    status      SMALLINT        NOT NULL DEFAULT 1 CHECK (status IN (0, 1)),
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  users            IS '用户表';
COMMENT ON COLUMN users.id         IS '主键';
COMMENT ON COLUMN users.username   IS '用户名（唯一）';
COMMENT ON COLUMN users.email      IS '邮箱';
COMMENT ON COLUMN users.phone      IS '手机号';
COMMENT ON COLUMN users.nickname   IS '昵称';
COMMENT ON COLUMN users.avatar_url IS '头像URL';
COMMENT ON COLUMN users.status     IS '状态：1=正常 0=禁用';
COMMENT ON COLUMN users.created_at IS '创建时间';
COMMENT ON COLUMN users.updated_at IS '更新时间';

-- 索引
CREATE INDEX IF NOT EXISTS idx_users_email     ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_status    ON users(status);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at DESC);

-- 自动维护 updated_at（可选：使用触发器）
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS set_updated_at ON users;
CREATE TRIGGER set_updated_at
BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 测试数据
INSERT INTO users (username, email, phone, nickname, status)
VALUES
    ('alice',  'alice@example.com',  '13800000001', 'Alice',  1),
    ('bob',    'bob@example.com',    '13800000002', 'Bob',    1),
    ('charlie','charlie@example.com','13800000003', 'Charlie',0)
ON CONFLICT (username) DO NOTHING;
