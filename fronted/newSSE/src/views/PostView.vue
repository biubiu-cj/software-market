<!-- eslint-disable vue/html-self-closing -->
<script setup lang="ts">
import type { Tag } from '@/api/info/getTeacher'

import { onMounted, onUnmounted, ref, useTemplateRef } from 'vue'
import { useRouter } from 'vue-router'
import { sendPost } from '@/api/editPostAndComment/editPost'

import { getTeachers } from '@/api/info/getTeacher'
import MarkdownEditor from '@/components/MarkdownEditor.vue'
import { showMsg } from '@/components/MessageBox'
import { useDraft } from '@/composables/useDraft'

const router = useRouter()
const { current, drafts, addDraft, selectDraft, deleteDraft, updateDraft, syncDrafts } = useDraft()
const partitions = ref([
  '主页',
  '日常吐槽',
  '打听求助',
  '学习交流',
  '院务',
  '课程专区',
  '求职招募',
  '其他',
  '打分',
])
const postContent = ref('')
const title = useTemplateRef('title')
const partition = ref('主页')

const teachers = ref<Tag[]>([])
// 好抽象的命名，完全想不到老师居然是作为tagList传过去的
const teacher = ref('')

// const currentDraftId = ref<number | null>(null)
let autoSaveTimer: number | null = null

async function loadDraft(id: number) {
  selectDraft(id)
  if (title.value)
    title.value.value = current.title
  postContent.value = current.content
}

async function removeDraft(id: number) {
  if (!id) {
    return
  }
  await deleteDraft(id)
  showMsg('草稿已删除')
}

async function saveDraft() {
  const draftTitle = title.value?.value || ''
  const draftContent = postContent.value

  if (!draftTitle && !draftContent) {
    showMsg('请输入标题或内容')
    return
  }

  const newDraftId = await addDraft({ title: draftTitle, content: draftContent }) as unknown as number
  selectDraft(newDraftId)
  showMsg('草稿已保存')
}

function autoSaveDraft() {
  if (!current.id) {
    return Promise.resolve(false)
  }
  const draftTitle = title.value?.value || ''
  const draftContent = postContent.value

  if (draftTitle || draftContent) {
    return updateDraft({ id: current.id, title: draftTitle, content: draftContent })
      .then(() => {
        return true
      })
  }
  return Promise.resolve(false)
}

function discardDraft() {
  current.content = ''
  current.title = ''
  current.id = 0
}

async function submitPost() {
  if (!title.value)
    return
  let postTitle = title.value.value
  const content = postContent.value
  const postPartition = partition.value

  // 去除标题的空格
  postTitle = postTitle.replace(/(^\s*)|(\s*$)/g, '')

  if (!postTitle || !content) {
    showMsg('请填写完整信息')
    return
  }
  const res = await sendPost(
    content,
    postPartition,
    '',
    teacher.value,
    postTitle,
  )
  router.push('/')
  showMsg(res.msg)
}

onMounted(async () => {
  const data = await getTeachers()
  teachers.value = data

  await syncDrafts()

  // 加载最新草稿
  if (drafts.length > 0) {
    const latestDraft = drafts[drafts.length - 1] // 默认加载最后一个
    if (latestDraft.id !== undefined) {
      selectDraft(latestDraft.id)
      if (title.value) {
        title.value.value = latestDraft.title
      }
      postContent.value = latestDraft.content
    }
  }

  // 启动自动保存，每20s保存一次
  autoSaveTimer = setInterval(() => {
    autoSaveDraft()
      .then((saved) => {
        if (saved) {
          showMsg('草稿已自动保存')
        }
      })
  }, 20000)
})

onUnmounted(() => {
  if (autoSaveTimer) {
    clearInterval(autoSaveTimer)
  }
})
</script>

<template>
  <div class="root page-section">
    <h2 class="page-heading">
      发布帖子
    </h2>
    <div class="compose-layout">
      <!-- 主编辑区 -->
      <div class="compose-card">
        <div class="form-field">
          <label for="postTitle">标题</label>
          <input
            id="postTitle"
            ref="title"
            type="text"
            placeholder="给帖子起个标题"
          >
        </div>

        <div class="form-field">
          <label for="postPartition">分区</label>
          <div class="select-row">
            <select
              id="postPartition"
              v-model="partition"
            >
              <option
                v-for="(p, index) in partitions"
                :key="index"
                :value="p"
              >
                {{ p }}
              </option>
            </select>
            <select
              v-if="partition === '课程专区'"
              v-model="teacher"
              aria-label="选择老师"
            >
              <option
                v-for="t in teachers"
                :key="t.TagID"
                :value="t.Name"
              >
                {{ t.Name }}
              </option>
            </select>
          </div>
        </div>

        <div class="form-field">
          <label>正文</label>
          <MarkdownEditor
            v-model="postContent"
            @send="submitPost"
          />
        </div>
      </div>

      <!-- 草稿面板 -->
      <aside class="drafts-card">
        <div class="drafts-head">
          <h3>草稿</h3>
          <span class="drafts-tip">手动保存一次后，每 20s 自动保存</span>
        </div>
        <div class="drafts-actions">
          <button type="button" class="ghost-btn" @click="discardDraft">
            不用草稿
          </button>
          <button type="button" class="ghost-btn" @click="autoSaveDraft">
            更新
          </button>
          <button type="button" class="ghost-btn primary" @click="saveDraft">
            保存
          </button>
        </div>
        <div v-if="drafts.length === 0" class="no-drafts">
          暂无草稿
        </div>
        <div v-else class="draft-list">
          <div
            v-for="draft in drafts"
            :key="draft.id"
            class="draft-item"
            :class="{ active: draft.id === current.id }"
          >
            <div class="draft-header">
              <h4 @click="loadDraft(draft.id!)">
                {{ draft.title || '无标题' }}
              </h4>
              <button type="button" class="delete-btn" @click="removeDraft(draft.id!)">
                删除
              </button>
            </div>
            <p @click="loadDraft(draft.id!)">
              {{ draft.content.slice(0, 10) }}...
            </p>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.root {
  width: 100%;
  color: var(--color-text);
}

