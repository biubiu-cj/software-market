<!-- eslint-disable regexp/no-super-linear-backtracking -->
<script lang="ts" setup>
import type { CustomFormData } from '@/composables/FormExam'
import { computed, ref, useTemplateRef } from 'vue'
import { useRouter } from 'vue-router'

import { getInfo } from '@/api/info/getInfo'
import { userRegister } from '@/api/LoginAndRegister/register'

import { sendCode } from '@/api/LoginAndRegister/utils'
import FormContainer from '@/components/FormContainer.vue'

import { showMsg } from '@/components/MessageBox.jsx'
import { useFormExam } from '@/composables/FormExam'
import { useUserStore } from '@/store/userStore'

const router = useRouter()
const { setToken, setRefreshToken, setUserInfo } = useUserStore()
const rememberMe = useTemplateRef('rememberMe')

// const passwordReg = /^\S*(?=\S{6})(?=\S*\d)(?=\S*[A-Z])(?=\S*[a-z])(?=\S*[!@#$%^&*? ])\S+$/

const registerForm = ref<CustomFormData[]>([
  {
    id: 'username',
    value: '',
    label: '用户名',
    autocomplete: 'username',
  },
  {
    id: 'email',
    value: '',
    label: '邮箱',
    type: 'email',
    autocomplete: 'email',
  },
  {
    id: 'password',
    value: '',
    label: '密码',
    type: 'password',
    // reg: passwordReg,
    autocomplete: 'new-password',
  },
  {
    id: 'password2',
    value: '',
    label: '确认密码',
    // reg: passwordReg,
    type: 'password',
  },
  {
    id: 'v_code',
    value: '',
    label: '验证码',
  },
  {
    id: 'cd_key',
    value: '',
    label: '邀请码',
  },
])

const emailIsCorrect = computed(() => {
  const email = registerForm.value[1].value
  const reg = /^[\w.%+-]+@[a-z0-9.-]+\.[a-z]{2,}$/i
  return reg.test(email)
})

const { correct } = useFormExam(registerForm)

// const passwordIsCorrect = computed(() => {
//   const password = registerForm.value[2].value
//   const password2 = registerForm.value[3].value
//   return password === password2 && passwordReg.test(password)
// })

async function sendCodeAction() {
  const email = registerForm.value[1].value
  if (!emailIsCorrect.value) {
    return
  }
  const data = await sendCode(email, 0)
  if (!data) {
    return
  }
  showMsg('验证码发送成功，请注意查收')
}

async function registerAction() {
  if (!correct.value) {
    return
  }

  const name = registerForm.value[0].value
  const email = registerForm.value[1].value
  const password = registerForm.value[2].value
  const password2 = registerForm.value[3].value
  const vcode = registerForm.value[4].value.trim() // 去除前后空格
  const cd_key = registerForm.value[5].value.trim() // 去除前后空格

  try {
    const resp = await userRegister(cd_key, email, name, password, password2, vcode)
    if (resp.code !== 200) {
      showMsg(resp.msg)
      return
    }

    const token = resp.data?.token
    const refreshToken = resp.data?.refreshToken ?? resp.data?.refresh_token
    if (!token || !refreshToken) {
      showMsg('注册成功，但登录信息异常，请手动登录')
      router.push('/auth/login')
      return
    }

    if (rememberMe.value?.checked) {
      localStorage.setItem('rememberMe', 'true')
    }
    else {
      localStorage.removeItem('rememberMe')
    }

    setToken(token)
    setRefreshToken(refreshToken)

    try {
      const info = await getInfo()
      setUserInfo(info)
    }
    catch (e) {
      console.error('获取用户信息失败', e)
    }

    showMsg('注册成功，已自动登录')
    router.push('/')
  }
  catch (error) {
    console.error('Register failed:', error)
    showMsg('注册失败，请稍后再试')
  }
}
</script>

<template>
  <FormContainer
    form-name="注册并登录"
    :form-data="registerForm"
    :disabled="!correct"
    @submit-form="registerAction"
  >
    <div class="auth-links register-extra">
      <label class="auth-row inline-row">
        <input ref="rememberMe" id="rememberMe" type="checkbox" class="auth-check">
        <span>记住我 <span class="hint">下次自动登录</span></span>
      </label>
      <RouterLink to="/auth/login">
        已有账号
      </RouterLink>
    </div>
    <button
      type="button"
      :disabled="!emailIsCorrect"
      class="btn-secondary"
      @click.prevent="sendCodeAction"
    >
      {{ emailIsCorrect ? '发送邮箱验证码' : '请先填写有效邮箱' }}
    </button>
  </FormContainer>
</template>

<style scoped lang="scss">
.register-extra {
  align-items: flex-start;
  margin-bottom: 0.25rem;
}

.inline-row {
  margin: 0;
  flex: 1;
  min-width: 0;
}

.hint {
  font-weight: 400;
  opacity: 0.75;
  font-size: 0.8rem;
}
</style>
