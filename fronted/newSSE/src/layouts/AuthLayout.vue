<script setup lang="ts">
import { computed, onUnmounted, watchEffect } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/userStore'

const route = useRoute()
const router = useRouter()
const { isLogin } = useUserStore()

const meta = computed(() => {
  const name = route.name as string | undefined
  if (name === 'Register') {
    return {
      title: '创建账号',
      subtitle: '加入 SSE MARKET 社区',
    }
  }
  if (name === 'Reset') {
    return {
      title: '重置密码',
      subtitle: '通过邮箱验证码设置新密码',
    }
  }
  return {
    title: '登录',
    subtitle: '欢迎回来',
  }
})

const stop = watchEffect(() => {
  if (isLogin.value) {
    const query = route.query
    if (!query || !('redirect' in query)) {
      router.push('/')
    }
    let redirect = query.redirect as string || '/'
    const otherParams = new URLSearchParams()
    for (const key in query) {
      if (key === 'redirect')
        continue
      const value = query[key] as string
      otherParams.append(key, value)
    }
    if (otherParams.toString()) {
      const separator = redirect.includes('?') ? '&' : '?'
      redirect += separator + otherParams.toString()
    }
    router.push(redirect)
  }
})
onUnmounted(() => {
  stop()
})
</script>

<template>
  <div class="auth-shell">
    <div class="auth-visual" aria-hidden="true">
      <div class="auth-visual-noise" />
      <div class="auth-visual-content">
        <RouterLink to="/" class="auth-logo">
          SSE MARKET
        </RouterLink>
        <p class="auth-tagline">
          校园社区 · 课程与市集 · 多端一致体验
        </p>
      </div>
    </div>
    <main class="auth-main">
      <div class="auth-card">
        <header class="auth-card-head">
          <h1 class="auth-card-title">
            {{ meta.title }}
          </h1>
          <p class="auth-card-sub">
            {{ meta.subtitle }}
          </p>
        </header>
        <div class="auth-card-body">
          <router-view />
        </div>
      </div>
      <p class="auth-footer-note">
        <RouterLink to="/">
          返回首页
        </RouterLink>
      </p>
    </main>
  </div>
</template>

<style scoped lang="scss">
.auth-shell {
  min-height: 100vh;
  min-height: 100dvh;
  display: grid;
  grid-template-columns: 1fr;
  background: var(--color-bg);
}

@media screen and (min-width: 900px) {
  .auth-shell {
    grid-template-columns: minmax(280px, 1fr) minmax(400px, 520px);
  }
}

.auth-visual {
  position: relative;
  display: none;
  overflow: hidden;
  background: var(--auth-gradient);
  color: #f8fafc;
}

@media screen and (min-width: 900px) {
  .auth-visual {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 3rem 2.5rem;
  }
}

.auth-visual-noise {
  position: absolute;
  inset: 0;
  opacity: 0.35;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.85' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='0.45'/%3E%3C/svg%3E");
  pointer-events: none;
}

.auth-visual-content {
  position: relative;
  z-index: 1;
  max-width: 22rem;
  text-align: left;
}

.auth-logo {
  font-size: clamp(1.5rem, 3vw, 1.85rem);
  font-weight: 800;
  letter-spacing: 0.2em;
  color: #f8fafc !important;
  text-decoration: none;
  display: inline-block;
  margin-bottom: 1rem;
  text-shadow: 0 2px 24px rgba(0, 0, 0, 0.25);
}

.auth-logo:hover {
  color: #ccfbf1 !important;
  background: transparent;
}

.auth-tagline {
  margin: 0;
  font-size: 0.95rem;
  line-height: 1.65;
  opacity: 0.88;
  font-weight: 500;
}

.auth-main {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: clamp(1.25rem, 4vw, 2.5rem) clamp(1rem, 4vw, 2rem) 2rem;
  min-height: 100vh;
  min-height: 100dvh;
}

.auth-card {
  width: 100%;
  max-width: 26rem;
  padding: clamp(1.5rem, 4vw, 2rem);
  border-radius: var(--radius-xl);
  background: var(--surface-1);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-lg);
}

.auth-card-head {
  margin-bottom: 1.25rem;
}

.auth-card-title {
  margin: 0;
  font-size: 1.35rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--color-text);
}

.auth-card-sub {
  margin: 0.35rem 0 0;
  font-size: 0.9rem;
  color: var(--shop-text-secondary, #64748b);
  line-height: 1.5;
}

.auth-card-body {
  width: 100%;
}

.auth-footer-note {
  margin-top: 1.5rem;
  font-size: 0.875rem;
  text-align: center;
  color: var(--shop-text-muted, #94a3b8);
}

.auth-footer-note a {
  font-weight: 600;
}

body.dark-mode .auth-card {
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.45);
}
</style>
