<script setup lang="ts">
import type { Post } from '@/types/post'
import { defineAsyncComponent, ref, useTemplateRef } from 'vue'
import { sendComment } from '@/api/editPostAndComment/editComment'
import { delPost } from '@/api/editPostAndComment/editPost'
import { submitReport } from '@/api/editPostAndComment/submitReport'

import { likePost, savePost } from '@/api/SaveAndLike/SaveAndLike'
import { showMsg } from '@/components/MessageBox'

import { useUserStore } from '@/store/userStore'

import { debounceAsync } from '@/utils/debounced'

import BasicInfo from '../BasicInfo.vue'
import MarkdownContainer from '../MarkdownContainer.vue'
import UserAvatar from '../UserAvatar.vue'
import UserButton from '../UserButton.vue'
import BasicCard from './BasicCard.vue'

const { post } = defineProps<{
  post: Post
}>()

const emits = defineEmits(['infoChange'])

const OldImages = defineAsyncComponent(() => import('@/components/OldImages.vue'))

const MarkdownEditor = defineAsyncComponent(
  () => import('../MarkdownEditor.vue'),
)

const commentButtonIsShow = ref(false)
const commentContent = ref('')
const reportReason = ref('')
const showReportInput = ref(false)

const { userInfo } = useUserStore()
const root = useTemplateRef('root')
/**
 * @description 发送评论
 */

async function sendCommentFunc() {
  const res = await sendComment(
    commentContent.value,
    Number(post.PostID),
  )
  if (res) {
    commentContent.value = ''
    commentButtonIsShow.value = false
    return true
  }
  else {
    return false
  }
}

const handlerDebounce = debounceAsync(handler)

async function handler(type: 'comment') {
  let event
  switch (type) {
    case 'comment':{
      const result = await sendCommentFunc()
      if (!result)
        return showMsg('评论失败，请稍后再试')
      event = new CustomEvent('comment-handle', {
        bubbles: true,
      })
      break
    }
    default:
      break
  }
  if (!event)
    return
  root.value?.dispatchEvent(event)
}

async function submitReportHandler() {
  const reason = reportReason.value.trim()
  if (!reason) {
    showMsg('请填写举报原因')
    return
  }
  try {
    await submitReport(Number(post.PostID), 'post', reason)
    showMsg('举报成功')
    reportReason.value = ''
    showReportInput.value = false
  }
  catch {
    showMsg('举报失败')
  }
}

const handleUserActionDebounce = debounceAsync(handleUserAction)

async function handleUserAction(type: 'delete' | 'save') {
  // 后端没有返回数据，不要赋值后再更新
  switch (type) {
    case 'save':{
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

/**
 * @description 点赞。
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

function useCustomEvent(type: 'delete' | 'save' | 'like') {
  emits('infoChange', type)
}
</script>

<template>
  <BasicCard class="w-15/16">
    <div ref="root" class="h-fit flex flex-row items-center">
      <UserAvatar
        :src="post.UserAvatar"
        :user-id="post.UserID"
        :user-identity="post.UserIdentity"
        :user-name="post.UserName"
        :user-score="post.UserScore"
      />
      <UserButton :is-saved="post.IsSaved" :is-self="post.UserID === userInfo.userID" @user-action="handleUserActionDebounce" />
    </div>
    <div class="extension-slot">
      <slot name="right-extension" />
    </div>
    <div
      class="card-title"
    >
      <h2>
        {{ post.Title || '' }}
      </h2>
    </div>
    <MarkdownContainer
      :markdown-content="post.Content || 'loading'"
    />
    <template v-if="post.Photos">
      <OldImages :photos="post.Photos" />
    </template>
    <BasicInfo :time="post.PostTime" :browse="post.Browse" :comment="post.Comment" :is-like="post.IsLiked" :like="post.Like" :no-deny="true" @like-change="like" />
    <div class="commentButton">
      <button v-if="post.UserID !== userInfo.userID" class="report-btn" @click="showReportInput = !showReportInput">
        举报
      </button>
      <button @click="commentButtonIsShow = !commentButtonIsShow">
        {{ commentButtonIsShow ? '隐藏' : '发评论' }}
      </button>
    </div>
    <div v-if="showReportInput" class="report-input">
      <input v-model="reportReason" placeholder="请填写举报原因" @keyup.enter="submitReportHandler">
      <button @click="submitReportHandler">
        提交举报
      </button>
    </div>
    <MarkdownEditor
      v-if="commentButtonIsShow"
      v-model="commentContent"
      class="max-w-full"
      @send="handlerDebounce('comment')"
    />
  </BasicCard>
</template>

<style>
/* 扩展插槽定位 */
.extension-slot {
  position: relative;
}

.card-title {
  margin-top: 10px;
  margin-bottom: 8px;
}

.card-title h2 {
  margin: 0;
  font-size: 1.3rem;
  font-weight: 700;
  letter-spacing: -0.01em;
  color: var(--color-text);
}

/* 评论按钮样式 - 与普通帖子保持一致 */
.commentButton {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 10px;
}

.commentButton button {
  min-height: 36px;
  min-width: 80px;
  padding: 6px 14px;
  margin: 0;
  border: 1px solid var(--accent);
  border-radius: var(--radius-md);
  background: var(--accent);
  color: var(--accent-foreground);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition:
    background-color 0.2s ease,
    border-color 0.2s ease,
    color 0.2s ease;
}

.commentButton button:hover {
  background: var(--accent-hover);
  border-color: var(--accent-hover);
  color: var(--accent-foreground);
}

.report-btn {
  background: var(--surface-1) !important;
  border-color: var(--color-border) !important;
  color: var(--color-text-secondary) !important;
}

.report-btn:hover {
  background: var(--surface-1) !important;
  border-color: color-mix(in srgb, var(--color-error) 55%, transparent) !important;
  color: var(--color-error) !important;
}

.report-input {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  justify-content: flex-end;
}

.report-input input {
  flex: 1;
  max-width: 300px;
  min-height: 36px;
  padding: 6px 10px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 14px;
  font-family: var(--font-sans);
  background: var(--surface-2);
  color: var(--color-text);
}

.report-input input:focus {
  outline: none;
  border-color: var(--accent);
  box-shadow: 0 0 0 3px var(--accent-muted);
}

.report-input button {
  min-height: 36px;
  background: var(--color-error);
  color: #fff;
  border: none;
  border-radius: var(--radius-md);
  padding: 6px 14px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.report-input button:hover {
  background: color-mix(in srgb, var(--color-error) 85%, #000);
  color: #fff;
}
</style>
