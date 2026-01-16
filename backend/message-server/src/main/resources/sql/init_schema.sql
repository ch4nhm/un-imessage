CREATE TABLE `log_msg_batch`
(
    `id`             bigint(20)  NOT NULL AUTO_INCREMENT,
    `batch_no`       varchar(64) NOT NULL COMMENT '业务批次号 (UUID)',
    `app_id`         bigint(20)  NOT NULL COMMENT '调用方ID',
    `template_id`    bigint(20)  NOT NULL COMMENT '使用的模板ID',
    `channel_id`     bigint(20)  NOT NULL COMMENT '实际发送渠道ID',
    `msg_type`       tinyint(4)  NOT NULL COMMENT '冗余消息类型',
    `title`          varchar(100) DEFAULT NULL COMMENT '最终发送标题',
    `content_params` text COMMENT '业务方传入的参数JSON',
    `total_count`    int(11)      DEFAULT '0' COMMENT '总发送人数',
    `success_count`  int(11)      DEFAULT '0' COMMENT '成功人数',
    `fail_count`     int(11)      DEFAULT '0' COMMENT '失败人数',
    `status`         tinyint(4)   DEFAULT '0' COMMENT '批次状态: 0处理中 10全部成功 20部分成功 30全部失败',
    `created_at`     datetime     DEFAULT CURRENT_TIMESTAMP,
    `template_name`  varchar(100) DEFAULT NULL COMMENT 'Template Name Snapshot',
    `channel_name`   varchar(100) DEFAULT NULL COMMENT 'Channel Name Snapshot',
    `content`        text COMMENT 'Template Content Snapshot',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_batch_no` (`batch_no`),
    KEY `idx_create_time` (`created_at`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 15
  DEFAULT CHARSET = utf8mb4 COMMENT ='消息发送批次记录表';

CREATE TABLE `log_msg_detail`
(
    `id`                 bigint(20)   NOT NULL AUTO_INCREMENT,
    `batch_id`           bigint(20)   NOT NULL COMMENT '关联批次ID',
    `recipient`          varchar(100) NOT NULL COMMENT '接收者 (手机号/邮箱/OpenID/UserId)',
    `content`            text COMMENT 'Final Rendered Content',
    `status`             tinyint(4)   DEFAULT '0' COMMENT '发送状态: 10发送中 20发送成功 30发送失败',
    `third_party_msg_id` varchar(64)  DEFAULT NULL COMMENT '第三方返回的消息ID (用于回执查询)',
    `error_msg`          varchar(255) DEFAULT NULL COMMENT '失败原因',
    `retry_count`        int(11)      DEFAULT '0' COMMENT '重试次数',
    `send_time`          datetime     DEFAULT NULL COMMENT '实际发送时间',
    `created_at`         datetime     DEFAULT CURRENT_TIMESTAMP,
    `recipient_name`     varchar(100) DEFAULT NULL COMMENT 'Recipient Name',
    PRIMARY KEY (`id`),
    KEY `idx_batch_id` (`batch_id`),
    KEY `idx_recipient` (`recipient`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 19
  DEFAULT CHARSET = utf8mb4 COMMENT ='消息发送详情表';

CREATE TABLE `short_url`
(
    `id`           bigint(20)    NOT NULL AUTO_INCREMENT,
    `short_code`   varchar(10)   NOT NULL COMMENT '短链码 (Base62编码)',
    `original_url` varchar(2048) NOT NULL COMMENT '原始长链接',
    `created_by`   bigint(20) DEFAULT NULL COMMENT '创建者ID (关联sys_app)',
    `click_count`  bigint(20) DEFAULT '0' COMMENT '点击次数',
    `status`       tinyint(4) DEFAULT '1' COMMENT '状态: 1启用 0禁用',
    `expire_at`    datetime   DEFAULT NULL COMMENT '过期时间 (NULL表示永不过期)',
    `created_at`   datetime   DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   datetime   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_short_code` (`short_code`),
    KEY `idx_original_url` (`original_url`(255)),
    KEY `idx_expire_at` (`expire_at`),
    KEY `idx_created_by` (`created_by`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 2
  DEFAULT CHARSET = utf8mb4 COMMENT ='短链接映射表';

CREATE TABLE `short_url_access_log`
(
    `id`          bigint(20)  NOT NULL AUTO_INCREMENT,
    `short_code`  varchar(10) NOT NULL COMMENT '短链码',
    `ip`          varchar(50)  DEFAULT NULL COMMENT '访问者IP',
    `user_agent`  varchar(500) DEFAULT NULL COMMENT 'User-Agent',
    `referer`     varchar(500) DEFAULT NULL COMMENT '来源页面',
    `access_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
    PRIMARY KEY (`id`),
    KEY `idx_short_code` (`short_code`),
    KEY `idx_access_time` (`access_time`),
    KEY `idx_ip` (`ip`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 43
  DEFAULT CHARSET = utf8mb4 COMMENT ='短链接访问日志表';

CREATE TABLE `short_url_ip_blacklist`
(
    `id`         bigint(20)  NOT NULL AUTO_INCREMENT,
    `ip`         varchar(50) NOT NULL COMMENT 'IP地址',
    `reason`     varchar(255) DEFAULT NULL COMMENT '封禁原因',
    `expire_at`  datetime     DEFAULT NULL COMMENT '解封时间 (NULL表示永久封禁)',
    `created_at` datetime     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ip` (`ip`),
    KEY `idx_expire_at` (`expire_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='IP黑名单表';

CREATE TABLE `sys_app`
(
    `id`          bigint(20)   NOT NULL AUTO_INCREMENT,
    `app_name`    varchar(50)  NOT NULL COMMENT '应用名称 (如: 订单系统)',
    `app_code`    varchar(50)  DEFAULT NULL COMMENT '应用编码 (唯一标识)',
    `app_key`     varchar(64)  NOT NULL COMMENT '接口鉴权Key',
    `app_secret`  varchar(128) NOT NULL COMMENT '接口鉴权Secret',
    `status`      tinyint(4)   DEFAULT '1' COMMENT '状态: 1启用 0禁用',
    `owner`       varchar(50)  DEFAULT NULL COMMENT '负责人',
    `description` varchar(255) DEFAULT NULL COMMENT '描述',
    `created_at`  datetime     DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_app_code` (`app_code`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 5
  DEFAULT CHARSET = utf8mb4 COMMENT ='接入应用表';

CREATE TABLE `sys_channel`
(
    `id`          bigint(20)  NOT NULL AUTO_INCREMENT,
    `name`        varchar(50) NOT NULL COMMENT '渠道名称 (如: 阿里云短信-营销)',
    `type`        varchar(20) NOT NULL COMMENT '渠道类型: SMS, EMAIL, WECHAT_OFFICIAL, WECHAT_WORK, DINGTALK',
    `provider`    varchar(20) NOT NULL COMMENT '供应商: ALIYUN, TENCENT, LOCAL_SMTP',
    `config_json` text        NOT NULL COMMENT '账号配置JSON (AccessKey, Secret, Host, Port等)',
    `status`      tinyint(4) DEFAULT '1' COMMENT '状态: 1启用 0禁用',
    `created_at`  datetime   DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 7
  DEFAULT CHARSET = utf8mb4 COMMENT ='渠道配置表';

CREATE TABLE `sys_config`
(
    `id`          bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `system_name` varchar(100) NOT NULL COMMENT '系统名称',
    `logo`        longtext COMMENT '系统Logo（Base64）',
    `icon`        longtext COMMENT '系统图标（Base64）',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 2
  DEFAULT CHARSET = utf8mb4 COMMENT ='系统基础配置表';

CREATE TABLE `sys_recipient`
(
    `id`         bigint(20)  NOT NULL AUTO_INCREMENT,
    `name`       varchar(50) NOT NULL COMMENT '姓名',
    `mobile`     varchar(20)  DEFAULT NULL COMMENT '手机号',
    `email`      varchar(100) DEFAULT NULL COMMENT '邮箱',
    `open_id`    varchar(100) DEFAULT NULL COMMENT '微信OpenID',
    `user_id`    varchar(100) DEFAULT NULL COMMENT '企微/钉钉/飞书 UserId',
    `status`     tinyint(4)   DEFAULT '1' COMMENT '状态: 1启用 0禁用',
    `created_at` datetime     DEFAULT CURRENT_TIMESTAMP,
    `updated_at` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_mobile` (`mobile`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 3
  DEFAULT CHARSET = utf8mb4 COMMENT ='接收者表';

CREATE TABLE `sys_recipient_group`
(
    `id`          bigint(20)  NOT NULL AUTO_INCREMENT,
    `name`        varchar(50) NOT NULL COMMENT '分组名称',
    `code`        varchar(50)  DEFAULT NULL COMMENT '分组编码',
    `description` varchar(255) DEFAULT NULL COMMENT '描述',
    `created_at`  datetime     DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='接收者分组表';

CREATE TABLE `sys_recipient_group_relation`
(
    `id`           bigint(20) NOT NULL AUTO_INCREMENT,
    `group_id`     bigint(20) NOT NULL COMMENT '分组ID',
    `recipient_id` bigint(20) NOT NULL COMMENT '接收者ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_recipient` (`group_id`, `recipient_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='接收者分组关联表';

CREATE TABLE `sys_template`
(
    `id`                   bigint(20)  NOT NULL AUTO_INCREMENT,
    `name`                 varchar(50) NOT NULL COMMENT '模板名称',
    `code`                 varchar(50) NOT NULL COMMENT '模板编码 (业务方SDK调用凭证)',
    `app_id`               bigint(20)   DEFAULT NULL COMMENT '关联的应用ID (null表示公共模板)',
    `channel_id`           bigint(20)  NOT NULL COMMENT '关联的渠道ID',
    `msg_type`             tinyint(4)  NOT NULL COMMENT '消息类型: 10通知 20营销 30验证码',
    `third_party_id`       varchar(64)  DEFAULT NULL COMMENT '第三方模板ID (如阿里云短信模板Code)',
    `title`                varchar(100) DEFAULT NULL COMMENT '消息标题 (邮件/钉钉等需要)',
    `content`              text        NOT NULL COMMENT '消息内容模板 (支持占位符 ${code})',
    `variables`            varchar(255) DEFAULT NULL COMMENT '预期变量列表 (JSON数组, 用于校验)',
    `deduplication_config` varchar(255) DEFAULT NULL COMMENT '去重配置JSON (可选)',
    `status`               tinyint(4)   DEFAULT '1' COMMENT '状态',
    `created_at`           datetime     DEFAULT CURRENT_TIMESTAMP,
    `recipient_group_ids`  varchar(255) DEFAULT NULL COMMENT '关联的接收者分组ID列表 (逗号分隔)',
    `recipient_ids`        varchar(255) DEFAULT NULL COMMENT '关联的接收者ID列表 (逗号分隔)',
    `rate_limit`           int(11)      DEFAULT '0' COMMENT '频率限制 (每秒最大请求数, 0或null表示不限制)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_channel_id` (`channel_id`),
    KEY `idx_app_id` (`app_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 8
  DEFAULT CHARSET = utf8mb4 COMMENT ='消息模板表';

CREATE TABLE `sys_user`
(
    `id`         bigint(20)   NOT NULL AUTO_INCREMENT,
    `username`   varchar(50)  NOT NULL COMMENT '用户名',
    `password`   varchar(100) NOT NULL COMMENT '密码',
    `nickname`   varchar(50) DEFAULT NULL COMMENT '昵称',
    `status`     tinyint(4)  DEFAULT '1' COMMENT '状态: 1启用 0禁用',
    `created_at` datetime    DEFAULT CURRENT_TIMESTAMP,
    `updated_at` datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 2
  DEFAULT CHARSET = utf8mb4 COMMENT ='系统用户表'

