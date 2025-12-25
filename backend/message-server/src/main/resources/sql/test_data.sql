-- =====================================================
-- UniMessage 统一消息平台 - 测试数据脚本
-- 创建时间: 2025-12-24
-- 说明: 包含系统初始化所需的测试数据
-- =====================================================

USE unimessage;

-- =====================================================
-- 1. 系统用户数据 (sys_user)
-- =====================================================
INSERT INTO sys_user (username, password, nickname, status)
VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 1),
       ('operator', 'e10adc3949ba59abbe56e057f20f883e', '运营人员', 1),
       ('developer', 'e10adc3949ba59abbe56e057f20f883e', '开发人员', 1);
-- 密码均为: 123456 (MD5加密)

-- =====================================================
-- 2. 系统配置数据 (sys_config)
-- =====================================================
INSERT INTO sys_config (system_name, logo, icon)
VALUES ('UniMessage 统一消息中心', NULL, NULL);

-- =====================================================
-- 3. 接入应用数据 (sys_app)
-- =====================================================
INSERT INTO sys_app (app_name, app_code, app_key, app_secret, status, owner, description)
VALUES ('订单系统', 'ORDER_SYS', 'order_app_key_001', 'order_app_secret_001_xxxxxxxxxx', 1, '张三',
        '电商订单系统，用于发送订单通知'),
       ('用户中心', 'USER_CENTER', 'user_app_key_002', 'user_app_secret_002_xxxxxxxxxx', 1, '李四',
        '用户中心系统，用于发送验证码和账户通知'),
       ('营销平台', 'MARKETING', 'marketing_app_key_003', 'marketing_app_secret_003_xxxxxxxxxx', 1, '王五',
        '营销活动平台，用于发送营销推广消息'),
       ('监控告警', 'MONITOR', 'monitor_app_key_004', 'monitor_app_secret_004_xxxxxxxxxx', 1, '赵六',
        '系统监控平台，用于发送告警通知');

-- =====================================================
-- 4. 渠道配置数据 (sys_channel)
-- =====================================================
-- 短信渠道
INSERT INTO sys_channel (name, type, provider, config_json, status)
VALUES ('阿里云短信-通知', 'SMS', 'ALIYUN',
        '{"accessKeyId":"your_access_key_id","accessKeySecret":"your_access_key_secret","signName":"UniMessage","endpoint":"dysmsapi.aliyuncs.com"}',
        1),
       ('阿里云短信-营销', 'SMS', 'ALIYUN',
        '{"accessKeyId":"your_access_key_id","accessKeySecret":"your_access_key_secret","signName":"UniMessage营销","endpoint":"dysmsapi.aliyuncs.com"}',
        1);

-- 邮件渠道
INSERT INTO sys_channel (name, type, provider, config_json, status)
VALUES ('企业邮箱-通知', 'EMAIL', 'LOCAL_SMTP',
        '{"host":"smtp.example.com","port":465,"username":"noreply@example.com","password":"your_password","ssl":true,"from":"UniMessage <noreply@example.com>"}',
        1),
       ('企业邮箱-营销', 'EMAIL', 'LOCAL_SMTP',
        '{"host":"smtp.example.com","port":465,"username":"marketing@example.com","password":"your_password","ssl":true,"from":"UniMessage营销 <marketing@example.com>"}',
        1);

-- 微信渠道
INSERT INTO sys_channel (name, type, provider, config_json, status)
VALUES ('微信服务号', 'WECHAT_OFFICIAL', 'WECHAT',
        '{"appId":"wx_app_id","appSecret":"wx_app_secret","token":"wx_token","aesKey":"wx_aes_key"}', 1),
       ('企业微信应用', 'WECHAT_WORK', 'WECHAT',
        '{"corpId":"ww_corp_id","agentId":"1000001","secret":"ww_agent_secret"}', 1);

-- 钉钉渠道
INSERT INTO sys_channel (name, type, provider, config_json, status)
VALUES ('钉钉工作通知', 'DINGTALK', 'DINGTALK',
        '{"appKey":"dingtalk_app_key","appSecret":"dingtalk_app_secret","agentId":"123456789"}', 1);

-- 飞书渠道
INSERT INTO sys_channel (name, type, provider, config_json, status)
VALUES ('飞书消息通知', 'FEISHU', 'FEISHU', '{"appId":"cli_feishu_app_id","appSecret":"feishu_app_secret"}', 1);

