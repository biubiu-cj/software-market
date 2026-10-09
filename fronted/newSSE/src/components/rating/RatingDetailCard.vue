<script setup lang="ts">
// 因为不会覆写sendcommentfunc所以复制了一遍
// 继承自DetailCard
import type { Rating } from '@/types/post'
import { defineAsyncComponent, nextTick, onMounted, onUnmounted, ref, triggerRef, watch } from 'vue'
import { getAverageRating, getStarsDistribution, getUserPostRating, sendComment, submitRating } from '@/api/editPostAndComment/editComment'
import { delPost } from '@/api/editPostAndComment/editPost'
import { submitReport } from '@/api/editPostAndComment/submitReport'
import { likePost } from '@/api/SaveAndLike/SaveAndLike'
import { showMsg } from '@/components/MessageBox'
import { useUserStore } from '@/store/userStore'

import BasicCard from '../card/BasicCard.vue'
import MarkdownContainer from '../MarkdownContainer.vue'
import UserAvatar from '../UserAvatar.vue'
import UserButton from '../UserButton.vue'
import RatingDistribution from './RatingDistribution.vue'

const { post } = defineProps<{
  post: Rating
}>()

const emits = defineEmits(['infoChange'])

const OldImages = defineAsyncComponent(() => import('@/components/OldImages.vue'))

const MarkdownEditor = defineAsyncComponent(
  () => import('../MarkdownEditor.vue'),
)

const commentButtonIsShow = ref(false)
const commentContent = ref('')
const commentRating = ref(post.UserRating || 0)
const reportReason = ref('')
const showReportInput = ref(false)

const { userInfo } = useUserStore()

const currRatingList = ref(post.Stars || [0, 0, 0, 0, 0])
const currRating = ref(post.UserRating || 0)
const averageRating = ref(post.Rating || 0)
const ratingListKey = ref(0)

// 响应式检测是否为移动端
const isMobile = ref(window.innerWidth <= 768) // 临时设置为true来测试效果

// 检测屏幕尺寸
function checkIsMobile() {
  isMobile.value = window.innerWidth <= 768
}

// 监听窗口大小变化
onMounted(() => {
  checkIsMobile()
  window.addEventListener('resize', checkIsMobile)
})

// 清理事件监听器
onUnmounted(() => {
  window.removeEventListener('resize', checkIsMobile)
})

// 移除强制刷新计数器，因为它会导致组件不必要的重新创建

// 监听 post 变化，更新评分相关数据
// 仅在有实际数据时才更新，避免 refreshPostData 返回 0 时覆盖 fetchUserRating 的结果
watch(() => post.UserRating, (newRating) => {
  if (typeof newRating === 'number' && newRating > 0) {
    currRating.value = newRating
    commentRating.value = newRating
  }
})

watch(() => post.Rating, (newRating) => {
  if (typeof newRating === 'number' && newRating > 0) {
    averageRating.value = newRating
  }
})

watch(() => post.Stars, (newStars) => {
  if (newStars && newStars.length === 5 && newStars.some(v => v > 0)) {
    currRatingList.value = [...newStars]
  }
})

// 移除对 post.stars 的监听，因为 post 是只读的 props

// 获取用户评分、评分分布和平均评分
async function fetchUserRating() {
  try {
    // 并行获取用户评分、评分分布和平均评分
    const [userRating, starsDistribution, averageRatingData] = await Promise.all([
      getUserPostRating(post.PostID),
      getStarsDistribution(post.PostID),
      getAverageRating(post.PostID),
    ])

    // 只有当后端返回的评分大于0时才更新，避免覆盖用户刚刚设置的评分
    if (userRating > 0) {
      currRating.value = userRating
      commentRating.value = userRating
    }

    // 直接更新响应式变量，不依赖 post.stars
    currRatingList.value = [...starsDistribution]

    // 更新平均评分
    averageRating.value = averageRatingData

    // 强制触发响应式更新
    triggerRef(currRating)
    triggerRef(commentRating)
    triggerRef(currRatingList)
    triggerRef(averageRating)

    // 增加评分列表的key以强制重新渲染
    ratingListKey.value++

    // 确保DOM更新
    await nextTick()
  }
  catch (error) {
    console.error('获取用户评分和评分分布失败:', error)
  }
}

// 组件挂载时获取用户评分
onMounted(() => {
  fetchUserRating()
})

// 更新本地评分数据（现在直接从后端获取最新数据，这个函数主要用于临时更新）
function updateLocalRatingData(newRating: number) {
  // 临时更新用户评分显示，实际数据会通过 fetchUserRating 从后端获取
  currRating.value = newRating
  commentRating.value = newRating
}

