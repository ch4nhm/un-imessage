create table log_msg_batch
(
    id             bigint auto_increment
        primary key,
    batch_no       varchar(64)                        not null comment '业务批次号 (UUID)',
    app_id         bigint                             not null comment '调用方ID',
    template_id    bigint                             not null comment '使用的模板ID',
    channel_id     bigint                             not null comment '实际发送渠道ID',
    channel_type   varchar(100)                       null comment '渠道类型',
    msg_type       tinyint                            not null comment '冗余消息类型',
    title          varchar(100)                       null comment '最终发送标题',
    content_params text                               null comment '业务方传入的参数JSON',
    total_count    int      default 0                 null comment '总发送人数',
    success_count  int      default 0                 null comment '成功人数',
    fail_count     int      default 0                 null comment '失败人数',
    status         tinyint  default 0                 null comment '批次状态: 0处理中 10全部成功 20部分成功 30全部失败',
    created_at     datetime default CURRENT_TIMESTAMP null,
    template_name  varchar(100)                       null comment 'Template Name Snapshot',
    channel_name   varchar(100)                       null comment 'Channel Name Snapshot',
    content        text                               null comment 'Template Content Snapshot',
    constraint uk_batch_no
        unique (batch_no)
)
    comment '消息发送批次记录表';

create index idx_channel_type
    on log_msg_batch (channel_type);

create index idx_create_time
    on log_msg_batch (created_at);

create index idx_created_at
    on log_msg_batch (created_at);

create index idx_status_created_at
    on log_msg_batch (status, created_at);

create table log_msg_detail
(
    id                 bigint auto_increment
        primary key,
    batch_id           bigint                             not null comment '关联批次ID',
    recipient          varchar(100)                       not null comment '接收者 (手机号/邮箱/OpenID/UserId)',
    content            text                               null comment 'Final Rendered Content',
    status             tinyint  default 0                 null comment '发送状态: 10发送中 20发送成功 30发送失败',
    third_party_msg_id varchar(64)                        null comment '第三方返回的消息ID (用于回执查询)',
    error_msg          text                               null comment '失败原因',
    retry_count        int      default 0                 null comment '重试次数',
    send_time          datetime                           null comment '实际发送时间',
    created_at         datetime default CURRENT_TIMESTAMP null,
    recipient_name     varchar(100)                       null comment 'Recipient Name'
)
    comment '消息发送详情表';

create index idx_batch_id
    on log_msg_detail (batch_id);

create index idx_batch_id_created_at
    on log_msg_detail (batch_id, created_at);

create index idx_created_at
    on log_msg_detail (created_at);

create index idx_recipient
    on log_msg_detail (recipient);

create index idx_recipient_created_at
    on log_msg_detail (recipient, created_at);

create index idx_status
    on log_msg_detail (status);

create index idx_status_created_at
    on log_msg_detail (status, created_at);

create table short_url
(
    id           bigint auto_increment
        primary key,
    short_code   varchar(10)                        not null comment '短链码 (Base62编码)',
    original_url varchar(2048)                      not null comment '原始长链接',
    created_by   bigint                             null comment '创建者ID (关联sys_app)',
    click_count  bigint   default 0                 null comment '点击次数',
    status       tinyint  default 1                 null comment '状态: 1启用 0禁用',
    expire_at    datetime                           null comment '过期时间 (NULL表示永不过期)',
    created_at   datetime default CURRENT_TIMESTAMP null,
    updated_at   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint uk_short_code
        unique (short_code)
)
    comment '短链接映射表';

create index idx_created_by
    on short_url (created_by);

create index idx_expire_at
    on short_url (expire_at);

create index idx_original_url
    on short_url (original_url(255));

create table short_url_access_log
(
    id          bigint auto_increment
        primary key,
    short_code  varchar(10)                        not null comment '短链码',
    ip          varchar(50)                        null comment '访问者IP',
    user_agent  varchar(500)                       null comment 'User-Agent',
    referer     varchar(500)                       null comment '来源页面',
    access_time datetime default CURRENT_TIMESTAMP null comment '访问时间'
)
    comment '短链接访问日志表';

create index idx_access_time
    on short_url_access_log (access_time);

create index idx_ip
    on short_url_access_log (ip);

create index idx_short_code
    on short_url_access_log (short_code);

create table short_url_ip_blacklist
(
    id         bigint auto_increment
        primary key,
    ip         varchar(50)                        not null comment 'IP地址',
    reason     varchar(255)                       null comment '封禁原因',
    expire_at  datetime                           null comment '解封时间 (NULL表示永久封禁)',
    created_at datetime default CURRENT_TIMESTAMP null,
    constraint uk_ip
        unique (ip)
)
    comment 'IP黑名单表';

create index idx_expire_at
    on short_url_ip_blacklist (expire_at);