-- =====================================================
-- 5. 接收者数据 (sys_recipient)
-- =====================================================
INSERT INTO sys_recipient (name, mobile, email, open_id, user_id, status)
VALUES ('张三', '13800138001', 'zhangsan@example.com', 'oXXXX_zhangsan', 'zhangsan_userid', 1),
       ('李四', '13800138002', 'lisi@example.com', 'oXXXX_lisi', 'lisi_userid', 1),
       ('王五', '13800138003', 'wangwu@example.com', 'oXXXX_wangwu', 'wangwu_userid', 1),
       ('赵六', '13800138004', 'zhaoliu@example.com', 'oXXXX_zhaoliu', 'zhaoliu_userid', 1),
       ('钱七', '13800138005', 'qianqi@example.com', 'oXXXX_qianqi', 'qianqi_userid', 1),
       ('孙八', '13800138006', 'sunba@example.com', 'oXXXX_sunba', 'sunba_userid', 1),
       ('周九', '13800138007', 'zhoujiu@example.com', 'oXXXX_zhoujiu', 'zhoujiu_userid', 1),
       ('吴十', '13800138008', 'wushi@example.com', 'oXXXX_wushi', 'wushi_userid', 1);

-- =====================================================
-- 6. 接收者分组数据 (sys_recipient_group)
-- =====================================================
INSERT INTO sys_recipient_group (name, code, description)
VALUES ('技术部门', 'TECH_DEPT', '技术研发部门全体成员'),
       ('运营部门', 'OPS_DEPT', '运营部门全体成员'),
       ('管理层', 'MANAGEMENT', '公司管理层成员'),
       ('VIP客户', 'VIP_CUSTOMER', 'VIP级别客户群体'),
       ('全体员工', 'ALL_STAFF', '公司全体员工');

-- =====================================================
-- 7. 接收者分组关联数据 (sys_recipient_group_relation)
-- =====================================================
-- 技术部门: 张三、李四、王五
INSERT INTO sys_recipient_group_relation (group_id, recipient_id)
VALUES (1, 1),
       (1, 2),
       (1, 3);

-- 运营部门: 赵六、钱七
INSERT INTO sys_recipient_group_relation (group_id, recipient_id)
VALUES (2, 4),
       (2, 5);

-- 管理层: 张三、赵六
INSERT INTO sys_recipient_group_relation (group_id, recipient_id)
VALUES (3, 1),
       (3, 4);

-- VIP客户: 孙八、周九、吴十
INSERT INTO sys_recipient_group_relation (group_id, recipient_id)
VALUES (4, 6),
       (4, 7),
       (4, 8);

-- 全体员工: 所有人
INSERT INTO sys_recipient_group_relation (group_id, recipient_id)
VALUES (5, 1),
       (5, 2),
       (5, 3),
       (5, 4),
       (5, 5),
       (5, 6),
       (5, 7),
       (5, 8);

-- =====================================================
-- 8. 消息模板数据 (sys_template)
-- =====================================================
-- 短信模板
INSERT INTO sys_template (name, code, channel_id, msg_type, third_party_id, title, content, variables, status,
                          recipient_group_ids, recipient_ids)
VALUES ('验证码短信', 'SMS_VERIFY_CODE', 1, 30, 'SMS_123456789', NULL,
        '您的验证码是${code}，${minutes}分钟内有效，请勿泄露给他人。', '["code","minutes"]', 1, NULL, NULL),
       ('订单发货通知', 'SMS_ORDER_SHIPPED', 1, 10, 'SMS_123456790', NULL,
        '尊敬的${name}，您的订单${orderNo}已发货，快递单号${trackingNo}，请注意查收。', '["name","orderNo","trackingNo"]',
        1, NULL, NULL),
       ('营销活动短信', 'SMS_MARKETING', 2, 20, 'SMS_123456791', NULL,
        '${name}您好！${activity}活动火热进行中，${discount}折优惠等你来！退订回T', '["name","activity","discount"]', 1,
        '4', NULL);

-- 邮件模板
INSERT INTO sys_template (name, code, channel_id, msg_type, third_party_id, title, content, variables, status,
                          recipient_group_ids, recipient_ids)
