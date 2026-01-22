-- 初始化数据库 Schema

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(64) NOT NULL COMMENT '用户名',
  `password` varchar(128) NOT NULL COMMENT '密码',
  `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
  `email` varchar(128) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(32) DEFAULT NULL COMMENT '手机号',
  `status` int DEFAULT '1' COMMENT '状态 1:启用 0:禁用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ----------------------------
-- Table structure for sys_app
-- ----------------------------
DROP TABLE IF EXISTS `sys_app`;
CREATE TABLE `sys_app` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `app_name` varchar(64) NOT NULL COMMENT '应用名称',
  `app_code` varchar(64) NOT NULL COMMENT '应用编码',
  `app_key` varchar(64) NOT NULL COMMENT 'AppKey',
  `app_secret` varchar(128) NOT NULL COMMENT 'AppSecret',
  `status` int DEFAULT '1' COMMENT '状态 1:启用 0:禁用',
  `owner` varchar(64) DEFAULT NULL COMMENT '负责人',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_code` (`app_code`),
  UNIQUE KEY `uk_app_key` (`app_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='接入应用表';

-- ----------------------------
-- Table structure for sys_channel
-- ----------------------------
DROP TABLE IF EXISTS `sys_channel`;
CREATE TABLE `sys_channel` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(64) NOT NULL COMMENT '渠道名称',
  `type` varchar(32) NOT NULL COMMENT '渠道类型: SMS, EMAIL, WECHAT等',
  `provider` varchar(32) NOT NULL COMMENT '供应商: ALIYUN, TENCENT等',
  `config_json` text NOT NULL COMMENT '配置信息JSON',
  `status` int DEFAULT '1' COMMENT '状态 1:启用 0:禁用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='渠道配置表';

-- ----------------------------
-- Table structure for sys_template
-- ----------------------------
DROP TABLE IF EXISTS `sys_template`;
CREATE TABLE `sys_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(64) NOT NULL COMMENT '模板名称',
  `code` varchar(64) NOT NULL COMMENT '模板编码',
  `app_id` bigint DEFAULT NULL COMMENT '关联应用ID',
  `channel_id` bigint DEFAULT NULL COMMENT '关联渠道ID',
  `msg_type` int NOT NULL COMMENT '消息类型: 10通知 20营销 30验证码',
  `third_party_id` varchar(128) DEFAULT NULL COMMENT '第三方模板ID',
  `title` varchar(128) DEFAULT NULL COMMENT '消息标题',
  `content` text COMMENT '消息内容模板',
  `variables` text COMMENT '变量列表JSON',
  `deduplication_config` text COMMENT '去重配置JSON',
  `rate_limit` int DEFAULT NULL COMMENT '限流(TPS)',
  `recipient_group_ids` varchar(255) DEFAULT NULL COMMENT '关联接收者分组ID列表',
  `recipient_ids` varchar(1024) DEFAULT NULL COMMENT '关联接收者ID列表',
  `status` int DEFAULT '1' COMMENT '状态 1:启用 0:禁用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_app_id` (`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息模板表';

-- ----------------------------
-- Table structure for sys_recipient
-- ----------------------------
DROP TABLE IF EXISTS `sys_recipient`;
CREATE TABLE `sys_recipient` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(64) NOT NULL COMMENT '姓名',
  `mobile` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(128) DEFAULT NULL COMMENT '邮箱',
  `user_id` varchar(1024) DEFAULT NULL COMMENT '各渠道用户ID(JSON格式)',
  `status` int DEFAULT '1' COMMENT '状态 1:启用 0:禁用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_mobile` (`mobile`),
  KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='接收者表';

-- ----------------------------
-- Table structure for sys_recipient_group
-- ----------------------------
DROP TABLE IF EXISTS `sys_recipient_group`;
CREATE TABLE `sys_recipient_group` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(64) NOT NULL COMMENT '分组名称',
  `code` varchar(64) DEFAULT NULL COMMENT '分组编码',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `status` int DEFAULT '1' COMMENT '状态 1:启用 0:禁用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='接收者分组表';

-- ----------------------------
-- Table structure for sys_recipient_group_relation
-- ----------------------------
DROP TABLE IF EXISTS `sys_recipient_group_relation`;
CREATE TABLE `sys_recipient_group_relation` (
  `group_id` bigint NOT NULL COMMENT '分组ID',
  `recipient_id` bigint NOT NULL COMMENT '接收者ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`group_id`,`recipient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分组关联表';

-- ----------------------------
-- Table structure for log_msg_batch
-- ----------------------------
DROP TABLE IF EXISTS `log_msg_batch`;
CREATE TABLE `log_msg_batch` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_no` varchar(64) NOT NULL COMMENT '批次号',
  `app_id` bigint NOT NULL COMMENT '应用ID',
  `template_id` bigint NOT NULL COMMENT '模板ID',
  `channel_id` bigint NOT NULL COMMENT '渠道ID',
  `channel_type` varchar(32) NOT NULL COMMENT '渠道类型',
  `msg_type` int NOT NULL COMMENT '消息类型',
  `title` varchar(128) DEFAULT NULL COMMENT '消息标题',
  `content_params` text COMMENT '内容参数JSON',
  `total_count` int DEFAULT '0' COMMENT '总条数',
  `success_count` int DEFAULT '0' COMMENT '成功条数',
  `fail_count` int DEFAULT '0' COMMENT '失败条数',
  `status` int DEFAULT '0' COMMENT '状态 0:处理中 10:成功 20:部分成功 30:失败',
  `template_name` varchar(64) DEFAULT NULL COMMENT '模板名称快照',
  `channel_name` varchar(64) DEFAULT NULL COMMENT '渠道名称快照',
  `content` text COMMENT '消息内容快照',
  `error_msg` text DEFAULT NULL COMMENT '批次级错误信息',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_batch_no` (`batch_no`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息批次日志表';

-- ----------------------------
-- Table structure for log_msg_detail
-- ----------------------------
DROP TABLE IF EXISTS `log_msg_detail`;
CREATE TABLE `log_msg_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_id` bigint NOT NULL COMMENT '批次ID',
  `recipient` varchar(128) NOT NULL COMMENT '接收者标识',
  `recipient_name` varchar(64) DEFAULT NULL COMMENT '接收者姓名',
  `content` text COMMENT '实际发送内容',
  `status` int DEFAULT '10' COMMENT '状态 10:发送中 20:成功 30:失败',
  `third_party_msg_id` varchar(128) DEFAULT NULL COMMENT '第三方消息ID',
  `error_msg` text DEFAULT NULL COMMENT '错误信息',
  `retry_count` int DEFAULT '0' COMMENT '重试次数',
  `send_time` datetime DEFAULT NULL COMMENT '发送时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_batch_id` (`batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息详情日志表';

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(64) NOT NULL COMMENT '配置键',
  `config_value` text COMMENT '配置值',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- ----------------------------
-- Table structure for short_url
-- ----------------------------
DROP TABLE IF EXISTS `short_url`;
CREATE TABLE `short_url` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `short_code` varchar(16) NOT NULL COMMENT '短链码',
  `original_url` varchar(1024) NOT NULL COMMENT '原始链接',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `click_count` bigint DEFAULT '0' COMMENT '点击次数',
  `status` int DEFAULT '1' COMMENT '状态 1:启用 0:禁用',
  `expire_at` datetime DEFAULT NULL COMMENT '过期时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_short_code` (`short_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短链接表';

-- ----------------------------
-- Table structure for short_url_access_log
-- ----------------------------
DROP TABLE IF EXISTS `short_url_access_log`;
CREATE TABLE `short_url_access_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `short_code` varchar(16) NOT NULL COMMENT '短链码',
  `ip` varchar(64) DEFAULT NULL COMMENT '访问IP',
  `user_agent` varchar(512) DEFAULT NULL COMMENT 'User Agent',
  `referer` varchar(1024) DEFAULT NULL COMMENT 'Referer',
  `access_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
  PRIMARY KEY (`id`),
  KEY `idx_short_code` (`short_code`),
  KEY `idx_access_time` (`access_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短链接访问日志表';

-- ----------------------------
-- Table structure for short_url_ip_blacklist
-- ----------------------------
DROP TABLE IF EXISTS `short_url_ip_blacklist`;
CREATE TABLE `short_url_ip_blacklist` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `ip` varchar(64) NOT NULL COMMENT 'IP地址',
  `reason` varchar(255) DEFAULT NULL COMMENT '拉黑原因',
  `expire_at` datetime DEFAULT NULL COMMENT '过期时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ip` (`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短链接IP黑名单表';

SET FOREIGN_KEY_CHECKS = 1;
