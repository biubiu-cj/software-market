<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'

const { noticeNum = 0, chatNum = 0 } = defineProps<{
  noticeNum?: number
  chatNum?: number
}>()
const route = useRoute()
const selected = computed<string>(() => {
  const path = route.path
  const params = route.params
  if ('name' in params)
    return params.name as string
  return path
})

// 控制 "我的" 菜单的显示与隐藏
const myMenuVisible = ref(false)
function toggleMyMenu() {
  myMenuVisible.value = !myMenuVisible.value
}
</script>

<template>
  <div
    id="bottomNavBar"
    class="root bottom-nav-bar"
  >
    <div class="nav-inner">
      <router-link
        class="nav"
        :class="{ selected: selected === '/' }"
        to="/"
      >
        <span class="nav-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
            <polyline points="9 22 9 12 15 12 15 22" />
          </svg>
        </span>
        主页
      </router-link>
      <router-link
        id="rating"
        class="nav"
        :class="{ selected: selected === '/rating' }"
        to="/rating"
      >
        <span class="nav-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
          </svg>
        </span>
        打分
      </router-link>
      <router-link
        id="heat"
        class="nav"
        :class="{ selected: selected === '/heat' }"
        to="/heat"
      >
        <span class="nav-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <polyline points="23 6 13.5 15.5 8.5 10.5 1 18" />
            <polyline points="17 6 23 6 23 12" />
          </svg>
        </span>
        热榜
      </router-link>
      <router-link
        id="shop"
        class="nav"
        :class="{ selected: selected === '/shop' }"
        to="/shop"
      >
        <span class="nav-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z" />
            <line x1="3" y1="6" x2="21" y2="6" />
            <path d="M16 10a4 4 0 0 1-8 0" />
          </svg>
        </span>
        商城
      </router-link>

      <router-link
        id="notice"
        class="nav"
        :class="{ selected: selected === '/notice' }"
        to="/notice"
      >
        <span class="nav-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
            <path d="M13.73 21a2 2 0 0 1-3.46 0" />
          </svg>
          <span v-if="noticeNum" class="nav-badge">{{ noticeNum > 99 ? '99+' : noticeNum }}</span>
        </span>
        通知
      </router-link>
      <router-link
        id="chat"
        class="nav"
        :class="{ selected: selected === '/chat' }"
        to="/chat"
      >
        <span class="nav-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
          </svg>
          <span v-if="chatNum" class="nav-badge">{{ chatNum > 99 ? '99+' : chatNum }}</span>
        </span>
        私信
      </router-link>
      <!-- "我的" 下拉菜单 -->
      <div
        class="nav relative overflow-visible"
        :class="{ selected: ['/options', '/save', '/history', '/feedback'].includes(selected) }"
        role="button"
        tabindex="0"
        aria-label="我的"
        @click="toggleMyMenu"
        @keydown.enter.prevent="toggleMyMenu"
      >
        <span class="nav-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
            <circle cx="12" cy="7" r="4" />
          </svg>
        </span>
        我的
        <div
          v-show="myMenuVisible"
          class="my-dropdown"
        >
          <router-link
            class="dropdown-item"
            :class="{ selected: selected === '/options' }"
            to="/options"
          >
            个人信息
          </router-link>
          <router-link
            class="dropdown-item"
            :class="{ selected: selected === '/save' }"
            to="/save"
          >
            我的收藏
          </router-link>
          <router-link
            class="dropdown-item"
            :class="{ selected: selected === '/history' }"
            to="/history"
          >
            发帖历史
          </router-link>
          <router-link
            class="dropdown-item"
            :class="{ selected: selected === '/feedback' }"
            to="/feedback"
          >
            反馈
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.bottom-nav-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  width: 100%;
  z-index: 1000;
  background-color: var(--bottom-nav-bg);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border-top: 1px solid var(--header-border);
  border-radius: var(--radius-lg) var(--radius-lg) 0 0;
  box-shadow: 0 -6px 20px rgba(15, 23, 42, 0.06);
  padding-bottom: env(safe-area-inset-bottom, 0px);
  transform: translateZ(0);
}

body.dark-mode .bottom-nav-bar {
  box-shadow: 0 -6px 20px rgba(0, 0, 0, 0.35);
}

.nav-inner {
  display: flex;
  align-items: stretch;
  justify-content: space-around;
  min-height: 60px;
}

.nav {
  flex: 1;
  min-height: 44px;
  padding: 7px 4px 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  text-align: center;
  text-decoration: none;
  color: var(--color-text-secondary);
  position: relative;
  font-size: 0.7rem;
  font-weight: 600;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  transition: color 0.2s ease;
}

.nav:hover {
  color: var(--color-text);
}

.nav:active {
  background: var(--color-hover);
}

/* 图标外圈：选中时呈现轻量胶囊背景 */
.nav-icon {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 26px;
  border-radius: var(--radius-full);
  transition: background-color 0.2s ease;
}

.nav-icon svg {
  width: 20px;
  height: 20px;
}

.nav.selected {
  color: var(--accent);
}

.nav.selected .nav-icon {
  background: var(--accent-muted);
}

/* "我的" 菜单样式 */
.my-dropdown {
  flex-direction: column;
  display: flex;
  position: absolute;
  bottom: calc(100% + 10px);
  right: 6px;
  min-width: 132px;
  background-color: var(--surface-1);
  padding: 0.3rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-lg);
  z-index: 10;
}

.dropdown-item {
  padding: 10px 14px;
  text-align: center;
  color: var(--color-text);
  text-decoration: none;
  border-radius: var(--radius-sm);
  font-size: 0.85rem;
  user-select: none;
  -webkit-user-select: none;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  transition:
    background-color 0.15s ease,
    color 0.15s ease;
}

.dropdown-item:hover {
  background-color: var(--color-nav-hover);
  color: var(--color-text);
}

.dropdown-item.selected {
  background: var(--accent-muted);
  color: var(--color-link);
}
</style>
