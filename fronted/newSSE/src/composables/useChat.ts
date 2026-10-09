import { reactive, ref } from 'vue'
import { getChatHistory, getChatNotice } from '@/api/chat/chat'
import { getInfoById } from '@/api/info/getInfo'
import { useUserStore } from '@/store/userStore'
import { useEventBus } from './useEventBus'
import { useWebSocket } from './useWebsocket'

export interface RelevantUser {
  userID: number
  email: string
  name: string
  avatarURL: string
  identity: string
  score: number
  unRead: number
}

export interface ChatMessageItem {
  chatMsgID: number
  targetUserID: number
  senderUserID: number
  content: string
  unread: number
  createdAt: string
  isAnonymous?: boolean // 新增：是否为匿名消息
}

export type Contact = RelevantUser & Partial<{ ban: string, phone: string, punishnum: number, intro: string, unRead: number, emailpush: boolean }>

interface ChatEventMap {
  RelevantUsers: {
    relevantUsers: RelevantUser[]
  }
  NewMessage: ChatMessageItem
  MessageAck: { code: number, msg: string }
}

// 客户端发送的信息
interface ChatMessage {
  Beat: { userID: number, beat: number }
  NewMessage: {
    senderUserID: number
    targetUserID: number
    content: string
    isAnonymous?: boolean // 新增：是否为匿名消息
  }
}

function isRelevantUsers(event: any): event is ChatEventMap['RelevantUsers'] {
  return 'relevantUsers' in event
}

function isNewMessage(event: any): event is ChatEventMap['NewMessage'] {
  return 'chatMsgID' in event && 'targetUserID' in event && 'senderUserID' in event && 'content' in event
}

function isMessageAck(event: any): event is ChatEventMap['MessageAck'] {
  return 'code' in event && 'msg' in event
}

// 单例模式
const api = {} as {
  connect: () => void
  disconnect: () => void
  sendChatMessage: (content: string, isAnonymous?: boolean) => boolean
  on: <K extends keyof ChatEventMap>(event: K, handler: (data: ChatEventMap[K]) => void) => void
  off: <K extends keyof ChatEventMap>(event: K, handler: (data: ChatEventMap[K]) => void) => void
  selectContact: (contact: Contact) => Promise<void>
  addContact: (contact: RelevantUser) => void
  updateChatNum: () => void
  setAnonymousMode: (anonymous: boolean) => void
}

let isInit = false
const { userInfo, token } = useUserStore()
const contacts = reactive<Contact[]>([])
const current = reactive<Contact>({
  userID: 0,
  email: '',
  name: '',
  avatarURL: '',
  identity: '',
  score: 0,
  unRead: 0,
})
// 当前会话的聊天记录
const chatHistory = reactive<ChatMessageItem[]>([])
const chatNum = ref(0)
// 当前是否在私信页面
const isOnChatPage = ref(false)
// 当前聊天是否为匿名模式
const isAnonymousMode = ref(false)

let dummyID = 100000000
function getDummyID() {
  dummyID += 1
  return dummyID
}

