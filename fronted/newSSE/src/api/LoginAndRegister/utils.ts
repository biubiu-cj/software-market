import { showMsg } from '@/components/MessageBox'
import { requestFunc } from '../req'

/***
 * @param {string} email
 * @param {0|1} mode 模式，未注册要用0，注册了的要用1
 */
async function sendCode(email: string, mode: 0 | 1) {
  try {
    const res = await requestFunc(
      `/auth/validateEmail`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: {
          email,
          mode,
        },
      },
      false,
    )
    if (!res) {
      showMsg('发送验证码失败, 请重新获取')
      return null
    }
    if (!res.ok) {
      const errorData = await res.json()
      if (errorData && typeof errorData.msg === 'string') {
        showMsg(errorData.msg)
      }
      else {
        showMsg('发送验证码失败，请检查环境问题')
      }
      return null
    }
    const data = await res.json()
    return data
  }
  catch (e) {
    console.error(e)
    showMsg('发送验证码失败, 请重新获取')
  }
}

export { sendCode }
