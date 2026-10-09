# CLAUDE.md

本文为 Claude Code（claude.ai/code）提供操作此代码仓库的指引。

## 命令

### 后端（Spring Boot + Maven）
- **运行**: `mvn spring-boot:run`（在项目根目录）
- **打包**: `mvn clean package -DskipTests`
- **测试**: `mvn test`

### 前端（Vue 3 + Vite + pnpm）
- **开发服务器**: `pnpm dev`（在 `fronted/newSSE/` 目录下）
- **构建**: `pnpm build`
- **Lint**: `pnpm lint`
- **类型检查**: `pnpm type-check`（vue-tsc --noEmit）
- **格式化**: `pnpm format`（prettier）

## 项目结构

```
software-market/
├── src/main/java/software_market/     # 后端
│   ├── controller/                    # REST 控制器
│   ├── service/                       # 业务逻辑
│   │   └── impl/
│   ├── config/security/               # JWT、Spring Security、CORS
│   ├── dto/request/                   # 请求 DTO
│   ├── dto/response/                  # 响应 DTO
│   ├── entity/                        # MyBatis-Plus 实体
│   ├── mapper/                        # MyBatis-Plus 映射器
│   ├── vo/                            # 视图对象
│   ├── exception/                     # BizException、GlobalExceptionHandler
│   ├── utils/                         # Result<T> 包装
│   └── constant/                      # Redis 键常量
├── fronted/newSSE/src/                # 前端
│   ├── api/                           # API 客户端（基于 fetch）
│   │   ├── req.ts                     # requestFunc 封装（token 刷新、JWT）
│   │   ├── info/getInfo.ts            # 用户信息接口（GET + POST）
│   │   └── LoginAndRegister/          # 认证接口
│   ├── components/                    # 可复用组件
│   ├── composables/                   # 组合式函数
│   ├── store/                         # 模块级单例（无 Pinia!）
│   ├── views/                         # 页面组件
│   ├── layouts/                       # 布局包装
│   ├── router/index.ts                # 路由 + 认证守卫
│   └── utils/                         # 工具函数
└── pom.xml
```

## 架构与关键模式

### 认证流程（JWT）
- **Access token**（7天）+ **Refresh token**（30天，存储在数据库，一次性使用）
- `src/api/req.ts` 中的 `requestFunc` 在 401 时通过 `/auth/refresh` 自动刷新 token
- `JwtAuthenticationFilter` 是一个 `OncePerRequestFilter`，带有白名单：
  - 公共端点：`/api/auth/login`、`/register`、`/validateEmail`、`/refresh`、`/modifyPassword`
  - 其他所有请求需要有效的 Bearer token
- `UserContext`（ThreadLocal）在 service 层提供 `getUserId()`
- Spring Security 为**无状态**（无会话），CORS 允许所有来源
- `rememberMe` localStorage 标志控制 refreshToken 的持久化

### API 响应格式
所有后端接口返回 `Result<T>`（`/utils/Result.java`）：
```json
{ "code": 200, "message": "操作成功", "data": {...} }
```
- `code: 200` = 成功，`code: 400` = BizException，`code: 401` = UnauthorizedException
- 异常 → `GlobalExceptionHandler` → `Result.fail(code, msg)`

### 前端状态管理（无 Pinia）
所有 store 使用**模块级单例**配合 `reactive`/`ref`：
- `userStore.ts` — token、refreshToken、userInfo、`isLogin` 计算属性
- `postStore.ts` — 帖子列表、筛选条件、分页
- `noticeStore.ts` — 未读通知数
- `newPostsStore.ts` — 轮询新帖子（60 秒间隔）

### 字段名不匹配（关键）
后端实体与前端的 TypeScript 接口之间存在系统性的字段命名不一致：
- **`CurrentUserVO`**（来自 `GET /auth/info`）：带有 `@JsonProperty("userID")` 和 `@JsonProperty("avatarURL")` — 字段与 store 的 `UserInfo` 接口匹配
- **`User` 实体**（来自 `POST /auth/getInfo`）：无 `@JsonProperty` — 返回原始小驼峰命名（`userId`、`avatarUrl`、`emailNotificationBlocked`、`punishNum`、`banend`）
- `getInfo.ts` 中的 `mapUserToAllInfo()` 函数将 `User` 实体字段桥接到 `AllInfo` 接口格式

### 前端 API 客户端
- 通过 `req.ts` 中的 `requestFunc` 使用原生 `fetch`（非 axios）
- 当 `tokenIsNeeded=true` 时自动添加 `Authorization: Bearer <token>` 请求头
- 遇到 401：尝试 `/auth/refresh` 一次，然后重试原始请求
- 通过 `refreshPromise` 守卫去重刷新 token 的请求

### 两个信息接口（勿混淆）
| 接口 | 方法 | 返回值 | 使用方 |
|---|---|---|---|
| `/api/auth/info` | GET | `CurrentUserVO`（带有 `@JsonProperty` 映射） | 登录恢复，App.vue 挂载时 |
| `/api/auth/getInfo` | POST | 原始 `User` 实体（无重命名） | OptionsView、UserProfileView、Chat |

### 路由守卫
- `/auth/*` 路径始终可访问
- 非认证路径检查 `isLogin`（token + userID > 0）。如果存在 refreshToken 但未登录，仍允许访问（App.vue 处理静默刷新）
- 否则重定向到 `/auth/login?redirect=<path>`
