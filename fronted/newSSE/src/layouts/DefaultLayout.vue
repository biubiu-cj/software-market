<script setup lang="ts">
import {
  computed,
  defineAsyncComponent,
  onMounted,
  onUnmounted,
  provide,
  ref,
  watch,
} from 'vue'

import { useRoute, useRouter } from 'vue-router'

import ModeButton from '@/components/ModeButton.vue'
import { useChat } from '@/composables/useChat'
import { usePWA } from '@/composables/usePWA'
import { useNewPostsStore } from '@/store/newPostsStore'
import { useNoticeStore } from '@/store/noticeStore'
import { showSnackbar } from '@/utils/snackbar'

const BottomNavbar = defineAsyncComponent(
  () => import('@/components/BottomNavbar.vue'),
)
const PcHeader = defineAsyncComponent(
  () => import('@/components/PcHeader.vue'),
)
const PwaPcHeader = defineAsyncComponent(
  () => import('@/components/PwaPcHeader.vue'),
)

const PartitionList = defineAsyncComponent(() => import('@/components/PartitionList.vue'))
const MobileHeader = defineAsyncComponent(() => import('@/components/MobileHeader.vue'))
const route = useRoute()
const router = useRouter()

const { noticeNum, refreshNoticeNum } = useNoticeStore()
const { startPolling, stopPolling, newPostsNotification, hideNotification } = useNewPostsStore()

const { connect, disconnect, chatNum, updateChatNum } = useChat()
const { desktopExperienceEnabled } = usePWA()

const windowWidth = ref(window.innerWidth)
const isPC = computed(() => {
  return windowWidth.value > 768
})
provide('isPC', isPC)

const isDesktopExperience = computed(() => isPC.value && desktopExperienceEnabled.value)

// PWA/电脑端菜单状态管理
const pwaMenuOpen = ref(true)

// 计算是否应该折叠菜单
const isMenuCollapsed = computed(() => {
  if (!isDesktopExperience.value) {
    return false
  }
  // 菜单关闭时返回true（折叠状态）
  return !pwaMenuOpen.value
})

// 新增：输入框焦点状态
const isAnyInputFocused = ref(false)
provide('isAnyInputFocused', isAnyInputFocused)

function updateWidth() {
  windowWidth.value = window.innerWidth
}

const isHomePage = computed(() => {
  const url = route.path.split('/')[1]
  const allows = ['', 'partition']
  if (allows.includes(url)) {
    return true
  }
  return false
})

const { headerHeight, handleTouchStart, handleTouchEnd } = (() => {
  const headerHeight = ref('3em')
  let startY = 0
  let endY = 0
  const handleTouchStart = (event: TouchEvent) => {
    startY = event.touches[0].clientY
  }
  const handleTouchEnd = (event: TouchEvent) => {
    endY = event.changedTouches[0].clientY
    if (Math.abs(startY - endY) < 10)
      return
    if (startY > endY) {
      headerHeight.value = '0'
    }
    else {
      // 如果当前在发帖页面，保持收起状态
      if (route.path === '/post') {
        headerHeight.value = '0'
      }
      else {
        headerHeight.value = '3em'
      }
    }
  }

  return {
    headerHeight,
    handleTouchStart,
    handleTouchEnd,
  }
})()

// 监听路由变化，在/post路由时自动收起header
watch(() => route.path, (newPath) => {
  if (newPath === '/post') {
    headerHeight.value = '0'
  }
  else {
    headerHeight.value = '3em'
  }
}, { immediate: true })

onMounted(() => {
  connect()
  refreshNoticeNum()
  updateChatNum()
  window.addEventListener('resize', updateWidth)

  // 导入模块并注册
  import('@/utils/focusHandler').then(({ setupFocusHandlers }) => {
    const _ = setupFocusHandlers(
      () => { isAnyInputFocused.value = true },
      () => { isAnyInputFocused.value = false },
    )
    // 可选：提供给子组件使用
    // provide('focusCleanup', _);
  })

  if (!isPC.value) {
    window.addEventListener('touchstart', handleTouchStart)
    window.addEventListener('touchend', handleTouchEnd)
  }
  // 启动新帖子轮询
  startPolling()
})

onUnmounted(() => {
  window.removeEventListener('resize', updateWidth)

  if (!isPC.value) {
    window.removeEventListener('touchstart', handleTouchStart)
    window.removeEventListener('touchend', handleTouchEnd)
  }
  // 停止新帖子轮询
  stopPolling()
  disconnect()
})

