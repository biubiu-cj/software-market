import type { Post } from '@/types/post'

import { reactive, ref, unref } from 'vue'

import { getPosts, getPostsNum } from '@/api/browse/getPost'

export const Partitions = ['主页', '收藏', '历史', '课程专区', '优质贴', '课程交流', '日常吐槽', '打听求助', '学习交流', '院务', '求职招募', '其他', '打分'] as const
export const SearchSort = ['home', 'save', 'history', 'rating'] as const

export interface Condition {
  limit: number
  offset: number
  category: typeof Partitions[number]
  searchSort: typeof SearchSort[number]
  searchInfo: string
  tag: string
}

const totalNum = ref(0)

const posts = reactive<Post[]>([])

const conditions = reactive<Condition>({
  limit: initLimit() + 1,
  offset: 0,
  category: '主页',
  searchSort: 'home',
  searchInfo: '',
  tag: '',
})

/**
 *
 * @returns 是否还有帖子可以获取
 */
async function addPost(): Promise<boolean> {
  const data = await getPosts({
    ...conditions,
  })
  if (!data || data.length === 0)
    return false
  posts.push(...data)
  conditions.offset += conditions.limit
  return conditions.limit === data.length
}

async function restorePosts(cachePosts: Post[], cacheTotalNum: number, cacheConditions: typeof conditions) {
  cachePosts = unref(cachePosts)
  cacheTotalNum = unref(cacheTotalNum)
  cacheConditions = { ...unref(cacheConditions) }
  posts.splice(0, posts.length, ...cachePosts)
  totalNum.value = cacheTotalNum
  conditions.limit = cacheConditions.limit
  conditions.offset = cacheConditions.offset
  conditions.category = cacheConditions.category
  conditions.searchSort = cacheConditions.searchSort
  conditions.searchInfo = cacheConditions.searchInfo
  conditions.tag = cacheConditions.tag
  await getNewPosts()
}

function storePosts() {
  const cachePosts = unref(posts)
  const cacheTotalNum = unref(totalNum)
  const cacheConditions = { ...unref(conditions) }
  return { cachePosts, cacheTotalNum, cacheConditions }
}

function updatePost(id: number, type: 'like' | 'save' | 'delete' | 'rating' | 'comment') {
  const target = posts.findIndex(p => p.PostID === id)
  if (target === -1)
    return
  const post = posts[target]
  switch (type) {
    case 'like':
      post.IsLiked = !post.IsLiked
      post.Like += post.IsLiked ? 1 : -1
      break
    case 'save':
      post.IsSaved = !post.IsSaved
      break
    case 'delete':
      posts.splice(target, 1)
      // 最稳妥的办法是重新获取一次信息, 但是offset是难以确认的
      // 低并发的情况下基本可以与服务端同步, 只要用户不长时间不刷新, 未来并发量高了再改吧
      // 如果后端是根据id来获取帖子列表就好了
      conditions.offset--
      totalNum.value--
      break
    case 'comment':
      post.Comment++
      break
  }
}

async function updateNum() {
  const num = await getPostsNum({ ...conditions })
  totalNum.value = num
}

async function getNewPosts() {
  const num = await getPostsNum({ ...conditions })
  if (num <= totalNum.value)
    return
  const oldOffset = conditions.offset
  const oldLimit = conditions.limit
  conditions.offset = 0
  conditions.limit = num - totalNum.value
  getPosts({ ...conditions }).then((newPosts) => {
    if (!newPosts || newPosts.length === 0)
      return
    posts.unshift(...newPosts)
  })
  conditions.offset = oldOffset + conditions.limit
  conditions.limit = oldLimit
  totalNum.value = num
}

function refreshPosts() {
  posts.splice(0, posts.length)
  totalNum.value = 0
  conditions.offset = 0
  conditions.category = '主页'
  conditions.searchInfo = ''
  conditions.searchSort = 'home'
  conditions.tag = ''
}

function changeTo(p: typeof Partitions[number], tag: string = '') {
  switch (p) {
    case '主页':
      conditions.category = '主页'
      conditions.searchInfo = ''
      conditions.searchSort = 'home'
      conditions.tag = tag
      break
    case '收藏':
      conditions.category = '收藏'
      conditions.searchInfo = ''
      conditions.searchSort = 'save'
      conditions.tag = tag
      break
    case '历史':
      conditions.category = '历史'
      conditions.searchInfo = ''
      conditions.searchSort = 'history'
      conditions.tag = tag
      break
    case '打分':
      conditions.category = '打分'
      conditions.searchInfo = ''
      conditions.searchSort = 'rating'
      conditions.tag = tag
      break
    default:
      conditions.category = p
      conditions.searchInfo = ''
      conditions.searchSort = 'home'
      conditions.tag = tag
      break
  }
}

function search(sinfo: string, p?: typeof Partitions[number]) {
  conditions.searchInfo = sinfo
  if (p) {
    conditions.category = p
  }
}

function initLimit() {
  const h = window.innerHeight
  const num = Math.floor(h / 250)
  return num
}

export function usePostStore() {
  return {
    updateNum,
    posts,
    restorePosts,
    storePosts,
    addPost,
    updatePost,
    changeTo,
    refreshPosts,
    search,
  }
}
