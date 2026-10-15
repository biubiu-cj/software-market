<script lang="ts" setup>
import { computed, useTemplateRef } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/userStore'

const props = defineProps<{
  unreadNoticeNum: number
  unreadChatNum: number
  menuOpen?: boolean
}>()

const emit = defineEmits<{
  'update:menuOpen': [value: boolean]
}>()

const router = useRouter()
const sinfo = useTemplateRef('sinfo')
const { userInfo } = useUserStore()
const defaultAvatar = `${import.meta.env.BASE_URL}default-avatar.svg`

const isMenuOpen = computed({
  get: () => props.menuOpen ?? true,
  set: (value: boolean) => emit('update:menuOpen', value),
})

// 检查是否可以返回上一页
const canGoBack = computed(() => {
  const currentPath = router.currentRoute.value.path
  if (currentPath !== '/') {
    return true
  }
  return window.history.length > 1
})

function toggleMenu() {
  isMenuOpen.value = !isMenuOpen.value
}

function goBack() {
  if (canGoBack.value) {
    router.back()
  }
}

function search() {
  const el = sinfo.value as HTMLInputElement
  if (!el || !el.value)
    return
  if (el.value === '年度报告') {
    router.push('/annual2025')
    el.value = ''
    return
  }
  const currentRoute = router.currentRoute.value
  if (currentRoute.path === '/partition/课程交流'
    // 如果先搜索一次然后返回，再次搜索时path会被URI编码
    || currentRoute.path === `/partition/${encodeURIComponent('课程交流')}`
    || currentRoute?.query?.partition === '课程交流'
    || currentRoute?.query?.partition === encodeURIComponent('课程交流')) {
    router.push(`/search?sinfo=${encodeURIComponent(el.value)}&partition=课程交流`)
  }
  else {
    router.push(`/search?sinfo=${encodeURIComponent(el.value)}`)
  }
  el.value = ''
}
</script>

<template>
  <!-- PWA模式下的常驻侧边栏菜单 -->
  <div class="pwa-sidebar" :class="{ collapsed: !isMenuOpen }">
    <div class="sidebar-links">
      <router-link to="/" :class="{ 'router-link-active': router.currentRoute.value.name === 'Home' }" active-class="">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
          <polyline points="9 22 9 12 15 12 15 22" />
        </svg>
        <span>首页</span>
      </router-link>
      <router-link to="/post">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M12 20h9" />
          <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z" />
        </svg>
        <span>发帖</span>
      </router-link>
      <router-link to="/partitions">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <polygon points="12 2 2 7 12 12 22 7 12 2" />
          <polyline points="2 17 12 22 22 17" />
          <polyline points="2 12 12 17 22 12" />
        </svg>
        <span>分区</span>
      </router-link>
      <router-link to="/course">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z" />
          <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z" />
        </svg>
        <span>课程专区</span>
      </router-link>
      <router-link to="/feedback">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
        </svg>
        <span>反馈</span>
      </router-link>
      <router-link to="/shop">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z" />
          <line x1="3" y1="6" x2="21" y2="6" />
          <path d="M16 10a4 4 0 0 1-8 0" />
        </svg>
        <span>闲置</span>
      </router-link>
      <router-link to="/rating">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
        </svg>
        <span>打分</span>
      </router-link>
      <div class="sidebar-divider" />
      <router-link to="/save">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" />
        </svg>
        <span>收藏</span>
      </router-link>
      <router-link to="/history">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <circle cx="12" cy="12" r="10" />
          <polyline points="12 6 12 12 16 14" />
        </svg>
        <span>历史</span>
      </router-link>
      <router-link to="/notice">
        <span class="icon-wrapper">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
            <path d="M13.73 21a2 2 0 0 1-3.46 0" />
          </svg>
          <span v-if="unreadNoticeNum" class="nav-badge">{{ unreadNoticeNum > 99 ? '99+' : unreadNoticeNum }}</span>
        </span>
        <span>通知</span>
      </router-link>
      <div class="sidebar-bottom">
        <router-link to="/chat">
          <span class="icon-wrapper">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
            </svg>
            <span v-if="unreadChatNum" class="nav-badge">{{ unreadChatNum > 99 ? '99+' : unreadChatNum }}</span>
          </span>
          <span>私信</span>
        </router-link>
        <router-link to="/options">
          <span class="user-avatar-icon">
            <img :src="userInfo.avatarURL || defaultAvatar" alt="用户头像">
          </span>
          <span>{{ userInfo.name }}</span>
        </router-link>
      </div>
    </div>
  </div>

  <!-- PWA模式下的搜索Header -->
  <div class="site-header pwa-header" :class="{ 'menu-collapsed': !isMenuOpen }">
    <button type="button" class="header-action" aria-label="展开或收起侧边栏" title="展开或收起侧边栏" @click="toggleMenu">
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M3 12H21M3 6H21M3 18H21" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
      </svg>
    </button>
    <button type="button" class="header-action" aria-label="返回上一页" title="返回上一页" :disabled="!canGoBack" @click="goBack">
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
      </svg>
    </button>
    <div class="search-container">
      <form class="search" role="search" @submit.prevent="search">
        <input
          id="sinfo"
          ref="sinfo"
          name="sinfo"
          type="search"
          placeholder="搜索帖子…"
          aria-label="搜索帖子"
        >
        <button type="submit" aria-label="搜索" title="搜索">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <circle cx="11" cy="11" r="8" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
        </button>
      </form>
    </div>
  </div>
