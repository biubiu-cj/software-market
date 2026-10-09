<script setup lang="ts">
import { onBeforeMount, ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getAdminInfo } from '@/api/admin'
import { useAdminStore } from '@/stores/admin'

const router = useRouter()
const route = useRoute()
const adminStore = useAdminStore()
const isReady = ref(false)
const collapsed = ref(false)

onBeforeMount(async () => {
  if (adminStore.isLoggedIn) {
    try {
      const res = await getAdminInfo()
      if (res.code === 200 && res.data.admin) {
        adminStore.setAdminInfo(res.data.admin)
      }
    } catch {
      adminStore.clearToken()
      router.push('/admin/login')
    }
  } else {
    router.push('/admin/login')
  }
  isReady.value = true
})

function handleLogout() {
  adminStore.clearToken()
  router.push('/admin/login')
}

const navGroups = [
  {
    items: [
      { path: '/admin/dashboard', label: '概览', icon: 'dashboard' },
    ],
  },
  {
    label: '管理',
    items: [
      { path: '/admin/users', label: '用户管理', icon: 'users' },
      { path: '/admin/sues', label: '举报审核', icon: 'flag' },
      { path: '/admin/feedback', label: '反馈管理', icon: 'feedback' },
    ],
  },
  {
    label: '系统',
    items: [
      { path: '/admin/add', label: '新增管理员', icon: 'userPlus' },
      { path: '/admin/changePassword', label: '修改信息', icon: 'lock' },
      { path: '/admin/post', label: '管理员发帖', icon: 'edit' },
    ],
  },
]

const breadcrumbs = computed(() => {
  const item = navGroups.flatMap(g => g.items).find(i => i.path === route.path)
  return item ? [item.label] : [route.name as string || '']
})
</script>

<template>
  <div v-if="isReady" class="admin-root">
    <aside class="admin-sidebar" :class="{ 'admin-sidebar--collapsed': collapsed }">
      <div class="admin-sidebar__brand">
        <RouterLink to="/admin/dashboard" class="admin-sidebar__logo">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 2L2 7l10 5 10-5-10-5z"/>
            <path d="M2 17l10 5 10-5"/>
            <path d="M2 12l10 5 10-5"/>
          </svg>
        </RouterLink>
      </div>

      <nav class="admin-sidebar__nav">
        <template v-for="(group, gi) in navGroups" :key="gi">
          <div v-if="group.label" class="admin-sidebar__group-label">{{ group.label }}</div>
          <RouterLink
            v-for="item in group.items"
            :key="item.path"
            :to="item.path"
            class="admin-sidebar__item"
            active-class="admin-sidebar__item--active"
            :title="item.label"
          >
            <svg v-if="item.icon === 'dashboard'" class="admin-sidebar__icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="7" height="7" rx="1"/>
              <rect x="14" y="3" width="7" height="7" rx="1"/>
              <rect x="3" y="14" width="7" height="7" rx="1"/>
              <rect x="14" y="14" width="7" height="7" rx="1"/>
            </svg>
            <svg v-else-if="item.icon === 'users'" class="admin-sidebar__icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
            <svg v-else-if="item.icon === 'flag'" class="admin-sidebar__icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z"/>
              <line x1="4" y1="22" x2="4" y2="15"/>
            </svg>
            <svg v-else-if="item.icon === 'userPlus'" class="admin-sidebar__icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="8.5" cy="7" r="4"/>
              <line x1="20" y1="8" x2="20" y2="14"/>
              <line x1="23" y1="11" x2="17" y2="11"/>
            </svg>
            <svg v-else-if="item.icon === 'lock'" class="admin-sidebar__icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
            <svg v-else-if="item.icon === 'edit'" class="admin-sidebar__icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
              <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
            </svg>
            <svg v-else-if="item.icon === 'feedback'" class="admin-sidebar__icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </svg>
            <span class="admin-sidebar__label">{{ item.label }}</span>
          </RouterLink>
        </template>
      </nav>

      <div class="admin-sidebar__footer">
        <div class="admin-sidebar__user">
          <div class="admin-sidebar__avatar">
            {{ (adminStore.adminInfo.name || '管')[0] }}
          </div>
          <span class="admin-sidebar__name">{{ adminStore.adminInfo.name || '管理员' }}</span>
        </div>
        <button class="admin-sidebar__logout" @click="handleLogout" title="退出登录">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
            <polyline points="16,17 21,12 16,7"/>
            <line x1="21" y1="12" x2="9" y2="12"/>
          </svg>
        </button>
      </div>
    </aside>

    <div class="admin-main-wrapper">
      <header class="admin-topbar">
        <div class="admin-topbar__left">
          <button class="admin-topbar__collapse" @click="collapsed = !collapsed" title="折叠菜单">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="3" y1="12" x2="21" y2="12"/>
              <line x1="3" y1="6" x2="21" y2="6"/>
              <line x1="3" y1="18" x2="21" y2="18"/>
            </svg>
          </button>
          <div class="admin-topbar__breadcrumb">
            <span class="admin-topbar__breadcrumb-item">管理端</span>
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><polyline points="9,18 15,12 9,6"/></svg>
            <span v-for="(crumb, i) in breadcrumbs" :key="i" class="admin-topbar__breadcrumb-item admin-topbar__breadcrumb-item--current">{{ crumb }}</span>
          </div>
        </div>
        <div class="admin-topbar__right">
          <div class="admin-topbar__user-menu">
            <div class="admin-topbar__avatar-sm">
              {{ (adminStore.adminInfo.name || '管')[0] }}
            </div>
            <span class="admin-topbar__user-name">{{ adminStore.adminInfo.name || '管理员' }}</span>
          </div>
        </div>
      </header>

      <main class="admin-main">
        <router-view v-slot="{ Component: ViewComponent }">
          <template v-if="ViewComponent">
            <component :is="ViewComponent" />
          </template>
        </router-view>
      </main>
    </div>
  </div>
