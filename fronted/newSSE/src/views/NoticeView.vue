<script setup lang="ts">
import type { Notice } from '@/api/notice/notice'
import { nextTick, onMounted, ref } from 'vue'

import { useRouter } from 'vue-router'
import { getNotices, readNotice } from '@/api/notice/notice'

import { showMsg } from '@/components/MessageBox'
import { useNoticeStore } from '@/store/noticeStore'
import { strHandler } from '@/utils/strHandler'

const router = useRouter()
const { noticeNum, refreshNoticeNum } = useNoticeStore()

const readPage = ref(true)
const noticesRead = ref<Notice[]>([])
const noticesUnread = ref<Notice[]>([])

/**
 * @description 标记通知为已读
 * @param noticeID 通知ID
 * @returns void
 */
async function readComment(noticeID: number) {
  const res = await readNotice(noticeID)
  if (res.status === 'success') {
    getNoticesFunc()
    await refreshNoticeNum()
    showMsg('success')
  }
}

async function readAll() {
  // 遍历未读通知，标记为已读
  const promises = noticesUnread.value.map(notice => readNotice(notice.noticeID))
  try {
    await Promise.all(promises)
    await refreshNoticeNum()
    showMsg('一键已读')
    nextTick(getNoticesFunc)
  }
  catch (error) {
    showMsg('失败')
    console.error('一键已读失败:', error)
  }
}

async function changeToPost(postID: number, noticeID: number) {
  await readComment(noticeID)
  setTimeout(() => {
    router.push(`/postdetail/${postID}`)
  }, 500)
}

async function getNoticesFunc() {
  const read = await getNotices(0, noticeNum.readTotalNum, 1)
  const unread = await getNotices(0, noticeNum.unreadTotalNum, 0)
  if ('noticeList' in read) {
    noticesRead.value = read.noticeList.filter(item => item.postID !== 0)
  }
  else {
    noticesRead.value = []
  }
  if ('noticeList' in unread) {
    noticesUnread.value = unread.noticeList
  }
  else {
    noticesUnread.value = []
  }
}

onMounted(async () => {
  // 处理在通知页面刷新/直接打开通知页面的情况
  if (!noticeNum.readTotalNum || !noticeNum.unreadTotalNum) {
    await refreshNoticeNum()
  }
  await getNoticesFunc()
})
</script>

<template>
  <div class="root page-section">
    <h2 class="page-heading">
      通知
    </h2>
    <div class="notice-toolbar">
      <div class="notice-tabs" role="tablist">
        <button
          type="button"
          class="notice-tab"
          :class="{ active: readPage }"
          role="tab"
          :aria-selected="readPage"
          @click="readPage = true"
        >
          未读通知
        </button>
        <button
          type="button"
          class="notice-tab"
          :class="{ active: !readPage }"
          role="tab"
          :aria-selected="!readPage"
          @click="readPage = false"
        >
          已读通知
        </button>
      </div>
      <button
        type="button"
        class="read-all-btn"
        :disabled="noticesUnread.length === 0"
        @click="readAll"
      >
        {{ noticesUnread.length === 0 ? '全读完了' : '一键已读' }}
      </button>
    </div>
    <!-- 未读界面 -->
    <div v-if="readPage" class="notice-list">
      <div
        v-for="notice in noticesUnread"
        :key="notice.noticeID"
        class="notice"
      >
        <div class="notice-head">
          <span class="notice-sender">{{ notice.senderName }}</span>
          <span class="notice-time">{{ strHandler('time', notice.time) }}</span>
        </div>
        <p class="notice-content">
          {{ notice.content }}
        </p>
        <div class="notice-actions">
          <button type="button" class="notice-btn" @click="readComment(notice.noticeID)">
            标记为已读
          </button>
          <button
            type="button"
            class="notice-btn primary"
            :disabled="notice.postID === 0"
            @click="changeToPost(notice.postID, notice.noticeID)"
          >
            {{ notice.postID === 0 ? '该评论已被删除' : '查看原帖' }}
          </button>
        </div>
      </div>
      <div v-if="noticesUnread.length === 0" class="empty-state">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
          <path d="M13.73 21a2 2 0 0 1-3.46 0" />
        </svg>
        <h3>没有未读通知</h3>
        <p>新的互动会出现在这里</p>
      </div>
    </div>
    <!-- 已读界面 -->
    <div v-else class="notice-list">
      <div
        v-for="notice in noticesRead"
        :key="notice.noticeID"
        class="notice"
      >
        <div class="notice-head">
          <span class="notice-sender">{{ notice.senderName }}</span>
          <span class="notice-time">{{ strHandler('time', notice.time) }}</span>
        </div>
        <p class="notice-content">
          {{ notice.content }}
        </p>
        <div class="notice-actions">
          <button type="button" class="notice-btn" @click="changeToPost(notice.postID, notice.noticeID)">
            查看原帖
          </button>
        </div>
      </div>
      <div v-if="noticesRead.length === 0" class="empty-state">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
          <path d="M13.73 21a2 2 0 0 1-3.46 0" />
        </svg>
        <h3>暂无已读通知</h3>
      </div>
    </div>
  </div>
