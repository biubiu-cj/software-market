<script lang="ts" setup>
import type { Ref } from 'vue'
import { computed, defineAsyncComponent, inject } from 'vue'
import { useRoute } from 'vue-router'

const ShopSidebar = defineAsyncComponent(
  () => import('@/components/ShopSidebar.vue'),
)

const route = useRoute()

const shopSidebarIsShow = computed(() => {
  return /^\/(?:myproducts|shop)/.test(route.fullPath)
})

const isPC = inject('isPC') as Ref<boolean>
</script>

<template>
  <div class="shop-layout">
    <div class="shop-layout-main">
      <RouterView />
    </div>
    <ShopSidebar v-if="isPC && shopSidebarIsShow" class="shop-layout-sidebar" />
  </div>
</template>

<style scoped>
.shop-layout {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  gap: 1.5rem;
  width: 100%;
  max-width: 74rem;
  margin-inline: auto;
  padding-inline: clamp(0.75rem, 3vw, 1.5rem);
  padding-top: 0.5rem;
}

.shop-layout-main {
  flex: 1;
  min-width: 0;
}

.shop-layout-sidebar {
  position: sticky;
  top: 5.5rem;
}

@media screen and (max-width: 768px) {
  .shop-layout {
    display: block;
    padding-top: 0.25rem;
  }
}
</style>
