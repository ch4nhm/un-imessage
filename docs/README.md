# UniMessage 统一消息推送平台

<p align="center">
 <img src="img.png" alt="系统架构图" width="100%"/>
</p>

## 📋 项目概述

UniMessage 是一个企业级统一消息推送平台，采用微服务架构设计，支持多渠道消息聚合发送（短信、邮件、微信、钉钉、飞书等），提供完整的消息生命周期管理和数据追踪能力。

### 核心特性

- **多渠道聚合**: 统一 API 接口支持 6+ 消息渠道
- **异步处理**: MQ 消息队列解耦，支持高吞吐量
- **完整追踪**: 批次级和详情级日志记录
- **易于集成**: SDK 自动装配，开箱即用
- **灵活配置**: 模板、渠道、接收者独立管理
- **企业级**: 权限认证、异常处理、数据安全
- **短链接**: 自带短链接生成、访问统计与追踪

## 🏗️ 技术架构

<p align="center">
  <img src="architecture.svg" alt="系统架构图" width="100%"/>
</p>
### 后端技术栈

| 技术             | 版本     | 说明       |
|----------------|--------|----------|
| Spring Boot    | 3.3.0  | 基础框架     |
| MyBatis Plus   | 3.5.5  | ORM 框架   |
| Sa-Token       | 1.38.0 | 权限认证     |
| MySQL          | 8.0+   | 数据存储     |
| Redis          | 6.0+   | 缓存/Token |
| Kafka/RocketMQ | -      | 消息队列     |
| FastJSON2      | 2.0.43 | JSON 处理  |
| Hutool         | 5.8.25 | 工具库      |

### 前端技术栈

| 技术           | 版本     | 说明       |
|--------------|--------|----------|
| React        | 19.2.0 | UI 框架    |
| TypeScript   | 5.9.3  | 类型支持     |
| Vite         | 7.2.4  | 构建工具     |
| Ant Design   | 6.0.1  | UI 组件库   |
| Axios        | 1.13.2 | HTTP 客户端 |
| ECharts      | 6.0.0  | 图表库      |
| React Router | 7.10.0 | 路由管理     |

### 第三方 SDK

| 渠道    | SDK               |
|-------|-------------------|
| 阿里云短信 | dysmsapi 2.0.24   |
| 微信    | weixin-java 4.6.0 |
| 钉钉    | dingtalk 2.0.14   |
| 飞书    | oapi-sdk 2.0.28   |

## 📁 项目结构

```
UniMessage/
├── backend/                          # 后端代码
│   ├── message-api/                  # SDK 模块 (供第三方集成)
│   │   └── src/main/java/com/unimessage/
│   │       ├── dto/                  # 数据传输对象
│   │       ├── enums/                # 枚举定义
│   │       └── sdk/                  # SDK 客户端
│   │
│   └── message-server/               # 核心服务模块
│       └── src/main/java/com/unimessage/
│           ├── controller/           # REST 控制器
│           ├── service/              # 业务服务层
│           ├── handler/              # 渠道处理器
│           ├── mapper/               # 数据访问层
│           ├── entity/               # 实体类
│           ├── mq/                   # 消息队列
│           └── config/               # 配置类
│
├── frontend/                         # 前端代码
│   └── src/
│       ├── api/                      # API 接口层
│       ├── pages/                    # 页面组件
│       ├── layout/                   # 布局组件
│       ├── utils/                    # 工具函数
│       └── assets/                   # 静态资源
│
└── docs/                             # 项目文档
    ├── README.md                     # 项目说明
    ├── architecture.svg              # 系统架构图
    ├── message-flow.svg              # 消息流程图
    └── database-er.svg               # 数据库 ER 图
```

## 🔄 消息发送流程

<p align="center">
  <img src="message-flow.svg" alt="消息发送流程图" width="100%"/>
</p>

### 流程说明

1. **客户端请求**: 业务系统通过 SDK 构建 `SendRequest`，包含模板编码、接收者列表、模板参数
2. **接口接收**: `MessageController` 接收 HTTP POST 请求
3. **业务校验**: `MessageService` 执行模板校验、渠道校验、接收者解析、Handler 检查
4. **创建批次**: 生成 `LogMsgBatch` 记录，保存模板快照，状态设为"处理中"
5. **MQ 异步**: 消息推送到 Kafka/RocketMQ，同步返回 `SendResponse`（包含 batchNo）
6. **渠道发送**: MQ Consumer 消费消息，创建 `LogMsgDetail`，调用对应 `ChannelHandler` 执行发送

