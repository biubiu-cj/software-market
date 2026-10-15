<script setup lang="ts">
import type { Post } from '@/types/post'
import { defineAsyncComponent } from 'vue'

import { delPost } from '@/api/editPostAndComment/editPost'
import { likePost, savePost } from '@/api/SaveAndLike/SaveAndLike'
import { showMsg } from '@/components/MessageBox'
import { usePostStore } from '@/store/postStore'
import { useUserStore } from '@/store/userStore'
import { debounceAsync } from '@/utils/debounced'

import BasicInfo from '../BasicInfo.vue'
import UserAvatar from '../UserAvatar.vue'
import UserButton from '../UserButton.vue'
import BasicCard from './BasicCard.vue'

const { isDense, post, isNew, noSave = false, showDelete = false } = defineProps<{
  isDense?: boolean
  post: Post
  isNew?: boolean
  noSave?: boolean
  showDelete?: boolean
}>()

const { updatePost } = usePostStore()
const OldImages = defineAsyncComponent(() => import('@/components/OldImages.vue'))

const { userInfo } = useUserStore()

/**
 * @description 点赞。懒得装防抖
 */
async function like() {
  // 后端没有返回数据，不要赋值后再更新
  try {
    await likePost(post.PostID)
    useCustomEvent('like')
  }
  catch (e) {
    showMsg('失败')
    console.error(e)
  }
}

const handleUserActionDebounce = debounceAsync(handleUserAction)
async function handleUserAction(type: 'delete' | 'save') {
  // 后端没有返回数据，不要赋值后再更新
  switch (type) {
    case 'save': {
      try {
        await savePost(
          post.PostID,
        )
        useCustomEvent('save')
      }
      catch (e) {
        console.error(e)
        showMsg('失败了:-(')
      }
      break
    }
    case 'delete': {
      try {
        await delPost(post.PostID)
        useCustomEvent('delete')
      }
      catch (error) {
        console.error(error)
        showMsg('删除失败，请稍后再试')
      }
    }
  }
}

function useCustomEvent(type: 'delete' | 'save' | 'like') {
  updatePost(post.PostID, type)
}
</script>

<template>
  <!-- 紧凑布局下不显示用户信息、时间等 -->
  <BasicCard :class="[isDense ? '' : 'min-h-37', { 'new-post': isNew }]">
    <div v-show="!isDense" class="h-fit flex flex-row items-start">
      <UserAvatar
        :src="post.UserAvatar"
        :user-id="post.UserID"
        :user-identity="post.UserIdentity"
        :user-name="post.UserName"
        :user-score="post.UserScore"
      />
      <UserButton :is-saved="post.IsSaved" :is-self="userInfo.userID === post.UserID" :no-save="noSave" :show-delete="showDelete" @user-action="handleUserActionDebounce" />
    </div>
    <!-- 添加具名插槽用于扩展内容 -->
    <div class="extension-slot">
      <slot name="right-extension" />
    </div>
    <RouterLink :to="`/postdetail/${post.PostID}`" class="post-link">
      <div class="post-title-row">
        <h3 class="post-title">
          {{ post.Title || '' }}
        </h3>
        <span v-if="isNew" class="new-chip">新</span>
      </div>
      <p v-show="!isDense" class="no-indent post-summary">
        {{ post.Content.slice(0, 15) || 'loading' }}
      </p>
      <template v-if="post.Photos">
        <OldImages :photos="post.Photos" />
      </template>
    </RouterLink>
    <div class="extension-slot">
      <slot name="center-extension" />
    </div>
    <BasicInfo
      :is-dense="isDense"
      :time="post.PostTime"
      :browse="post.Browse"
      :comment="post.Comment"
      :is-like="post.IsLiked"
      :like="post.Like"
      :no-deny="true"
      @like-change="like"
    />
  </BasicCard>
</template>

<style scoped>
.post-link {
  display: block;
  color: var(--color-text);
}

.post-link:hover {
  color: var(--color-text);
}

.post-title-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.4rem;
}

.post-title {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 600;
  line-height: 1.45;
  letter-spacing: -0.01em;
  color: var(--color-text);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.new-chip {
  flex-shrink: 0;
  padding: 1px 8px;
  border-radius: var(--radius-full);
  background: var(--accent-muted);
  color: var(--color-link);
  font-size: 0.7rem;
  font-weight: 700;
  line-height: 1.6;
}

.post-summary {
  margin: 0.45rem 0 0;
  font-size: 0.9rem;
  line-height: 1.65;
  color: var(--color-text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-summary::after {
  content: '...';
}

/* 新帖子：左侧主色描边 + 轻微底色 */
.card-root.new-post {
  border-left: 3px solid var(--accent);
  background: linear-gradient(to right, var(--accent-muted), transparent 4.5rem), var(--surface-1);
}

/* 扩展插槽定位 */
.extension-slot {
  position: relative;
}
</style>
