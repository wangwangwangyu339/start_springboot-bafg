# Cloud-Native Spring Boot Demo

> **Spring Boot 3.5.x** · **JDK 21** · **PostgreSQL + MyBatis** · **MongoDB** · **Upstash Redis** · **OpenFeign** · **Swagger3** · **Sentry** · **Vercel Edge Config**

---

## 技术栈一览

| 分类 | 技术 | 版本 |
|------|------|------|
| 核心框架 | Spring Boot | 3.5.3 |
| Web 层 | Spring Web MVC | 6.2.x (随 Boot) |
| 运行时 | JDK | 21 LTS |
| 关系型数据库 | PostgreSQL + MyBatis | PG 15 / MyBatis-Spring-Boot 3.0.4 |
| 文档数据库 | MongoDB + Spring Data | MongoDB 7 |
| 云缓存 | Upstash Redis (SSL) + Spring Cache | Lettuce + Redis 7 |
| 远程调用 | Spring Cloud OpenFeign | 2023.0.5 |
| 接口文档 | springdoc-openapi (Swagger3) | 2.8.9 |
| 异常监控 | Sentry | 8.7.0 |
| 全局配置 | Vercel Edge Config | HTTP API |
| 工具 | Lombok | 1.18.36 |
| 容器化 | Docker + docker compose | — |

> **说明：** Spring Boot 4.0 截至 2026 年 4 月尚未发布正式版（GA），
> 本项目使用当前最新稳定版 **3.5.3**。
> 待 4.0 正式发布后，仅需修改 `pom.xml` 中的 `<parent>` 版本号即可无缝升级。

---

## 项目结构

```
learn/
├── pom.xml
├── Dockerfile                        # 多阶段构建
├── docker-compose.yml                # 本地一键启动
├── .env.example                      # 环境变量示例
└── src/main/java/com/example/demo/
    ├── DemoApplication.java           # 启动类
    ├── common/
    │   ├── constant/CacheConstants    # 缓存 key 常量
    │   ├── exception/
    │   │   ├── BusinessException      # 业务异常
    │   │   └── GlobalExceptionHandler # 全局异常处理器
    │   └── result/
    │       ├── R<T>                   # 统一响应体
    │       ├── ResultCode             # 状态码枚举
    │       └── PageResult<T>          # 分页响应
    ├── config/
    │   ├── RedisConfig                # Redis + Cache 配置
    │   ├── SwaggerConfig              # OpenAPI 3 配置
    │   ├── EdgeConfigManager          # Vercel Edge Config 拉取 + 定时刷新
    │   └── SentryTracingAspect        # 接口性能追踪 AOP
    ├── controller/
    │   ├── UserController             # 用户 CRUD（PostgreSQL）
    │   ├── UserLogController          # 操作日志（MongoDB）
    │   ├── FeignDemoController        # OpenFeign 调用示例
    │   ├── EdgeConfigController       # Edge Config 查询
    │   └── HealthController           # 健康检查
    ├── entity/
    │   ├── pg/User                    # PostgreSQL 用户实体
    │   ├── pg/CreateUserRequest       # 创建请求 DTO
    │   ├── pg/UpdateUserRequest       # 更新请求 DTO
    │   └── mongo/UserLog              # MongoDB 日志文档
    ├── mapper/
    │   ├── UserMapper                 # MyBatis Mapper 接口
    │   └── UserLogRepository          # MongoDB Repository
    ├── service/
    │   ├── UserService / impl         # 用户服务（含 @Cacheable / @CacheEvict）
    │   └── UserLogService / impl      # 日志服务
    ├── feign/
    │   ├── JsonPlaceholderClient      # Feign 客户端示例
    │   └── dto/JsonPlaceholderPost    # 响应 DTO
    └── util/HttpUtils                 # 客户端 IP 获取工具
```

---

## 快速启动

### 方式一：docker compose（推荐，一键启动所有服务）

```bash
cd learn

# 构建并启动（首次需拉取镜像，约 2-3 分钟）
docker compose up -d

# 查看日志
docker compose logs -f app
```

访问地址：
- 接口文档：http://localhost:8080/swagger-ui.html
- 健康检查：http://localhost:8080/api/health
- Actuator：http://localhost:8080/actuator/health

