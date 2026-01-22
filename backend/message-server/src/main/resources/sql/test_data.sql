-- 全量 Mock 数据

-- 1. sys_user (系统用户 - 保持不变)
INSERT INTO sys_user (id, username, password, nickname, status)
VALUES (1, 'admin', '$2a$10$i/HNdMLxxirCZCRIBoW6A.InbFWJS.B3EedfRcV1C1uAVHlpwR9EO', '管理员', 1),
       (2, 'operator', '$2a$10$i/HNdMLxxirCZCRIBoW6A.InbFWJS.B3EedfRcV1C1uAVHlpwR9EO', '运营人员', 1);

-- 2. sys_app (接入应用 - 覆盖常见场景)
INSERT INTO sys_app (id, app_name, app_code, app_key, app_secret, status, owner, description)
VALUES (1, '订单系统', 'ORDER_CENTER', 'order_app_key', 'order_app_secret', 1, '张三', '负责订单创建、支付、发货等通知'),
       (2, '营销中心', 'MARKETING', 'market_app_key', 'market_app_secret', 1, '李四', '负责活动推广、优惠券发放'),
       (3, '监控告警', 'MONITOR', 'monitor_app_key', 'monitor_app_secret', 1, '王五', '服务器监控、业务异常告警'),
       (4, '用户中心', 'USER_CENTER', 'user_app_key', 'user_app_secret', 1, '赵六', '验证码、账号变动通知');

-- 3. sys_channel (消息渠道 - 覆盖8种类型)
INSERT INTO sys_channel (id, name, type, provider, config_json, status)
VALUES 
(1, '阿里云短信', 'SMS', 'ALIYUN', '{"accessKeyId":"test","accessKeySecret":"test","signName":"UniMessage"}', 1),
(2, '腾讯云短信', 'TENCENT_SMS', 'TENCENT', '{"secretId":"test","secretKey":"test","sdkAppId":"1400000000","signName":"UniMessage"}', 1),
(3, '系统邮件通知', 'EMAIL', 'LOCAL_SMTP', '{"host":"smtp.exmail.qq.com","port":465,"username":"admin@unimessage.com","password":"password","ssl":true}', 1),
(4, '微信服务号', 'WECHAT_OFFICIAL', 'WECHAT_OFFICIAL', '{"appId":"wx1234567890","appSecret":"secret","templateId":"tmpl_123"}', 1),
(5, '企业微信应用', 'WECHAT_WORK', 'WECHAT_WORK', '{"corpId":"ww123456","corpSecret":"secret","agentId":1000001}', 1),
(6, '钉钉工作通知', 'DINGTALK', 'DINGTALK', '{"appKey":"ding123","appSecret":"secret","agentId":123456}', 1),
(7, '飞书机器人', 'FEISHU', 'FEISHU', '{"appId":"cli_123","appSecret":"secret"}', 1),
(8, '通用Webhook', 'WEBHOOK', 'WEBHOOK', '{"url":"https://api.example.com/callback","method":"POST"}', 1);

-- 4. sys_recipient (接收者 - 包含多渠道ID)
INSERT INTO sys_recipient (id, name, mobile, email, user_id, status)
VALUES 
(1, '张全蛋', '13800138001', 'zhangqd@example.com', '{"WECHAT_WORK": "zhangqd", "DINGTALK": "001", "FEISHU": "ou_123"}', 1),
(2, '李小花', '13800138002', 'lixh@example.com', '{"WECHAT_OFFICIAL": "oX_123_openid"}', 1),
(3, '王大锤', '13800138003', 'wangdc@example.com', NULL, 1),
(4, '运维值班', '13800138004', 'ops@example.com', '{"DINGTALK": "ops_group"}', 1),
(5, '测试账号', '13800138005', 'test@example.com', NULL, 1);

-- 5. sys_recipient_group (接收者分组)
INSERT INTO sys_recipient_group (id, name, code, description, status)
VALUES 
(1, '全员广播', 'ALL_STAFF', '公司全体员工', 1),
(2, '运维组', 'OPS_TEAM', '负责系统稳定性', 1),
(3, 'VIP客户', 'VIP_USERS', '高价值客户群体', 1),
(4, '测试组', 'TEST_GROUP', '内部测试使用', 1);