</template>

<style scoped lang="scss">
/* ====== Root ====== */
.admin-root {
  display: flex;
  height: 100vh;
  height: 100dvh;
  overflow: hidden;
  background: #f8fafc;

  :global(body.dark-mode) & {
    background: #0b0f14;
  }
}

/* ====== Sidebar ====== */
.admin-sidebar {
  width: 240px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: #0f172a;
  color: #e2e8f0;
  transition: width 0.2s ease;
  overflow: hidden;

  &--collapsed {
    width: 64px;

    .admin-sidebar__label,
    .admin-sidebar__group-label,
    .admin-sidebar__name {
      opacity: 0;
      visibility: hidden;
    }

    .admin-sidebar__item {
      justify-content: center;
      padding: 0.65rem 0;
    }

    .admin-sidebar__brand {
      justify-content: center;
    }

    .admin-sidebar__footer {
      justify-content: center;
    }
  }
}

.admin-sidebar__brand {
  display: flex;
  align-items: center;
  height: 56px;
  padding: 0 1.25rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
}

.admin-sidebar__logo {
  color: #0d9488;
  display: flex;
  align-items: center;

  &:hover {
    color: #2dd4bf;
  }
}

.admin-sidebar__nav {
  flex: 1;
  overflow-y: auto;
  padding: 0.75rem 0;

  &::-webkit-scrollbar {
    width: 3px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.1);
    border-radius: 3px;
  }
}

.admin-sidebar__group-label {
  padding: 0.85rem 1.25rem 0.35rem;
  font-size: 0.68rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: rgba(255, 255, 255, 0.3);
  white-space: nowrap;
  transition: opacity 0.15s;
}

.admin-sidebar__item {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  padding: 0.6rem 1.25rem;
  margin: 0.1rem 0.5rem;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 500;
  color: #94a3b8;
  text-decoration: none;
  transition: all 0.15s ease;
  white-space: nowrap;
  position: relative;

  &:hover {
    background: rgba(255, 255, 255, 0.06);
    color: #e2e8f0;
  }

  &--active {
    background: rgba(13, 148, 136, 0.15);
    color: #2dd4bf;
    font-weight: 600;

    .admin-sidebar__icon {
      color: #0d9488;
    }
  }
}

