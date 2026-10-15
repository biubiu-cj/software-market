<script setup lang="ts">
import type { Post } from '@/types/post'

import { onUnmounted, useTemplateRef, watch } from 'vue'
import PostCard from './card/PostCard.vue'
import RatingCard from './rating/RatingCard.vue'

const { isDense, posts = [], isLoading = false, hasMore = true, newPostIds = [], postType = 'default', showDelete = false } = defineProps<{
  isDense?: boolean
  posts?: Post[]
  isLoading?: boolean
  hasMore?: boolean
  newPostIds?: number[]
  postType?: 'post' | 'rating'
  showDelete?: boolean
}>()

const emits = defineEmits(['bottom'])

// 直接根据 postType 选择组件
const cardComponent = postType === 'rating' ? RatingCard : PostCard

const bottom = useTemplateRef('bottom')
let observer: IntersectionObserver

function startObserver() {
  observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          emits('bottom')
        }
      })
    },
    {
      root: null,
      rootMargin: '0px',
      threshold: 0.5,
    },
  )
  if (bottom.value) {
    observer.observe(bottom.value)
  }
}

function endObserver() {
  if (observer) {
    observer.disconnect()
  }
}

watch(
  () => bottom.value,
  (newValue) => {
    if (newValue) {
      endObserver()
      startObserver()
    }
  },
  { immediate: true },
)

onUnmounted(() => {
  endObserver()
})
</script>

<template>
  <div class="w-full">
    <component
      :is="cardComponent"
      v-for="post in posts"
      :key="post.PostID"
      class="mb-3 w-full"
      :is-dense="isDense"
      :is-new="newPostIds.includes(post.PostID)"
      :post="post"
      :show-delete="showDelete"
    />
    <template v-if="hasMore">
      <div
        v-if="!isLoading"
        ref="bottom"
        class="list-hint"
      >
        <span class="spinner" aria-hidden="true" />
        加载中…
      </div>
    </template>
    <template v-else>
      <div v-if="posts.length > 0" class="list-hint">
        已经到底啦
      </div>
    </template>
    <div v-if="!isLoading && !hasMore && posts.length === 0" class="empty-state">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
      </svg>
      <h3>这里还什么都没有</h3>
      <p>{{ postType === 'rating' ? '还没有打分内容，去发一条吧' : '换个分区或稍后再来看看' }}</p>
    </div>
  </div>
</template>

<style scoped lang="scss">
.list-enter-active,
.list-leave-active {
  opacity: 1;
  transition: all 0.5s ease;
}
.list-enter-from,
.list-leave-to {
  opacity: 0;
}
</style>
