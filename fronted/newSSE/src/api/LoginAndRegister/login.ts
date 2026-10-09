import { requestFunc } from '../req'

/**
 *
 * @param {string} userEmail
 * @param {string} userPassword 未加密的密码
 * @returns {Promise<{ token: string; refresh_token: string } | null>} 登录返回的 token 与 refresh_token
 */
async function userLogin(userEmail: string, userPassword: string): Promise<{ token: string, refresh_token: string } | null> {
  try {
    const res = await requestFunc(`/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: {
        email: userEmail,
        password: userPassword,
      },
    }, false)
    const data = await res?.json()
    const token = data.data?.token
    const refreshToken = data.data?.refreshToken ?? data.data?.refresh_token

    if (token && refreshToken) {
      return { token, refresh_token: refreshToken }
    }
    return null
  }
  catch (e) {
    console.error(e)
    return null
  }
}

export { userLogin }
