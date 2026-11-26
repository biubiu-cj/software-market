import type { NoticeNum } from '@/api/notice/notice'
import { reactive } from 'vue'
import { getNoticesNum } from '@/api/notice/notice'

const noticeNum = reactive<NoticeNum>({
  totalNum: 0,
  unreadTotalNum: 0,
  readTotalNum: 0,
})

// 同一时刻只发一个请求：WS 推送和页面挂载可能同时触发刷新
let refreshPromise: Promise<void> | null = null

function refreshNoticeNum(): Promise<void> {
  if (refreshPromise) {
    return refreshPromise
  }
  refreshPromise = getNoticesNum()
    .then((data) => {
      Object.assign(noticeNum, data)
    })
    .catch((error) => {
      console.error('Error refreshing notice number:', error)
      throw error
    })
    .finally(() => {
      refreshPromise = null
    })
  return refreshPromise
}

export function useNoticeStore() {
  return {
    noticeNum,
    refreshNoticeNum,
  }
}
