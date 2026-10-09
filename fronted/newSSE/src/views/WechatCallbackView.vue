<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getInfo } from '@/api/info/getInfo'
import { useUserStore } from '@/store/userStore'

const route = useRoute()
const router = useRouter()
const { setToken, setRefreshToken, setUserInfo } = useUserStore()
const error = ref('')

onMounted(async () => {
  const token = route.query.token as string | undefined
  const refreshToken = route.query.refreshToken as string | undefined

  if (!token || !refreshToken) {
    error.value = '登录参数缺失，请重试'
    return
  }

  try {
    setToken(token)
    setRefreshToken(refreshToken)

    const info = await getInfo()
    if (info) {
      setUserInfo(info)
    }

    router.push('/')
  } catch {
    error.value = '获取用户信息失败，请重试'
  }
})
</script>

<template>
  <div class="callback-shell">
    <div v-if="!error" class="callback-loading">
      <div class="callback-spinner" />
      <p>正在登录...</p>
    </div>
    <div v-else class="callback-error">
      <p>{{ error }}</p>
      <router-link to="/auth/login">返回登录</router-link>
    </div>
  </div>
</template>

<style scoped>
.callback-shell {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg);
}

.callback-loading {
  text-align: center;
  color: #64748b;
}

.callback-error {
  text-align: center;
}

.callback-error p {
  color: #ef4444;
  margin-bottom: 1rem;
}

.callback-error a {
  color: #0d9488;
  font-weight: 600;
}

.callback-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid rgba(15, 23, 42, 0.08);
  border-top-color: #0d9488;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
