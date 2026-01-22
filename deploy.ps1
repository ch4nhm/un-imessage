# UniMessage 一键部署脚本 (Windows PowerShell)

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "      UniMessage Docker 部署向导" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# 检查 docker-compose
if (-not (Get-Command "docker-compose" -ErrorAction SilentlyContinue)) {
    Write-Error "未检测到 docker-compose，请先安装 Docker Desktop。"
    exit
}

# 1. 检查并生成 .env 文件
$EnvFile = ".env"
if (-not (Test-Path $EnvFile)) {
    Write-Host "`n[1/3] 配置环境信息..." -ForegroundColor Yellow
    
    $DB_HOST = Read-Host "请输入 MySQL 地址 (默认: 127.0.0.1)"
    if ([string]::IsNullOrWhiteSpace($DB_HOST)) { $DB_HOST = "127.0.0.1" }

    $DB_PORT = Read-Host "请输入 MySQL 端口 (默认: 3306)"
    if ([string]::IsNullOrWhiteSpace($DB_PORT)) { $DB_PORT = "3306" }

    $DB_NAME = Read-Host "请输入 数据库名称 (默认: unimessage)"
    if ([string]::IsNullOrWhiteSpace($DB_NAME)) { $DB_NAME = "unimessage" }

    $DB_USER = Read-Host "请输入 数据库用户名 (默认: root)"
    if ([string]::IsNullOrWhiteSpace($DB_USER)) { $DB_USER = "root" }

    $DB_PASSWORD = Read-Host "请输入 数据库密码 (默认: root)"
    if ([string]::IsNullOrWhiteSpace($DB_PASSWORD)) { $DB_PASSWORD = "root" }

    $REDIS_HOST = Read-Host "请输入 Redis 地址 (默认: 127.0.0.1)"
    if ([string]::IsNullOrWhiteSpace($REDIS_HOST)) { $REDIS_HOST = "127.0.0.1" }

    $REDIS_PORT = Read-Host "请输入 Redis 端口 (默认: 6379)"
    if ([string]::IsNullOrWhiteSpace($REDIS_PORT)) { $REDIS_PORT = "6379" }

    $REDIS_PASSWORD = Read-Host "请输入 Redis 密码 (无密码直接回车)"

    $Content = @"
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
"@
    Set-Content -Path $EnvFile -Value $Content -Encoding UTF8
    Write-Host "配置文件 .env 已生成。" -ForegroundColor Green
} else {
    Write-Host "`n[1/3] 检测到 .env 文件已存在，跳过配置。" -ForegroundColor Green
}

# 2. 构建镜像
Write-Host "`n[2/3] 开始构建镜像 (这可能需要几分钟)..." -ForegroundColor Yellow
docker-compose build

if ($LASTEXITCODE -ne 0) {
    Write-Error "镜像构建失败，请检查 Docker 环境或网络。"
    exit
}

# 3. 启动服务
Write-Host "`n[3/3] 启动服务..." -ForegroundColor Yellow
docker-compose up -d

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n==========================================" -ForegroundColor Cyan
    Write-Host "   部署成功！" -ForegroundColor Green
    Write-Host "   前端访问地址: http://localhost" -ForegroundColor Cyan
    Write-Host "   后端接口地址: http://localhost:8079" -ForegroundColor Cyan
    Write-Host "==========================================" -ForegroundColor Cyan
} else {
    Write-Error "服务启动失败。"
}
