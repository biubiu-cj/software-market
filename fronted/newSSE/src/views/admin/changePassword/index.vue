<script setup lang="ts">
import { reactive, ref } from 'vue'
import { updateAdminInfo } from '@/api/admin'
import { showMsg } from '@/components/MessageBox'

const loading = ref(false)

const form = reactive({
  account: '',
  name: '',
  password1: '',
  password2: '',
})

const errors = reactive({
  account: '',
  name: '',
  password1: '',
  password2: '',
})

function validate(): boolean {
  errors.account = ''
  errors.name = ''
  errors.password1 = ''
  errors.password2 = ''
  let valid = true

  if (!form.account.trim()) {
    errors.account = '请输入管理员账号'
    valid = false
  } else if (!form.account.includes('@')) {
    errors.account = '账号必须为邮箱格式'
    valid = false
  }

  // 密码：如果填了其中一个，必须都填且匹配
  const hasPwd1 = form.password1.trim() !== ''
  const hasPwd2 = form.password2.trim() !== ''

  if (hasPwd1 || hasPwd2) {
    if (!hasPwd1) {
      errors.password1 = '请输入新密码'
      valid = false
    } else if (form.password1.length < 6) {
      errors.password1 = '密码不能少于6位'
      valid = false
    }
    if (!hasPwd2) {
      errors.password2 = '请确认新密码'
      valid = false
    } else if (form.password1 !== form.password2) {
      errors.password2 = '两次密码不一致'
      valid = false
    }
  }

  // 至少需要填写用户名或密码
  if (!hasPwd1 && !form.name.trim()) {
    errors.name = '请填写新用户名或新密码'
    valid = false
  }

  return valid
}

async function handleSubmit() {
  if (!validate()) return

  loading.value = true
  try {
    const payload: { account: string; name?: string; password1?: string; password2?: string } = {
      account: form.account,
    }
    if (form.name.trim()) payload.name = form.name.trim()
    if (form.password1.trim()) {
      payload.password1 = form.password1
      payload.password2 = form.password2
    }

    const res = await updateAdminInfo(payload)

    if (res.code === 200) {
      showMsg('信息修改成功')
      form.name = ''
      form.password1 = ''
      form.password2 = ''
    } else {
      showMsg(res.msg || '修改失败')
    }
  } catch {
    showMsg('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="form-page">
    <div class="page-top">
      <h1 class="page-top__title">修改管理员信息</h1>
      <p class="page-top__desc">修改管理员的用户名或密码</p>
    </div>

    <div class="form-card">
      <form @submit.prevent="handleSubmit">
        <div class="field">
          <label class="field-label">管理员账号（邮箱）</label>
          <input
            v-model="form.account"
            class="field-input"
            type="email"
            placeholder="admin@example.com"
            autocomplete="off"
            :class="{ 'field-input--error': errors.account }"
          >
          <p v-if="errors.account" class="field-error">{{ errors.account }}</p>
        </div>

        <div class="field">
          <label class="field-label">新用户名（留空则不修改）</label>
          <input
            v-model="form.name"
            class="field-input"
            type="text"
            placeholder="输入新的用户名"
            autocomplete="off"
            :class="{ 'field-input--error': errors.name }"
          >
          <p v-if="errors.name" class="field-error">{{ errors.name }}</p>
        </div>

        <div class="form-divider">
          <span>密码修改（选填）</span>
        </div>

        <div class="form-row">
          <div class="field">
            <label class="field-label">新密码</label>
            <input
              v-model="form.password1"
              class="field-input"
              type="password"
              placeholder="至少6位"
              autocomplete="new-password"
              :class="{ 'field-input--error': errors.password1 }"
            >
            <p v-if="errors.password1" class="field-error">{{ errors.password1 }}</p>
          </div>

          <div class="field">
            <label class="field-label">确认新密码</label>
            <input
              v-model="form.password2"
              class="field-input"
              type="password"
              placeholder="再次输入新密码"
              autocomplete="new-password"
              :class="{ 'field-input--error': errors.password2 }"
            >
            <p v-if="errors.password2" class="field-error">{{ errors.password2 }}</p>
          </div>
        </div>

        <button class="submit-btn" type="submit" :disabled="loading">
          {{ loading ? '保存中...' : '保存修改' }}
        </button>
      </form>
    </div>
  </div>
</template>

<style scoped lang="scss">
.form-page {
  max-width: 600px;
}

.page-top {
  margin-bottom: 1.5rem;
}

.page-top__title {
  font-size: 1.35rem;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.02em;
  margin: 0;

  :global(body.dark-mode) & {
    color: #f1f5f9;
  }
}

.page-top__desc {
  margin: 0.2rem 0 0;
  font-size: 0.85rem;
  color: #94a3b8;
}

.form-card {
  background: #ffffff;
  border: 1px solid rgba(15, 23, 42, 0.06);
  border-radius: 12px;
  padding: 1.75rem;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);

  :global(body.dark-mode) & {
    background: #111827;
    border-color: rgba(255, 255, 255, 0.06);
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  }
}

.form-divider {
  display: flex;
  align-items: center;
  margin: 0.75rem 0 0.5rem;
  font-size: 0.78rem;
  font-weight: 600;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  gap: 0.75rem;

  &::before,
  &::after {
    content: '';
    flex: 1;
    height: 1px;
    background: rgba(15, 23, 42, 0.06);

    :global(body.dark-mode) & {
      background: rgba(255, 255, 255, 0.06);
    }
  }
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 1rem;
}

@media screen and (max-width: 480px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}

.field {
  margin-bottom: 1.25rem;
}

.field-label {
  display: block;
  font-size: 0.825rem;
  font-weight: 600;
  color: #334155;
  margin-bottom: 0.4rem;

  :global(body.dark-mode) & {
    color: #e2e8f0;
  }
}

.field-input {
  width: 100%;
  height: 42px;
  padding: 0 0.85rem;
  border: 1px solid rgba(15, 23, 42, 0.1);
  border-radius: 10px;
  background: #ffffff;
  color: #0f172a;
  font-size: 0.9rem;
  font-family: inherit;
  transition: border-color 0.15s, box-shadow 0.15s;

  &::placeholder {
    color: #cbd5e1;
  }

  &:focus {
    outline: none;
    border-color: #0d9488;
    box-shadow: 0 0 0 3px rgba(13, 148, 136, 0.1);
  }

  &--error {
    border-color: #ef4444;

    &:focus {
      box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
    }
  }

  :global(body.dark-mode) & {
    background: #0f172a;
    border-color: rgba(255, 255, 255, 0.1);
    color: #e2e8f0;

    &:focus {
      border-color: #0d9488;
      box-shadow: 0 0 0 3px rgba(13, 148, 136, 0.15);
    }
  }
}

.field-error {
  margin: 0.3rem 0 0;
  font-size: 0.78rem;
  color: #ef4444;
}

.submit-btn {
  width: 100%;
  height: 44px;
  margin-top: 0.5rem;
  border: none;
  border-radius: 10px;
  background: #0d9488;
  color: #ffffff;
  font-size: 0.95rem;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: background 0.15s, transform 0.1s;

  &:hover:not(:disabled) {
    background: #0f766e;
  }

  &:active:not(:disabled) {
    transform: scale(0.99);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}
</style>