-- sys_recipient_group_relation
INSERT INTO sys_recipient_group_relation (group_id, recipient_id)
VALUES (1, 1), (1, 2), (1, 3), (2, 1), (2, 4), (3, 2), (4, 5);

-- 6. sys_template (消息模板 - 丰富场景)
INSERT INTO sys_template (id, name, code, app_id, channel_id, msg_type, third_party_id, title, content, variables, rate_limit, recipient_group_ids, recipient_ids, status)
VALUES
-- 验证码 (短信)
(1, '登录验证码', 'LOGIN_CODE', 4, 1, 30, 'SMS_1001', NULL, '您的验证码是${code}，5分钟内有效。', '["code"]', 100, NULL, NULL, 1),
-- 订单通知 (微信模板消息)
(2, '订单发货通知', 'ORDER_SHIPPED', 1, 4, 10, 'tmpl_order_001', '发货提醒', '您的订单${orderNo}已发货，物流单号${trackingNo}。', '["orderNo","trackingNo"]', 50, NULL, '2', 1),
-- 系统告警 (钉钉)
(3, '服务器异常告警', 'SERVER_ALERT', 3, 6, 10, NULL, '严重告警', '## 服务器异常\nIP: ${ip}\n错误: ${error}\n时间: ${time}', '["ip","error","time"]', 10, '2', NULL, 1),
-- 营销推广 (邮件)
(4, '双11大促活动', 'PROMO_1111', 2, 3, 20, NULL, '双11特惠来袭', '<h1>尊敬的${name}</h1><p>点击<a href="${link}">这里</a>参与活动！</p>', '["name","link"]', 20, '3', NULL, 1),
-- 内部通知 (飞书)
(5, '会议提醒', 'MEETING_NOTIFY', 4, 7, 10, NULL, '会议提醒', '会议主题：${topic}\n时间：${time}\n地点：${location}', '["topic","time","location"]', 50, '1', NULL, 1);

-- 7. log_msg_batch & detail (发送日志)
INSERT INTO log_msg_batch (id, batch_no, app_id, template_id, channel_id, channel_type, msg_type, title, content_params, total_count, success_count, fail_count, status, template_name, channel_name, content)
VALUES 
(1, 'BATCH_20260122001', 4, 1, 1, 'SMS', 30, NULL, '{"code":"123456"}', 1, 1, 0, 10, '登录验证码', '阿里云短信', '您的验证码是123456，5分钟内有效。'),
(2, 'BATCH_20260122002', 3, 3, 6, 'DINGTALK', 10, '严重告警', '{"ip":"192.168.1.100","error":"OOM","time":"2026-01-22 10:00:00"}', 2, 1, 1, 20, '服务器异常告警', '钉钉工作通知', '## 服务器异常\nIP: 192.168.1.100\n错误: OOM\n时间: 2026-01-22 10:00:00');

INSERT INTO log_msg_detail (id, batch_id, recipient, recipient_name, content, status, third_party_msg_id, error_msg, send_time)
VALUES 
(1, 1, '13800138005', '测试账号', '您的验证码是123456，5分钟内有效。', 20, 'ALI_SMS_RESP_001', NULL, NOW()),
(2, 2, 'zhangqd', '张全蛋', '## 服务器异常...', 20, 'DING_MSG_001', NULL, NOW()),
(3, 2, 'ops_group', '运维值班', '## 服务器异常...', 30, NULL, '网络超时', NOW());

-- 8. short_url (短链接)
INSERT INTO short_url (id, short_code, original_url, created_by, click_count, status, expire_at)
VALUES 
(1, 'UniMsg', 'https://github.com/unimessage/unimessage', 1, 1024, 1, '2030-12-31 23:59:59'),
(2, 'Promo', 'https://example.com/promotion/v1', 2, 567, 1, '2026-02-01 00:00:00');

INSERT INTO short_url_access_log (short_code, ip, user_agent, referer)
VALUES 
('UniMsg', '10.0.0.1', 'Mozilla/5.0 (Windows NT 10.0)', 'https://google.com'),
('Promo', '10.0.0.2', 'Mozilla/5.0 (iPhone; CPU iPhone OS 16_0)', NULL);

INSERT INTO short_url_ip_blacklist (ip, reason, expire_at)
VALUES ('1.2.3.4', '恶意爬虫', '2026-02-01 00:00:00');
