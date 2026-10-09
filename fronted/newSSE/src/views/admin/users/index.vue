<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { showUsers, deleteUser, deleteAdmin } from '@/api/admin'
import type { UserVO } from '@/api/admin'
import { showMsg, showConfirm } from '@/components/MessageBox'

const loading = ref(false)
const users = ref<UserVO[]>([])
const searched = ref(false)

const form = reactive({
  name: '',
  idPass: -1,
})

const deleteAdminForm = reactive({
  account: '',
})
const deleteAccountLoading = ref(false)

// Pagination
const page = ref(1)
const pageSize = 10

const totalUsers = computed(() => users.value.length)
const pagedUsers = computed(() => {
  const start = (page.value - 1) * pageSize
  return users.value.slice(start, start + pageSize)
})
const totalPages = computed(() => Math.max(1, Math.ceil(users.value.length / pageSize)))

function setPage(p: number) {
  if (p >= 1 && p <= totalPages.value) page.value = p
}

// Dropdown state per row
const openMenuUserId = ref<number | null>(null)

function toggleMenu(userId: number) {
  openMenuUserId.value = openMenuUserId.value === userId ? null : userId
}

function closeMenu() {
  openMenuUserId.value = null
}

async function handleSearch() {
  loading.value = true
  page.value = 1
  try {
    const res = await showUsers({
      name: form.name || undefined,
      idPass: form.idPass,
    })
    if (res.code === 200) {
      users.value = res.data
      searched.value = true
    } else {
      showMsg(res.msg || '查询失败')
    }
  } catch {
    showMsg('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

async function handleDeleteUser(user: UserVO) {
  closeMenu()
  const confirmed = await showConfirm(`确定要删除用户「${user.name}」吗？此操作不可撤销。`)
  if (!confirmed) return

  try {
    const res = await deleteUser({ name: user.name })
    if (res.code === 200) {
      showMsg('用户已删除')
      users.value = users.value.filter(u => u.userId !== user.userId)
    } else {
      showMsg(res.msg || '删除失败')
    }
  } catch {
    showMsg('网络错误，请稍后重试')
  }
}

async function handleDeleteAdmin() {
  if (!deleteAdminForm.account.trim()) {
    showMsg('请输入管理员账号')
    return
  }
  if (!deleteAdminForm.account.includes('@')) {
    showMsg('账号必须为邮箱格式')
    return
  }

  const confirmed = await showConfirm(`确定要移除管理员「${deleteAdminForm.account}」的权限吗？`)
  if (!confirmed) return

  deleteAccountLoading.value = true
  try {
    const res = await deleteAdmin({ account: deleteAdminForm.account })
    if (res.code === 200) {
      showMsg('管理员权限已移除')
      deleteAdminForm.account = ''
    } else {
      showMsg(res.msg || '操作失败')
    }
  } catch {
    showMsg('网络错误，请稍后重试')
  } finally {
    deleteAccountLoading.value = false
  }
}

function identityLabel(identity: string): string {
  const map: Record<string, string> = {
    admin: '管理员',
    official: '官方',
    teacher: '教师',
    organization: '组织',
  }
  return map[identity] || '普通用户'
}

function identityVariant(identity: string): string {
  if (identity === 'admin') return 'admin'
  if (identity === 'official') return 'official'
  if (identity === 'teacher' || identity === 'organization') return 'org'
  return 'default'
}
</script>

<template>
  <div class="users-page">
    <!-- Page Header -->
    <div class="page-top">
      <div>
        <h1 class="page-top__title">用户管理</h1>
        <p class="page-top__desc">查询和管理平台注册用户</p>
      </div>
    </div>

    <!-- Filter Bar -->
    <div class="filter-bar">
      <div class="filter-bar__row">
        <div class="filter-bar__search">
          <svg class="filter-bar__search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="11" cy="11" r="8"/>
            <line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          <input
            v-model="form.name"
            class="filter-bar__input"
            type="text"
            placeholder="搜索用户名..."
            @keyup.enter="handleSearch"
          >
        </div>
        <select v-model="form.idPass" class="filter-bar__select">
          <option :value="-1">全部实名状态</option>
          <option :value="1">已实名</option>
          <option :value="0">未实名</option>
        </select>
        <button class="filter-bar__btn" :disabled="loading" @click="handleSearch">
          {{ loading ? '查询中...' : '查询' }}
        </button>
      </div>
    </div>

    <!-- Results Card -->
    <div v-if="searched" class="content-card">
      <div class="content-card__header">
        <span class="content-card__count">共 {{ users.length }} 个用户</span>
        <div v-if="totalPages > 1" class="pagination pagination--top">
          <button class="pagination__btn" :disabled="page <= 1" @click="setPage(page - 1)">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><polyline points="15,18 9,12 15,6"/></svg>
          </button>
          <span class="pagination__info">{{ page }} / {{ totalPages }}</span>
          <button class="pagination__btn" :disabled="page >= totalPages" @click="setPage(page + 1)">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><polyline points="9,18 15,12 9,6"/></svg>
          </button>
        </div>
      </div>

      <!-- Empty -->
      <div v-if="users.length === 0" class="content-card__empty">
        <div class="content-card__empty-icon">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round">
            <circle cx="11" cy="11" r="8"/>
            <line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
        </div>
        <p>没有找到匹配的用户</p>
      </div>

      <!-- Table -->
      <div v-else class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th class="col-id">ID</th>
              <th class="col-name">用户名</th>
              <th class="col-email">邮箱</th>
              <th class="col-phone">手机号</th>
              <th class="col-identity">身份</th>
              <th class="col-score">信用分</th>
              <th class="col-punish">处罚</th>
              <th class="col-action"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in pagedUsers" :key="user.userId">
              <td class="cell-mono">{{ user.userId }}</td>
              <td class="cell-strong">{{ user.name }}</td>
              <td class="cell-secondary">{{ user.email || '-' }}</td>
              <td class="cell-secondary">{{ user.phone || '-' }}</td>
              <td>
                <span :class="['badge', `badge--${identityVariant(user.identity)}`]">
                  {{ identityLabel(user.identity) }}
                </span>
              </td>
              <td class="cell-strong">{{ user.score ?? '-' }}</td>
              <td>
                <span v-if="user.punishNum > 0" class="punish-dot">{{ user.punishNum }}</span>
                <span v-else class="cell-secondary">-</span>
              </td>
              <td class="cell-action">
                <div class="action-wrapper">
                  <button
                    class="action-trigger"
                    :class="{ 'action-trigger--open': openMenuUserId === user.userId }"
                    :disabled="user.identity === 'admin' || user.identity === 'official'"
                    @click.stop="toggleMenu(user.userId)"
                    title="操作"
                  >
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                      <circle cx="12" cy="5" r="2"/>
                      <circle cx="12" cy="12" r="2"/>
                      <circle cx="12" cy="19" r="2"/>
                    </svg>
                  </button>
                  <div v-if="openMenuUserId === user.userId" class="action-dropdown">
                    <button class="action-dropdown__item action-dropdown__item--danger" @click="handleDeleteUser(user)">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <polyline points="3,6 5,6 21,6"/>
                        <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                      </svg>
                      删除用户
                    </button>
                  </div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Bottom Pagination -->
      <div v-if="totalPages > 1" class="content-card__footer">
        <div class="pagination">
          <button class="pagination__btn" :disabled="page <= 1" @click="setPage(page - 1)">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><polyline points="15,18 9,12 15,6"/></svg>
          </button>
          <template v-for="p in totalPages" :key="p">
            <button
              v-if="p <= 7 || Math.abs(p - page) <= 1 || p === totalPages"
              class="pagination__num"
              :class="{ 'pagination__num--active': p === page }"
              @click="setPage(p)"
            >
              {{ p }}
            </button>
            <span v-else-if="p === page + 2 || p === page - 2" class="pagination__dots">...</span>
          </template>
          <button class="pagination__btn" :disabled="page >= totalPages" @click="setPage(page + 1)">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><polyline points="9,18 15,12 9,6"/></svg>
          </button>
        </div>
      </div>
    </div>

    <!-- Remove Admin Section -->
    <div class="content-card" style="margin-top: 1.5rem;">
      <div class="content-card__header">
        <div>
          <h3 class="content-card__title">移除管理员</h3>
          <p class="content-card__desc">输入管理员账号以移除其管理权限</p>
        </div>
      </div>
      <div class="remove-admin-form">
        <input
          v-model="deleteAdminForm.account"
          class="filter-bar__input"
          type="email"
          placeholder="管理员账号（邮箱）"
          style="flex: 1;"
        >
        <button
          class="btn-danger-sm"
          :disabled="deleteAccountLoading"
          @click="handleDeleteAdmin"
        >
          {{ deleteAccountLoading ? '处理中...' : '移除权限' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.users-page {
  max-width: 1060px;
}

/* ====== Page Top ====== */
.page-top {
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

/* ====== Filter Bar ====== */
.filter-bar {
  margin-bottom: 1.25rem;
}

.filter-bar__row {
  display: flex;
  gap: 0.65rem;
  align-items: center;
}

.filter-bar__search {
  position: relative;
  flex: 1;
  max-width: 320px;
}

.filter-bar__search-icon {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
  pointer-events: none;
}

.filter-bar__input {
  width: 100%;
  height: 40px;
  padding: 0 0.85rem 0 2.25rem;
  border: 1px solid rgba(15, 23, 42, 0.1);
  border-radius: 10px;
  background: #ffffff;
  color: #0f172a;
  font-size: 0.875rem;
  font-family: inherit;
  transition: border-color 0.15s, box-shadow 0.15s;

  &::placeholder {
    color: #94a3b8;
  }

  &:focus {
    outline: none;
    border-color: #0d9488;
    box-shadow: 0 0 0 3px rgba(13, 148, 136, 0.1);
  }

  :global(body.dark-mode) & {
    background: #111827;
    border-color: rgba(255, 255, 255, 0.1);
    color: #e2e8f0;

    &:focus {
      border-color: #0d9488;
      box-shadow: 0 0 0 3px rgba(13, 148, 136, 0.15);
    }
  }
}

.filter-bar__select {
  height: 40px;
  padding: 0 2rem 0 0.75rem;
  border: 1px solid rgba(15, 23, 42, 0.1);
  border-radius: 10px;
  background: #ffffff;
  color: #0f172a;
  font-size: 0.875rem;
  font-family: inherit;
  cursor: pointer;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg width='10' height='6' viewBox='0 0 10 6' fill='none' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M1 1l4 4 4-4' stroke='%2394a3b8' stroke-width='1.5' stroke-linecap='round'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 0.65rem center;

  &:focus {
    outline: none;
    border-color: #0d9488;
    box-shadow: 0 0 0 3px rgba(13, 148, 136, 0.1);
  }

  :global(body.dark-mode) & {
    background: #111827;
    border-color: rgba(255, 255, 255, 0.1);
    color: #e2e8f0;
  }
}

.filter-bar__btn {
  height: 40px;
  padding: 0 1.25rem;
  border: none;
  border-radius: 10px;
  background: #0d9488;
  color: #ffffff;
  font-size: 0.875rem;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: background 0.15s;

  &:hover:not(:disabled) {
    background: #0f766e;
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

@media screen and (max-width: 540px) {
  .filter-bar__row {
    flex-wrap: wrap;
  }

  .filter-bar__search {
    max-width: 100%;
    flex: 1 1 100%;
  }
}

/* ====== Content Card ====== */
.content-card {
  background: #ffffff;
  border: 1px solid rgba(15, 23, 42, 0.06);
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);

  :global(body.dark-mode) & {
    background: #111827;
    border-color: rgba(255, 255, 255, 0.06);
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  }
}

.content-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid rgba(15, 23, 42, 0.05);

  :global(body.dark-mode) & {
    border-color: rgba(255, 255, 255, 0.05);
  }
}

.content-card__title {
  font-size: 0.95rem;
  font-weight: 600;
  color: #0f172a;

  :global(body.dark-mode) & {
    color: #f1f5f9;
  }
}

.content-card__desc {
  font-size: 0.8rem;
  color: #94a3b8;
  margin: 0.1rem 0 0;
}

.content-card__count {
  font-size: 0.8rem;
  color: #94a3b8;
  font-weight: 500;
}

.content-card__footer {
  padding: 0.85rem 1.25rem;
  border-top: 1px solid rgba(15, 23, 42, 0.05);
  display: flex;
  justify-content: center;

  :global(body.dark-mode) & {
    border-color: rgba(255, 255, 255, 0.05);
  }
}

.content-card__empty {
  text-align: center;
  padding: 4rem 1.5rem;
  color: #94a3b8;

  p {
    margin-top: 0.75rem;
    font-size: 0.9rem;
  }
}

.content-card__empty-icon {
  color: #cbd5e1;

  :global(body.dark-mode) & {
    color: #475569;
  }
}

/* ====== Table ====== */
.table-wrap {
  overflow-x: auto;
}

.table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.875rem;

  th {
    text-align: left;
    padding: 0.75rem 1.25rem;
    font-size: 0.73rem;
    font-weight: 600;
    color: #94a3b8;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    white-space: nowrap;
    background: #f8fafc;

    :global(body.dark-mode) & {
      background: rgba(255, 255, 255, 0.02);
      color: #64748b;
    }
  }

  td {
    padding: 0.85rem 1.25rem;
    border-bottom: 1px solid rgba(15, 23, 42, 0.04);
    white-space: nowrap;
    color: #334155;

    :global(body.dark-mode) & {
      border-color: rgba(255, 255, 255, 0.04);
      color: #cbd5e1;
    }
  }

  tbody tr {
    transition: background 0.12s ease;

    &:hover {
      background: #f8fafc;

      :global(body.dark-mode) & {
        background: rgba(255, 255, 255, 0.02);
      }
    }

    &:last-child td {
      border-bottom: none;
    }
  }
}

.col-id { width: 60px; }
.col-score { width: 70px; }
.col-punish { width: 60px; }
.col-identity { width: 90px; }
.col-action { width: 50px; text-align: right; }

.cell-mono {
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 0.8rem;
  color: #94a3b8 !important;
}

.cell-strong {
  font-weight: 600;
}

.cell-secondary {
  color: #64748b !important;
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;

  :global(body.dark-mode) & {
    color: #94a3b8 !important;
  }
}

/* ====== Badges ====== */
.badge {
  display: inline-flex;
  align-items: center;
  padding: 0.2em 0.6em;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 500;
  background: #f1f5f9;
  color: #64748b;

  :global(body.dark-mode) & {
    background: rgba(255, 255, 255, 0.06);
    color: #94a3b8;
  }

  &--admin {
    background: rgba(13, 148, 136, 0.08);
    color: #0d9488;

    :global(body.dark-mode) & {
      background: rgba(13, 148, 136, 0.15);
      color: #2dd4bf;
    }
  }

  &--official {
    background: rgba(59, 130, 246, 0.08);
    color: #3b82f6;

    :global(body.dark-mode) & {
      background: rgba(59, 130, 246, 0.15);
      color: #60a5fa;
    }
  }

  &--org {
    background: rgba(139, 92, 246, 0.08);
    color: #8b5cf6;

    :global(body.dark-mode) & {
      background: rgba(139, 92, 246, 0.15);
      color: #a78bfa;
    }
  }
}

/* ====== Punish Dot ====== */
.punish-dot {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 22px;
  height: 22px;
  border-radius: 11px;
  background: #fef2f2;
  color: #ef4444;
  font-size: 0.72rem;
  font-weight: 700;

  :global(body.dark-mode) & {
    background: rgba(239, 68, 68, 0.15);
    color: #f87171;
  }
}

/* ====== Actions ====== */
.cell-action {
  text-align: right;
}

.action-wrapper {
  position: relative;
  display: inline-block;
}

.action-trigger {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: none;
  background: transparent;
  color: #94a3b8;
  cursor: pointer;
  transition: all 0.15s;

  &:hover:not(:disabled) {
    background: #f1f5f9;
    color: #334155;
  }

  &--open {
    background: #f1f5f9;
    color: #334155;
  }

  &:disabled {
    opacity: 0.3;
    cursor: not-allowed;
  }

  :global(body.dark-mode) & {
    &:hover:not(:disabled),
    &--open {
      background: rgba(255, 255, 255, 0.06);
      color: #e2e8f0;
    }
  }
}

.action-dropdown {
  position: absolute;
  right: 0;
  top: calc(100% + 4px);
  z-index: 50;
  min-width: 140px;
  background: #ffffff;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 10px;
  box-shadow: 0 10px 25px rgba(15, 23, 42, 0.1);
  padding: 0.35rem;
  animation: fadeIn 0.12s ease;

  :global(body.dark-mode) & {
    background: #1e293b;
    border-color: rgba(255, 255, 255, 0.08);
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.4);
  }
}

.action-dropdown__item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  width: 100%;
  padding: 0.5rem 0.75rem;
  border: none;
  border-radius: 7px;
  background: transparent;
  font-size: 0.8125rem;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: background 0.12s;
  color: #334155;

  &:hover {
    background: #f8fafc;
  }

  &--danger {
    color: #ef4444;

    &:hover {
      background: #fef2f2;
    }

    :global(body.dark-mode) & {
      color: #f87171;

      &:hover {
        background: rgba(239, 68, 68, 0.1);
      }
    }
  }

  :global(body.dark-mode) & {
    color: #e2e8f0;

    &:hover {
      background: rgba(255, 255, 255, 0.04);
    }
  }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-4px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ====== Pagination ====== */
.pagination {
  display: flex;
  align-items: center;
  gap: 0.25rem;

  &--top {
    .pagination__btn {
      width: 26px;
      height: 26px;
    }
    .pagination__info {
      font-size: 0.75rem;
    }
  }
}

.pagination__btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: #ffffff;
  color: #334155;
  cursor: pointer;
  transition: all 0.12s;

  &:hover:not(:disabled) {
    border-color: rgba(15, 23, 42, 0.15);
    background: #f8fafc;
  }

  &:disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }

  :global(body.dark-mode) & {
    background: #1e293b;
    border-color: rgba(255, 255, 255, 0.08);
    color: #94a3b8;

    &:hover:not(:disabled) {
      background: rgba(255, 255, 255, 0.06);
    }
  }
}

.pagination__num {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  height: 32px;
  border-radius: 8px;
  border: none;
  background: transparent;
  color: #64748b;
  font-size: 0.8125rem;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.12s;

  &:hover {
    background: #f1f5f9;
    color: #0f172a;
  }

  &--active {
    background: #0d9488;
    color: #ffffff;
    font-weight: 600;

    &:hover {
      background: #0f766e;
      color: #ffffff;
    }
  }

  :global(body.dark-mode) & {
    color: #94a3b8;

    &:hover {
      background: rgba(255, 255, 255, 0.06);
      color: #e2e8f0;
    }

    &--active {
      background: #0d9488;
      color: #ffffff;

      &:hover {
        background: #0f766e;
        color: #ffffff;
      }
    }
  }
}

.pagination__info {
  font-size: 0.8rem;
  color: #94a3b8;
  padding: 0 0.35rem;
}

.pagination__dots {
  color: #94a3b8;
  padding: 0 0.15rem;
  font-size: 0.8rem;
}

/* ====== Remove Admin ====== */
.remove-admin-form {
  display: flex;
  gap: 0.65rem;
  padding: 1rem 1.25rem;
  align-items: center;
}

.btn-danger-sm {
  height: 40px;
  padding: 0 1.15rem;
  border: none;
  border-radius: 10px;
  background: #ef4444;
  color: #ffffff;
  font-size: 0.85rem;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.15s;

  &:hover:not(:disabled) {
    background: #dc2626;
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

@media screen and (max-width: 480px) {
  .remove-admin-form {
    flex-direction: column;
  }
}
</style>
