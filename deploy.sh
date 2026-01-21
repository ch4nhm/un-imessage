#!/bin/bash

# UniMessage 一键部署脚本 (Linux/macOS)

echo -e "\033[36m==========================================\033[0m"
echo -e "\033[36m      UniMessage Docker 部署向导\033[0m"
echo -e "\033[36m==========================================\033[0m"

# 检查 docker-compose
if ! command -v docker-compose &> /dev/null; then
    echo -e "\033[31m未检测到 docker-compose，请先安装 Docker 和 Docker Compose。\033[0m"
    exit 1
fi

# 1. 检查并生成 .env 文件
ENV_FILE=".env"
if [ ! -f "$ENV_FILE" ]; then
    echo -e "\n\033[33m[1/3] 配置环境信息...\033[0m"
    
    read -p "请输入 MySQL 地址 (默认: 127.0.0.1): " DB_HOST
    DB_HOST=${DB_HOST:-127.0.0.1}

    read -p "请输入 MySQL 端口 (默认: 3306): " DB_PORT
    DB_PORT=${DB_PORT:-3306}

    read -p "请输入 数据库名称 (默认: unimessage): " DB_NAME
    DB_NAME=${DB_NAME:-unimessage}

    read -p "请输入 数据库用户名 (默认: root): " DB_USER
    DB_USER=${DB_USER:-root}

    read -p "请输入 数据库密码 (默认: root): " DB_PASSWORD
    DB_PASSWORD=${DB_PASSWORD:-root}

    read -p "请输入 Redis 地址 (默认: 127.0.0.1): " REDIS_HOST
    REDIS_HOST=${REDIS_HOST:-127.0.0.1}

    read -p "请输入 Redis 端口 (默认: 6379): " REDIS_PORT
    REDIS_PORT=${REDIS_PORT:-6379}

    read -p "请输入 Redis 密码 (无密码直接回车): " REDIS_PASSWORD

    cat > "$ENV_FILE" <<EOF
# 数据库配置
DB_HOST=$DB_HOST
DB_PORT=$DB_PORT
DB_NAME=$DB_NAME
DB_USER=$DB_USER
DB_PASSWORD=$DB_PASSWORD

# Redis 配置
REDIS_HOST=$REDIS_HOST
REDIS_PORT=$REDIS_PORT
REDIS_PASSWORD=$REDIS_PASSWORD

# JVM 参数
JAVA_OPTS=-Xms512m -Xmx1024m
EOF
    echo -e "\033[32m配置文件 .env 已生成。\033[0m"
else
    echo -e "\n\033[32m[1/3] 检测到 .env 文件已存在，跳过配置。\033[0m"
fi

# 2. 构建镜像
echo -e "\n\033[33m[2/3] 开始构建镜像 (这可能需要几分钟)...\033[0m"
docker-compose build

if [ $? -ne 0 ]; then
    echo -e "\033[31m镜像构建失败，请检查 Docker 环境或网络。\033[0m"
    exit 1
fi

# 3. 启动服务
echo -e "\n\033[33m[3/3] 启动服务...\033[0m"
docker-compose up -d

if [ $? -eq 0 ]; then
    echo -e "\n\033[36m==========================================\033[0m"
    echo -e "\033[32m   部署成功！\033[0m"
    echo -e "\033[36m   前端访问地址: http://localhost\033[0m"
    echo -e "\033[36m   后端接口地址: http://localhost:8079\033[0m"
    echo -e "\033[36m==========================================\033[0m"
else
    echo -e "\033[31m服务启动失败。\033[0m"
    exit 1
fi