</template>

<style lang="scss" scoped>
/* PWA模式侧边栏（从品牌条+搜索行下方开始，避免首项被搜索行遮住） */
.pwa-sidebar {
  position: fixed;
  top: calc(2.75rem + 3.25rem);
  left: 0;
  width: 280px;
  height: calc(100vh - 2.75rem - 3.25rem);
  background: var(--surface-1);
  border-right: 1px solid var(--header-border);
  box-shadow: var(--shadow-sm);
  z-index: 1998;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  transition: transform 0.3s ease;
}

.pwa-sidebar.collapsed {
  transform: translateX(-100%);
}

.sidebar-links {
  flex: 1;
  padding: 0.6rem 0.6rem;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.sidebar-bottom {
  margin-top: auto;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.sidebar-links a {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.6rem 0.75rem;
  min-height: 44px;
  border-radius: var(--radius-md);
  text-decoration: none;
  color: var(--color-text);
  font-size: 0.9rem;
  font-weight: 500;
  transition:
    background-color 0.2s ease,
    color 0.2s ease;
}

.sidebar-links a:hover {
  background: var(--color-hover);
}

.sidebar-links a.router-link-active:not([active-class='']) {
  background: var(--accent-muted);
  color: var(--color-link);
}

.sidebar-links a svg {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  color: var(--color-text-secondary);
}

.sidebar-links a.router-link-active:not([active-class='']) svg {
  color: var(--color-link);
}

.sidebar-links a .icon-wrapper,
.sidebar-links a .user-avatar-icon {
  position: relative;
  flex: 0 0 auto;
}

.sidebar-links a .icon-wrapper {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.sidebar-links a .user-avatar-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sidebar-links a .user-avatar-icon img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 文本标签：占满剩余宽度并省略溢出 */
.sidebar-links a > span:not(.icon-wrapper):not(.user-avatar-icon) {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sidebar-divider {
  height: 1px;
  background: var(--color-border);
  margin: 0.5rem 0.75rem;
}

/* PWA搜索Header */
.site-header.pwa-header {
  display: flex;
  background: var(--header-bg);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  min-height: 3.25rem;
  color: var(--color-text);
  align-items: center;
  padding: 0 0.75rem;
  border-bottom: 1px solid var(--header-border);
  box-shadow: var(--shadow-xs);
  width: 100%;
  justify-content: flex-start;
  gap: 0.4rem;
  position: relative;
  z-index: 1999;
}

.header-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  margin: 0;
  padding: 0;
  flex-shrink: 0;
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--color-text-secondary);
  transition:
    background-color 0.2s ease,
    color 0.2s ease;
}

.header-action:hover:not(:disabled) {
  background: var(--color-hover);
  color: var(--color-text);
}

.header-action:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.site-header.pwa-header .search-container {
  display: flex;
  align-items: center;
  flex: 1;
  margin-left: 280px;
  transition: margin-left 0.3s ease;
  justify-content: center;
}

.site-header.pwa-header.menu-collapsed .search-container {
  margin-left: 0;
}

.site-header.pwa-header .search {
  position: relative;
  display: flex;
  flex: 1;
  max-width: 30rem;
}

.site-header.pwa-header .search input {
  width: 100%;
  height: 2.35rem;
  padding: 0 2.5rem 0 0.95rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  background: var(--surface-2);
  color: var(--color-text);
  font-size: 0.875rem;
  font-family: var(--font-sans);
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    background-color 0.2s ease;
}

.site-header.pwa-header .search input::placeholder {
  color: var(--color-text-secondary);
}

.site-header.pwa-header .search input:focus {
  outline: none;
  border-color: var(--accent);
  background: var(--surface-1);
  box-shadow: 0 0 0 3px var(--accent-muted);
}

.site-header.pwa-header .search button {
  position: absolute;
  top: 50%;
  right: 3px;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  justify-content: center;
  width: 1.9rem;
  height: 1.9rem;
  margin: 0;
  padding: 0;
  border-radius: 50%;
  background: transparent;
  color: var(--color-text-secondary);
}

.site-header.pwa-header .search button:hover {
  background: var(--color-hover);
  color: var(--accent);
}

.site-header.pwa-header .search button svg {
  width: 16px;
  height: 16px;
}
</style>