async function currentRatingClick(rating: number) {
  const oldRating = currRating.value

  // 立即显示新评分（乐观更新）
  currRating.value = rating
  commentRating.value = rating

  // 使用专门的评分API提交评分
  try {
    const success = await submitRating(
      post.PostID,
      rating,
    )

    if (success) {
      // 重新获取评分分布和平均分
      await fetchUserRating()

      emits('infoChange', 'rating')
    }
    else {
      // 恢复原评分
      currRating.value = oldRating
      commentRating.value = oldRating
    }
  }
  catch {
    showMsg('评分提交失败，请稍后再试')

    // 恢复原评分
    currRating.value = oldRating
    commentRating.value = oldRating
  }
}

// 提交评论（包含评分）
async function submitComment() {
  if (!commentContent.value.trim() && commentRating.value === 0) {
    showMsg('请输入评论内容或选择评分')
    return
  }

  const hasRating = commentRating.value > 0

  try {
    const success = await sendComment(
      commentContent.value,
      post.PostID,
      commentRating.value,
    )

    if (success) {
      showMsg('评论提交成功')

      // 如果有评分，更新本地评分数据
      if (hasRating) {
        currRating.value = commentRating.value
        updateLocalRatingData(commentRating.value)

        // 重新获取评分分布和平均分
        await fetchUserRating()
      }

      // 清空表单
      commentContent.value = ''
      commentRating.value = 0
      commentButtonIsShow.value = false

      // 触发更新事件
      emits('infoChange', 'comment')
    }
    else {
      showMsg('评论提交失败')
    }
  }
  catch {
    showMsg('评论提交出错')
  }
}

/**
 * @description 点赞功能
 */
async function like() {
  try {
    await likePost(post.PostID)
    // 触发事件通知父组件更新数据
    emits('infoChange', 'like')
  }
  catch (e) {
    showMsg('点赞失败')
    console.error(e)
  }
}

function useCustomEvent(type: 'delete' | 'save' | 'like') {
  emits('infoChange', type)
}

async function deleteRatingPost() {
  try {
    await delPost(post.PostID)
    useCustomEvent('delete')
  }
  catch (error) {
    console.error(error)
    showMsg('删除失败，请稍后再试')
  }
}

async function submitReportHandler() {
  const reason = reportReason.value.trim()
  if (!reason) {
    showMsg('请填写举报原因')
    return
  }
  try {
    await submitReport(Number(post.PostID), 'post', reason)
    showMsg('举报成功')
    reportReason.value = ''
    showReportInput.value = false
  }
  catch {
    showMsg('举报失败')
  }
}
</script>

