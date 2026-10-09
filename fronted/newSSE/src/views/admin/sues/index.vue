<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getSues, noViolation, violation } from '@/api/admin'
import type { SueVO } from '@/api/admin'
import { showMsg, showConfirm } from '@/components/MessageBox'

const loading = ref(false)
const sues = ref<SueVO[]>([])
const actingSueId = ref<number | null>(null)
const filterType = ref<string>('all')

const filteredSues = computed(() => {
  if (filterType.value === 'all') return sues.value
  return sues.value.filter(s => s.targettype === filterType.value)
})

async function fetchSues() {
  loading.value = true
  try {
    sues.value = await getSues()
  } catch {
    showMsg('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

async function handleNoViolation(sue: SueVO) {
  const confirmed = await showConfirm('确认将该举报标记为「无违规」吗？')
  if (!confirmed) return

  actingSueId.value = sue.sueId
  try {
    const res = await noViolation({ sueID: sue.sueId })
    if (res.code === 200) {
      showMsg('已标记为无违规')
      sues.value = sues.value.filter(s => s.sueId !== sue.sueId)
    } else {
      showMsg(res.msg || '操作失败')
    }
  } catch {
    showMsg('网络错误，请稍后重试')
  } finally {
    actingSueId.value = null
  }
}

async function handleViolation(sue: SueVO) {
  const confirmed = await showConfirm('确认判定该举报为「违规」并执行处罚吗？')
  if (!confirmed) return

  actingSueId.value = sue.sueId
  try {
    const res = await violation({ sueID: sue.sueId })
    if (res.code === 200) {
      showMsg('已标记为违规并处罚')
      sues.value = sues.value.filter(s => s.sueId !== sue.sueId)
    } else {
      showMsg(res.msg || '操作失败')
    }
  } catch {
    showMsg('网络错误，请稍后重试')
  } finally {
    actingSueId.value = null
  }
}

function targetTypeLabel(type: string): string {
  const map: Record<string, string> = { post: '帖子', pcomment: '帖子评论', ccomment: '子评论' }
  return map[type] || type
}

function targetTypeVariant(type: string): string {
  if (type === 'post') return 'post'
  if (type === 'pcomment') return 'pcomment'
  return 'ccomment'
}

function formatTime(time: string): string {
  if (!time) return '-'
  const d = new Date(time)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

onMounted(fetchSues)
</script>

<template>
  <div class="sues-page">
    <!-- Page Header -->
    <div class="page-top">
      <div>
        <h1 class="page-top__title">举报审核</h1>
        <p class="page-top__desc">审核和处理用户提交的举报</p>
      </div>
      <button class="refresh-btn" :disabled="loading" @click="fetchSues">
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
        v-for="opt in [{ v: 'all', l: '全部' }, { v: 'post', l: '帖子' }, { v: 'pcomment', l: '帖子评论' }, { v: 'ccomment', l: '子评论' }]"
        :key="opt.v"
        class="filter-chip"
        :class="{ 'filter-chip--active': filterType === opt.v }"
        @click="filterType = opt.v"
      >
        {{ opt.l }}
      </button>
      <span class="filter-bar__count">{{ filteredSues.length }} 条举报</span>
    </div>

    <!-- Loading -->
    <div v-if="loading && sues.length === 0" class="loading-state">
      <div class="loading-state__spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- Empty -->
    <div v-else-if="sues.length === 0" class="empty-card">
      <div class="empty-card__icon">
        <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
          <polyline points="22,4 12,14.01 9,11.01"/>
        </svg>
      </div>
      <h3>暂无待处理举报</h3>
      <p>所有举报已处理完毕</p>
    </div>

    <!-- Filtered Empty -->
    <div v-else-if="filteredSues.length === 0" class="empty-card">
      <p style="color: #94a3b8;">当前筛选条件下没有匹配的举报</p>
    </div>

    <!-- Report Cards -->
    <div v-else class="sues-list">
      <article
        v-for="sue in filteredSues"
        :key="sue.sueId"
        class="sue-card"
        :class="{ 'sue-card--acting': actingSueId === sue.sueId }"
      >
        <div class="sue-card__body">
          <div class="sue-card__header">
            <div class="sue-card__header-left">
              <span :class="['type-chip', `type-chip--${targetTypeVariant(sue.targettype)}`]">
                {{ targetTypeLabel(sue.targettype) }}
              </span>
              <span class="sue-card__id">#{{ sue.sueId }}</span>
            </div>
            <span class="sue-card__time">{{ formatTime(sue.time) }}</span>
          </div>

          <div class="sue-card__details">
            <div class="sue-card__detail">
              <span class="sue-card__label">举报人</span>
              <span class="sue-card__value">{{ sue.reporterName || '未知用户' }}</span>
            </div>
            <div class="sue-card__detail">
              <span class="sue-card__label">被举报内容</span>
              <span class="sue-card__value sue-card__target">{{ sue.targetTitle || '(内容已删除)' }}</span>
            </div>
            <div class="sue-card__detail">
              <span class="sue-card__label">举报原因</span>
              <span class="sue-card__reason">"{{ sue.reason }}"</span>
            </div>
          </div>

          <div class="sue-card__actions">
            <button
              class="action-btn action-btn--dismiss"
              :disabled="actingSueId !== null"
              @click="handleNoViolation(sue)"
            >
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="20,6 9,17 4,12"/>
              </svg>
              无违规
            </button>
            <button
              class="action-btn action-btn--violate"
              :disabled="actingSueId !== null"
              @click="handleViolation(sue)"
            >
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
                <line x1="12" y1="9" x2="12" y2="13"/>
                <line x1="12" y1="17" x2="12.01" y2="17"/>
              </svg>
              违规处罚
            </button>
          </div>
        </div>
      </article>
    </div>
  </div>
</template>

<style scoped lang="scss">
.sues-page {
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

/* ====== Refresh Button ====== */
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

/* ====== Sues List ====== */
.sues-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

/* ====== Sue Card ====== */
.sue-card {
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

.sue-card__body {
  padding: 1.25rem;
}

.sue-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.sue-card__header-left {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.sue-card__id {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 0.75rem;
  color: #94a3b8;
}

.sue-card__time {
  font-size: 0.78rem;
  color: #94a3b8;
}

.sue-card__details {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
  margin-bottom: 1.15rem;
}

.sue-card__detail {
  display: flex;
  gap: 0.75rem;
  font-size: 0.875rem;
}

.sue-card__label {
  flex-shrink: 0;
  width: 5.8em;
  color: #94a3b8;
  font-weight: 500;
  font-size: 0.8rem;
  padding-top: 0.1em;
}

.sue-card__value {
  color: #334155;
  font-weight: 500;

  :global(body.dark-mode) & {
    color: #e2e8f0;
  }
}

.sue-card__target {
  color: #0d9488;
  max-width: 360px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;

  :global(body.dark-mode) & {
    color: #2dd4bf;
  }
}

.sue-card__reason {
  color: #64748b;
  font-style: italic;

  :global(body.dark-mode) & {
    color: #94a3b8;
  }
}

.sue-card__actions {
  display: flex;
  gap: 0.6rem;
  justify-content: flex-end;
  padding-top: 1rem;
  border-top: 1px solid rgba(15, 23, 42, 0.05);

  :global(body.dark-mode) & {
    border-color: rgba(255, 255, 255, 0.05);
  }
}

/* ====== Type Chips ====== */
.type-chip {
  display: inline-flex;
  align-items: center;
  padding: 0.18em 0.6em;
  border-radius: 6px;
  font-size: 0.72rem;
  font-weight: 600;
  letter-spacing: 0.03em;

  &--post {
    background: rgba(13, 148, 136, 0.08);
    color: #0d9488;

    :global(body.dark-mode) & {
      background: rgba(13, 148, 136, 0.15);
      color: #2dd4bf;
    }
  }

  &--pcomment {
    background: rgba(59, 130, 246, 0.08);
    color: #3b82f6;

    :global(body.dark-mode) & {
      background: rgba(59, 130, 246, 0.15);
      color: #60a5fa;
    }
  }

  &--ccomment {
    background: rgba(139, 92, 246, 0.08);
    color: #8b5cf6;

    :global(body.dark-mode) & {
      background: rgba(139, 92, 246, 0.15);
      color: #a78bfa;
    }
  }
}

/* ====== Action Buttons ====== */
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

  &--dismiss {
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

  &--violate {
    background: #fef2f2;
    color: #ef4444;

    &:hover:not(:disabled) {
      background: #fee2e2;
    }

    :global(body.dark-mode) & {
      background: rgba(239, 68, 68, 0.1);
      color: #f87171;

      &:hover:not(:disabled) {
        background: rgba(239, 68, 68, 0.18);
      }
    }
  }
}
</style>
