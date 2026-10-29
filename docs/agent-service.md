# AgentScope RAG 智能助手启动与联调

本文件说明 `agent-service` 与主应用、Kafka、Milvus、Embedding/大模型的启动与联调方式。

## 1. 架构

```text
Vue 前端
  ↓ /api（JWT）
Spring Boot 主应用 8080
  ├── 用户 / 帖子 / 评论 / MySQL / Redis
  ├── GET /api/auth/post/{postId}/ai-analysis
  ├── Kafka Producer → rag-index-topic
  └── HTTP → agent-service
                ↓
        agent-service 8090（只监听 127.0.0.1）
          ├── Kafka Consumer（agent-rag-indexer）
          ├── 只读 MySQL（Post/Pcomment/Ccomment）
          ├── 本地 Ollama BGE-M3（1024 维）
          ├── Milvus 2.4 Standalone
          ├── 查询改写 + 双路召回 + RRF 融合
          ├── RAG 评测（Hit@K / MRR / Recall@K + LLM Judge）
          └── AgentScope 2.0.3 + DeepSeek
```

- 前端只访问 8080，不能直接访问 8090。
- AgentScope 依赖只在 `agent-service/pom.xml`。
- 主应用不引入 Spring AI。

## 2. 启动顺序

```text
1. Kafka
2. Milvus
3. Spring Boot 主应用（8080）
4. agent-service（8090）
5. Vue 前端（pnpm dev）
```

### 2.1 启动 Kafka

使用你本地已有的 Kafka：

```bash
docker start kafka
# 或按现有开发环境启动 Kafka
```

### 2.2 启动 Milvus Standalone

```bash
cd infra/milvus
docker compose up -d
docker compose ps
```

健康检查：

```bash
curl http://127.0.0.1:9091/healthz
```

Milvus SDK 连接地址：`127.0.0.1:19530`。

> 当前 Compose 使用 Milvus 2.4.15 单容器模式：
> `ETCD_USE_EMBED=true` + `COMMON_STORAGETYPE=local`，不再依赖 MinIO。
> 原因是 MinIO 官方 Docker Hub 镜像已不可用。
> 数据保存在 `infra/milvus/volumes/`，已加入 `.gitignore`。
> 首次启动使用 `infra/milvus/embedEtcd.yaml` 和 `user.yaml`。

### 2.3 启动主应用

```bash
mvn spring-boot:run
```

默认端口 8080。需要 Kafka、MySQL、Redis 已启动。

### 2.4 启动 agent-service

```bash
cd agent-service
mvn spring-boot:run
```

默认端口 8090，并只绑定 `127.0.0.1`。

需要配置的环境变量：

```text
AGENT_SERVICE_TOKEN   内部调用 Token，必须与主应用一致
RAG_DB_URL            主应用数据库连接（只读账号优先）
RAG_DB_USERNAME
RAG_DB_PASSWORD
KAFKA_SERVERS
MILVUS_HOST
MILVUS_PORT
OLLAMA_BASE_URL       默认 http://127.0.0.1:11434
OLLAMA_EMBEDDING_MODEL 默认 bge-m3
DEEPSEEK_API_KEY      没有时使用检索摘要兜底
RAG_QUERY_REWRITE_ENABLED 是否启用 LLM 查询改写，默认 true
AGENT_EVAL_REPORT_DIR 评测报告目录，默认 ./eval-reports
```

可参考 `agent-service/.env.example`，不要把真实 Key 提交到仓库。

### 2.5 启动前端

```bash
cd fronted/newSSE
pnpm dev
```

## 3. 向量模型配置

当前使用本地 Ollama BGE-M3：

```text
Base URL：http://127.0.0.1:11434
模型：bge-m3
向量维度：1024
接口：POST /api/embed
```

启动 Ollama：

```powershell
$env:OLLAMA_MODELS="D:\Ollama\models"
Start-Process -FilePath "D:\Ollama\ollama.exe" -ArgumentList "serve" -WindowStyle Hidden
```

确认模型存在：

```powershell
D:\Ollama\ollama.exe list
curl http://127.0.0.1:11434/api/tags
```

不需要 DashScope API Key，也不使用 Mock 向量。Ollama 不可用或 BGE-M3 未下载时，分析接口返回 `SERVICE_UNAVAILABLE`。

写入和检索必须使用同一个模型和同一维度。修改模型或维度后需要重建 collection。
Milvus 的向量维度以 `agent.embedding.dimension` 为准（默认 1024）。
本地模型部署细节见 `docs/ollama-bge-m3.md`。

## 4. 大模型配置

使用 DeepSeek：

```bash
export DEEPSEEK_API_KEY=sk-xxxx
export DEEPSEEK_BASE_URL=https://api.deepseek.com
export DEEPSEEK_MODEL=deepseek-chat
```

未配置 Key 时：

