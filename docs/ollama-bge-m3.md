# 本地 Ollama + BGE-M3 向量模型部署说明

本文记录本机 Ollama 与 BGE-M3 的实际部署情况，以及后续 `agent-service` 接入本地 Embedding 所需的配置。

## 1. Ollama 实际安装位置

- 安装方式：winget 官方包 `Ollama.Ollama.Portable`
- 版本：`0.40.10`
- 安装目录：`D:\Ollama`
- 可执行文件：`D:\Ollama\ollama.exe`
- 运行库目录：`D:\Ollama\lib\ollama`
- 用户级 PATH 已包含：`D:\Ollama`
- 当前没有创建 Windows 服务或开机计划任务，Ollama 以独立后台进程方式运行。

> 本机 GitHub 直连受限，本次通过 winget 完成安装；`ollama pull bge-m3` 已成功。

## 2. BGE-M3 模型存储位置

- 模型名称：`bge-m3:latest`
- 模型 ID：`790764642607`
- 模型大小：约 1.2 GB
- 向量维度：`1024`
- 模型存储目录：`D:\Ollama\models`
- 用户级环境变量：`OLLAMA_MODELS=D:\Ollama\models`
- 目录结构：
  - `D:\Ollama\models\blobs`
  - `D:\Ollama\models\manifests`
  - `D:\Ollama\models\metadata`

模型文件没有写入 C 盘默认的 `C:\Users\<用户>\.ollama\models` 目录。

## 3. 启动、停止与检查

### 3.1 启动（独立后台进程）

```powershell
$env:OLLAMA_MODELS="D:\Ollama\models"
Start-Process -FilePath "D:\Ollama\ollama.exe" -ArgumentList "serve" -WindowStyle Hidden
```

### 3.2 前台启动（调试用）

```powershell
$env:OLLAMA_MODELS="D:\Ollama\models"
D:\Ollama\ollama.exe serve
```

### 3.3 停止

```powershell
Get-Process ollama -ErrorAction SilentlyContinue | Stop-Process -Force
```

### 3.4 检查服务

```powershell
curl http://127.0.0.1:11434/api/tags
D:\Ollama\ollama.exe list
```

预期能看到：

```text
NAME             ID              SIZE
bge-m3:latest    790764642607    1.2 GB
```

### 3.5 重新加载模型路径

1. 停止所有 `ollama` 进程。
2. 确认用户环境变量已生效：

```powershell
[Environment]::GetEnvironmentVariable("OLLAMA_MODELS","User")
```

应输出：

```text
D:\Ollama\models
```

3. 用 3.1 或 3.2 的命令重新启动。

> 用户级环境变量只对之后启动的新进程生效；已经运行的 Ollama 需要重启才会使用新的 `OLLAMA_MODELS`。

如果需要开机自启，可自行创建一个“登录时运行”的 Windows 计划任务，执行：

```text
D:\Ollama\ollama.exe serve
```

并确保运行用户的 `OLLAMA_MODELS` 指向 `D:\Ollama\models`。

## 4. 测试向量生成

测试文本：

```text
如何解决数据库连接超时问题？
```

请求：

```powershell
$body = @{
  model = "bge-m3"
  input = "如何解决数据库连接超时问题？"
} | ConvertTo-Json -Compress

Invoke-RestMethod `
  -Uri "http://127.0.0.1:11434/api/embed" `
  -Method Post `
  -ContentType "application/json; charset=utf-8" `
  -Body ([System.Text.Encoding]::UTF8.GetBytes($body))
```

也可以使用 Python：

```python
import json
import urllib.request

text = "如何解决数据库连接超时问题？"
body = json.dumps({"model": "bge-m3", "input": text}, ensure_ascii=False).encode("utf-8")
req = urllib.request.Request(
    "http://127.0.0.1:11434/api/embed",
    data=body,
    headers={"Content-Type": "application/json; charset=utf-8"},
    method="POST",
)
with urllib.request.urlopen(req, timeout=120) as response:
    data = json.loads(response.read().decode("utf-8"))

embedding = data["embeddings"][0]
print("dimension:", len(embedding))
print("first_5:", embedding[:5])
```

本次实测结果：

```text
dimension: 1024
first_5: [-0.007962, -0.015038, -0.050502, 0.044651, -0.04517]
model: bge-m3
```

Ollama 返回字段是 `embeddings`（数组），与 OpenAI 的 `data[].embedding` 不同，Java 适配时需要注意。

## 5. Java / agent-service 接入配置

`agent-service` 已接入本地 Ollama，DashScope 相关实现已移除。使用以下配置：

```text
Base URL：http://127.0.0.1:11434
模型名称：bge-m3
向量维度：1024
接口：POST /api/embed
```

`agent-service/src/main/resources/application.yml` 中的实际配置：

```yaml
agent:
  embedding:
    base-url: ${OLLAMA_BASE_URL:http://127.0.0.1:11434}
    model: ${OLLAMA_EMBEDDING_MODEL:bge-m3}
    dimension: ${OLLAMA_EMBEDDING_DIMENSION:1024}
    batch-size: 16
    connect-timeout: 5s
    read-timeout: 60s
```

请求示例：

```json
{
  "model": "bge-m3",
  "input": "如何解决数据库连接超时问题？"
}
```

响应关键字段：

```json
{
  "model": "bge-m3",
  "embeddings": [
    [0.001, -0.002, "...1024 个 float..."]
  ]
}
```

已实现的适配层：

```text
software_market_agent.embedding.OllamaEmbeddingClient
```

行为：

- 调用 `POST /api/embed`；
- 读取 `embeddings`；
- 强制校验数量与 1024 维，否则抛出 `RagUnavailableException`；
- 批量索引时把多条文本放入 `input` 数组；
- 连接超时 5 秒、读取超时 60 秒，最多重试 3 次；
- 日志只记录模型、数量、维度和耗时，不打印文本和向量。

切换后需要：

1. 重启 `agent-service`；
2. 对现有帖子执行一次全量回填，使 Milvus 中的向量全部由 BGE-M3 生成；
3. 确认 Milvus collection 维度仍是 1024，与 BGE-M3 匹配。

之前如果已经用 Mock/DashScope 写入了向量，必须重新回填，否则向量空间不一致，检索结果不可用。

## 6. 注意事项

- 不要删除 `D:\Ollama\models`，否则需要重新执行 `ollama pull bge-m3`。
- 如果 Ollama 启动时没有继承 `OLLAMA_MODELS`，模型可能被下载到 C 盘默认目录；用 `D:\Ollama\ollama.exe list` 和 `D:\Ollama\models` 目录共同确认。
- BGE-M3 上下文长度为 8192，本项目按 500～800 字切分，远低于该限制。
- 需要下载其他模型时：

```powershell
D:\Ollama\ollama.exe pull <model-name>
```

- 如果后续 `pull` 因网络失败，可临时给 Ollama 进程配置本机代理环境变量后再启动；不要把代理或密钥写入仓库。