VALUES ('欢迎注册邮件', 'EMAIL_WELCOME', 3, 10, NULL, '欢迎加入 UniMessage',
        '<h1>欢迎您，${name}！</h1><p>感谢您注册 UniMessage 平台，您的账号已创建成功。</p><p>登录账号：${email}</p>',
        '["name","email"]', 1, NULL, NULL),
       ('密码重置邮件', 'EMAIL_RESET_PWD', 3, 10, NULL, '密码重置通知',
        '<p>尊敬的${name}：</p><p>您正在重置密码，验证码为：<strong>${code}</strong>，${minutes}分钟内有效。</p><p>如非本人操作，请忽略此邮件。</p>',
        '["name","code","minutes"]', 1, NULL, NULL),
       ('系统告警邮件', 'EMAIL_ALERT', 3, 10, NULL, '【告警】${level} - ${service}',
        '<h2>系统告警通知</h2><p><strong>告警级别：</strong>${level}</p><p><strong>服务名称：</strong>${service}</p><p><strong>告警内容：</strong>${message}</p><p><strong>发生时间：</strong>${time}</p>',
        '["level","service","message","time"]', 1, '1', NULL);

-- 微信模板
INSERT INTO sys_template (name, code, channel_id, msg_type, third_party_id, title, content, variables, status,
                          recipient_group_ids, recipient_ids)
VALUES ('微信订单通知', 'WECHAT_ORDER', 5, 10, 'OPENTM_ORDER_001', '订单状态更新',
        '{{first.DATA}}\n订单编号：{{keyword1.DATA}}\n订单状态：{{keyword2.DATA}}\n{{remark.DATA}}',
        '["first","keyword1","keyword2","remark"]', 1, NULL, NULL);

-- 企业微信模板
INSERT INTO sys_template (name, code, channel_id, msg_type, third_party_id, title, content, variables, status,
                          recipient_group_ids, recipient_ids)
VALUES ('企微工作通知', 'WXWORK_NOTICE', 6, 10, NULL, '工作通知', '【${title}】\n${content}\n\n发送时间：${time}',
        '["title","content","time"]', 1, '5', NULL);

-- 钉钉模板
INSERT INTO sys_template (name, code, channel_id, msg_type, third_party_id, title, content, variables, status,
                          recipient_group_ids, recipient_ids)
VALUES ('钉钉审批通知', 'DINGTALK_APPROVAL', 7, 10, NULL, '审批通知',
        '您有一条新的审批待处理\n\n审批类型：${type}\n申请人：${applicant}\n申请时间：${time}\n\n请及时处理！',
        '["type","applicant","time"]', 1, '3', NULL);

-- 飞书模板
INSERT INTO sys_template (name, code, channel_id, msg_type, third_party_id, title, content, variables, status,
                          recipient_group_ids, recipient_ids)
VALUES ('飞书会议提醒', 'FEISHU_MEETING', 8, 10, NULL, '会议提醒',
        '您有一个即将开始的会议\n\n会议主题：${topic}\n会议时间：${time}\n会议地点：${location}\n\n请准时参加！',
        '["topic","time","location"]', 1, '1', NULL),
       ('Telegram通知', 'TELEGRAM_NOTICE', 9, 10, NULL, '通知', 'Hello ${name}, new message: ${msg}', '["name","msg"]',
        1, NULL, NULL),
       ('Slack告警', 'SLACK_ALERT', 10, 10, NULL, '告警', 'Alert: ${service} is down!', '["service"]', 1, NULL, NULL),
       ('腾讯云验证码', 'TENCENT_CODE', 11, 30, '12345', NULL, '${1}为您的登录验证码，请于${2}分钟内填写。', '["1","2"]',
        1, NULL, NULL);

-- =====================================================
-- 9. 消息批次记录示例数据 (log_msg_batch)
-- =====================================================
INSERT INTO log_msg_batch (batch_no, app_id, template_id, channel_id, msg_type, title, content_params, total_count,
                           success_count, fail_count, status, template_name, channel_name, content)