- 不调用大模型；
- 返回检索到的历史内容摘要，状态为 `READY`，`model=extractive-fallback`；
- 不会编造历史帖子。

## 5. 索引流程

### 5.1 自动索引

```text
发帖 / 评论 / 删除评论 / 删除帖子
  ↓
Spring 事务提交
  ↓
Kafka rag-index-topic
  ↓
agent-service 消费
  ↓
按 postId 删除旧向量
  ↓
只读 MySQL 读取 Post + Pcomment + Ccomment
  ↓
文档切分 + Embedding
  ↓
Milvus Upsert
```

- 事件类型：
  - `POST_REINDEX`：新增/删除评论、新增帖子，整帖重建。
  - `POST_DELETE`：删除帖子/注销账号，按 postId 删除向量。
- 重复消费：整帖删除后使用稳定文档 ID 重新写入，不产生重复向量。
- 消费失败：重试 3 次后进入 `rag-index-topic.DLT`。

### 5.2 手动回填

首次启动 Milvus 后需要回填现有帖子：

```bash
curl -X POST http://127.0.0.1:8090/internal/agent/index/backfill \
  -H "X-Internal-Token: dev-token"
```

单帖重建：

```bash
curl -X POST http://127.0.0.1:8090/internal/agent/index \
  -H "X-Internal-Token: dev-token" \
  -H "Content-Type: application/json" \
  -d '{"postId":123,"version":1,"delete":false}'
```

单帖删除：

```bash
curl -X POST http://127.0.0.1:8090/internal/agent/index \
  -H "X-Internal-Token: dev-token" \
  -H "Content-Type: application/json" \
  -d '{"postId":123,"delete":true}'
```

## 6. 分析接口

前端只调用主应用：

```text
GET /api/auth/post/{postId}/ai-analysis
Authorization: Bearer <token>
```

返回状态：

| status | 含义 |
|---|---|
| `READY` | 已生成分析结论 |
| `NO_HISTORY` | 没有足够相关的历史讨论 |
| `SERVICE_UNAVAILABLE` | agent-service / Milvus / Embedding 不可用 |
| `MODEL_ERROR` | 大模型失败或返回非法结果 |
| `DISABLED` | 主应用配置关闭了 Agent |
| `RATE_LIMITED` | 每用户每分钟调用超过 5 次 |

主应用调用 agent-service：

```text
POST http://127.0.0.1:8090/internal/agent/post-analysis
X-Internal-Token: <token>
```

分析结果采用“AI 总结 + 论文式引用”结构：

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
  "relatedReplies": [
    {
      "postId": 41,
      "commentId": 89,
      "citationIndex": 1,
      "content": "……"
    }
  ]
}
```

- `conclusion` 是 AI 自己的总结，不在前端展示原帖原文。
- `[n]` 是引用标记，`n` 对应 `citations[].index`。
- `citations` 提供跳转所需的信息；`relatedReplies[].citationIndex` 与之一致。
- 当前前端只展示“相关历史帖子”（标题 + 链接），不再渲染“相关历史回复”区块；`[n]` 引用仍可点击跳转。
- 不再返回旧的 `evidence` 原帖片段列表。

## 7. 检索增强与评测

### 7.1 检索流程

```text
新帖标题 + 正文
  ↓ DeepSeek 改写为简短检索查询（可关闭；模型不可用/失败时自动退回原查询）
原文查询 ── Embedding ── Milvus TopK ┐
改写查询 ── Embedding ── Milvus TopK ┴─ RRF 融合（按主键去重、合并命中来源）
  ↓ 按 RRF 融合分排序
严格阈值（0.55）无命中 → 回退阈值（0.45）再检索
  ↓
弱证据只展示相关历史帖子，不调用大模型生成结论
```

- RRF 只按排名累加 `1/(k+rank)`，不直接比较不同查询的余弦分；同一分片被两路召回时融合分更高。
- 展示用的 `matchScore` 仍是最大余弦相似度，融合分只用于排序。
- 改写路径检索失败时只记录告警，不影响原文召回。
- 相关配置：

| 配置 | 默认值 | 说明 |
|---|---|---|
| `agent.rag.rewrite-enabled` | `true` | 是否启用查询改写 |
| `agent.rag.rewrite-timeout` | `20s` | 改写调用超时 |
| `agent.rag.rewrite-max-chars` | `100` | 改写结果最大长度 |
| `agent.rag.rrf-k` | `60` | RRF 平滑常数 |
| `agent.rag.score-threshold` | `0.55` | 严格召回阈值 |
| `agent.rag.fallback-score-threshold` | `0.45` | 严格阈值无命中时的回退阈值 |
| `agent.rag.max-context-docs` | `8` | 注入提示词的上下文条数上限 |

### 7.2 评测接口

评测复用线上检索与生成链路（`PostAnalysisService.prepare` / `analyzeWithRetrieval`），计算：

- 检索层：`Hit@K`、`MRR`、`Recall@K`，真值支持帖子 ID、帖子标题、回复键（`postId:commentId`）。
- 生成层（LLM-as-Judge）：忠实度、相关性、答案正确性。
- 上下文层：上下文精确率、上下文召回率。
- 延迟：平均值、P50、P95。

运行评测（默认数据集 `classpath:eval/seed-eval.jsonl`，105 条种子用例）：

```bash
curl -X POST http://127.0.0.1:8090/internal/agent/eval/run \
  -H "X-Internal-Token: dev-token" \
  -H "Content-Type: application/json" \
  -d '{"config":{"concurrency":2,"maxCases":20}}'
