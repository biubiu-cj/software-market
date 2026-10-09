<script setup lang="ts">
import {
  onMounted,
} from 'vue'

import PostList from '@/components/PostList.vue'
import { usePostView } from '@/composables/usePostView'

const { posts, hasMore, isLoading, update, initialize } = usePostView()

onMounted(() => {
  initialize('收藏')
})
</script>

<template>
  <div class="page-section w-full">
    <h2 class="page-heading">
      收藏
    </h2>
    <PostList :posts="posts" :is-loading="isLoading" :has-more="hasMore" @bottom="update" />
  </div>
</template>
