<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { adminLogin, getAdminInfo } from '@/api/admin'
import { showMsg } from '@/components/MessageBox'
import { useAdminStore } from '@/stores/admin'

const router = useRouter()
const adminStore = useAdminStore()

const loading = ref(false)
const rememberMe = ref(false)

const form = reactive({
  account: '',
  password: '',
})

const errors = reactive({
  account: '',
  password: '',
})

function validate(): boolean {
  errors.account = ''
  errors.password = ''
  let valid = true

  if (!form.account.trim()) {
    errors.account = '请输入管理员账号'
    valid = false
  }
  if (!form.password.trim()) {
    errors.password = '请输入密码'
    valid = false
  }

  return valid
}

async function handleSubmit() {
  if (!validate()) return

  loading.value = true
  try {
    const res = await adminLogin({
      account: form.account,
      password: form.password,
    })

    if (res.code === 200) {
      adminStore.setToken(res.data.token)

      try {
        const infoRes = await getAdminInfo()
        if (infoRes.code === 200 && infoRes.data.admin) {
          adminStore.setAdminInfo(infoRes.data.admin)
        }
      } catch {
        // 获取管理员信息失败不影响登录流程
      }

      showMsg('登录成功')
      router.push('/admin/dashboard')
    } else {
      showMsg(res.msg || '登录失败')
    }
  } catch {
    showMsg('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-shell">
    <!-- Left Brand Panel -->
    <div class="login-brand">
      <div class="login-brand__inner">
        <div class="login-brand__logo">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 2L2 7l10 5 10-5-10-5z"/>
            <path d="M2 17l10 5 10-5"/>
            <path d="M2 12l10 5 10-5"/>
          </svg>
          软工集市
        </div>
        <p class="login-brand__tagline">管理控制台</p>
        <p class="login-brand__desc">统一管理平台用户、内容和系统配置</p>
      </div>
    </div>

    <!-- Right Form Panel -->
    <div class="login-form-panel">
      <div class="login-card">
        <h1 class="login-card__title">管理员登录</h1>
        <p class="login-card__sub">请输入管理员账号和密码</p>

        <form class="login-form" @submit.prevent="handleSubmit">
          <div class="field">
            <label class="field-label">管理员账号</label>
            <input
              v-model="form.account"
              class="field-input"
              type="text"
              placeholder="输入账号"
              autocomplete="username"
              :class="{ 'field-input--error': errors.account }"
            >
            <p v-if="errors.account" class="field-error">{{ errors.account }}</p>
          </div>

          <div class="field">
            <label class="field-label">密码</label>
            <input
              v-model="form.password"
              class="field-input"
              type="password"
              placeholder="输入密码"
              autocomplete="current-password"
              :class="{ 'field-input--error': errors.password }"
            >
            <p v-if="errors.password" class="field-error">{{ errors.password }}</p>
          </div>

          <label class="remember-row">
            <input v-model="rememberMe" type="checkbox" class="remember-check">
            <span>记住我</span>
          </label>

          <button class="login-btn" type="submit" :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </button>
        </form>
      </div>

      <p class="login-footer">
        <RouterLink to="/">返回首页</RouterLink>
      </p>
    </div>
  </div>
</template>

<style scoped lang="scss">
.login-shell {
  min-height: 100vh;
  min-height: 100dvh;
  display: grid;
  grid-template-columns: 1fr;
  background: #f8fafc;

  :global(body.dark-mode) & {
    background: #0b0f14;
  }
}

@media screen and (min-width: 900px) {
  .login-shell {
    grid-template-columns: minmax(320px, 1fr) minmax(420px, 520px);
  }
}

/* ====== Left Brand ====== */
.login-brand {
  display: none;
  background: #0f172a;
  color: #f8fafc;
  position: relative;
  overflow: hidden;

  @media screen and (min-width: 900px) {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 3rem;
  }
}

.login-brand::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse at 20% 50%, rgba(13, 148, 136, 0.15) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 20%, rgba(13, 148, 136, 0.08) 0%, transparent 50%);
  pointer-events: none;
}

.login-brand__inner {
  position: relative;
  z-index: 1;
  max-width: 24rem;
  text-align: left;
}

.login-brand__logo {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  font-size: 1.5rem;
  font-weight: 800;
  letter-spacing: 0.08em;
  color: #f8fafc;
  margin-bottom: 1rem;

  svg {
    color: #0d9488;
  }
}

.login-brand__tagline {
  font-size: 0.95rem;
  font-weight: 600;
  opacity: 0.8;
  margin: 0 0 0.5rem;
  letter-spacing: 0.04em;
}

.login-brand__desc {
  font-size: 0.85rem;
  opacity: 0.55;
  margin: 0;
  line-height: 1.5;
}

/* ====== Right Form ====== */
.login-form-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  min-height: 100vh;
  min-height: 100dvh;
}

.login-card {
  width: 100%;
  max-width: 380px;
  background: #ffffff;
  border: 1px solid rgba(15, 23, 42, 0.06);
  border-radius: 16px;
  padding: 2rem;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06), 0 1px 2px rgba(15, 23, 42, 0.04);

  :global(body.dark-mode) & {
    background: #111827;
    border-color: rgba(255, 255, 255, 0.06);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
  }
}

.login-card__title {
  font-size: 1.3rem;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.02em;
  margin: 0;

  :global(body.dark-mode) & {
    color: #f1f5f9;
  }
}

.login-card__sub {
  font-size: 0.85rem;
  color: #94a3b8;
  margin: 0.35rem 0 0;
}

.login-form {
  margin-top: 1.5rem;
}

/* ====== Fields ====== */
.field {
  margin-bottom: 1.1rem;
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
  height: 44px;
  padding: 0 0.85rem;
  border: 1px solid rgba(15, 23, 42, 0.1);
  border-radius: 10px;
  background: #f8fafc;
  color: #0f172a;
  font-size: 0.925rem;
  font-family: inherit;
  transition: border-color 0.15s, box-shadow 0.15s, background 0.15s;

  &::placeholder {
    color: #cbd5e1;
  }

  &:focus {
    outline: none;
    border-color: #0d9488;
    box-shadow: 0 0 0 3px rgba(13, 148, 136, 0.1);
    background: #ffffff;
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
      background: #0f172a;
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

/* ====== Remember ====== */
.remember-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin: 0.25rem 0 1.25rem;
  font-size: 0.875rem;
  color: #64748b;
  cursor: pointer;
  user-select: none;

  :global(body.dark-mode) & {
    color: #94a3b8;
  }
}

.remember-check {
  width: 1rem;
  height: 1rem;
  accent-color: #0d9488;
  cursor: pointer;
}

/* ====== Submit ====== */
.login-btn {
  width: 100%;
  height: 44px;
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

/* ====== Footer ====== */
.login-footer {
  margin-top: 1.5rem;
  font-size: 0.85rem;
  text-align: center;

  a {
    color: #94a3b8;
    font-weight: 500;
    text-decoration: none;

    &:hover {
      color: #0d9488;

      :global(body.dark-mode) & {
        color: #2dd4bf;
      }
    }
  }
}
</style>
