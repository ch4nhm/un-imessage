-- Mock Data SQL File

-- sys_app
INSERT INTO sys_app (id, app_name, app_code, app_key, app_secret, status, owner, description)
VALUES (1, '订单系统', 'ORDER_SYS', 'app_key_order', 'app_secret_order', 1, '张三', '订单相关消息'),
       (2, '营销系统', 'MARKET_SYS', 'app_key_market', 'app_secret_market', 1, '李四', '营销消息系统');

-- sys_channel
INSERT INTO sys_channel (id, name, type, provider, config_json, status)
VALUES (1, '阿里云短信-通知', 'SMS', 'ALIYUN',
        '{"accessKey":"testKey","secret":"testSecret","signName":"测试签名"}', 1),
       (2, 'SMTP邮件', 'EMAIL', 'LOCAL_SMTP',
        '{"host":"smtp.test.com","port":25,"username":"test","password":"123456"}', 1);

-- sys_template
INSERT INTO sys_template
(id, name, code, app_id, channel_id, msg_type, third_party_id, title, content, variables, status, rate_limit)
VALUES (1, '订单支付成功通知', 'ORDER_PAY_SUCCESS', 1, 1, 10,
        'SMS_123456', NULL,
        '您的订单${orderNo}已支付成功，金额${amount}元。',
        '["orderNo","amount"]', 1, 100),
       (2, '营销邮件模板', 'MARKET_PROMO_EMAIL', 2, 2, 20,
        NULL, '双11大促',
        '尊敬的${name}，点击${url}查看优惠活动。',
        '["name","url"]', 1, 50);

-- sys_recipient
INSERT INTO sys_recipient (id, name, mobile, email, status)
VALUES (1, '王小明', '13800000001', 'wangxm@test.com', 1),
       (2, '李小红', '13800000002', 'lixh@test.com', 1),
       (3, '赵六', '13800000003', 'zhaoliu@test.com', 1);

-- sys_recipient_group
INSERT INTO sys_recipient_group (id, name, code, description)
VALUES (1, 'VIP用户', 'VIP', '高价值用户'),
       (2, '普通用户', 'NORMAL', '普通注册用户');

-- sys_recipient_group_relation
INSERT INTO sys_recipient_group_relation (group_id, recipient_id)
VALUES (1, 1),
       (1, 2),
       (2, 3);

-- log_msg_batch
INSERT INTO log_msg_batch
(id, batch_no, app_id, template_id, channel_id, channel_type, msg_type,
 title, content_params, total_count, success_count, fail_count, status,
 template_name, channel_name, content)
VALUES (1, 'batch-uuid-001', 1, 1, 1, 'SMS', 10,
        NULL,
        '{"orderNo":"ORD20260120001","amount":"99.00"}',
        3, 2, 1, 20,
        '订单支付成功通知', '阿里云短信-通知',
        '您的订单ORD20260120001已支付成功，金额99.00元。');

-- log_msg_detail
INSERT INTO log_msg_detail
(id, batch_id, recipient, content, status, third_party_msg_id, error_msg, retry_count, send_time, recipient_name)
VALUES (1, 1, '13800000001',
        '您的订单ORD20260120001已支付成功，金额99.00元。',
        20, 'ALI_MSG_001', NULL, 0, NOW(), '王小明'),
       (2, 1, '13800000002',
        '您的订单ORD20260120001已支付成功，金额99.00元。',
        20, 'ALI_MSG_002', NULL, 0, NOW(), '李小红'),
       (3, 1, '13800000003',
        '您的订单ORD20260120001已支付成功，金额99.00元。',
        30, NULL, '手机号格式错误', 1, NOW(), '赵六');

-- short_url
INSERT INTO short_url
(id, short_code, original_url, created_by, click_count, status, expire_at)
VALUES (1, 'aB3dE9',
        'https://www.test.com/promo?activity=double11',
        2, 5, 1, '2026-12-31 23:59:59');

-- short_url_access_log
INSERT INTO short_url_access_log
    (short_code, ip, user_agent, referer)
VALUES ('aB3dE9', '192.168.1.10', 'Chrome/120.0', 'https://mail.test.com'),
       ('aB3dE9', '192.168.1.11', 'iPhone Safari', NULL);

-- short_url_ip_blacklist
INSERT INTO short_url_ip_blacklist (ip, reason, expire_at)
VALUES ('192.168.1.100', '恶意刷访问', NULL);

-- sys_user
INSERT INTO sys_user (id, username, password, nickname, status)
VALUES (1, 'admin', '$2a$10$i/HNdMLxxirCZCRIBoW6A.InbFWJS.B3EedfRcV1C1uAVHlpwR9EO', '管理员', 1),
       (2, 'operator', '$2a$10$i/HNdMLxxirCZCRIBoW6A.InbFWJS.B3EedfRcV1C1uAVHlpwR9EO', '运营人员', 1);
