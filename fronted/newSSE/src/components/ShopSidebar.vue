<script lang="ts" setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store/userStore'

const route = useRoute()
const IsMain = computed(() => {
  return /^\/shop\/?$/.test(route.fullPath)
})
const { userInfo } = useUserStore()
const defaultAvatar = `${import.meta.env.BASE_URL}default-avatar.svg`
</script>

<template>
  <aside class="shop-sidebar" aria-label="商城侧边导航">
    <!-- 用户信息 -->
    <div class="user-info">
      <div class="avatar-container">
        <img :src="userInfo.avatarURL || defaultAvatar" :alt="userInfo.name ? `${userInfo.name}的头像` : '用户头像'" class="avatar">
      </div>
      <div class="user-id">
        {{ userInfo.name }}
      </div>
    </div>

    <!-- 导航 -->
    <nav class="navigation-buttons">
      <RouterLink v-if="IsMain" to="/shop/myproducts" class="nav-button">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z" />
          <line x1="3" y1="6" x2="21" y2="6" />
          <path d="M16 10a4 4 0 0 1-8 0" />
        </svg>
        <span class="nav-text">商品</span>
      </RouterLink>
      <RouterLink v-else to="/shop" class="nav-button">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M19 12H5" />
          <polyline points="12 19 5 12 12 5" />
        </svg>
        <span class="nav-text">返回首页</span>
      </RouterLink>
      <RouterLink to="/chat" class="nav-button">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
        </svg>
        <span class="nav-text">消息</span>
      </RouterLink>
      <RouterLink to="/shop/sale" class="nav-button">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M12 20h9" />
          <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z" />
        </svg>
        <span class="nav-text">出售</span>
      </RouterLink>
      <RouterLink to="/" class="nav-button">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
          <polyline points="9 22 9 12 15 12 15 22" />
        </svg>
        <span class="nav-text">主页</span>
      </RouterLink>
    </nav>
  </aside>
</template>

<style scoped>
.shop-sidebar {
  width: 200px;
  flex-shrink: 0;
  padding: 1.1rem 0.85rem;
  background: var(--surface-1);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.5rem 1rem;
  border-bottom: 1px solid var(--color-border);
  margin-bottom: 0.85rem;
}

.avatar-container {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid var(--color-border);
  background: var(--surface-2);
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-id {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--color-text);
}

.navigation-buttons {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-button {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  min-height: 44px;
  padding: 0.5rem 0.75rem;
  border-radius: var(--radius-md);
  color: var(--color-text);
  font-size: 0.9rem;
  font-weight: 500;
  text-decoration: none;
  transition:
    background-color 0.2s ease,
    color 0.2s ease;
}

.nav-button svg {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
  color: var(--color-text-secondary);
  transition: color 0.2s ease;
}

.nav-button:hover {
  background: var(--color-hover);
}

.nav-button:hover svg {
  color: var(--color-text);
}

.nav-button.router-link-active {
  background: var(--accent-muted);
  color: var(--color-link);
}

.nav-button.router-link-active svg {
  color: var(--color-link);
}

.nav-text {
  flex: 1;
}

@media (max-width: 768px) {
  .shop-sidebar {
    width: 180px;
  }
}
</style>