### 渠道处理器 (策略模式)

```java
public interface ChannelHandler {
    boolean support(String channelType);

    boolean send(SysChannel channel, SysTemplate template,
                 LogMsgDetail msgDetail, Map<String, Object> params);
}
```

已实现的处理器:

- `AliyunSmsHandler` - 阿里云短信
- `TencentSmsHandler` - 腾讯云短信
- `TwilioHandler` - Twilio 短信
- `EmailHandler` - SMTP 邮件
- `WechatOfficialHandler` - 微信服务号模板消息
- `WechatWorkHandler` - 企业微信应用消息
- `DingTalkHandler` - 钉钉工作通知
- `FeishuHandler` - 飞书消息通知
- `TelegramHandler` - Telegram 机器人
- `SlackHandler` - Slack 机器人
- `WebhookHandler` - 自定义 Webhook

## 📊 数据库设计

<p align="center">
  <img src="database-er.svg" alt="数据库 ER 图" width="100%"/>
</p>

### 核心表说明

| 表名                             | 说明                |
|--------------------------------|-------------------|
| `sys_user`                     | 系统用户表             |
| `sys_app`                      | 接入应用表 (调用方鉴权)     |
| `sys_channel`                  | 渠道配置表 (短信/邮件/微信等) |
| `sys_template`                 | 消息模板表             |
| `sys_recipient`                | 接收者表              |
| `sys_recipient_group`          | 接收者分组表            |
| `sys_recipient_group_relation` | 分组关联表 (多对多)       |
| `log_msg_batch`                | 消息发送批次记录表         |
| `log_msg_detail`               | 消息发送详情表           |
| `short_url`                    | 短链接映射表             |
| `short_url_access_log`         | 短链接访问日志表         |
| `short_url_ip_blacklist`       | 短链接IP黑名单表         |
| `sys_config`                   | 系统基础配置表           |

### 状态码定义

**消息类型 (msg_type)**:

- `10` - 通知消息
- `20` - 营销消息
- `30` - 验证码

**批次状态 (batch.status)**:

- `0` - 处理中
- `10` - 全部成功
- `20` - 部分成功
- `30` - 全部失败

**详情状态 (detail.status)**:

- `10` - 发送中
- `20` - 发送成功
- `30` - 发送失败

## 🚀 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+
- Kafka 或 RocketMQ

### Docker 部署 (推荐)

项目支持通过 Docker Compose 一键启动后端服务。

1. **构建并启动**

```bash
cd backend/message-server
docker-compose up -d --build
```

或使用项目根目录下的自动化部署脚本 (推荐):

**Windows (PowerShell)**:
```powershell
.\deploy.ps1
```

**Linux / macOS**:
```bash
chmod +x deploy.sh
./deploy.sh
```

脚本将引导您配置数据库和Redis连接信息，自动生成 `.env` 文件并启动服务。

2. **服务检查**

- 后端服务端口: `8079`
- 挂载日志目录: `backend/message-server/logs`

### 后端启动 (本地开发)

```bash
# 1. 初始化数据库
mysql -u root -p < backend/message-server/src/main/resources/sql/init_schema.sql
mysql -u root -p < backend/message-server/src/main/resources/sql/test_data.sql

# 2. 修改配置文件
# backend/message-server/src/main/resources/application.yml

# 3. 编译运行
cd backend
mvn clean package -DskipTests
java -jar message-server/target/message-server-0.0.1-SNAPSHOT.jar
```

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

### Docker 部署前端 (推荐)

项目提供了前端 Docker 镜像构建配置，内置 Nginx 服务器。

1. **构建并启动**

```bash
cd frontend
pnpm run build
```

2. **Nginx 配置说明**

前端使用 Nginx 托管静态资源，并反向代理后端 API。默认配置 (`nginx.conf`) 监听 `80` 端口，将 `/api/` 开头的请求转发到后端服务。

