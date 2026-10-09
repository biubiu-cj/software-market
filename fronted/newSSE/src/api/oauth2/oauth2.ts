import { requestFunc } from '../req'

export interface OAuth2Info {
  app_id: string
  redirect_uri: string
  response_type: string
  scope: string
  state: string
}

export interface OAuth2AppInfo {
  app_id: string
  app_name: string
  app_icon: string
  description: string
}

export interface OAuth2AuthorizeResponse {
  redirect_url: string
}

export interface OAuth2TokenResponse {
  access_token: string
  token_type: string
  expires_in: number
  scope: string
}

export interface OAuth2UserInfo {
  user_id: number
  name: string
  email: string
  avatar_url: string
  intro: string
  identity: string
}

export interface OAuth2Record {
  app_id: string
  app_name: string
  app_icon: string
  scope: string
  created_at: string
}

export interface OAuth2RecordsPage {
  records: OAuth2Record[]
  total: number
  page: number
  page_size: number
  total_pages: number
}

/**
 * 检查OAuth2应用信息
 * @param appId 应用ID
 * @returns 应用信息
 */
export async function checkOAuth2App(appId: string): Promise<OAuth2AppInfo> {
  const response = await requestFunc('/auth/connect/check', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: {
      app_id: appId,
    },
  }, true)

  if (!response) {
    throw new Error('网络请求失败')
  }

  const result = await response.json()

  if (result.code !== 200) {
    throw new Error(result.message || '获取应用信息失败')
  }

  return result.data
}

/**
 * 授权OAuth2应用
 * @param oauth2Info OAuth2授权信息
 * @returns 授权结果
 */
export async function authorizeOAuth2App(oauth2Info: OAuth2Info): Promise<OAuth2AuthorizeResponse> {
  const response = await requestFunc('/auth/connect/authorize', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: oauth2Info,
  }, true)

  if (!response) {
    throw new Error('网络请求失败')
  }

  const result = await response.json()

  if (result.code !== 200) {
    throw new Error(result.message || '授权失败')
  }

  return result.data
}

/**
 * 用授权码换取 access_token（第三方服务端调用，不需要用户 JWT）
 */
export async function getToken(code: string, appId: string, appSecret: string): Promise<OAuth2TokenResponse> {
  const body = new URLSearchParams()
  body.append('code', code)
  body.append('app_id', appId)
  body.append('app_secret', appSecret)

  const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/auth/connect/token`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: body.toString(),
  })

  if (!response.ok) {
    throw new Error('网络请求失败')
  }

  const result = await response.json()

  if (result.code !== 200) {
    throw new Error(result.message || '获取令牌失败')
  }

  return result.data
}

/**
 * 获取 OAuth2 用户信息（使用 OAuth2 access_token）
 */
export async function getOAuth2UserInfo(accessToken: string): Promise<OAuth2UserInfo> {
  const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/auth/connect/userinfo`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${accessToken}`,
    },
  })

  if (!response.ok) {
    throw new Error('网络请求失败')
  }

  const result = await response.json()

  if (result.code !== 200) {
    throw new Error(result.message || '获取用户信息失败')
  }

  return result.data
}

/**
 * 获取授权记录（需要用户 JWT）
 */
export async function getOAuth2Records(page: number = 1, pageSize: number = 20): Promise<OAuth2RecordsPage> {
  const response = await requestFunc('/auth/connect/records', {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
    query: {
      page: String(page),
      page_size: String(pageSize),
    },
  }, true)

  if (!response) {
    throw new Error('网络请求失败')
  }

  const result = await response.json()

  if (result.code !== 200) {
    throw new Error(result.message || '获取授权记录失败')
  }

  return result.data
}
