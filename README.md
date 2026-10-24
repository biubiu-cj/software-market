# Software Market

校园软件市场与校园论坛系统，包含帖子、评论、用户、商城、评分、通知等业务，并在帖子详情页提供基于站内历史讨论的 AI 分析与引用。

## 功能概览

- 用户注册、登录、邮箱验证码、JWT Access Token + Refresh Token、密码重置
- 微信登录/OAuth2 应用接入
- 帖子分区、搜索、标签、发帖、删帖
- 一级评论、二级回复、点赞/点踩
- 帖子点赞、收藏、浏览历史、热度排行
- 课程评分与评分帖
- 商品与商城模块
- 通知、反馈、举报
- 管理后台：用户、帖子、举报、反馈管理
- AI 分析：
  - 发帖成功后跳转新帖详情
  - 在帖子内容和评论区之间展示 AI 总结
  - AI 总结中使用 `[1]`、`[2]` 论文式引用
  - 展示相关历史帖子，点击标题跳转原帖
  - AI 不可用时降级，不影响帖子、评论、点赞等主功能
- RAG 索引：
  - 帖子/评论变更后通过 Kafka 异步重建整帖向量
  - 本地 Ollama BGE-M3 生成 Embedding
  - Milvus 存储和检索历史知识文档
  - 支持首次全量回填和单帖手动重建

## 架构

```text
Vue 3 + Vite 前端
        │ /api（JWT）
        ▼
Spring Boot 主应用 :8080
  ├── 用户 / 帖子 / 评论 / 权限
  ├── MySQL / Redis / Kafka
  ├── 发帖接口（返回 postId）
  ├── GET /api/auth/post/{postId}/ai-analysis
  ├── Kafka Producer → rag-index-topic
  └── HTTP → agent-service
                    │
                    ▼
        agent-service :8090（仅监听 127.0.0.1）
          ├── AgentScope 2.0.3 + DeepSeek
          ├── Kafka Consumer（agent-rag-indexer）
          ├── 只读 MySQL
          ├── Ollama BGE-M3（1024 维）
          └── Milvus 2.4.15
```

## 技术栈

| 模块 | 技术 |
|---|---|
| 后端 | Spring Boot 3.3.6、Java 17、MyBatis-Plus、Spring Security、JWT、Spring Kafka、Redis、WebSocket、Caffeine |
| 前端 | Vue 3、Vite、TypeScript、UnoCSS、markdown-it、pnpm |
| AI 服务 | AgentScope Java 2.0.3、DeepSeek、Ollama BGE-M3、Milvus Standalone 2.4.15 |
| 数据与中间件 | MySQL 8、Redis、Kafka、Milvus |
| 部署 | Nginx、systemd、GitHub Actions |

## 目录结构

```text
software-market/
├── src/main/java/software_market/       # Spring Boot 后端
│   ├── controller/
│   ├── service/
│   ├── mapper/
│   ├── entity/
│   ├── dto/
│   ├── vo/
│   ├── event/                           # RAG 索引事件
│   └── config/security/                 # JWT / Spring Security / CORS
├── src/main/resources/
│   ├── application.yml
│   ├── application-local.yml.example    # 本地配置模板
│   └── mapper/
├── fronted/newSSE/                      # Vue 3 前端
│   └── src/
│       ├── api/
│       ├── components/post/PostAiAnalysis.vue
│       ├── views/
│       ├── store/
│       └── router/
├── agent-service/                       # 独立 AgentScope RAG 服务
│   ├── pom.xml
│   └── src/main/java/software_market_agent/
│       ├── agentscope/                  # AgentScope 模型与 Agent
│       ├── embedding/                   # Ollama BGE-M3 适配
│       ├── milvus/                      # Milvus 客户端
│       ├── rag/                         # 文档、索引、检索
│       ├── kafka/                       # RAG 索引消费者
│       ├── datasource/                  # 只读 MySQL
│       └── controller/                  # 内部接口
├── infra/milvus/                        # Milvus Docker Compose
├── docs/                                # 项目说明文档
├── .github/workflows/                   # 自动部署
└── pom.xml
```

## 快速开始

### 环境要求

- JDK 17
- Maven 3.9+
- Node.js 22、pnpm 11
- MySQL 8
- Redis
- Kafka
- Docker（运行 Milvus）
- Ollama + `bge-m3`（使用 AI 功能时需要）
- DeepSeek API Key（使用 AI 生成时需要）

### 1. 准备数据库和中间件

默认数据库名：

```text
software_market
```

主应用依赖：

```text
MySQL   localhost:3306
Redis   localhost:6379
Kafka   localhost:9092
```

