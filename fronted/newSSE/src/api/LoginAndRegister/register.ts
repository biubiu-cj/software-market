import { requestFunc } from '../req'

async function userRegister(
  CDKey: string,
  email: string,
  name: string,
  password1: string,
  password2: string,
  valiCode: string,
): Promise<any> {
  try {
    const res = await requestFunc(
      `/auth/register`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: {
          CDKey,
          email,
          name,
          password: password1,
          password2: password2,
          phone: '',
          valiCode,
        },
      },
      false,
    )
    const data = await res?.json()
    return data
  }
  catch (e) {
    console.error(e)
    return null
  }
}

export { userRegister }
