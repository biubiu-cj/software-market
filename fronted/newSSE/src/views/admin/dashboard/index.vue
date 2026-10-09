<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin'

const router = useRouter()
const adminStore = useAdminStore()

const quickLinks = [
  { label: '用户管理', desc: '查询、管理平台用户', path: '/admin/users', icon: 'users' },
  { label: '举报审核', desc: '处理用户举报内容', path: '/admin/sues', icon: 'flag' },
  { label: '新增管理员', desc: '创建新的管理员账号', path: '/admin/add', icon: 'userPlus' },
  { label: '管理员发帖', desc: '以官方身份发布帖子', path: '/admin/post', icon: 'edit' },
]

const statCards = [
  { label: '管理员身份', value: adminStore.adminInfo.identity || '-', hint: '当前账号权限', icon: 'shield' },
  { label: '管理账号', value: adminStore.adminInfo.account || '-', hint: '当前登录账号', icon: 'mail' },
]
</script>

<template>
  <div class="dashboard">
    <!-- Header -->
    <div class="dashboard__header">
      <div>
        <h1 class="dashboard__greeting">
          你好，{{ adminStore.adminInfo.name || '管理员' }}
        </h1>
        <p class="dashboard__subtitle">欢迎回到管理控制台</p>
      </div>
      <div class="dashboard__header-avatar">
        {{ (adminStore.adminInfo.name || '管')[0] }}
      </div>
    </div>

    <!-- Stat Cards Row -->
    <div class="dashboard__stats">
      <div v-for="stat in statCards" :key="stat.label" class="stat-card">
        <div class="stat-card__icon">
          <svg v-if="stat.icon === 'shield'" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
          </svg>
          <svg v-else-if="stat.icon === 'mail'" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <rect x="2" y="4" width="20" height="16" rx="2"/>
            <path d="m22 4-10 8L2 4"/>
          </svg>
        </div>
        <div class="stat-card__body">
          <span class="stat-card__value">{{ stat.value }}</span>
          <span class="stat-card__hint">{{ stat.hint }}</span>
        </div>
      </div>
    </div>

    <!-- Quick Links -->
    <section class="dashboard__section">
      <h2 class="dashboard__section-title">快捷操作</h2>
      <div class="dashboard__quick-links">
        <button
          v-for="link in quickLinks"
          :key="link.path"
          class="quick-link-card"
          @click="router.push(link.path)"
        >
          <div class="quick-link-card__icon">
            <svg v-if="link.icon === 'users'" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
            <svg v-else-if="link.icon === 'flag'" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
              <path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z"/>
              <line x1="4" y1="22" x2="4" y2="15"/>
            </svg>
            <svg v-else-if="link.icon === 'userPlus'" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
              <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="8.5" cy="7" r="4"/>
              <line x1="20" y1="8" x2="20" y2="14"/>
              <line x1="23" y1="11" x2="17" y2="11"/>
            </svg>
            <svg v-else-if="link.icon === 'edit'" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
              <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
              <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
            </svg>
          </div>
          <div class="quick-link-card__content">
            <span class="quick-link-card__label">{{ link.label }}</span>
            <span class="quick-link-card__desc">{{ link.desc }}</span>
          </div>
          <svg class="quick-link-card__arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="9,18 15,12 9,6"/>
          </svg>
        </button>
      </div>
    </section>
  </div>
</template>

<style scoped lang="scss">
.dashboard {
  max-width: 960px;
}

/* ====== Header ====== */
.dashboard__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 2rem;
}

.dashboard__greeting {
  font-size: 1.5rem;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.02em;
  margin: 0;
  line-height: 1.3;

  :global(body.dark-mode) & {
    color: #f1f5f9;
  }
}

.dashboard__subtitle {
  margin: 0.25rem 0 0;
  font-size: 0.875rem;
  color: #94a3b8;
}

.dashboard__header-avatar {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: #0d9488;
  color: #f0fdfa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.15rem;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: 0 1px 3px rgba(13, 148, 136, 0.3);
}

/* ====== Stat Cards ====== */
.dashboard__stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: #ffffff;
  border: 1px solid rgba(15, 23, 42, 0.06);
  border-radius: 12px;
  padding: 1.25rem;
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
  transition: box-shadow 0.2s ease, transform 0.2s ease;

  &:hover {
    box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06);
    transform: translateY(-1px);
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

.stat-card__icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: #f0fdfa;
  color: #0d9488;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  :global(body.dark-mode) & {
    background: rgba(13, 148, 136, 0.12);
  }
}

.stat-card__body {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.stat-card__value {
  font-size: 0.95rem;
  font-weight: 600;
  color: #0f172a;
  word-break: break-all;

  :global(body.dark-mode) & {
    color: #f1f5f9;
  }
}

.stat-card__hint {
  font-size: 0.78rem;
  color: #94a3b8;
  margin-top: 0.15rem;
}

@media screen and (max-width: 480px) {
  .dashboard__stats {
    grid-template-columns: 1fr;
  }
}

/* ====== Section ====== */
.dashboard__section {
  margin-bottom: 2rem;
}

.dashboard__section-title {
  font-size: 0.85rem;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  margin: 0 0 0.85rem;

  :global(body.dark-mode) & {
    color: #94a3b8;
  }
}

/* ====== Quick Links ====== */
.dashboard__quick-links {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.75rem;
}

.quick-link-card {
  display: flex;
  align-items: center;
  gap: 0.85rem;
  padding: 1.05rem 1.15rem;
  background: #ffffff;
  border: 1px solid rgba(15, 23, 42, 0.06);
  border-radius: 12px;
  cursor: pointer;
  text-align: left;
  font-family: inherit;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
  transition: all 0.2s ease;

  &:hover {
    box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06);
    transform: translateY(-1px);
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

.quick-link-card__icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: #f8fafc;
  color: #64748b;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.2s ease;

  :global(body.dark-mode) & {
    background: rgba(255, 255, 255, 0.04);
    color: #94a3b8;
  }

  .quick-link-card:hover & {
    background: #f0fdfa;
    color: #0d9488;

    :global(body.dark-mode) & {
      background: rgba(13, 148, 136, 0.15);
      color: #2dd4bf;
    }
  }
}

.quick-link-card__content {
  flex: 1;
  min-width: 0;
}

.quick-link-card__label {
  display: block;
  font-size: 0.9rem;
  font-weight: 600;
  color: #0f172a;

  :global(body.dark-mode) & {
    color: #f1f5f9;
  }
}

.quick-link-card__desc {
  display: block;
  font-size: 0.78rem;
  color: #94a3b8;
  margin-top: 0.1rem;
}

.quick-link-card__arrow {
  color: #cbd5e1;
  flex-shrink: 0;
  transition: transform 0.2s ease;

  :global(body.dark-mode) & {
    color: #475569;
  }

  .quick-link-card:hover & {
    transform: translateX(2px);
    color: #0d9488;

    :global(body.dark-mode) & {
      color: #2dd4bf;
    }
  }
}

@media screen and (max-width: 600px) {
  .dashboard__quick-links {
    grid-template-columns: 1fr;
  }
}
</style>