> 仓库当前只包含 OAuth2 建表脚本和 RAG 种子数据脚本，完整的业务表结构以现有数据库为准。

### 2. 启动主应用

复制本地配置模板：

```bash
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
```

填写 MySQL 密码、QQ 邮箱 SMTP 授权码和 JWT 密钥。`application-local.yml` 已被 `.gitignore` 忽略。

启动：

```bash
mvn spring-boot:run
```

默认地址：

```text
http://localhost:8080
```

### 3. 启动前端

```bash
cd fronted/newSSE
pnpm install
pnpm dev
```

前端默认通过 `.env` 使用相对地址 `/api`，Vite 开发服务器会代理到 `localhost:8080`。

生产构建：

```bash
pnpm build
```

### 4. 启动 AI 服务（可选）

AI 功能由独立进程 `agent-service` 提供，不影响主应用启动。

#### 4.1 启动 Milvus

```bash
cd infra/milvus
docker compose up -d
curl http://127.0.0.1:9091/healthz
```

当前 Compose 使用 Milvus 单容器模式：

```text
ETCD_USE_EMBED=true
COMMON_STORAGETYPE=local
```

不需要额外启动 MinIO。数据保存在 `infra/milvus/volumes/`。

#### 4.2 启动 Ollama 并下载 BGE-M3

参考 [`docs/ollama-bge-m3.md`](docs/ollama-bge-m3.md)。

```powershell
ollama pull bge-m3
ollama serve
```

确认：

```bash
curl http://127.0.0.1:11434/api/tags
```

#### 4.3 启动 agent-service

```bash
cd agent-service
mvn spring-boot:run
```

常用环境变量：

```text
AGENT_SERVICE_TOKEN          内部调用 Token，必须与主应用一致
RAG_DB_URL                   主应用数据库连接
RAG_DB_USERNAME
RAG_DB_PASSWORD
KAFKA_SERVERS
MILVUS_HOST
MILVUS_PORT
OLLAMA_BASE_URL              默认 http://127.0.0.1:11434
OLLAMA_EMBEDDING_MODEL       默认 bge-m3
DEEPSEEK_API_KEY
DEEPSEEK_BASE_URL
DEEPSEEK_MODEL
```

本地也可以创建 `agent-service/src/main/resources/application-local.yml` 保存数据库密码，该文件已被 `.gitignore` 忽略。

#### 4.4 首次全量回填

```bash
curl -X POST http://127.0.0.1:8090/internal/agent/index/backfill \
  -H "X-Internal-Token: dev-token"
```

返回示例：

```json
{"status":"OK","indexedPosts":136}
```

#### 4.5 推荐启动顺序

```text
Kafka
  → Milvus
  → Spring Boot 主应用 8080
  → agent-service 8090
  → Vue 前端
```

## 配置说明

### 主应用

主要配置在 `src/main/resources/application.yml`，本地敏感配置放在 `application-local.yml`。

| 配置 | 说明 |
|---|---|
| `server.port` | 主应用端口，默认 8080 |
| `spring.datasource.*` | MySQL 连接 |
| `spring.data.redis.*` | Redis 连接 |
| `spring.kafka.*` | Kafka 连接和序列化 |
| `jwt.secret` | JWT 签名密钥 |
| `app.upload-dir` | 上传文件目录 |
| `agent.service.base-url` | agent-service 地址，默认 `http://127.0.0.1:8090` |
| `agent.service.token` | 内部调用 Token |
| `app.ai.analysis-cache-minutes` | AI 分析缓存时间 |
| `app.ai.rate-limit-per-minute` | 每用户 AI 请求限流 |

### agent-service

| 配置 | 说明 |
|---|---|
| `agent.internal-token` | 内部 Token |
| `agent.rag.*` | TopK、相似度阈值、切分大小、相关帖子数量 |
| `agent.embedding.*` | Ollama 地址、模型、维度、超时 |
| `agent.chat.*` | DeepSeek 地址、模型、超时 |
| `agent.milvus.*` | Milvus 地址和 collection |
| `spring.datasource.*` | 只读 MySQL |
| `spring.kafka.*` | RAG 索引消费者 |

> 本地 `agent-service/src/main/resources/application-local.yml` 被 `.gitignore` 忽略；生产环境请使用环境变量或只读数据库账号。

## 主要接口

### 认证

```text
POST /api/auth/login
POST /api/auth/register
POST /api/auth/refresh
POST /api/auth/modifyPassword
```

除白名单接口外，请求需要：

```text
Authorization: Bearer <access-token>
```

### 帖子

