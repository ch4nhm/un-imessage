-- =====================================================
-- UniMessage v1.2 - 短链接服务表结构
-- 创建时间: 2026-01-14
-- =====================================================

-- 短链接映射表
CREATE TABLE IF NOT EXISTS short_url
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    short_code   VARCHAR(10)   NOT NULL COMMENT '短链码 (Base62编码)',
    original_url VARCHAR(2048) NOT NULL COMMENT '原始长链接',
    created_by   BIGINT        NULL COMMENT '创建者ID (关联sys_app)',
    click_count  BIGINT   DEFAULT 0 COMMENT '点击次数',
    status       TINYINT  DEFAULT 1 COMMENT '状态: 1启用 0禁用',
    expire_at    DATETIME      NULL COMMENT '过期时间 (NULL表示永不过期)',
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_short_code (short_code),
    INDEX idx_original_url (original_url(255)),
    INDEX idx_expire_at (expire_at),
    INDEX idx_created_by (created_by)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='短链接映射表';

-- 短链接访问日志表
CREATE TABLE IF NOT EXISTS short_url_access_log
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    short_code  VARCHAR(10)  NOT NULL COMMENT '短链码',
    ip          VARCHAR(50)  NULL COMMENT '访问者IP',
    user_agent  VARCHAR(500) NULL COMMENT 'User-Agent',
    referer     VARCHAR(500) NULL COMMENT '来源页面',
    access_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
    INDEX idx_short_code (short_code),
    INDEX idx_access_time (access_time),
    INDEX idx_ip (ip)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='短链接访问日志表';

-- IP黑名单表 (用于限流封禁)
CREATE TABLE IF NOT EXISTS short_url_ip_blacklist
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    ip         VARCHAR(50)  NOT NULL COMMENT 'IP地址',
    reason     VARCHAR(255) NULL COMMENT '封禁原因',
    expire_at  DATETIME     NULL COMMENT '解封时间 (NULL表示永久封禁)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_ip (ip),
    INDEX idx_expire_at (expire_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='IP黑名单表';