### 方式二：本地直接运行

**前置条件：** JDK 21、PostgreSQL 15、MongoDB 7、Redis 7

```bash
# 1. 初始化数据库
psql -U postgres -d demo -f src/main/resources/db/init.sql

# 2. 配置环境变量（或直接修改 application-local.yml）
export PG_HOST=localhost PG_USER=postgres PG_PASSWORD=xxx
export MONGODB_URI="mongodb://localhost:27017/demo"
export REDIS_URL="redis://localhost:6379"

# 3. 启动（使用本地 profile）
JAVA_HOME=/usr/local/sdkman/candidates/java/21.0.9-ms \
  ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### 方式三：JAR 运行

```bash
JAVA_HOME=/usr/local/sdkman/candidates/java/21.0.9-ms \
  mvn clean package -DskipTests

java -jar target/demo-1.0.0-SNAPSHOT.jar \
     --spring.profiles.active=local
```

---

## 环境变量配置

复制 `.env.example` 为 `.env` 并填写真实值：

```bash
cp .env.example .env
```

| 变量 | 说明 | 示例 |
|------|------|------|
| `PG_HOST` | PostgreSQL 主机 | `db.example.com` |
| `PG_USER` / `PG_PASSWORD` | PostgreSQL 账号密码 | — |
| `MONGODB_URI` | MongoDB 连接串 | `mongodb+srv://...` |
| `REDIS_URL` | Upstash Redis URL（`rediss://` SSL） | `rediss://default:token@endpoint:6379` |
| `EDGE_CONFIG` | Vercel Edge Config 连接串 | `https://edge-config.vercel.com/...` |
| `SENTRY_DSN` | Sentry DSN | `https://xxx@sentry.io/yyy` |

---

## 接口文档

启动后访问：**http://localhost:8080/swagger-ui.html**

| 模块 | 端点 | 说明 |
|------|------|------|
| 用户管理 | `GET /api/users` | 分页查询用户 |
| | `GET /api/users/{id}` | 按 ID 查询（Redis 缓存）|
| | `POST /api/users` | 创建用户 |
| | `PUT /api/users/{id}` | 更新用户（自动清缓存）|
| | `DELETE /api/users/{id}` | 删除用户（自动清缓存）|
| 用户日志 | `POST /api/logs` | 新增操作日志（MongoDB）|
| | `GET /api/logs/user/{userId}` | 查询用户日志 |
| Feign 示例 | `GET /api/feign/posts` | 调用外部服务 |
| | `GET /api/feign/posts/{id}` | 调用外部服务 |
| Edge Config | `GET /api/config` | 全量配置快照 |
| | `GET /api/config/string/{key}` | 获取 String 配置 |
| | `GET /api/config/bool/{key}` | 获取功能开关 |
| | `GET /api/config/int/{key}` | 获取数值配置 |
| | `POST /api/config/refresh` | 手动刷新配置 |
| 系统 | `GET /api/health` | 健康检查 |

---

## 统一响应格式

所有接口均返回 `R<T>` 包装体：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... },
  "timestamp": "2026-04-03T14:00:00"
}
```

错误响应示例：

```json
{
  "code": 404,
  "message": "用户不存在，id=999",
  "timestamp": "2026-04-03T14:00:00"
}
```

---

## Redis 缓存策略

| 场景 | 注解 | Cache Key |
|------|------|-----------|
| 查询单个用户 | `@Cacheable` | `users::user::{id}` |
| 更新用户 | `@CacheEvict` | `users::user::{id}` |
| 删除用户 | `@Caching(evict)` | `users::user::{id}` + `user_detail::{id}` |

默认 TTL：**1 小时**（可在 `application.yml` 的 `spring.cache.redis.time-to-live` 调整）

---

## 升级到 Spring Boot 4.0（待正式发布）

当 Spring Boot 4.0 正式发布后，只需修改 `pom.xml`：

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.0</version>   <!-- 修改此处 -->
</parent>
```

同时将 `javax.*` 命名空间已全部使用 `jakarta.*`，无需任何代码修改。

---

## PostgreSQL 建表

```bash
psql -h $PG_HOST -U $PG_USER -d $PG_DB -f src/main/resources/db/init.sql
```

---

## License

MIT
