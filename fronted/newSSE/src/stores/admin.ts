import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { AdminInfo } from '@/api/admin'

export interface AdminState {
  account: string
  name: string
  avatarUrl: string
  identity: string
}

export const useAdminStore = defineStore('admin', () => {
  const token = ref<string>(localStorage.getItem('adminToken') || '')
  const adminInfo = ref<AdminState>({
    account: '',
    name: '',
    avatarUrl: '',
    identity: '',
  })

  const isLoggedIn = computed<boolean>(() => !!token.value)

  function setToken(newToken: string): void {
    token.value = newToken
    localStorage.setItem('adminToken', newToken)
  }

  function setAdminInfo(info: AdminInfo): void {
    adminInfo.value = {
      account: info.account || '',
      name: info.name || '',
      avatarUrl: info.avatarUrl || '',
      identity: info.identity || '',
    }
  }

  function clearToken(): void {
    token.value = ''
    adminInfo.value = { account: '', name: '', avatarUrl: '', identity: '' }
    localStorage.removeItem('adminToken')
  }

  return { token, adminInfo, isLoggedIn, setToken, setAdminInfo, clearToken }
})
