<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getOAuth2Records } from '@/api/oauth2/oauth2'
import type { OAuth2Record, OAuth2RecordsPage } from '@/api/oauth2/oauth2'

const loading = ref(true)
const records = ref<OAuth2Record[]>([])
const page = ref(1)
const total = ref(0)
const totalPages = ref(0)
const pageSize = 20

async function fetchRecords() {
  loading.value = true
  try {
    const data: OAuth2RecordsPage = await getOAuth2Records(page.value, pageSize)
    records.value = data.records
    total.value = data.total
    totalPages.value = data.total_pages
  } catch {
    records.value = []
  } finally {
    loading.value = false
  }
}

function goPage(p: number) {
  if (p < 1 || p > totalPages.value) return
  page.value = p
  fetchRecords()
}

const scopeLabels: Record<string, string> = {
  read_basic: '读取基本信息',
}

function formatTime(time: string): string {
  if (!time) return '-'
  const d = new Date(time)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

onMounted(fetchRecords)
</script>

<template>
  <div class="root page-section">
    <h2 class="page-heading">授权管理</h2>

    <div v-if="loading" class="loading-state">
      <p>加载中...</p>
    </div>

    <div v-else-if="records.length === 0" class="empty-state">
      <p>暂无授权记录</p>
      <p class="empty-hint">当你通过第三方应用登录时，授权记录将显示在这里</p>
    </div>

    <div v-else class="records-list">
      <div
        v-for="record in records"
        :key="record.app_id"
        class="record-card"
      >
        <div class="record-left">
          <img
            v-if="record.app_icon"
            :src="record.app_icon"
            class="record-icon"
            @error="(e) => { (e.target as HTMLImageElement).style.display = 'none' }"
          >
          <div v-else class="record-icon-placeholder">
            {{ record.app_name[0] }}
          </div>
          <div class="record-info">
            <div class="record-name">{{ record.app_name }}</div>
            <div class="record-meta">
              <span class="record-scope">{{ scopeLabels[record.scope] || record.scope }}</span>
              <span class="record-time">{{ formatTime(record.created_at) }}</span>
            </div>
          </div>
        </div>
      </div>

      <div v-if="totalPages > 1" class="pagination">
        <button
          class="page-btn"
          :disabled="page <= 1"
          @click="goPage(page - 1)"
        >
          上一页
        </button>
        <span class="page-info">{{ page }} / {{ totalPages }}（共 {{ total }} 条）</span>
        <button
          class="page-btn"
          :disabled="page >= totalPages"
          @click="goPage(page + 1)"
        >
          下一页
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.root {
  color: var(--color-text);
  width: 100%;
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 3rem 1.5rem;
  color: #94a3b8;
}

.empty-hint {
  font-size: 0.85rem;
  margin-top: 0.5rem;
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin: 1.25rem 5%;
}

.record-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.25rem;
  background: #f9f9f9;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  transition: box-shadow 0.15s;
}

.record-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

:global(body.dark-mode) .record-card {
  background: #1e293b;
  border-color: #334155;
}

.record-left {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  min-width: 0;
}

.record-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  object-fit: cover;
  flex-shrink: 0;
}

.record-icon-placeholder {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: #0d9488;
  color: #f0fdfa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
  font-weight: 700;
  flex-shrink: 0;
}

.record-info {
  min-width: 0;
}

.record-name {
  font-size: 0.95rem;
  font-weight: 600;
  color: #1e293b;
}

:global(body.dark-mode) .record-name {
  color: #f1f5f9;
}

.record-meta {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-top: 0.2rem;
  font-size: 0.8rem;
  color: #94a3b8;
}

.record-scope {
  background: rgba(13, 148, 136, 0.1);
  color: #0d9488;
  padding: 0.1em 0.5em;
  border-radius: 5px;
  font-weight: 500;
}

.record-time {
  white-space: nowrap;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  padding: 1rem 0;
}

.page-btn {
  padding: 0.4em 1em;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #ffffff;
  color: #334155;
  font-size: 0.85rem;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.15s;
}

.page-btn:hover:not(:disabled) {
  border-color: #0d9488;
  color: #0d9488;
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

:global(body.dark-mode) .page-btn {
  background: #1e293b;
  border-color: #334155;
  color: #cbd5e1;
}

.page-info {
  font-size: 0.85rem;
  color: #64748b;
}

@media screen and (min-width: 768px) {
  .root {
    width: 60%;
  }
}
</style>
