<script setup lang="ts">
import type { Condition, Partitions } from '@/store/postStore'
import type { Post } from '@/types/post'
import {
  onActivated,
  onMounted,
  reactive,
  ref,
} from 'vue'
import { onBeforeRouteLeave, onBeforeRouteUpdate, useRoute, useRouter } from 'vue-router'
import { showMsg } from '@/components/MessageBox'
import PostList from '@/components/PostList.vue'
import { usePostView } from '@/composables/usePostView'
import { usePostStore } from '@/store/postStore'

defineOptions({
  name: 'PartitionView',
})

const name = ref('')
const route = useRoute()
const router = useRouter()
const { storePosts } = usePostStore()
const { posts, update, isLoading, hasMore, initialize } = usePostView()

const isDense = ref(false)

// 保存滚动位置
const scrollTop = ref(0)
const hasCache = ref(false)
const cachePosts = reactive<Post[]>([])
const cacheTotalNum = ref(0)
const cachePartition = ref(route.params.name)
const cacheCondition = reactive<Condition>({
  limit: 10,
  offset: 0,
  category: cachePartition.value as typeof Partitions[number],
  searchSort: 'home',
  searchInfo: '',
  tag: '',
})

onMounted(async () => {
  const params = route.params
  if (!('name' in params)) {
    router.push('/')
    showMsg('分区不存在')
    return
  }
  const partition = params.name as typeof Partitions[number]
  if (partition === '课程专区') {
    router.push('/course')
    return
  }
  if (partition === '课程交流')
    // 对于课程交流分区，采用更紧凑的卡片布局
    isDense.value = true
  if (partition === '打分')
    // 对于打分分区，采用更紧凑的卡片布局
    isDense.value = true
  name.value = partition
  if (!hasCache.value)
    await initialize(partition)
  hasCache.value = true
})

// 组件被激活时
onActivated(async () => {
  if (!hasCache.value)
    return
  // 始终重新拉取数据，避免缓存了已被删除的帖子
  await initialize(name.value as typeof Partitions[number])
})

onBeforeRouteUpdate(async () => {
  // 清空当前页面的帖子列表
  const data = storePosts()
  cachePosts.splice(0, cachePosts.length, ...data.cachePosts)
  cacheTotalNum.value = data.cacheTotalNum
  Object.assign(cacheCondition, data.cacheConditions)
})

onBeforeRouteLeave(async () => {
  // 保存滚动位置
  scrollTop.value = document.body.scrollTop
  // 清空当前页面的帖子列表
  const data = storePosts()
  cachePosts.splice(0, cachePosts.length, ...data.cachePosts)
  cachePartition.value = route.params.name
  cacheTotalNum.value = data.cacheTotalNum
  Object.assign(cacheCondition, data.cacheConditions)
})
</script>

<template>
  <div class="page-section w-full">
    <h2 class="page-heading">
      {{ name }}
    </h2>
    <PostList :posts="posts" :is-dense="isDense" :is-loading="isLoading" :has-more="hasMore" @bottom="update" />
  </div>
</template>
