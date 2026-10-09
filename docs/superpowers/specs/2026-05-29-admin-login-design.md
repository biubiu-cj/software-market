# Admin Login Page Design

## Overview

为软件市场新增独立的管理端登录页面。

管理端采用独立技术栈（Axios + Pinia + Element Plus）和独立路由树，与客户端完全隔离。

当前仅实现管理员登录页面与认证流程，不实现后端逻辑，后端接口由开发者后续自行实现。

---

# Tech Stack

* Vue3
* TypeScript
* Vite
* Vue Router
* Pinia
* Axios
* Element Plus

---

# Route Design

```txt
/admin/login          → AdminLoginView（公开，无需登录）
/admin/*              → 后续后台页面（需 admin token 认证）
```

## Router Guard Logic

### `/admin/login`

* 已存在 `adminToken` 时：
  自动跳转 `/admin/dashboard`

### 其他 `/admin/*`

* 未登录时：
  自动跳转 `/admin/login`

---

# File Structure

```txt
src/
├── api/
│   └── admin.ts                  # 管理员 API
│
├── stores/
│   └── admin.ts                  # Pinia 管理员状态
│
├── router/
│   └── index.ts                  # 路由 + 路由守卫
│
├── views/
│   └── admin/
│       └── login/
│           └── index.vue         # 管理员登录页
│
├── layouts/
│   └── AdminLayout.vue           # 后续后台 Layout（预留）
│
├── utils/
│   └── request.ts                # Axios 实例
```

---

# Component Structure

```txt
AdminLoginView
├── 左侧品牌区
│   ├── Logo
│   ├── 系统名称
│   ├── Slogan
│   └── 插图 / 背景
│
└── 右侧登录区
    ├── ElCard
    │   ├── 标题
    │   ├── ElForm
    │   │   ├── account 输入框
    │   │   ├── password 输入框
    │   │   ├── remember me
    │   │   └── login button
    │   └── forgot password
```

---

# Data Flow

```txt
用户输入账号密码
    ↓
Element Plus Form 收集数据
    ↓
调用 adminApi.login()
    ↓
POST /api/auth/adminLogin
    ↓
登录成功：
token → Pinia → localStorage
    ↓
router.push('/admin/dashboard')

登录失败：
ElMessage.error(msg)
```

---

# API Specification

## 管理员登录

### Request

```http
POST /api/auth/adminLogin
Content-Type: application/json
```

### Request Body

```json
{
  "account": "string",
  "password": "string"
}
```

### Response

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "token": "xxxx"
  }
}
```

---

# Axios Configuration

## Base URL

开发环境：

```txt
/api
```

使用 Vite Proxy 转发后端请求。

---

## Authorization Header

登录成功后：

所有 `/admin/*` 接口自动携带：

```txt
Authorization: Bearer {adminToken}
```

---

# Admin Store Design

## State

```ts
token: string
```

---

## Actions

### setToken(token)

功能：

* 保存 token 到 Pinia
* 保存到 localStorage

localStorage Key：

```txt
adminToken
```

---

### clearToken()

功能：

* 清除 Pinia token
* 清除 localStorage

---

## Getter

### isLoggedIn

```ts
return !!token
```

---

# Token Isolation

管理员端 Token 与普通用户 Token 完全隔离。

## 用户端

```txt
token
```

## 管理员端

```txt
adminToken
```

不得共用。

---

# Token Expired Handling

当接口返回：

```txt
401 Unauthorized
```

需要：

1. 自动 clearToken()
2. 自动跳转 `/admin/login`
3. ElMessage.error("登录已过期，请重新登录")

---

# Form Validation

使用 Element Plus Form Rules。

## account

* 必填
* 不能为空

## password

* 必填
* 不能为空

---

# Visual Style

## Layout

左右分栏布局：

* 左侧：40%~50%
* 右侧：50%~60%

---

## Left Section

### Background

深蓝渐变：

```txt
#1a365d → #2563eb
```

### Content

* Logo
* 软件市场管理端
* 一站式软件交易平台管理系统

文字颜色：

```txt
#ffffff
```

---

## Right Section

### Background

浅灰：

```txt
#f5f7fa
```

### Login Card

* 白色 ElCard
* 宽度约 380px
* 垂直水平居中

---

## Login Button

* 全宽
* 品牌主色
* loading 状态

---

# Responsive Design

当屏幕宽度 ≤ 768px：

改为上下布局：

```txt
品牌区
↓
登录区
```

移动端表单宽度：

```txt
90%
```

---

# Future Admin Layout

登录成功后进入独立后台 Layout：

```txt
AdminLayout
├── Sidebar
├── Header
└── RouterView
```

管理员后台与客户端 Layout 完全隔离。

---

# TypeScript Requirement

所有：

* API
* Request
* Response
* Store

必须定义 TypeScript Interface。

不得使用 any。

---

# Path Alias

统一使用：

```txt
@/
```

例如：

```ts
@/api/admin
@/stores/admin
@/views/admin/login
```

---

# Error Handling

## 网络错误

```txt
ElMessage.error("网络错误，请稍后重试")
```

---

## 400 / 401

显示后端返回：

```txt
msg
```

---

## 防重复提交

登录按钮 loading 状态期间：

禁止重复点击。

---

# Output Requirement

请直接生成完整代码文件：

* api/admin.ts
* stores/admin.ts
* utils/request.ts
* router/index.ts
* views/admin/login/index.vue

不要只给思路或伪代码。

不要生成后端代码。

严格按照接口字段实现。

不得自由发挥接口字段名称。