<template>
  <BasicCard class="w-15/16">
    <div class="h-fit flex flex-row items-center">
      <UserAvatar
        :src="post.UserAvatar"
        :user-id="post.UserID"
        :user-identity="post.UserIdentity"
        :user-name="post.UserName"
        :user-score="post.UserScore"
      />
      <div v-if="isMobile">
        <UserButton :no-save="true" :is-saved="post.IsSaved" :is-self="post.UserID === userInfo.userID" @user-action="deleteRatingPost" />
      </div>
    </div>
    <div class="card-title">
      <h2>
        {{ post.Title || '' }}
      </h2>
    </div>

    <!-- 桌面端评分统计面板 - 放在左上方 -->
    <div v-if="!isMobile" class="rating-panel-small">
      <RatingDistribution
        :stars="currRatingList"
        :average-rating="averageRating"
        :user-rating="currRating"
        :is-mobile="false"
        :show-user-rating="true"
        @rating-click="currentRatingClick"
      />
    </div>

    <MarkdownContainer
      :markdown-content="post.Content || 'loading'"
    />
    <template v-if="post.Photos">
      <OldImages :photos="post.Photos" />
    </template>

    <!-- 手机端评分面板 - 放在帖子内容下方 -->
    <div v-if="isMobile">
      <RatingDistribution
        :stars="currRatingList"
        :average-rating="averageRating"
        :user-rating="currRating"
        :is-mobile="true"
        :show-user-rating="true"
        @rating-click="currentRatingClick"
      />
    </div>

    <div class="post-actions-container">
      <div class="post-info">
        <span class="post-time text-3">{{
          new Date(post.PostTime).toLocaleString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
          })
        }}</span>
        <span class="post-browse">浏览 {{ post.Browse }}</span>
      </div>
      <div class="action-buttons">
        <button v-if="post.UserID !== userInfo.userID" class="report-btn" @click="showReportInput = !showReportInput">
          举报
        </button>
        <button class="like-button" :class="{ liked: post.IsLiked }" @click="like">
          <span class="like-icon">{{ post.IsLiked ? '❤️' : '🤍' }}</span>
          <span class="like-count">{{ post.Like }}</span>
        </button>
        <div class="commentButton">
          <button @click="commentButtonIsShow = !commentButtonIsShow">
            {{ commentButtonIsShow ? '隐藏' : '发评论' }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="showReportInput" class="report-input">
      <input v-model="reportReason" placeholder="请填写举报原因" @keyup.enter="submitReportHandler">
      <button @click="submitReportHandler">
        提交举报
      </button>
    </div>

    <div v-if="post.UserID === userInfo.userID && !isMobile" class="deleteButton">
      <button @click="deleteRatingPost()">
        删除
      </button>
    </div>

    <MarkdownEditor
      v-if="commentButtonIsShow"
      v-model="commentContent"
      class="comment-editor max-w-full"
      @send="submitComment"
    />
  </BasicCard>
</template>

<style scoped>
/* 确保卡片容器有相对定位 */
.w-15\/16 {
  position: relative;
}

.card-title {
  margin-top: 10px;
  margin-bottom: 8px;
}

/* 小型评分统计面板 - 左上方 */
.rating-panel-small {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 10;
}

.rating-summary-small {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 8px 10px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(248, 250, 252, 0.9) 100%);
  border-radius: 12px;
  border: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow:
    0 6px 16px rgba(0, 0, 0, 0.08),
    0 3px 6px rgba(0, 0, 0, 0.04);
  backdrop-filter: blur(16px);
  min-width: 120px;
  transform: scale(0.85);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.rating-summary-small:hover {
  transform: translateY(-2px) scale(0.9);
  box-shadow:
    0 12px 28px rgba(0, 0, 0, 0.12),
    0 6px 12px rgba(0, 0, 0, 0.08);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 250, 252, 0.95) 100%);
}

.rating-main {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(240, 240, 240, 0.6);
  position: relative;
}

.rating-main::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 50%;
  transform: translateX(-50%);
  width: 30px;
  height: 1px;
  background: linear-gradient(90deg, transparent 0%, #4a90e2 50%, transparent 100%);
}

.rating-number-small {
  font-size: 14px;
  font-weight: 800;
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 2px 4px rgba(37, 99, 235, 0.15);
}

.rating-stars-small {
  font-size: 12px;
  color: #4a90e2;
  filter: drop-shadow(0 2px 4px rgba(74, 144, 226, 0.3));
  animation: twinkle 2s ease-in-out infinite alternate;
}

@keyframes twinkle {
  0% {
    transform: scale(1);
  }
  100% {
    transform: scale(1.1);
  }
}

