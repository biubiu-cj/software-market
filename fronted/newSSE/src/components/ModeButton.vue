<script lang="ts" setup>
import { ref } from 'vue'

const mode = ref(document.body.className)
function changeMode() {
  document.body.classList.toggle('dark-mode')
  mode.value = document.body.classList.contains('dark-mode')
    ? 'dark-mode'
    : 'light-mode'
  localStorage.setItem('mode', mode.value)
}

mode.value = localStorage.getItem('mode') || ''
document.body.className = mode.value
</script>

<template>
  <button
    type="button"
    class="icon-btn mode-select"
    :title="mode === 'dark-mode' ? '切换到浅色模式' : '切换到深色模式'"
    :aria-label="mode === 'dark-mode' ? '切换到浅色模式' : '切换到深色模式'"
    @click="changeMode"
  >
    <!-- 月亮：当前为深色模式 -->
    <svg
      v-if="mode === 'dark-mode'"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      stroke-width="2"
      stroke-linecap="round"
      stroke-linejoin="round"
      aria-hidden="true"
    >
      <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
    </svg>
    <!-- 太阳：当前为浅色模式 -->
    <svg
      v-else
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      stroke-width="2"
      stroke-linecap="round"
      stroke-linejoin="round"
      aria-hidden="true"
    >
      <circle cx="12" cy="12" r="4" />
      <path d="M12 2v2" />
      <path d="M12 20v2" />
      <path d="M4.93 4.93l1.41 1.41" />
      <path d="M17.66 17.66l1.41 1.41" />
      <path d="M2 12h2" />
      <path d="M20 12h2" />
      <path d="M6.34 17.66l-1.41 1.41" />
      <path d="M19.07 4.93l-1.41 1.41" />
    </svg>
  </button>
</template>

<style scoped lang="scss">
.mode-select {
  color: var(--color-text-secondary);
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
}

.mode-select:hover {
  color: var(--color-text);
}

.mode-select svg {
  width: 20px;
  height: 20px;
}
</style>
