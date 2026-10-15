<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import ShopSidebar from '@/components/ShopSidebar.vue'

const route = useRoute()
const isSidebarVisible = ref(false)

// 导航后自动收起抽屉
watch(() => route.fullPath, () => {
  isSidebarVisible.value = false
})

function toggleSidebar() {
  isSidebarVisible.value = !isSidebarVisible.value
}

function closeSidebar() {
  isSidebarVisible.value = false
}
</script>

<template>
  <button
    type="button"
    class="floating-ball"
    aria-label="打开商城菜单"
    title="商城菜单"
    @click="toggleSidebar"
  >
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
      <line x1="4" y1="7" x2="20" y2="7" />
      <line x1="4" y1="12" x2="20" y2="12" />
      <line x1="4" y1="17" x2="20" y2="17" />
    </svg>
  </button>
  <template v-if="isSidebarVisible">
    <div class="shop-drawer-overlay" @click="closeSidebar" />
    <div class="shop-drawer">
      <ShopSidebar @close="closeSidebar" />
    </div>
  </template>
</template>

<style scoped>
.floating-ball {
  position: fixed;
  right: 14px;
  bottom: calc(84px + env(safe-area-inset-bottom, 0px));
  width: 48px;
  height: 48px;
  margin: 0;
  padding: 0;
  border: none;
  border-radius: 50%;
  background: var(--accent);
  color: var(--accent-foreground);
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  box-shadow: var(--shadow-lg);
  z-index: 1200;
  transition:
    transform 0.2s ease,
    background-color 0.2s ease;
}

.floating-ball:hover {
  background: var(--accent-hover);
  transform: scale(1.05);
}

.floating-ball svg {
  width: 22px;
  height: 22px;
}

.shop-drawer-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(2px);
  z-index: 1290;
}

.shop-drawer {
  position: fixed;
  top: 0;
  bottom: 0;
  left: 0;
  width: min(78vw, 260px);
  padding: 0.75rem;
  background: var(--surface-1);
  border-right: 1px solid var(--color-border);
  box-shadow: var(--shadow-lg);
  z-index: 1300;
  overflow-y: auto;
  animation: drawer-in 0.2s ease;
}

.shop-drawer :deep(.shop-sidebar) {
  width: 100%;
  border: none;
  box-shadow: none;
  padding: 0.25rem 0;
  background: transparent;
}

@keyframes drawer-in {
  from {
    transform: translateX(-100%);
  }
  to {
    transform: translateX(0);
  }
}
</style>