</template>

<style scoped>
.root {
  width: 100%;
  color: var(--color-text);
  display: flex;
  flex-direction: column;
}

.notice-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  margin-bottom: 1rem;
  flex-wrap: wrap;
}

.notice-tabs {
  display: flex;
  gap: 2px;
  padding: 3px;
  background: var(--surface-2);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}

.notice-tab {
  min-height: 34px;
  padding: 0 1rem;
  margin: 0;
  border: none;
  border-radius: calc(var(--radius-md) - 3px);
  background: transparent;
  color: var(--color-text-secondary);
  font-size: 0.875rem;
  font-weight: 500;
  transition:
    background-color 0.2s ease,
    color 0.2s ease,
    box-shadow 0.2s ease;
}

.notice-tab:hover {
  color: var(--color-text);
  background: transparent;
}

.notice-tab.active {
  background: var(--surface-1);
  color: var(--accent);
  box-shadow: var(--shadow-xs);
}

.read-all-btn {
  min-height: 38px;
  padding: 0 1.1rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--surface-1);
  color: var(--color-text);
  font-size: 0.875rem;
  font-weight: 500;
  transition:
    border-color 0.2s ease,
    color 0.2s ease,
    background-color 0.2s ease;
}

.read-all-btn:hover:not(:disabled) {
  border-color: var(--accent);
  color: var(--accent);
  background: var(--surface-1);
}

.read-all-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.read-all-btn:disabled:hover {
  border-color: var(--color-border);
  color: var(--color-text);
  background: var(--surface-1);
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.notice {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--surface-1);
  box-shadow: var(--shadow-xs);
  padding: 0.9rem 1rem;
}

.notice-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 0.75rem;
  margin-bottom: 0.35rem;
}

.notice-sender {
  font-weight: 600;
  font-size: 0.9rem;
  color: var(--color-text);
}

.notice-time {
  font-size: 0.78rem;
  color: var(--color-text-secondary);
  white-space: nowrap;
}

.notice-content {
  margin: 0 0 0.7rem;
  color: var(--color-text-secondary);
  font-size: 0.9rem;
  line-height: 1.6;
  word-break: break-all;
  white-space: pre-wrap;
}

.notice-actions {
  display: flex;
  gap: 0.5rem;
  justify-content: flex-end;
  flex-wrap: wrap;
}

.notice-btn {
  min-height: 34px;
  padding: 0 0.9rem;
  margin: 0;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--surface-1);
  color: var(--color-text);
  font-size: 0.82rem;
  font-weight: 500;
  transition:
    border-color 0.2s ease,
    color 0.2s ease,
    background-color 0.2s ease;
}

.notice-btn:hover:not(:disabled) {
  border-color: var(--accent);
  color: var(--accent);
  background: var(--surface-1);
}

.notice-btn.primary {
  background: var(--accent);
  border-color: var(--accent);
  color: var(--accent-foreground);
}

.notice-btn.primary:hover:not(:disabled) {
  background: var(--accent-hover);
  border-color: var(--accent-hover);
  color: var(--accent-foreground);
}

.notice-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.notice-btn:disabled:hover {
  border-color: var(--color-border);
  color: var(--color-text);
  background: var(--surface-1);
}

.notice-btn.primary:disabled:hover {
  background: var(--accent);
  border-color: var(--accent);
  color: var(--accent-foreground);
}
</style>