```

请求字段：

```json
{
  "datasetPath": "classpath:eval/seed-eval.jsonl",
  "config": {
    "topK": 8,
    "concurrency": 2,
    "enableGeneration": true,
    "enableLlmJudge": true,
    "maxCases": 0,
    "reviewScoreThreshold": 0.6,
    "reviewMrrThreshold": 0.5,
    "reviewRecallAtKThreshold": 0.5
  }
}
```

- `enableGeneration=false` 时只评检索与上下文，不调用大模型生成答案。
- 未配置 `DEEPSEEK_API_KEY` 时自动跳过 Judge，仅保留检索指标。
- 报告写入 `agent.eval.report-dir`（默认 `./eval-reports`），不新增数据库表。

查询报告：

```bash
curl http://127.0.0.1:8090/internal/agent/eval/runs?page=1&size=20 \
  -H "X-Internal-Token: dev-token"

curl http://127.0.0.1:8090/internal/agent/eval/report/{runId}?includeCases=true \
  -H "X-Internal-Token: dev-token"

curl -o eval-{runId}.csv \
  http://127.0.0.1:8090/internal/agent/eval/report/{runId}/export.csv \
  -H "X-Internal-Token: dev-token"
```

评测集格式（JSONL，每行一条，`#` 开头为注释）：

```json
{"id":"seed-01-1","question":"校园卡丢了怎么补办\n昨天在二食堂吃完饭发现校园卡不见了","groundTruthAnswer":"先挂失，再带身份证到行政楼103补办","groundTruthPostIds":[23,24],"groundTruthPostTitles":["一卡通丢失如何挂失","饭卡掉了求补办流程"],"excludePostId":22}
```

种子评测集由 `docs/seed/generate_rag_eval.py` 从 35 个主题簇生成：
簇内每题做 leave-one-out，同簇另外两帖作为检索真值，该帖自身从检索中排除。

## 8. 数据与分片

- 一个“帖子 + 主评论 + 评论回复”为一个知识单元。
- 没有可用评论时，使用帖子正文作为知识单元。
- 评论长度小于 8 个字符的噪声评论不进入索引。
- 单条知识单元超过 800 字符时按 500～800 字符切分，重叠 80 字符。
- 每个分片都保留帖子标题、postId、分区和标签。
- 文档 ID 示例：
  - `post:123:comment:456:v1`
  - `post:123:comment:456:v1:chunk:1`
  - `post:123:body:v1`

Milvus collection：

```text
software_market_rag
```

## 9. 测试

### 8.1 功能链路

1. 启动 Kafka、Milvus、主应用、agent-service、前端。
2. 执行一次全量回填。
3. 登录前端，发一个新帖子。
4. 应自动跳转 `/postdetail/{postId}`。
5. 帖子内容和评论区正常显示。
6. AI 区域显示加载中，随后展示结论、依据、相关帖子、相关回复。
7. 点击相关帖子应跳转到原帖。
8. 停止 agent-service，刷新帖子详情：
   - 帖子、评论、点赞正常；
   - AI 区域显示“AI 服务不可用”。

### 8.2 单元测试

```bash
cd agent-service
mvn test
```

```bash
mvn test
```

前端：

```bash
cd fronted/newSSE
pnpm type-check
```

## 10. 当前限制

- 数据库中的 `post`、`pcomment`、`ccomment` 没有 `status`/`deleted` 字段，删除是物理删除。
  - Milvus 中的 `status` 第一版统一为 `ACTIVE`。
  - 删除依赖 Kafka 事件；如果事件丢失，需要执行全量回填清理残留向量。
- 管理员软删除用户（`user.deleted=true`）不会删除其帖子，帖子仍可能被索引。
- 多路召回来自「原文查询 + 改写查询」两路向量检索，不做 BM25 与 Cross Encoder 重排。
- 第一版不做追问 AI、SSE 流式输出、MCP、多 Agent。
- Ollama 未启动或 BGE-M3 未下载时，AI 返回 `SERVICE_UNAVAILABLE`，不再使用 Mock 向量。
- 没有 DeepSeek Key 时不查询改写、不运行 Judge，分析只返回检索摘要。
- `agent-service` 通过只读 MySQL 获取帖子和评论；生产环境应使用只有 SELECT 权限的账号。