.rating-breakdown {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.rating-item-small {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 9px;
  margin-bottom: 2px;
  padding: 1px 0;
  border-radius: 3px;
  transition: background-color 0.2s ease;
}

.rating-item-small:hover {
  background: rgba(74, 144, 226, 0.05);
}

.star-info {
  display: flex;
  align-items: center;
  gap: 2px;
  min-width: 18px;
}

.star-num {
  color: #64748b;
  font-weight: 600;
  font-size: 9px;
  background: linear-gradient(135deg, #64748b 0%, #475569 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.star-icon {
  color: #4a90e2;
  font-size: 9px;
  filter: drop-shadow(0 1px 2px rgba(74, 144, 226, 0.3));
}

.progress-container-small {
  flex: 1;
  height: 4px;
  background: linear-gradient(90deg, #f1f5f9 0%, #e2e8f0 100%);
  border-radius: 2px;
  overflow: hidden;
  margin: 0 4px;
  box-shadow: inset 0 1px 2px rgba(0, 0, 0, 0.1);
}

.progress-bar-small {
  height: 100%;
  background: linear-gradient(90deg, #4a90e2 0%, #357abd 50%, #1d4ed8 100%);
  border-radius: 2px;
  transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 3px rgba(74, 144, 226, 0.3);
}

.star-count {
  color: #2563eb;
  font-weight: 700;
  min-width: 14px;
  text-align: right;
  font-size: 9px;
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 帖子操作容器 */
.post-actions-container {
  position: relative;
  margin: 12px 0;
  padding: 8px 0;
  border-top: 1px solid #f0f0f0;
}

/* 简化的帖子信息 */
.post-info {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 14px;
  color: #666;
  margin: 0;
  padding: 0;
  border: none;
}

/* 操作按钮容器 - 浮动到右侧 */
.action-buttons {
  position: absolute;
  top: 0;
  right: 0;
  display: flex;
  align-items: flex-start;
  gap: 8px;
  height: auto;
  max-height: 40px; /* 限制最大高度 */
}

/* .post-time 样式现在由 text-3 类处理 */

.post-browse {
  color: #666;
}

/* 点赞按钮样式 */
.like-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 6px 12px;
  background: transparent;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 14px;
  min-width: 80px;
  margin-left: auto;
  margin-right: 5px;
}

.like-button:hover {
  background: #f5f5f5;
  border-color: #ccc;
}

.like-button.liked {
  background: #ffe6e6;
  border-color: #ff6b6b;
}

.like-icon {
  font-size: 16px;
}

.like-count {
  color: #666;
  font-weight: 500;
}

.like-button.liked .like-count {
  color: #ff6b6b;
}

/* 响应式布局控制现在通过 v-if 实现 */

/* 手机端虎扑风格评分分布样式 */
.mobile-rating-breakdown {
  margin-top: 4px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.mobile-rating-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 12px;
  padding: 0;
}

.mobile-stars {
  display: flex;
  align-items: center;
  gap: 1px;
}

.mobile-star-filled {
  font-size: 8px;
  color: #4a90e2;
  line-height: 1;
}

.mobile-percentage {
  font-size: 7px;
  font-weight: 600;
  color: #2563eb;
  min-width: 20px;
  text-align: right;
}

/* 手机端用户评分星星样式 */
.mobile-user-rating {
  display: flex;
  align-items: center;
  gap: 4px;
}

.mobile-rating-star {
  font-size: 16px;
  color: #d1d5db;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
  padding: 2px;
}

.mobile-rating-star.filled {
  color: #4a90e2;
}

.mobile-rating-star:hover {
  color: #4a90e2;
  transform: scale(1.2);
}

/* 手机端评分面板样式 */
.mobile-rating-panel {
  margin: 16px 0;
  padding: 0 8px;
}

.mobile-rating-container {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(248, 250, 252, 0.9) 100%);
  border-radius: 12px;
  border: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  padding: 16px;
  width: 100%;
}

.mobile-rating-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(240, 240, 240, 0.6);
}

.mobile-rating-title {
  font-size: 16px;
  font-weight: 600;
  color: #374151;
}

.mobile-average-rating {
  font-size: 18px;
  font-weight: 700;
  color: #4a90e2;
}

.mobile-user-rating-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  padding: 8px 12px;
  background: rgba(74, 144, 226, 0.1);
  border-radius: 8px;
  border: 1px solid rgba(74, 144, 226, 0.2);
}

.mobile-user-rating-label {
  font-size: 14px;
  font-weight: 600;
  color: #4a90e2;
}

.mobile-rating-breakdown {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.mobile-rating-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 16px;
  padding: 0;
}

.mobile-stars {
  display: flex;
  align-items: center;
  gap: 2px;
}

.mobile-star-filled {
  font-size: 12px;
  color: #4a90e2;
  line-height: 1;
}

.mobile-percentage {
  font-size: 12px;
  font-weight: 600;
  color: #2563eb;
  min-width: 30px;
  text-align: right;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .rating-panel-small {
    top: 60px;
    right: 8px;
  }

  .rating-summary-small {
    padding: 6px 8px;
    min-width: 90px;
    max-width: 100px;
    transform: scale(0.8);
  }

  .rating-main {
    padding-bottom: 6px;
    gap: 4px;
    border-bottom: 1px solid rgba(240, 240, 240, 0.6);
  }

  .rating-number-small {
    font-size: 12px;
  }

  .rating-stars-small {
    font-size: 10px;
  }

  .user-rating-section {
    margin: 6px 0;
    padding: 4px;
    gap: 4px;
    background: rgba(74, 144, 226, 0.1);
    border-radius: 4px;
    border: 1px solid rgba(74, 144, 226, 0.2);
  }

  .user-rating-label {
    font-size: 8px;
    font-weight: 600;
    color: #4a90e2;
  }

  .rating-hint {
    font-size: 6px;
    color: #6b7280;
    font-style: italic;
  }

  .post-actions-container {
    position: relative;
    padding-bottom: 20px; /* 减少为浮动按钮留出的空间 */
  }

  .post-info {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }

  .action-buttons {
    position: absolute;
    top: 0;
    right: 0;
    flex-direction: column;
    gap: 4px;
  }
}

/* 深色模式支持 */
@media (prefers-color-scheme: dark) {
  .rating-summary-small {
    background: linear-gradient(135deg, rgba(31, 41, 55, 0.95) 0%, rgba(17, 24, 39, 0.9) 100%);
    border-color: rgba(55, 65, 81, 0.8);
  }

  .rating-summary-small:hover {
    background: linear-gradient(135deg, rgba(31, 41, 55, 0.98) 0%, rgba(17, 24, 39, 0.95) 100%);
  }

  .rating-main {
    border-bottom-color: rgba(55, 65, 81, 0.6);
  }

  .rating-main::after {
    background: linear-gradient(90deg, transparent 0%, #60a5fa 50%, transparent 100%);
  }

  .rating-number-small {
    background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  .rating-stars-small {
    color: #60a5fa;
    filter: drop-shadow(0 2px 4px rgba(96, 165, 250, 0.3));
  }

  .rating-item-small:hover {
    background: rgba(96, 165, 250, 0.1);
  }

  .star-num {
    background: linear-gradient(135deg, #9ca3af 0%, #6b7280 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  .star-icon {
    color: #60a5fa;
    filter: drop-shadow(0 1px 2px rgba(96, 165, 250, 0.3));
  }

  .progress-container-small {
    background: linear-gradient(90deg, #374151 0%, #1f2937 100%);
  }

  .progress-bar-small {
    background: linear-gradient(90deg, #60a5fa 0%, #3b82f6 50%, #1d4ed8 100%);
    box-shadow: 0 1px 3px rgba(96, 165, 250, 0.3);
  }

  .star-count {
    background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  .like-button {
    background: #374151;
    border-color: #4b5563;
    color: #d1d5db;
  }

  .like-button:hover {
    background: #4b5563;
  }

  .like-button.liked {
    background: #451a03;
    border-color: #dc2626;
    color: #fca5a5;
  }

  /* 评论功能深色模式 */
  .commentButton button {
    background: #60a5fa;
  }

  .commentButton button:hover {
    background: #3b82f6;
  }

  /* 评论功能深色模式 */
  .deleteButton button {
    background: rgb(255, 21, 21);
  }

  .deleteButton button:hover {
    background: rgb(255, 51, 51);
  }

  .rating-selector {
    background: #1f2937;
    border-color: #374151;
  }

  .rating-label {
    color: #d1d5db;
  }

  .rating-display {
    color: #60a5fa;
  }

  .user-rating-section {
    background: rgba(96, 165, 250, 0.1);
    border-color: rgba(96, 165, 250, 0.2);
  }

  .user-rating-label {
    color: #60a5fa;
  }

  .rating-hint {
    color: #9ca3af;
  }
}

/* 浮动评论按钮样式 */
.commentButton {
  display: flex;
  justify-content: flex-end;
  margin-top: 0;
}

/* 评论编辑器样式 */
.comment-editor {
  margin-top: 10px;
  clear: both;
}

.commentButton button {
  margin-left: 5px;
  margin-right: 5px;
  background: #4a90e2;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 6px 12px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s ease;
  min-width: 80px;
}

.commentButton button:hover {
  background: #357abd;
}

.report-btn {
  background: #e74c3c !important;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 6px 12px;
  font-size: 14px;
  cursor: pointer;
  min-width: 80px;
}

.report-btn:hover {
  background: #c0392b !important;
}

.report-input {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  justify-content: flex-end;
}

.report-input input {
  flex: 1;
  max-width: 300px;
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.report-input button {
  background: #e74c3c;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 6px 12px;
  font-size: 14px;
  cursor: pointer;
}

.report-input button:hover {
  background: #c0392b;
}

/* 评论按钮样式 - 与普通帖子保持一致 */
.deleteButton {
  display: flex;
  margin-top: 10px;
}

.deleteButton button {
  margin-left: 5px;
  margin-right: 5px;
  background: rgb(255, 50, 50);
  color: white;
  border: none;
  border-radius: 4px;
  padding: 6px 12px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.deleteButton button:hover {
  background: rgb(255, 25, 25);
}

/* 评分选择器样式 */
.rating-selector {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 10px 0;
  padding: 8px 12px;
  background: #f8fafc;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
}

.rating-label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.rating-display {
  font-size: 14px;
  font-weight: 600;
  color: #4a90e2;
}

/* 用户评分区域样式 */
.user-rating-section {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 6px 0;
  padding: 6px;
  background: rgba(74, 144, 226, 0.1);
  border-radius: 4px;
  border: 1px solid rgba(74, 144, 226, 0.2);
}

.user-rating-label {
  font-size: 10px;
  font-weight: 600;
  color: #4a90e2;
}

.rating-hint {
  font-size: 8px;
  color: #6b7280;
  font-style: italic;
  margin-left: auto;
}
</style>
