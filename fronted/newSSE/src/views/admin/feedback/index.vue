<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getAllFeedback, resolveFeedback } from '@/api/admin'
import type { FeedbackVO } from '@/api/admin'
import { showMsg, showConfirm } from '@/components/MessageBox'

const loading = ref(false)
const feedbacks = ref<FeedbackVO[]>([])
const actingId = ref<number | null>(null)
const filterStatus = ref<string>('all')

const filteredFeedbacks = computed(() => {
  if (filterStatus.value === 'all') return feedbacks.value
  return feedbacks.value.filter(f => f.status === filterStatus.value)
})

async function fetchFeedbacks() {
  loading.value = true
  try {
    const res = await getAllFeedback()
    if (res.code === 200) {
      feedbacks.value = res.data.feedbacks
    } else {
      showMsg(res.msg || '获取反馈失败')
    }
  } catch {
    showMsg('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

async function handleResolve(fb: FeedbackVO) {
  const confirmed = await showConfirm('确认将该反馈标记为「已处理」吗？')
  if (!confirmed) return

  actingId.value = fb.feedbackId
  try {
    const res = await resolveFeedback(fb.feedbackId)
    if (res.code === 200) {
      showMsg('反馈已处理')
      feedbacks.value = feedbacks.value.filter(f => f.feedbackId !== fb.feedbackId)
    } else {
      showMsg(res.msg || '操作失败')
    }
  } catch {
    showMsg('网络错误，请稍后重试')
  } finally {
    actingId.value = null
  }
}

function formatTime(time: string): string {
  if (!time) return '-'
  const d = new Date(time)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

onMounted(fetchFeedbacks)
</script>

<template>
  <div class="feedback-page">
    <!-- Page Header -->
    <div class="page-top">
      <div>
        <h1 class="page-top__title">反馈管理</h1>
        <p class="page-top__desc">查看和处理用户提交的反馈</p>
      </div>
      <button class="refresh-btn" :disabled="loading" @click="fetchFeedbacks">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
          :class="{ 'refresh-btn__spin': loading }">
          <polyline points="23,4 23,10 17,10"/>
          <polyline points="1,20 1,14 7,14"/>
          <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
        </svg>
        {{ loading ? '刷新中...' : '刷新' }}
      </button>
    </div>

    <!-- Filter Bar -->
    <div class="filter-bar">
      <button
        v-for="opt in [{ v: 'all', l: '全部' }, { v: 'wait', l: '待处理' }, { v: 'ok', l: '已处理' }]"
        :key="opt.v"
        class="filter-chip"
        :class="{ 'filter-chip--active': filterStatus === opt.v }"
        @click="filterStatus = opt.v"
      >
        {{ opt.l }}
      </button>
      <span class="filter-bar__count">{{ filteredFeedbacks.length }} 条反馈</span>
    </div>

    <!-- Loading -->
    <div v-if="loading && feedbacks.length === 0" class="loading-state">
      <div class="loading-state__spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- Empty -->
    <div v-else-if="feedbacks.length === 0" class="empty-card">
      <div class="empty-card__icon">
        <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        </svg>
      </div>
      <h3>暂无反馈</h3>
      <p>还没有用户提交反馈</p>
    </div>

    <!-- Filtered Empty -->
    <div v-else-if="filteredFeedbacks.length === 0" class="empty-card">
      <p style="color: #94a3b8;">当前筛选条件下没有匹配的反馈</p>
    </div>

    <!-- Feedback Cards -->
    <div v-else class="feedback-list">
      <article
        v-for="fb in filteredFeedbacks"
        :key="fb.feedbackId"
        class="feedback-card"
        :class="{ 'feedback-card--acting': actingId === fb.feedbackId }"
      >
        <div class="feedback-card__body">
          <div class="feedback-card__header">
            <div class="feedback-card__header-left">
              <span :class="['status-chip', fb.status === 'ok' ? 'status-chip--done' : 'status-chip--pending']">
                {{ fb.status === 'ok' ? '已处理' : '待处理' }}
              </span>
              <span class="feedback-card__id">#{{ fb.feedbackId }}</span>
            </div>
            <span class="feedback-card__time">{{ formatTime(fb.time) }}</span>
          </div>

          <p class="feedback-card__text">{{ fb.ftext }}</p>

          <div v-if="fb.attachment" class="feedback-card__attachment">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/>
            </svg>
            <a :href="fb.attachment" target="_blank" rel="noopener">{{ fb.attachment }}</a>
          </div>

          <div v-if="fb.status === 'wait'" class="feedback-card__actions">
            <button
              class="action-btn action-btn--resolve"
              :disabled="actingId !== null"
              @click="handleResolve(fb)"
            >
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="20,6 9,17 4,12"/>
              </svg>
              标记为已处理
            </button>
          </div>
        </div>
      </article>
    </div>
  </div>
</template>

<style scoped lang="scss">
.feedback-page {
  max-width: 800px;
}

/* ====== Page Top ====== */
.page-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 1.5rem;
}

.page-top__title {
  font-size: 1.35rem;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.02em;
  margin: 0;

  :global(body.dark-mode) & {
    color: #f1f5f9;
  }
}

.page-top__desc {
  margin: 0.2rem 0 0;
  font-size: 0.85rem;
  color: #94a3b8;
}

/* ====== Refresh ====== */
.refresh-btn {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  height: 36px;
  padding: 0 1rem;
  border: 1px solid rgba(15, 23, 42, 0.1);
  border-radius: 10px;
  background: #ffffff;
  color: #64748b;
  font-size: 0.85rem;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.15s;
  white-space: nowrap;

  &:hover:not(:disabled) {
    border-color: rgba(15, 23, 42, 0.15);
    color: #334155;
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  :global(body.dark-mode) & {
    background: #111827;
    border-color: rgba(255, 255, 255, 0.1);
    color: #94a3b8;

    &:hover:not(:disabled) {
      color: #e2e8f0;
      border-color: rgba(255, 255, 255, 0.15);
    }
  }
}

.refresh-btn__spin {
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ====== Filter Bar ====== */
.filter-bar {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1.25rem;
  flex-wrap: wrap;
}

.filter-chip {
  padding: 0.35em 0.75em;
  border-radius: 8px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: #ffffff;
  color: #64748b;
  font-size: 0.8rem;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.15s;

  &:hover {
    border-color: rgba(15, 23, 42, 0.15);
    color: #334155;
  }

  &--active {
    background: #0d9488;
    border-color: #0d9488;
    color: #ffffff;

    &:hover {
      background: #0f766e;
      border-color: #0f766e;
      color: #ffffff;
    }
  }

  :global(body.dark-mode) & {
    background: #111827;
    border-color: rgba(255, 255, 255, 0.08);
    color: #94a3b8;

    &:hover {
      border-color: rgba(255, 255, 255, 0.15);
      color: #e2e8f0;
    }

    &--active {
      background: #0d9488;
      border-color: #0d9488;
      color: #ffffff;
    }
  }
}

.filter-bar__count {
  margin-left: auto;
  font-size: 0.8rem;
  color: #94a3b8;
}

/* ====== Loading ====== */
.loading-state {
  text-align: center;
  padding: 4rem 1.5rem;
  color: #94a3b8;
}

.loading-state__spinner {
  width: 28px;
  height: 28px;
  border: 2.5px solid rgba(15, 23, 42, 0.08);
  border-top-color: #0d9488;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  margin: 0 auto 0.75rem;

  :global(body.dark-mode) & {
    border-color: rgba(255, 255, 255, 0.08);
    border-top-color: #2dd4bf;
  }
}

/* ====== Empty ====== */
.empty-card {
  text-align: center;
  padding: 3.5rem 1.5rem;
  background: #ffffff;
  border: 1px solid rgba(15, 23, 42, 0.06);
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);

  :global(body.dark-mode) & {
    background: #111827;
    border-color: rgba(255, 255, 255, 0.06);
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  }

  h3 {
    font-size: 0.95rem;
    font-weight: 600;
    color: #334155;
    margin: 0.75rem 0 0.25rem;

    :global(body.dark-mode) & {
      color: #e2e8f0;
    }
  }

  p {
    font-size: 0.85rem;
    color: #94a3b8;
    margin: 0;
  }
}

.empty-card__icon {
  color: #0d9488;

  :global(body.dark-mode) & {
    color: #2dd4bf;
  }
}

/* ====== Feedback List ====== */
.feedback-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

/* ====== Feedback Card ====== */
.feedback-card {
  background: #ffffff;
  border: 1px solid rgba(15, 23, 42, 0.06);
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
  transition: opacity 0.2s, box-shadow 0.2s, transform 0.2s;

  &:hover {
    box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06);
    transform: translateY(-1px);
  }

  &--acting {
    opacity: 0.5;
    pointer-events: none;
  }

  :global(body.dark-mode) & {
    background: #111827;
    border-color: rgba(255, 255, 255, 0.06);
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    }
  }
}

.feedback-card__body {
  padding: 1.25rem;
}

.feedback-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.85rem;
}

.feedback-card__header-left {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.feedback-card__id {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.75rem;
  color: #94a3b8;
}

.feedback-card__time {
  font-size: 0.78rem;
  color: #94a3b8;
}

.feedback-card__text {
  font-size: 0.9rem;
  color: #334155;
  line-height: 1.6;
  margin: 0 0 0.75rem;
  white-space: pre-wrap;
  word-break: break-word;

  :global(body.dark-mode) & {
    color: #cbd5e1;
  }
}

.feedback-card__attachment {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  margin-bottom: 0.75rem;
  font-size: 0.8rem;
  color: #94a3b8;

  a {
    color: #0d9488;
    text-decoration: none;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;

    &:hover {
      text-decoration: underline;
    }

    :global(body.dark-mode) & {
      color: #2dd4bf;
    }
  }
}

.feedback-card__actions {
  padding-top: 0.85rem;
  border-top: 1px solid rgba(15, 23, 42, 0.05);
  display: flex;
  justify-content: flex-end;

  :global(body.dark-mode) & {
    border-color: rgba(255, 255, 255, 0.05);
  }
}

/* ====== Status Chips ====== */
.status-chip {
  display: inline-flex;
  align-items: center;
  padding: 0.18em 0.6em;
  border-radius: 6px;
  font-size: 0.72rem;
  font-weight: 600;
  letter-spacing: 0.03em;

  &--pending {
    background: rgba(245, 158, 11, 0.08);
    color: #f59e0b;

    :global(body.dark-mode) & {
      background: rgba(245, 158, 11, 0.15);
      color: #fbbf24;
    }
  }

  &--done {
    background: rgba(16, 185, 129, 0.08);
    color: #10b981;

    :global(body.dark-mode) & {
      background: rgba(16, 185, 129, 0.15);
      color: #34d399;
    }
  }
}

/* ====== Action Button ====== */
.action-btn {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.45em 1em;
  border-radius: 10px;
  border: none;
  font-size: 0.8rem;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.15s;

  &:disabled {
    opacity: 0.45;
    cursor: not-allowed;
  }

  &--resolve {
    background: #f0fdfa;
    color: #0d9488;

    &:hover:not(:disabled) {
      background: #ccfbf1;
    }

    :global(body.dark-mode) & {
      background: rgba(13, 148, 136, 0.1);
      color: #2dd4bf;

      &:hover:not(:disabled) {
        background: rgba(13, 148, 136, 0.18);
      }
    }
  }
}
</style>
