<script setup lang="ts">
import { reactive, ref } from 'vue'
import { addAdmin } from '@/api/admin'
import { showMsg } from '@/components/MessageBox'

const loading = ref(false)
const result = ref<{ adminId: number, account: string } | null>(null)

const form = reactive({
  account: '',
  password1: '',
  password2: '',
})

const errors = reactive({
  account: '',
  password1: '',
  password2: '',
})

function validate(): boolean {
  errors.account = ''
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

  if (!form.password1) {
    errors.password1 = '请输入密码'
    valid = false
  } else if (form.password1.length < 6) {
    errors.password1 = '密码不能少于6位'
    valid = false
  }

  if (!form.password2) {
    errors.password2 = '请确认密码'
    valid = false
  } else if (form.password1 !== form.password2) {
    errors.password2 = '两次密码不一致'
    valid = false
  }

  return valid
}

async function handleSubmit() {
  if (!validate()) return
  result.value = null

  loading.value = true
  try {
    const res = await addAdmin({
      account: form.account,
      password1: form.password1,
      password2: form.password2,
    })

    if (res.code === 200) {
      result.value = { adminId: res.data.adminId, account: res.data.account }
      form.account = ''
      form.password1 = ''
      form.password2 = ''
      showMsg('新增管理员成功')
    } else {
      showMsg(res.msg || '新增失败')
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
      <h1 class="page-top__title">新增管理员</h1>
      <p class="page-top__desc">创建新的管理员账号</p>
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

        <div class="form-row">
          <div class="field">
            <label class="field-label">密码</label>
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
            <label class="field-label">确认密码</label>
            <input
              v-model="form.password2"
              class="field-input"
              type="password"
              placeholder="再次输入密码"
              autocomplete="new-password"
              :class="{ 'field-input--error': errors.password2 }"
            >
            <p v-if="errors.password2" class="field-error">{{ errors.password2 }}</p>
          </div>
        </div>

        <button class="submit-btn" type="submit" :disabled="loading">
          {{ loading ? '创建中...' : '创建管理员' }}
        </button>
      </form>

      <div v-if="result" class="success-banner">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
          <polyline points="22,4 12,14.01 9,11.01"/>
        </svg>
        <div>
          <strong>{{ result.account }}</strong> 创建成功
          <span class="success-banner__id">ID: {{ result.adminId }}</span>
        </div>
      </div>
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

/* ====== Form Card ====== */
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

/* ====== Fields ====== */
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

/* ====== Submit ====== */
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

/* ====== Success ====== */
.success-banner {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  margin-top: 1.25rem;
  padding: 0.85rem 1rem;
  border-radius: 10px;
  background: #f0fdfa;
  color: #0d9488;
  font-size: 0.875rem;

  strong {
    font-weight: 600;
  }

  :global(body.dark-mode) & {
    background: rgba(13, 148, 136, 0.1);
    color: #2dd4bf;
  }
}

.success-banner__id {
  display: block;
  font-size: 0.75rem;
  opacity: 0.7;
  margin-top: 0.1rem;
}
</style>
