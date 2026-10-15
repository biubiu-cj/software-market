<script lang="ts" setup>
import { computed, useTemplateRef } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const sinfo = useTemplateRef('sinfo')
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
    // 如果先搜索一次然后返回，再次搜索时path会被URI编码，参见PartitionListView的sendPartition函数
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
const shopSidebarIsShow = computed(() => {
  return /^\/(?:myproducts|shop)/.test(route.fullPath)
})
</script>

<template>
  <div v-if="!shopSidebarIsShow" class="site-header">
    <div class="header-inner">
      <RouterLink
        to="/course"
        class="header-icon-link"
        title="课程专区"
        aria-label="课程专区"
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z" />
          <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z" />
        </svg>
      </RouterLink>
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
      <router-link
        to="/post"
        class="header-icon-link post-link"
        title="发帖"
        aria-label="发帖"
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M12 20h9" />
          <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z" />
        </svg>
      </router-link>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.site-header {
  height: 100%;
  background: var(--header-bg);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  color: var(--color-text);
  border-bottom: 1px solid var(--header-border);
  box-shadow: var(--shadow-xs);
  overflow: hidden;
  transition: height 0.3s ease;
}

.header-inner {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  height: 100%;
  padding-inline: 0.6rem;
}

.header-icon-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  flex-shrink: 0;
  border-radius: var(--radius-md);
  color: var(--color-text);
  transition:
    background-color 0.2s ease,
    color 0.2s ease;
}

.header-icon-link:hover,
.header-icon-link.router-link-active {
  background: var(--accent-muted);
  color: var(--accent);
}

.header-icon-link svg {
  width: 22px;
  height: 22px;
}

.post-link {
  color: var(--accent);
}

.search {
  position: relative;
  display: flex;
  flex: 1;
  min-width: 0;

  input {
    width: 100%;
    height: 2.4rem;
    padding: 0 2.4rem 0 0.9rem;
    border: 1px solid var(--color-border);
    border-radius: var(--radius-full);
    background: var(--surface-2);
    color: var(--color-text);
    font-size: 0.9rem;
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

  button svg {
    width: 16px;
    height: 16px;
  }
}
</style>