VALUES ('BATCH_20251224_001', 2, 1, 1, 30, NULL, '{"code":"123456","minutes":"5"}', 1, 1, 0, 10, '验证码短信',
        '阿里云短信-通知', '您的验证码是123456，5分钟内有效，请勿泄露给他人。'),
       ('BATCH_20251224_002', 1, 2, 1, 10, NULL,
        '{"name":"张三","orderNo":"ORD202512240001","trackingNo":"SF1234567890"}', 1, 1, 0, 10, '订单发货通知',
        '阿里云短信-通知', '尊敬的张三，您的订单ORD202512240001已发货，快递单号SF1234567890，请注意查收。'),
       ('BATCH_20251224_003', 3, 3, 2, 20, NULL, '{"name":"VIP客户","activity":"双旦狂欢","discount":"5"}', 3, 2, 1, 20,
        '营销活动短信', '阿里云短信-营销', 'VIP客户您好！双旦狂欢活动火热进行中，5折优惠等你来！退订回T'),
       ('BATCH_20251224_004', 4, 6, 3, 10, '【告警】ERROR - 订单服务',
        '{"level":"ERROR","service":"订单服务","message":"数据库连接超时","time":"2025-12-24 10:30:00"}', 3, 3, 0, 10,
        '系统告警邮件', '企业邮箱-通知',
        '<h2>系统告警通知</h2><p><strong>告警级别：</strong>ERROR</p><p><strong>服务名称：</strong>订单服务</p><p><strong>告警内容：</strong>数据库连接超时</p><p><strong>发生时间：</strong>2025-12-24 10:30:00</p>');

-- =====================================================
-- 10. 消息详情记录示例数据 (log_msg_detail)
-- =====================================================
INSERT INTO log_msg_detail (batch_id, recipient, recipient_name, content, status, third_party_msg_id, error_msg,
                            retry_count, send_time)
VALUES
-- 批次1: 验证码短信
(1, '13800138001', '张三', '您的验证码是123456，5分钟内有效，请勿泄露给他人。', 20, 'ALI_MSG_001', NULL, 0, NOW()),

-- 批次2: 订单发货通知
(2, '13800138001', '张三', '尊敬的张三，您的订单ORD202512240001已发货，快递单号SF1234567890，请注意查收。', 20,
 'ALI_MSG_002', NULL, 0, NOW()),

-- 批次3: 营销活动短信 (部分成功)
(3, '13800138006', '孙八', 'VIP客户您好！双旦狂欢活动火热进行中，5折优惠等你来！退订回T', 20, 'ALI_MSG_003', NULL, 0,
 NOW()),
(3, '13800138007', '周九', 'VIP客户您好！双旦狂欢活动火热进行中，5折优惠等你来！退订回T', 20, 'ALI_MSG_004', NULL, 0,
 NOW()),
(3, '13800138008', '吴十', 'VIP客户您好！双旦狂欢活动火热进行中，5折优惠等你来！退订回T', 30, NULL, '手机号已退订营销短信',
 1, NULL),

-- 批次4: 系统告警邮件
(4, 'zhangsan@example.com', '张三', '<h2>系统告警通知</h2>...', 20, NULL, NULL, 0, NOW()),
(4, 'lisi@example.com', '李四', '<h2>系统告警通知</h2>...', 20, NULL, NULL, 0, NOW()),
(4, 'wangwu@example.com', '王五', '<h2>系统告警通知</h2>...', 20, NULL, NULL, 0, NOW());

-- =====================================================
-- 补充索引 (优化查询性能)
-- =====================================================

-- sys_app 表索引
CREATE INDEX idx_app_status ON sys_app (status);
CREATE INDEX idx_app_key ON sys_app (app_key);

-- sys_channel 表索引
CREATE INDEX idx_channel_type ON sys_channel (type);
CREATE INDEX idx_channel_status ON sys_channel (status);
CREATE INDEX idx_channel_provider ON sys_channel (provider);

-- sys_template 表索引
CREATE INDEX idx_template_status ON sys_template (status);
CREATE INDEX idx_template_msg_type ON sys_template (msg_type);
CREATE INDEX idx_template_code ON sys_template (code);

-- sys_recipient 表索引
CREATE INDEX idx_recipient_email ON sys_recipient (email);
CREATE INDEX idx_recipient_status ON sys_recipient (status);

-- sys_recipient_group 表索引
CREATE INDEX idx_group_code ON sys_recipient_group (code);

-- log_msg_batch 表索引
CREATE INDEX idx_batch_app_id ON log_msg_batch (app_id);
CREATE INDEX idx_batch_template_id ON log_msg_batch (template_id);
CREATE INDEX idx_batch_channel_id ON log_msg_batch (channel_id);
CREATE INDEX idx_batch_status ON log_msg_batch (status);
CREATE INDEX idx_batch_msg_type ON log_msg_batch (msg_type);

-- log_msg_detail 表索引
CREATE INDEX idx_detail_status ON log_msg_detail (status);
CREATE INDEX idx_detail_send_time ON log_msg_detail (send_time);
CREATE INDEX idx_detail_created_at ON log_msg_detail (created_at);