create table sys_app
(
    id          bigint auto_increment
        primary key,
    app_name    varchar(50)                        not null comment '应用名称 (如: 订单系统)',
    app_code    varchar(50)                        null comment '应用编码 (唯一标识)',
    app_key     varchar(64)                        not null comment '接口鉴权Key',
    app_secret  varchar(128)                       not null comment '接口鉴权Secret',
    status      tinyint  default 1                 null comment '状态: 1启用 0禁用',
    owner       varchar(50)                        null comment '负责人',
    description varchar(255)                       null comment '描述',
    created_at  datetime default CURRENT_TIMESTAMP null,
    updated_at  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint uk_app_code
        unique (app_code)
)
    comment '接入应用表';

create table sys_channel
(
    id          bigint auto_increment
        primary key,
    name        varchar(50)                        not null comment '渠道名称 (如: 阿里云短信-营销)',
    type        varchar(20)                        not null comment '渠道类型: SMS, EMAIL, WECHAT_OFFICIAL, WECHAT_WORK, DINGTALK',
    provider    varchar(20)                        not null comment '供应商: ALIYUN, TENCENT, LOCAL_SMTP',
    config_json text                               not null comment '账号配置JSON (AccessKey, Secret, Host, Port等)',
    status      tinyint  default 1                 null comment '状态: 1启用 0禁用',
    created_at  datetime default CURRENT_TIMESTAMP null
)
    comment '渠道配置表';

create table sys_config
(
    id          bigint auto_increment comment '主键ID'
        primary key,
    system_name varchar(100) not null comment '系统名称',
    logo        longtext     null comment '系统Logo（Base64）',
    icon        longtext     null comment '系统图标（Base64）'
)
    comment '系统基础配置表';

create table sys_recipient
(
    id         bigint auto_increment
        primary key,
    name       varchar(50)                        not null comment '姓名',
    mobile     varchar(20)                        null comment '手机号',
    email      varchar(100)                       null comment '邮箱',
    open_id    varchar(100)                       null comment '微信OpenID',
    user_id    text                               null comment '企微/钉钉/飞书 UserId',
    status     tinyint  default 1                 null comment '状态: 1启用 0禁用',
    created_at datetime default CURRENT_TIMESTAMP null,
    updated_at datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
)
    comment '接收者表';

create index idx_mobile
    on sys_recipient (mobile);

create table sys_recipient_group
(
    id          bigint auto_increment
        primary key,
    name        varchar(50)                        not null comment '分组名称',
    code        varchar(50)                        null comment '分组编码',
    description varchar(255)                       null comment '描述',
    created_at  datetime default CURRENT_TIMESTAMP null,
    updated_at  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
)
    comment '接收者分组表';

create table sys_recipient_group_relation
(
    id           bigint auto_increment
        primary key,
    group_id     bigint not null comment '分组ID',
    recipient_id bigint not null comment '接收者ID',
    constraint uk_group_recipient
        unique (group_id, recipient_id)
)
    comment '接收者分组关联表';

create table sys_template
(
    id                   bigint auto_increment
        primary key,
    name                 varchar(50)                        not null comment '模板名称',
    code                 varchar(50)                        not null comment '模板编码 (业务方SDK调用凭证)',
    app_id               bigint                             null comment '关联的应用ID (null表示公共模板)',
    channel_id           bigint                             not null comment '关联的渠道ID',
    msg_type             tinyint                            not null comment '消息类型: 10通知 20营销 30验证码',
    third_party_id       varchar(64)                        null comment '第三方模板ID (如阿里云短信模板Code)',
    title                varchar(100)                       null comment '消息标题 (邮件/钉钉等需要)',
    content              text                               not null comment '消息内容模板 (支持占位符 ${code})',
    variables            varchar(255)                       null comment '预期变量列表 (JSON数组, 用于校验)',
    deduplication_config varchar(255)                       null comment '去重配置JSON (可选)',
    status               tinyint  default 1                 null comment '状态',
    created_at           datetime default CURRENT_TIMESTAMP null,
    recipient_group_ids  varchar(255)                       null comment '关联的接收者分组ID列表 (逗号分隔)',
    recipient_ids        varchar(255)                       null comment '关联的接收者ID列表 (逗号分隔)',
    rate_limit           int      default 0                 null comment '频率限制 (每秒最大请求数, 0或null表示不限制)',
    constraint uk_code
        unique (code)
)
    comment '消息模板表';

create index idx_app_id
    on sys_template (app_id);

create index idx_channel_id
    on sys_template (channel_id);

create table sys_user
(
    id         bigint auto_increment
        primary key,
    username   varchar(50)                        not null comment '用户名',
    password   varchar(100)                       not null comment '密码',
    nickname   varchar(50)                        null comment '昵称',
    status     tinyint  default 1                 null comment '状态: 1启用 0禁用',
    created_at datetime default CURRENT_TIMESTAMP null,
    updated_at datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint uk_username
        unique (username)
)
    comment '系统用户表';

