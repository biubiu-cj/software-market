<script setup lang="ts">
import type { CustomFormData } from '@/composables/FormExam'

import { ref } from 'vue'
import { useRouter } from 'vue-router'

import { getInfo } from '@/api/info/getInfo'
import { userLogin } from '@/api/LoginAndRegister/login'
import FormContainer from '@/components/FormContainer.vue'
import { showMsg } from '@/components/MessageBox'
import { useFormExam } from '@/composables/FormExam'

import { useUserStore } from '@/store/userStore'

const router = useRouter()
const { setToken, setRefreshToken, setUserInfo } = useUserStore()

const form = ref<CustomFormData[]>([
  {
    id: 'email',
    label: '邮箱',
    value: '',
    type: 'email',
    autocomplete: 'email',
  },
  {
    id: 'password',
    label: '密码',
    value: '',
    type: 'password',
    autocomplete: 'current-password',
  },
])

const { correct } = useFormExam(form)
const rememberMe = ref(true)

async function loginHandler() {
  const email = form.value[0].value
  const password = form.value[1].value
  try {
    if (email && password) {
      const result = await userLogin(email, password)
      if (!result) {
        showMsg('登录失败')
        return
      }
      if (rememberMe.value) {
        localStorage.setItem('rememberMe', 'true')
      }
      else {
        localStorage.removeItem('rememberMe')
      }
      setToken(result.token)
      setRefreshToken(result.refresh_token)
      const info = await getInfo()
      if (info) {
        setUserInfo(info)
      }
      router.push('/')
    }
  }
  catch (error) {
    console.error('登录失败', error)
    showMsg('登录失败')
  }
}
</script>

<template>
  <FormContainer
    form-name="登录"
    :form-data="form"
    :disabled="!correct"
    @submit-form="loginHandler"
  >
    <label class="auth-row">
      <input v-model="rememberMe" type="checkbox" class="auth-check">
      <span>记住我</span>
    </label>
    <div class="auth-links">
      <router-link to="/auth/register">
        注册账号
      </router-link>
      <router-link to="/auth/reset">
        忘记密码？
      </router-link>
    </div>
    <div class="wechat-divider">
      <span class="wechat-divider__line" />
      <span class="wechat-divider__text">或</span>
      <span class="wechat-divider__line" />
    </div>
    <a href="/api/auth/wechat/authorize" class="wechat-btn">
      <svg class="wechat-btn__icon" width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
        <path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 0 0 .167-.054l1.903-1.114a.864.864 0 0 1 .717-.098 10.16 10.16 0 0 0 2.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178A1.17 1.17 0 0 1 4.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178 1.17 1.17 0 0 1-1.162-1.178c0-.651.52-1.18 1.162-1.18zm5.34 2.867c-1.797-.052-3.746.512-5.28 1.786-1.72 1.428-2.687 3.72-1.78 6.22.942 2.453 3.666 4.229 6.884 4.229.826 0 1.622-.12 2.361-.336a.722.722 0 0 1 .598.082l1.584.926a.272.272 0 0 0 .14.047c.134 0 .24-.111.24-.247 0-.06-.023-.12-.038-.177l-.327-1.233a.582.582 0 0 1-.023-.156.49.49 0 0 1 .201-.398C23.024 18.48 24 16.82 24 14.98c0-3.21-2.931-5.952-7.062-6.122zm-2.18 2.696c.535 0 .969.44.969.982a.976.976 0 0 1-.969.983.976.976 0 0 1-.969-.983c0-.542.434-.982.97-.982zm4.844 0c.535 0 .969.44.969.982a.976.976 0 0 1-.969.983.976.976 0 0 1-.969-.983c0-.542.434-.982.969-.982z"/>
      </svg>
      微信登录
    </a>
  </FormContainer>
</template>