- 访问地址: `http://localhost:80`
- 接口转发: `http://localhost:80/api/` -> `http://backend:8079/api/`

**注意**: 请确保 Nginx 配置中的后端服务地址 (`proxy_pass`) 正确指向运行中的后端容器或服务地址。

### SDK 集成

```xml

<dependency>
    <groupId>com.unimessage</groupId>
    <artifactId>message-api</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

```yaml
# application.yml
un-imessage:
  client:
    host: localhost
    port: 8079
    app-key: your-app-key
    app-secret: your-app-secret
```

```java

@Resource
private UniMessageClient uniMessageClient;

public void sendMessage() {
    SendRequest request = new SendRequest();
    request.setTemplateCode("SMS_VERIFY_CODE");
    request.setRecipients(List.of("13800138000"));
    request.setParams(Map.of("code", "123456", "minutes", "5"));

    SendResponse response = uniMessageClient.send(request);
    if (response.isSuccess()) {
        System.out.println("发送成功，批次号: " + response.getBatchNo());
    }
}
```

## 📡 API 接口

### 消息发送

```http
POST /api/v1/message/send
Content-Type: application/json

{
  "templateCode": "SMS_VERIFY_CODE",
  "recipients": ["13800138000"],
  "params": {
    "code": "123456",
    "minutes": "5"
  },
  "bizId": "ORDER_123456"
}
```

**响应**:

```json
{
  "success": true,
  "message": "提交成功",
  "batchNo": "BATCH_20251224_001"
}
```

### 管理接口

| 接口                              | 方法   | 说明     |
|---------------------------------|------|--------|
| `/api/v1/auth/login`            | POST | 用户登录   |
| `/api/v1/template`              | CRUD | 模板管理   |
| `/api/v1/channel`               | CRUD | 渠道管理   |
| `/api/v1/recipient`             | CRUD | 接收者管理  |
| `/api/v1/recipient-group`       | CRUD | 接收者分组  |
| `/api/v1/app`                   | CRUD | 应用管理   |
| `/api/v1/user`                  | CRUD | 用户管理   |
| `/api/v1/log/batch/page`        | GET  | 批次日志查询 |
| `/api/v1/log/detail/page`       | GET  | 详情日志查询 |
| `/api/v1/log/detail/{id}/retry` | POST | 消息重试   |

## 🔐 安全特性

- **认证框架**: Sa-Token 权限认证
- **Token 管理**: 前端 localStorage 存储，请求头自动添加
- **接口鉴权**: App Key + Secret 签名验证
- **异常处理**: 全局异常处理器，统一错误响应
- **敏感配置**: 渠道配置信息加密存储

## 📈 扩展指南

### 添加新渠道

1. 在 `ChannelType` 枚举中添加新类型
2. 实现 `ChannelHandler` 接口
3. 添加 `@Component` 注解，自动注册到工厂

```java

@Component
public class NewChannelHandler implements ChannelHandler {
    @Override
    public boolean support(String channelType) {
        return "NEW_CHANNEL".equals(channelType);
    }

    @Override
    public boolean send(SysChannel channel, SysTemplate template,
                        LogMsgDetail msgDetail, Map<String, Object> params) {
        // 实现发送逻辑
        return true;
    }
}
```

### 切换消息队列

项目支持默认使用Redis作为消息队列，也支持切换到 Kafka 和 RocketMQ，通过配置切换:

```yaml
# Kafka
spring:
  kafka:
    bootstrap-servers: localhost:9092

# RocketMQ
rocketmq:
  name-server: localhost:9876
```

## 📝 ### 测试数据

项目提供了完整的测试数据脚本，包含:

- **2 个系统用户** (密码: `admin123`)
  - `admin` (管理员)
  - `operator` (运营人员)
- 4 个接入应用 (订单系统/用户中心/营销平台/监控告警)
- 8 个消息渠道 (短信/邮件/微信/钉钉/飞书)
- 11 个消息模板
- 8 个接收者和 5 个分组
- 示例消息批次和详情记录

执行测试数据:

```bash
mysql -u root -p unimessage < backend/message-server/src/main/resources/sql/test_data.sql
```

## 📄 License

Apache2.0 License

---

**UniMessage** - 让消息触达更简单 🚀
