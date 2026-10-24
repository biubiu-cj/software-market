<script setup lang="ts">
import type { Comment, RatingComment } from '@/types/comment'
import type { Post, PostType, Rating } from '@/types/post'

import { computed, defineAsyncComponent, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCommentsByPostID } from '@/api/browse/getComment'

import { getPostByID, getPostTypeByID } from '@/api/browse/getPost'

import DetailCard from '@/components/card/DetailCard.vue'

import { showImg } from '@/components/ImageShower'
import { showMsg } from '@/components/MessageBox'
import PostAiAnalysis from '@/components/post/PostAiAnalysis.vue'
import RatingDetailCard from '@/components/rating/RatingDetailCard.vue'
import { useNewPostsStore } from '@/store/newPostsStore'
import { usePostStore } from '@/store/postStore'
import { strHandler } from '@/utils/strHandler'
import { setTitle } from '@/utils/title'

const { removeNewPostId } = useNewPostsStore()

const CommentCard = defineAsyncComponent(() => import('@/components/card/CommentCard.vue'))
const RatingCommentCard = defineAsyncComponent(() => import('@/components/rating/RatingCommentCard.vue'))

const route = useRoute()
const router = useRouter()

const { updatePost, posts: storePosts } = usePostStore()
const post = ref<Post | Rating | null>(null)
const comments = ref<Comment[]>([])

const postType = ref<PostType>('post')

type SortType = 'time' | 'likes'
const sortType = ref<SortType>('time')
const sortedComments = computed(() => {
  // 如果是按时间排序，直接返回原数组
  if (sortType.value === 'time') {
    return comments.value
  }
  // 只有按点赞数排序时才创建新数组并排序
  // .slice()是为了不改变原数组
  return comments.value.slice().sort((a, b) => b.LikeNum - a.LikeNum)
})
function setSortType(type: SortType) {
  sortType.value = type
}

function updateCommentCount() {
  if (!post.value)
    return
  let len = 0
  for (const comment of comments.value) {
    if (comment.SubComments) {
      len += comment.SubComments.length
    }
    len += 1 // 包括主评论
  }
  post.value.Comment = len
}

async function commentHandler() {
  if (!post.value)
    return
  await getCommentList()
  updateCommentCount()
}

async function getCommentList() {
  try {
    const ID = Number(route.params.id)
    const curComments = await getCommentsByPostID(ID)
    if (curComments)
      curComments.reverse()
    comments.value = curComments
  }
  catch (e) {
    showMsg(`获取评论失败: ${e}`)
  }
}

async function refreshPostData() {
  try {
    const ID = Number(route.params.id)
    const curPost = await getPostByID(ID, postType.value)
    post.value = curPost
  }
  catch (e) {
    showMsg(`获取帖子数据失败: ${e}`)
  }
}

/**
 *
 * @description 复制代码和展示图片。直接绑定根容器
 */
async function clickHandler(event: MouseEvent) {
  /**
   * 在css里已经去除了pre标签的点击，只保留了pre::before的点击
   */
  const el = event.target as HTMLElement
  if (!el)
    return
  if (el.tagName === 'CODE') {
    const code = el.textContent
    if (!code)
      return
    await navigator.clipboard.writeText(code)
    showMsg('代码已复制')
  }
  else if (el.tagName === 'IMG') {
    // 拿到图片的src
    const src = (el as HTMLImageElement).src
    // 如果class名为user-avatar，直接不展示
    if (el.classList.contains('user-avatar-img')) {
      return
    }
    const uploadImg = strHandler('postImg', src)
    showImg(uploadImg)
  }
}

function infoChange(type: 'like' | 'save' | 'delete' | 'comment' | 'rating') {
  const ID = Number(route.params.id)
  updatePost(ID, type)
  switch (type) {
    case 'like': {
      post.value!.IsLiked = !post.value!.IsLiked
      post.value!.Like += post.value!.IsLiked ? 1 : -1
      break
    }
    case 'save': {
      post.value!.IsSaved = !post.value!.IsSaved
      break
    }
    case 'delete': {
      router.push('/')
      break
    }
    case 'comment': {
      // 评论更新，重新获取评论列表
      commentHandler()
      break
    }
    case 'rating': {
      // 评分更新，重新获取帖子数据以更新评分信息
      refreshPostData()
      commentHandler()

      break
    }
  }
}