.admin-sidebar__icon {
  flex-shrink: 0;
  color: inherit;
}

.admin-sidebar__label {
  transition: opacity 0.15s, visibility 0.15s;
}

.admin-sidebar__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 1.25rem;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
}

.admin-sidebar__user {
  display: flex;
  align-items: center;
  gap: 0.55rem;
  min-width: 0;
}

.admin-sidebar__avatar {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: #0d9488;
  color: #f0fdfa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  font-weight: 700;
  flex-shrink: 0;
}

.admin-sidebar__name {
  font-size: 0.8rem;
  font-weight: 500;
  color: #cbd5e1;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: opacity 0.15s, visibility 0.15s;
}

.admin-sidebar__logout {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: none;
  background: transparent;
  color: #64748b;
  cursor: pointer;
  transition: all 0.15s;
  flex-shrink: 0;

  &:hover {
    background: rgba(239, 68, 68, 0.15);
    color: #f87171;
  }
}

/* ====== Main Wrapper ====== */
.admin-main-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
}

/* ====== Topbar ====== */
.admin-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 1.5rem;
  background: #ffffff;
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
  flex-shrink: 0;

  :global(body.dark-mode) & {
    background: #111827;
    border-color: rgba(255, 255, 255, 0.06);
  }
}

.admin-topbar__left {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.admin-topbar__collapse {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: none;
  background: transparent;
  color: #64748b;
  cursor: pointer;
  transition: all 0.15s;

  &:hover {
    background: #f1f5f9;
    color: #334155;
  }

  :global(body.dark-mode) & {
    color: #94a3b8;

    &:hover {
      background: rgba(255, 255, 255, 0.06);
      color: #e2e8f0;
    }
  }
}

.admin-topbar__breadcrumb {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.8rem;
  color: #94a3b8;

  svg {
    flex-shrink: 0;
  }
}

.admin-topbar__breadcrumb-item {
  &--current {
    color: #334155;
    font-weight: 600;

    :global(body.dark-mode) & {
      color: #e2e8f0;
    }
  }
}

.admin-topbar__right {
  display: flex;
  align-items: center;
}

.admin-topbar__user-menu {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.3rem 0.6rem 0.3rem 0.3rem;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.15s;

  &:hover {
    background: #f1f5f9;

    :global(body.dark-mode) & {
      background: rgba(255, 255, 255, 0.06);
    }
  }
}

.admin-topbar__avatar-sm {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: #0d9488;
  color: #f0fdfa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 700;
  flex-shrink: 0;
}

.admin-topbar__user-name {
  font-size: 0.825rem;
  font-weight: 500;
  color: #334155;

  :global(body.dark-mode) & {
    color: #e2e8f0;
  }
}

/* ====== Content ====== */
.admin-main {
  flex: 1;
  overflow-y: auto;
  padding: 2rem 1.5rem;

  :global(body.dark-mode) & {
    background: #0b0f14;
  }
}

/* ====== Responsive ====== */
@media screen and (max-width: 768px) {
  .admin-sidebar {
    width: 64px;

    .admin-sidebar__label,
    .admin-sidebar__group-label,
    .admin-sidebar__name {
      display: none;
    }

    .admin-sidebar__item {
      justify-content: center;
      padding: 0.65rem 0;
      margin: 0.1rem 0.35rem;
    }

    .admin-sidebar__brand {
      justify-content: center;
      padding: 0 0.5rem;
    }

    .admin-sidebar__footer {
      justify-content: center;
      padding: 0.75rem 0.5rem;
    }
  }

  .admin-topbar {
    padding: 0 1rem;
  }

  .admin-main {
    padding: 1.25rem 1rem;
  }

  .admin-topbar__user-name {
    display: none;
  }

  .admin-topbar__collapse {
    display: none;
  }
}
</style>
