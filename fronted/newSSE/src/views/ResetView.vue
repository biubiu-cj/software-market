<script setup lang="ts">
import type { CustomFormData } from '@/composables/FormExam'
import { computed, ref } from 'vue'

import { useRouter } from 'vue-router'
import { updatePassword } from '@/api/LoginAndRegister/forgetPwd'
import { sendCode } from '@/api/LoginAndRegister/utils'
import FormContainer from '@/components/FormContainer.vue'
import { showMsg } from '@/components/MessageBox'
import { useFormExam } from '@/composables/FormExam'

const router = useRouter()
const form = ref<CustomFormData[]>([
  {
    id: 'email',
    value: '',
    label: '邮箱',
    type: 'email',
    autocomplete: 'email',
    reg: /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\])|(([a-z\-0-9]+\.)+[a-z]{2,}))$/i,
  },
  {
    id: 'password',
    value: '',
    label: '新密码',
    type: 'password',
    autocomplete: 'new-password',
    reg: /^(?![a-zA-Z]+$)(?![A-Z0-9]+$)(?![A-Z\W_]+$)(?![a-z0-9]+$)(?![a-z\W_]+$)(?![0-9\W_]+$)[\s\S]{8,12}/,
  },
  {
    id: 'password2',
    value: '',
    label: '确认新密码',
    type: 'password',
    reg: /^(?![a-zA-Z]+$)(?![A-Z0-9]+$)(?![A-Z\W_]+$)(?![a-z0-9]+$)(?![a-z\W_]+$)(?![0-9\W_]+$)[\s\S]{8,12}/,
  },
  {
    id: 'code',
    value: '',
    label: '验证码',
  },
])

const { correct } = useFormExam(form)

const passwordCorrect = computed(() => {
  return (
    form.value[1].reg?.test(form.value[1].value)
    && form.value[1].value === form.value[2].value
  )
})
const emailCorrect = computed(() => {
  return form.value[0].reg?.test(form.value[0].value)
})

async function resetHandler() {
  const email = form.value[0].value
  const password1 = form.value[1].value
  const password2 = form.value[2].value
  const code = form.value[3].value
  if (
    email
    && password1
    && password2
    && code
  ) {
    try {
      const res = await updatePassword(
        email,
        password1,
        password2,
        code,
      )
      if (res.code !== 200) {
        showMsg(res.msg)
        return
      }
      showMsg('重置密码成功')
      router.push('/auth/login')
    }
    catch (e) {
      console.error(e)
      showMsg('重置密码失败，请稍后再试')
    }
  }
  else {
    showMsg('不要输入空白信息')
  }
}

async function sendVerificationCode() {
  const email = form.value[0].value
  if (!email)
    return
  const res = await sendCode(email, 1)
  if (res.code !== 200) {
    showMsg(res.msg)
    return
  }
  showMsg('验证码已发送')
}
</script>

<template>
  <FormContainer
    form-name="确认重置"
    :form-data="form"
    :disabled="!correct || !passwordCorrect"
    @submit-form="resetHandler"
  >
    <p class="reset-hint" :class="{ ok: passwordCorrect }">
      {{
        passwordCorrect
          ? '密码格式与两次输入已符合要求'
          : '密码需含大小写字母、数字与特殊字符，长度 8–12 位，且两次一致'
      }}
    </p>
    <div class="auth-links single">
      <router-link to="/auth/login">
        返回登录
      </router-link>
    </div>
    <button
      type="button"
      :disabled="!emailCorrect"
      class="btn-secondary"
      @click="sendVerificationCode"
    >
      {{ emailCorrect ? '发送邮箱验证码' : '请先填写有效邮箱' }}
    </button>
  </FormContainer>
</template>

<style scoped lang="scss">
.reset-hint {
  margin: 0.5rem 0 0.75rem;
  padding: 0.65rem 0.75rem;
  font-size: 0.8rem;
  line-height: 1.45;
  border-radius: var(--radius-md);
  background: rgba(244, 63, 94, 0.08);
  color: #be123c;
  border: 1px solid rgba(244, 63, 94, 0.2);
}

.reset-hint.ok {
  background: rgba(16, 185, 129, 0.1);
  color: #047857;
  border-color: rgba(16, 185, 129, 0.25);
}

body.dark-mode .reset-hint {
  color: #fda4af;
}

body.dark-mode .reset-hint.ok {
  color: #6ee7b7;
}

.auth-links.single {
  justify-content: flex-start;
  margin-bottom: 0.25rem;
}
</style>