export function useChat() {
  const init = () => {
    if (isInit) {
      return
    }

    isInit = true
    // VITE_API_BASE_URL 可能是相对路径（如 /api），而 new URL 的 base 必须是绝对 URL。
    // 用当前页面 origin 兜底，同时兼容相对和绝对两种配置。
    const apiURL = new URL(import.meta.env.VITE_API_BASE_URL, window.location.origin)
    const wsURL = new URL('/websocket/auth/chat', apiURL)
    wsURL.searchParams.append('token', token.value)
    wsURL.protocol = wsURL.protocol.replace('http', 'ws')
    const { connect, sendWebSocketMessage, on: listenWebSocket, disconnect: disconnectWebSocket } = useWebSocket<ChatEventMap, ChatMessage>(wsURL, {
      keepAliveOptions: {
        interval: 30000,
        fn: (sendWebSocketMessage) => {
          sendWebSocketMessage({ userID: userInfo.userID, beat: 1 })
        },
      },
    })
    const { emit, on, off, cleanup } = useEventBus<ChatEventMap>()

    // 也许未来有机会可以再封装吧, 现在好像没什么用
    const sendMessage = sendWebSocketMessage

    const sendChatMessage = (content: string, isAnonymous?: boolean) => {
      if (current.userID === 0) {
        return false
      }

      // 如果没有指定匿名状态，使用当前的匿名模式
      const anonymousMode = isAnonymous !== undefined ? isAnonymous : isAnonymousMode.value

      const message = {
        senderUserID: userInfo.userID,
        targetUserID: current.userID,
        content,
        isAnonymous: anonymousMode,
      }
      const result = sendMessage(message)

      if (result) {
        chatHistory.push({
          ...message,
          createdAt: new Date().toISOString(),
          chatMsgID: getDummyID(),
          unread: 0,
        })
      }

      return result
    }

    const updateChatNum = () => {
      // TODO: 需要取消上一次请求, 不过之后再统一封装一个函数吧
      getChatNotice(userInfo.userID)
        .then((resp) => {
          console.log('[useChat] updateChatNum 响应:', resp)
          chatNum.value = resp.data
        })
        .catch((err) => {
          console.error('[useChat] updateChatNum 失败:', err)
          chatNum.value = 0
        })
    }

    const handleSocketMessage = <K extends keyof ChatEventMap>(data: ChatEventMap[K]) => {
      if (isNewMessage(data)) {
        console.log('[useChat] isNewMessage=true, targetUserID=', data.targetUserID, 'myUserID=', userInfo.userID, 'senderUserID=', data.senderUserID, 'currentUserID=', current.userID)
        if (data.targetUserID !== userInfo.userID) {
          console.log('[useChat] 不是发给我的消息，忽略')
          return
        }

        emit('NewMessage', data)
        if (data.senderUserID === current.userID) {
          console.log('[useChat] 当前会话消息，推入chatHistory')
          chatHistory.push(data)
          return
        }

        // 不在私信页面才更新未读数
        if (!isOnChatPage.value) {
          updateChatNum()
        }
        console.log('[useChat] 新消息，发送者不在当前会话，更新联系人列表')
        // 不是当前会话
        const idx = contacts.findIndex(it => it.userID === data.senderUserID)
        if (idx === -1) {
          // 新发来的信息
          getInfoById(data.senderUserID).then((res) => {
            contacts.unshift({
              ...res,
              unRead: 1,
              identity: '',
            })
          }).catch(() => {
            contacts.unshift({
              userID: data.senderUserID,
              email: '',
              name: '未知用户',
              avatarURL: '',
              identity: '',
              score: 0,
              unRead: 1,
            })
          })
        }
        else {
          const contact = contacts[idx]
          contact.unRead = (contact.unRead || 0) + 1
        }
      }

      if (isRelevantUsers(data)) {
        // 这个事件貌似只在最开始的时候触发一次
        // 不知道为什么要去重, 不过既然原本去重了, 这里也去重吧
        const seen = new Set<number>()
        const users = data.relevantUsers.reduce<Contact[]>((acc, currentUser) => {
          if (seen.has(currentUser.userID)) {
            return acc
          }
          acc.push(currentUser)
          seen.add(currentUser.userID)
          return acc
        }, [])
        Promise.all(users.map(user => getInfoById(user.userID))).then((resList) => {
          contacts.splice(0, contacts.length, ...users.map((user, index) => ({ ...user, ...resList[index], unRead: 0 })))
        })
      }

      if (isMessageAck(data)) {
        emit('MessageAck', data)
      }
    }

    listenWebSocket('Message', (data) => {
      console.log('[useChat] 收到WebSocket消息:', JSON.stringify(data))
      handleSocketMessage(data)
    })

    const addContact = (contact: RelevantUser) => {
      if (!contacts.find(c => c.userID === contact.userID)) {
        contacts.push(contact)
      }
    }

    const selectContact = async (contact: Contact) => {
      // 如果是同一个联系人且已有聊天记录，无需重新加载
      if (current.userID === contact.userID && chatHistory.length > 0) {
        return Promise.resolve()
      }
      // 在contacts中找到这个contact
      const idx = contacts.findIndex(c => c.userID === contact.userID)
      if (idx === -1) {
        return Promise.reject(new Error('Contact not found'))
      }

      // 立即扣减本地未读数
      const prevUnread = contacts[idx].unRead || 0
      chatNum.value = Math.max(0, chatNum.value - prevUnread)
      contacts[idx].unRead = 0
      Object.assign(current, contacts[idx])
      sessionStorage.setItem('chat_current_contact', JSON.stringify(current))
      chatHistory.splice(0, chatHistory.length)
      // 看上去是获取聊天记录, 实际上还有个已读功能
      await getChatHistory(userInfo.userID, current.userID).then((res) => {
        if (res.code === 200) {
          chatHistory.splice(0, chatHistory.length, ...res.data)
        }
      })
      // 服务端同步纠正
      updateChatNum()
    }

    const setAnonymousMode = (anonymous: boolean) => {
      isAnonymousMode.value = anonymous
    }

    Object.assign(api, {
      connect,
      disconnect() {
        cleanup()
        disconnectWebSocket()
      },
      sendChatMessage,
      on,
      off,

      addContact,
      selectContact,

      updateChatNum,
      setAnonymousMode,
    })

    // 刷新后恢复上次的聊天对象
    const saved = sessionStorage.getItem('chat_current_contact')
    if (saved && current.userID === 0) {
      try {
        const contact = JSON.parse(saved) as Contact
        if (contact.userID) {
          contacts.push(contact)
          Object.assign(current, contact)
        }
      }
      catch { /* ignore parse errors */ }
    }

    // 初始化时获取未读消息数
    if (userInfo.userID > 0) {
      updateChatNum()
    }
  }

  init()

  return {
    ...api,
    contacts,
    chatHistory,
    current,
    chatNum,
    isOnChatPage,
    isAnonymousMode,
  }
}