onMounted(async () => {
  try {
    const ID = Number(route.params.id)
    postType.value = await getPostTypeByID(ID)
    const curPost = await getPostByID(ID, postType.value)

    post.value = curPost
    setTitle(post.value.Title)

    // 同步浏览数到首页缓存
    const cached = storePosts.find(p => p.PostID === ID)
    if (cached) {
      cached.Browse = curPost.Browse
    }

    await getCommentList()
    updateCommentCount()
    // 从新帖子列表中移除当前帖子ID
    removeNewPostId(ID)
  }
  catch (e) {
    showMsg(`获取帖子失败: ${e}`)
  }
})
</script>

<template>
  <div
    class="root page-section w-full flex flex-col p-1%"
    @click="clickHandler"
    @comment-handle="commentHandler"
  >
    <template v-if="post">
      <template v-if="postType === 'post'">
        <DetailCard
          :post="post as Post"
          @info-change="infoChange"
        />
      </template>
      <template v-else-if="postType === 'rating'">
        <RatingDetailCard
          :post="post as Rating"
          @info-change="infoChange"
        />
      </template>
      <template v-else>
        <div>未知帖子类型</div>
      </template>
    </template>
    <div v-else>
      loading...
    </div>
    <PostAiAnalysis
      v-if="post && postType === 'post'"
      :post-id="post.PostID"
      class="mx-a w-15/16"
    />
    <div class="mt-5 h-a w-full">
      <div class="mb-3 flex items-center justify-between">
        <h2 class="comment-heading">
          评论
        </h2>
        <div class="sort-toggle">
          <button
            type="button"
            class="sort-btn"
            :class="{ active: sortType === 'time' }"
            @click="setSortType('time')"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <circle cx="12" cy="12" r="10" />
              <polyline points="12 6 12 12 16 14" />
            </svg>
            时间
          </button>
          <button
            type="button"
            class="sort-btn"
            :class="{ active: sortType === 'likes' }"
            @click="setSortType('likes')"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <polyline points="23 6 13.5 15.5 8.5 10.5 1 18" />
              <polyline points="17 6 23 6 23 12" />
            </svg>
            热度
          </button>
        </div>
      </div>
      <!-- 这是评论区 -->
      <div
        v-if="post"
        class="commentList min-w-full w-full"
      >
        <!-- 使用id-评论数作为key使每次评论重新渲染当前评论 -->
        <!-- Post类型固定渲染 -->
        <template v-if="postType === 'post'">
          <CommentCard
            v-for="comment in sortedComments"
            :key="comment.PcommentID"
            class="mx-a my-3 w-15/16"
            :post-id="post.PostID"
            :commentable="true"
            :is-dence="false"
            :comment="comment as Comment"
          />
        </template>

        <!-- Rating类型固定渲染 -->
        <template v-else-if="postType === 'rating'">
          <RatingCommentCard
            v-for="comment in sortedComments"
            :key="comment.PcommentID"
            class="mx-a my-3 w-15/16"
            :post-id="post.PostID"
            :commentable="false"
            :is-dence="false"
            :comment="comment as RatingComment"
          />
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
.root {
  color: var(--color-text);
}

.comment-heading {
  margin: 0.5rem;
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--color-text);
}

.sort-toggle {
  display: flex;
  gap: 2px;
  padding: 3px;
  background: var(--surface-2);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}

.sort-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.3rem;
  min-height: 34px;
  padding: 0 0.9rem;
  margin: 0;
  border: none;
  border-radius: calc(var(--radius-md) - 3px);
  background: transparent;
  color: var(--color-text-secondary);
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  transition:
    background-color 0.2s ease,
    color 0.2s ease,
    box-shadow 0.2s ease;
}

.sort-btn svg {
  width: 15px;
  height: 15px;
}

.sort-btn:hover {
  color: var(--color-text);
  background: transparent;
}

.sort-btn.active {
  background: var(--surface-1);
  color: var(--accent);
  box-shadow: var(--shadow-xs);
}
</style>