```text
POST /api/auth/post                 发帖，返回 data.postId
POST /api/auth/browse               帖子列表
POST /api/auth/showDetails          帖子详情
POST /api/auth/showPcomments        评论树
POST /api/auth/postPcomment         发一级评论
POST /api/auth/postCcomment         发二级回复
```

### AI 分析

```text
GET /api/auth/post/{postId}/ai-analysis
```

返回结构：

```json
{
  "status": "READY",
  "conclusion": "补办校园卡需要先挂失，再带身份证到行政楼 103 办理 [1]，工本费 20 元 [2]。",
  "citations": [
    {
      "index": 1,
      "postId": 41,
      "commentId": 89,
      "title": "校园卡丢了怎么补办",
      "url": "/postdetail/41?commentId=89"
    }
  ],
  "relatedPosts": [
    {
      "postId": 41,
      "title": "校园卡丢了怎么补办",
      "url": "/postdetail/41"
    }
  ],
  "notice": "以上内容来自站内历史讨论，仅供参考。"
}
```

说明：

- `conclusion` 是 AI 自己的总结。
- `[n]` 是引用标记，点击跳转到对应历史回复。
- `citations` 保存引用映射。
- 详情页当前只展示“相关历史帖子”，以标题链接形式跳转原帖。
- 没有足够历史内容时返回 `NO_HISTORY`，不会编造内容。
- AI 服务不可用返回 `SERVICE_UNAVAILABLE`，不影响主站。

### 追问 AI

```text
POST /api/auth/post/{postId}/ai-chat
```

当前为预留接口，返回 501。

## RAG 数据流

### 索引

```text
发帖 / 评论 / 删除
  → 事务提交
  → Kafka rag-index-topic
  → agent-service 消费
  → 按 postId 删除旧向量
  → 读取 Post + Pcomment + Ccomment
  → 文档切分
  → Ollama BGE-M3 生成 Embedding
  → Milvus Upsert
```

### 检索与生成

```text
当前帖子标题 + 正文
  → BGE-M3 Embedding
  → Milvus TopK 检索
  → 排除当前 postId
  → 按 postId 聚合
  → 拼接历史资料
  → AgentScope 单 Agent + DeepSeek
  → AI 总结 + 引用 + 相关历史帖子
```

## 种子数据

仓库包含一份用于 RAG 演示的校园讨论种子数据：

```bash
python docs/seed/generate_rag_seed.py
mysql -uroot -p software_market < docs/seed/rag_seed.sql
```

脚本只清理邮箱前缀为 `ragseed_%` 的种子用户及其帖子，不会删除现有业务数据。

## 测试与检查

```bash
# 主应用
mvn test
mvn -DskipTests package

# agent-service
cd agent-service
mvn test
mvn -DskipTests package

# 前端
cd fronted/newSSE
pnpm type-check
pnpm build
```

## 部署

主应用和前端已配置 GitHub Actions：

- `.github/workflows/deploy-backend.yml`
- `.github/workflows/deploy-frontend.yml`

两者只在推送到 `master` 时触发：

- 后端改动：`src/**`、`pom.xml`
- 前端改动：`fronted/newSSE/**`

`agent-service`、Milvus、Ollama 目前不在这两个工作流中，需要单独部署。

Nginx 反向代理约定：

```text
/api/       → 127.0.0.1:8080
/avatar/    → 127.0.0.1:8080
/websocket/ → 127.0.0.1:8080
```

`agent-service` 只监听 `127.0.0.1:8090`，不通过 Nginx 暴露给前端或公网。

> 完整 AI 栈（Milvus + Ollama BGE-M3 + agent-service）对内存有要求，部署前请检查服务器资源。

## 文档

- [AgentScope RAG 服务说明](docs/agent-service.md)
- [本地 Ollama + BGE-M3 部署说明](docs/ollama-bge-m3.md)
- [RAG 种子数据生成器](docs/seed/generate_rag_seed.py)
- [OAuth2 建表脚本](docs/oauth2-schema.sql)

## 已知限制

- 仓库未包含完整业务建表 SQL，需要准备现有 `software_market` 数据库。
- 帖子、评论没有软删除字段，删除是物理删除；向量库依赖 Kafka 删除事件，事件丢失时需全量回填。
- RAM 较小的服务器不适合同时运行 Milvus、Ollama BGE-M3 和 agent-service。
- 未实现追问 AI、SSE 流式输出、MCP、多 Agent。
- 未实现 BM25、混合检索和 Cross Encoder 重排。
- 当前 AI 页面只展示相关历史帖子；`citations` 仍保留历史回复映射，供引用跳转和后续扩展使用。
