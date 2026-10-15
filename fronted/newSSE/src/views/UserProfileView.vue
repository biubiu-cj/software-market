<script setup lang="ts">
import type { AllInfo } from '@/api/info/getInfo'
import { Icon } from '@iconify/vue'
import { onBeforeMount, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getInfoById } from '@/api/info/getInfo'
import { showMsg } from '@/components/MessageBox'
import UserAvatar from '@/components/UserAvatar.vue'
import { useUserStore } from '@/store/userStore'

const router = useRouter()

const user = ref<AllInfo>()
const { userInfo } = useUserStore()

onBeforeMount(() => {
  const { id } = useRoute().params
  getInfoById(Number(id))
    .then((res) => {
      user.value = res
    })
    .catch((error) => {
      if (error.response) {
        const res = error.response
        showMsg(`请求无效：${res.msg} (${res.code})`)
      }
      else {
        showMsg('请求无效')
      }
    })
})

function navigateChat() {
  if (user.value && user.value.userID > 0) {
    router.push({ name: 'Chat', query: { user: user.value.userID } })
    // stopPropagation()
  }
}
</script>

<template>
  <div v-if="user" class="page-section user-profile-wrapper">
    <div class="profile-card profile-header-wrapper">
      <div class="profile-header">
        <UserAvatar class="relative" :src="user.avatarURL" :user-name="user.name" :user-score="user.score" />
        <div v-if="userInfo.userID !== user.userID" class="btn-chat whitespace-nowrap" @click="navigateChat">
          <Icon icon="tabler:send" />
          <span class="whitespace-nowrap">
            私信
          </span>
        </div>
      </div>
    </div>
    <div class="flex flex-row">
      <div class="flex-1">
        <div class="profile-card profile-dynamic">
          <span>
            {{ user.intro || '没有个人简介呢' }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss">
.user-profile-wrapper {
  width: 100%;
}

.profile-header-wrapper {
  display: grid;
  width: 100%;
  min-height: 140px;
  align-items: end;
  padding: 0.5rem 0;

  .profile-header {
    display: flex;
    align-items: center;

    .btn-chat {
      display: inline-flex;
      align-items: center;
      min-height: 40px;
      color: var(--color-text);
      padding: 0.3em 1.1em;
      border: 1px solid var(--color-border);
      border-radius: var(--radius-md);
      background: var(--surface-1);
      cursor: pointer;
      -webkit-tap-highlight-color: transparent;
      align-self: flex-end;
      font-size: 0.9rem;
      font-weight: 500;
      transition:
        border-color 0.2s ease,
        color 0.2s ease;

      svg {
        vertical-align: middle;
        margin-right: 0.3em;
      }

      &:hover {
        color: var(--accent);
        border-color: var(--accent);
        background: var(--surface-1);
      }
    }
  }
}

.profile-card {
  background: var(--surface-1);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
  margin: 0.5em 0;
  padding: 1.1rem 1.2rem;
  border-radius: var(--radius-lg);
  color: var(--color-text);
}
</style>