.compose-layout {
  display: flex;
  align-items: flex-start;
  gap: 1.25rem;
}

/* 主编辑卡片 */
.compose-card {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  background: var(--surface-1);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  padding: clamp(1.1rem, 3vw, 1.75rem);
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-field label {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-text);
}

.form-field > input[type='text'] {
  width: 100%;
  min-height: 2.75rem;
  padding: 0 0.9rem;
  font-size: 1rem;
  font-family: var(--font-sans);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--surface-2);
  color: var(--color-text);
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    background-color 0.2s ease;
}

.form-field > input[type='text']::placeholder {
  color: var(--color-text-secondary);
}

.form-field > input[type='text']:focus {
  outline: none;
  border-color: var(--accent);
  background: var(--surface-1);
  box-shadow: 0 0 0 3px var(--accent-muted);
}

.select-row {
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem;
}

.select-row select {
  min-width: 10rem;
  min-height: 2.6rem;
  padding: 0 0.8rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  background: var(--surface-2);
  color: var(--color-text);
  font-family: var(--font-sans);
  font-size: 0.95rem;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    background-color 0.2s ease;
}

.select-row select:focus {
  outline: none;
  border-color: var(--accent);
  background: var(--surface-1);
  box-shadow: 0 0 0 3px var(--accent-muted);
}

/* 草稿面板 */
.drafts-card {
  position: sticky;
  top: 6.5rem;
  width: 300px;
  flex-shrink: 0;
  background: var(--surface-1);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  padding: 1rem 1.1rem;
  max-height: calc(100vh - 8rem);
  overflow-y: auto;
}

.drafts-head {
  margin-bottom: 0.75rem;
}

.drafts-head h3 {
  margin: 0 0 0.2rem;
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--color-text);
}

.drafts-tip {
  display: block;
  font-size: 0.75rem;
  color: var(--color-text-secondary);
}

.drafts-actions {
  display: flex;
  gap: 0.4rem;
  margin-bottom: 0.9rem;
}

.ghost-btn {
  flex: 1;
  min-height: 34px;
  margin: 0;
  padding: 0 0.6rem;
  background: var(--surface-1);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 0.8rem;
  font-weight: 500;
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    color 0.2s ease,
    background-color 0.2s ease;
}

.ghost-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
  background: var(--surface-1);
}

.ghost-btn.primary {
  background: var(--accent);
  border-color: var(--accent);
  color: var(--accent-foreground);
}

.ghost-btn.primary:hover {
  background: var(--accent-hover);
  border-color: var(--accent-hover);
  color: var(--accent-foreground);
}

.no-drafts {
  text-align: center;
  color: var(--color-text-secondary);
  font-size: 0.85rem;
  padding: 1.25rem 0;
}

.draft-list {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
}

.draft-item {
  padding: 0.65rem 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--surface-1);
  cursor: pointer;
  transition:
    background-color 0.2s ease,
    border-color 0.2s ease;
}

.draft-item:hover {
  background-color: var(--color-hover);
}

.draft-item.active {
  background-color: var(--accent-muted);
  border-color: color-mix(in srgb, var(--accent) 40%, var(--color-border));
}

.draft-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 0.5rem;
}

.draft-item h4 {
  margin: 0 0 0.2rem;
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-width: 0;
}

.draft-item p {
  margin: 0;
  font-size: 0.78rem;
  color: var(--color-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.delete-btn {
  flex-shrink: 0;
  min-height: 26px;
  margin: 0;
  padding: 0 0.5rem;
  background: transparent;
  border: 1px solid transparent;
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
  font-size: 0.75rem;
  cursor: pointer;
  transition:
    color 0.2s ease,
    border-color 0.2s ease;
}

.delete-btn:hover {
  color: var(--color-error);
  border-color: color-mix(in srgb, var(--color-error) 40%, transparent);
  background: transparent;
}

@media screen and (max-width: 900px) {
  .compose-layout {
    flex-direction: column;
  }

  .drafts-card {
    position: static;
    width: 100%;
    max-height: none;
  }
}
</style>
