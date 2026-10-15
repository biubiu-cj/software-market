<script lang="ts" setup>
import { useTemplateRef } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/userStore'

defineProps<{
  unreadNoticeNum: number
  unreadChatNum: number
}>()

const router = useRouter()
const route = useRoute()
const sinfo = useTemplateRef('sinfo')
const { userInfo } = useUserStore()
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
  <div class="site-header">
    <div class="header-inner">
      <nav class="links" aria-label="主导航">
        <router-link
          to="/"
          title="首页"
          active-class=""
          :class="{ 'router-link-active': route.name === 'Home' }"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
            <polyline points="9 22 9 12 15 12 15 22" />
          </svg>
          <span class="label">首页</span>
        </router-link>
        <router-link to="/post" title="发帖">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M12 20h9" />
            <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z" />
          </svg>
          <span class="label">发帖</span>
        </router-link>
        <router-link to="/partitions" title="分区">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <polygon points="12 2 2 7 12 12 22 7 12 2" />
            <polyline points="2 17 12 22 22 17" />
            <polyline points="2 12 12 17 22 12" />
          </svg>
          <span class="label">分区</span>
        </router-link>
        <router-link to="/course" title="课程专区">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z" />
            <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z" />
          </svg>
          <span class="label">课程专区</span>
        </router-link>
        <router-link to="/feedback" title="反馈">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
          </svg>
          <span class="label">反馈</span>
        </router-link>
        <router-link to="/shop" title="商城">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z" />
            <line x1="3" y1="6" x2="21" y2="6" />
            <path d="M16 10a4 4 0 0 1-8 0" />
          </svg>
          <span class="label">商城</span>
        </router-link>
        <router-link to="/heat" title="热榜">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <polyline points="23 6 13.5 15.5 8.5 10.5 1 18" />
            <polyline points="17 6 23 6 23 12" />
          </svg>
          <span class="label">热榜</span>
        </router-link>
        <router-link to="/rating" title="打分">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
          </svg>
          <span class="label">打分</span>
        </router-link>
      </nav>
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
      <nav class="links account" aria-label="账户">
        <RouterLink to="/save" title="收藏">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" />
          </svg>
          <span class="label">收藏</span>
        </RouterLink>
        <RouterLink to="/history" title="历史">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <circle cx="12" cy="12" r="10" />
            <polyline points="12 6 12 12 16 14" />
          </svg>
          <span class="label">历史</span>
        </RouterLink>
        <router-link to="/notice" title="通知">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
            <path d="M13.73 21a2 2 0 0 1-3.46 0" />
          </svg>
          <span class="label">通知</span>
          <span v-if="unreadNoticeNum" class="nav-badge">{{ unreadNoticeNum > 99 ? '99+' : unreadNoticeNum }}</span>
        </router-link>
        <router-link to="/chat" title="私信">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
          </svg>
          <span class="label">私信</span>
          <span v-if="unreadChatNum" class="nav-badge">{{ unreadChatNum > 99 ? '99+' : unreadChatNum }}</span>
        </router-link>
        <router-link to="/options" class="user-link" title="个人设置">
          <span class="user-name">{{ userInfo.name }}</span>
        </router-link>
      </nav>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.site-header {
  background: var(--header-bg);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  color: var(--color-text);
  border-bottom: 1px solid var(--header-border);
  box-shadow: var(--shadow-xs);
}

.header-inner {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  max-width: 74rem;
  min-height: 3.25rem;
  margin-inline: auto;
  padding-inline: clamp(0.75rem, 3vw, 1.5rem);
}

.links {
  display: flex;
  align-items: center;
  gap: 2px;

  a {
    position: relative;
    display: inline-flex;
    align-items: center;
    gap: 0.3rem;
    padding: 0.45em 0.6em;
    border-radius: var(--radius-md);
    color: var(--color-text);
    font-size: 0.875rem;
    font-weight: 500;
    white-space: nowrap;
    transition:
      background-color 0.2s ease,
      color 0.2s ease;
  }

  svg {
    width: 18px;
    height: 18px;
    flex-shrink: 0;
    color: var(--color-text-secondary);
    transition: color 0.2s ease;
  }

  a:hover {
    background: var(--color-hover);
    color: var(--color-text);
  }

  a.router-link-active {
    background: var(--accent-muted);
    color: var(--color-link);

    svg {
      color: var(--color-link);
    }
  }
}

.account a:hover svg {
  color: var(--color-text);
}

.user-link .user-name {
  max-width: 8rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 600;
  color: var(--accent);
}

.search {
  position: relative;
  display: flex;
  flex: 1;
  max-width: 30rem;
  margin-inline: auto;

  input {
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

  input::placeholder {
    color: var(--color-text-secondary);
  }

  input:focus {
    outline: none;
    border-color: var(--accent);
    background: var(--surface-1);
    box-shadow: 0 0 0 3px var(--accent-muted);
  }

  button {
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

  button:hover {
    background: var(--color-hover);
    color: var(--accent);
  }

  button svg {
    width: 16px;
    height: 16px;
  }
}

/* 中等宽度只显示图标，避免导航换行或溢出 */
@media (max-width: 1150px) {
  .links .label {
    display: none;
  }

  .links a {
    padding: 0.5em;
  }
}
</style>