// 监听新帖子通知变化，显示Snackbar
watch(() => newPostsNotification.updatedAt, (updatedAt: number) => {
  if (updatedAt > 0 && newPostsNotification.isVisible) {
    const isCurrentlyOnHomePage = route.path === '/' || route.path === '/partition'

    showSnackbar({
      id: 'new-posts-notification',
      message: `有 ${newPostsNotification.newPostIds.length} 条未查看的新帖子`,
      actionText: isCurrentlyOnHomePage ? '去查看' : '去首页查看',
      type: 'info',
      position: isPC.value ? 'top' : 'bottom',
      alwaysShow: true,
      onAction: () => {
        hideNotification()
        router.push({ path: '/', query: { refresh: Date.now() } })
      },
      onClose: () => {
        hideNotification()
      },
    })
  }
})
</script>

<template>
  <div class="root w-full" :class="{ 'pwa-mode': isDesktopExperience }">
    <header>
      <div class="site-top">
        <div class="site-top-inner">
          <div class="top-left" />
          <RouterLink class="title" to="/">
            <span class="brand-mark" aria-hidden="true">S</span>
            <span class="brand-text">SSE MARKET</span>
          </RouterLink>
          <div class="top-right">
            <ModeButton />
          </div>
        </div>
      </div>
      <template v-if="isPC">
        <PwaPcHeader v-if="isDesktopExperience" v-model:menu-open="pwaMenuOpen" :unread-chat-num="chatNum" :unread-notice-num="noticeNum.unreadTotalNum" />
        <PcHeader v-else :unread-chat-num="chatNum" :unread-notice-num="noticeNum.unreadTotalNum" />
      </template>
      <template v-else>
        <MobileHeader :style="{ height: headerHeight }" />
      </template>
    </header>
    <template v-if="!isPC">
      <BottomNavbar
        v-show="!isAnyInputFocused"
        :notice-num="noticeNum.unreadTotalNum"
        :chat-num="chatNum"
      />
    </template>
    <main class="mt-2 w-full flex flex-row p-0">
      <div class="content" :class="{ 'menu-collapsed': isMenuCollapsed }" :data-menu-collapsed="isMenuCollapsed">
        <template v-if="!isPC && isHomePage">
          <PartitionList />
        </template>
        <router-view v-slot="{ Component }">
          <KeepAlive include="HomeView,PartitionView">
            <component :is="Component" :key="route.fullPath" />
          </KeepAlive>
        </router-view>
      </div>
    </main>
  </div>
</template>

<style lang="scss" scoped>
header {
  position: sticky;
  top: 0;
  width: 100%;
  z-index: 1999;
}

.site-top {
  background: var(--header-bg);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--header-border);
}

.site-top-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
  max-width: 74rem;
  min-height: 2.75rem;
  margin-inline: auto;
  padding-inline: clamp(0.75rem, 3vw, 1.5rem);
}

.title {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.25rem 0.25rem;
  border-radius: var(--radius-md);
  color: var(--color-text);
  font-family: var(--font-sans);
  font-size: 0.95rem;
  font-weight: 800;
  letter-spacing: 0.14em;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
}

.title:hover {
  color: var(--accent);
}

.brand-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 1.5rem;
  height: 1.5rem;
  border-radius: 7px;
  background: var(--accent);
  color: #fff;
  font-size: 0.8rem;
  font-weight: 800;
  letter-spacing: 0;
}

.top-left,
.top-right {
  display: flex;
  align-items: center;
}

@media screen and (max-width: 767px) {
  .top-left,
  .top-right {
    flex: 1;
  }

  .top-right {
    justify-content: flex-end;
  }

  .title {
    justify-content: center;
  }
}

@media screen and (min-width: 768px) {
  .top-left {
    display: none;
  }
}

.content {
  width: 100%;
  padding-bottom: calc(88px + env(safe-area-inset-bottom, 0px));
  display: flex;
  flex-direction: column;
  align-items: center;
  transition:
    margin-left 0.3s ease,
    width 0.3s ease;
}

/* 大屏幕样式 >768px：底部导航不显示，只留出到页面底部的呼吸空间 */
@media screen and (min-width: 768px) {
  .content {
    padding-bottom: 2.5rem;
  }
}

/* PWA模式样式 */
.root.pwa-mode {
  position: relative;
}

.root.pwa-mode .content {
  transition:
    margin-left 0.3s ease,
    width 0.3s ease;
}

/* 移动端不受PWA模式影响 */
@media screen and (max-width: 767px) {
  .root.pwa-mode .content:not(.menu-collapsed),
  .root.pwa-mode .content.menu-collapsed {
    margin-left: 0;
    width: 100%;
  }
}

/* PC端PWA模式样式 */
@media screen and (min-width: 768px) {
  .root.pwa-mode .content:not(.menu-collapsed) {
    margin-left: 280px;
    width: calc(100% - 280px);
  }

  .root.pwa-mode .content.menu-collapsed {
    margin-left: 0;
    width: 100%;
  }
}

.root.pwa-mode .site-top {
  position: relative;
  z-index: 2001;
}
</style>
