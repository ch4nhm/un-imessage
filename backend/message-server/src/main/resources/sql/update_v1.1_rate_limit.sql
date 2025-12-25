-- 2025-12-25 Add rate_limit to sys_template
ALTER TABLE sys_template
    ADD COLUMN rate_limit INT DEFAULT 0 COMMENT '频率限制 (每秒最大请求数, 0或null表示不限制)';
