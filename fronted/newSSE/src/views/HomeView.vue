<script setup lang="ts">
import type { Condition } from '@/store/postStore'

import type { Post } from '@/types/post'
import {
  onActivated,
  onMounted,
  reactive,
  ref,
  watch,
} from 'vue'
import { onBeforeRouteLeave, useRoute } from 'vue-router'
import PostList from '@/components/PostList.vue'
import { usePostView } from '@/composables/usePostView'
import { useNewPostsStore } from '@/store/newPostsStore'
import { usePostStore } from '@/store/postStore'

defineOptions({
  name: 'HomeView',
})
const route = useRoute()
const { restorePosts, storePosts } = usePostStore()
const { posts, isLoading, hasMore, update, initialize } = usePostView()
const { initNewPostsCache, newPostsNotification } = useNewPostsStore()
// 保存滚动位置
const scrollTop = ref(0)
const hasCache = ref(false)
const cachePosts = reactive<Post[]>([])
const cacheTotalNum = ref(0)
const cacheCondition = reactive<Condition>({
  limit: 10,
  offset: 0,
  category: '主页',
  searchSort: 'home',
  searchInfo: '',
  tag: '',
})

onMounted(async () => {
  await initialize('主页')
  await initNewPostsCache() // 初始化新帖子缓存
  hasCache.value = true
})

// 组件被激活时
onActivated(async () => {
  if (!hasCache.value)
    return

  // 始终重新拉取数据，避免缓存了已被删除的帖子
  await initialize('主页')
  await initNewPostsCache()
})

onBeforeRouteLeave(() => {
  // 保存滚动位置
  scrollTop.value = document.body.scrollTop
  // 清空当前页面的帖子列表
  const data = storePosts()
  cachePosts.splice(0, cachePosts.length, ...data.cachePosts)
  cacheTotalNum.value = data.cacheTotalNum
  Object.assign(cacheCondition, data.cacheConditions)
})

// 监听路由参数变化，当有refresh参数时重新加载数据
watch(() => route.query.refresh, async (newRefresh) => {
  if (newRefresh && hasCache.value) {
    // 重新加载最新数据
    await initialize('主页')
    await initNewPostsCache() // 重新初始化缓存

    // 清除refresh参数，避免重复触发
    const newQuery = { ...route.query }
    delete newQuery.refresh
    // 使用replace避免在历史记录中留下refresh参数
    const queryString = Object.keys(newQuery).length > 0 ? `?${new URLSearchParams(newQuery as Record<string, string>).toString()}` : ''
    window.history.replaceState({}, '', `${route.path}${queryString}`)
  }
})
</script>

<template>
  <div class="page-section w-full">
    <h2 class="page-heading">
      主页
    </h2>
    <PostList
      :posts="posts"
      :is-loading="isLoading"
      :has-more="hasMore"
      :new-post-ids="newPostsNotification.newPostIds"
      @bottom="update"
    />
  </div>
</template>
