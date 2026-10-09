<script setup lang="ts">
import type { Tag } from '@/api/info/getTeacher'

import {
  onMounted,
  reactive,
  ref,
  watch,
} from 'vue'
import { getTeachers } from '@/api/info/getTeacher'
import PostList from '@/components/PostList.vue'
import { usePostView } from '@/composables/usePostView'
import { usePostStore } from '@/store/postStore'

const tag = ref('')
const teachers = reactive<Tag[]>([])

const { changeTo, refreshPosts } = usePostStore()
const { posts, isLoading, hasMore, initialize, update } = usePostView()

onMounted(async () => {
  await initialize('课程专区')
  const resp = await getTeachers()
  teachers.splice(0, teachers.length, ...resp)
})
watch(tag, (newTag) => {
  refreshPosts()
  changeTo('课程专区', newTag)
  hasMore.value = true
})
</script>

<template>
  <div class="page-section w-full">
    <h2 class="page-heading">
      课程专区
    </h2>
    <div>
      <span class="gradientUnderline">请选择你的老师，不选也没关系 </span>
      <select
        id="teacher"
        v-model="tag"
      >
        <option
          v-for="teacher in teachers"
          :key="teacher.TagID"
          :value="teacher.Name"
        >
          {{ teacher.Name }}
        </option>
      </select>
    </div>
    <PostList :posts="posts" :is-loading="isLoading" :has-more="hasMore" @bottom="update" />
  </div>
</template>

<style scoped lang="scss">
.gradientUnderline {
  display: inline-block;
  margin-bottom: 0.75rem;
  font-size: 0.95rem;
  color: var(--shop-text-secondary);
}

select {
  margin: 0.25rem 0.5rem 0.25rem 0;
  padding: 0.45rem 0.65rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  background: var(--surface-1);
  color: var(--color-text);
  font-family: var(--font-sans);
  font-size: 0.9rem;
}

select:focus {
  outline: none;
  border-color: var(--accent);
  box-shadow: 0 0 0 3px var(--accent-muted);
}
</style>
