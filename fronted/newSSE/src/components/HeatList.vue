<script setup lang="ts">
import type { HeatPost } from '@/api/browse/getPost'

import { onMounted, shallowRef } from 'vue'
import { getHeatPosts } from '@/api/browse/getPost'
import { showMsg } from '@/components/MessageBox'

const heatPosts = shallowRef<HeatPost[]>([])

onMounted(async () => {
  try {
    heatPosts.value = await getHeatPosts()
  }
  catch (e) {
    showMsg(`获取热帖失败:${e}`)
  }
})
</script>

<template>
  <div
    class="root nav-bar heat"
  >
    <h2 id="heat" class="page-heading">
      热榜
    </h2>
    <router-link
      v-for="post in heatPosts"
      :key="post.PostID"
      class="nav"
      :to="`/postdetail/${post.PostID}`"
    >
      <span>
        {{ post.Title }}
      </span>
    </router-link>
  </div>
</template>

<style scoped lang="scss">
#heat.page-heading {
  text-align: center;
  border-bottom: none;
  padding-bottom: 0.35rem;
  margin-bottom: 1.25rem;
}

#heat::before {
  content: '🔥 ';
}

#heat::after {
  content: ' 🔥';
}

.nav-bar {
  display: flex;
  flex-direction: column;
}

.heat {
  text-align: center;
  width: 100%;
}

.nav {
  width: 100%;
  margin-top: 0.5rem;
  margin-bottom: 0.5rem;
  padding: 1rem 1.15rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--surface-1);
  box-shadow: var(--shadow-xs);
  text-align: center;
  text-decoration: none;
  color: var(--color-text);
  font-weight: 600;
  transition:
    background-color 0.2s ease,
    box-shadow 0.2s ease,
    border-color 0.2s ease,
    transform 0.15s ease;
  -webkit-tap-highlight-color: transparent;
}

.nav:hover {
  background: var(--accent-muted);
  border-color: rgba(13, 148, 136, 0.35);
  box-shadow: var(--shadow-sm);
  transform: translateY(-1px);
}

@media screen and (min-width: 768px) {
  .root {
    width: 100%;
    max-width: 28rem;
    margin-inline: auto;
  }

  .nav {
    margin-left: 0;
    margin-right: 0;
  }
}
</style>
