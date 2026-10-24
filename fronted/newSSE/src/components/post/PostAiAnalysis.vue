<script setup lang="ts">
import type { AiCitation, AiRelatedPost, PostAiAnalysis } from '@/types/ai'
import { computed, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getPostAiAnalysis } from '@/api/ai/postAi'

const props = defineProps<{
  postId: number
}>()

const router = useRouter()

const loading = ref(true)
const failed = ref(false)
const data = ref<PostAiAnalysis | null>(null)
let requestSeq = 0
let disposed = false

const status = computed(() => data.value?.status)

const showDisabled = computed(() => !failed.value && status.value === 'DISABLED')
const showUnavailable = computed(() =>
  !failed.value && (status.value === 'SERVICE_UNAVAILABLE' || status.value === 'RATE_LIMITED'),
)
const showModelError = computed(() => !failed.value && status.value === 'MODEL_ERROR')
const showEmpty = computed(() => !failed.value && status.value === 'NO_HISTORY')
const showReady = computed(() => !failed.value && status.value === 'READY')
const knowStatuses = new Set(['READY', 'NO_HISTORY', 'SERVICE_UNAVAILABLE', 'MODEL_ERROR', 'DISABLED', 'RATE_LIMITED'])
const showUnknown = computed(() =>
  !failed.value && !!data.value && !knowStatuses.has(data.value.status),
)

const unavailableText = computed(() =>
  status.value === 'RATE_LIMITED'
    ? 'AI 请求过于频繁，请稍后再试。'
    : 'AI 助手暂时不可用，不影响帖子浏览、评论和点赞。',
)

const citationMap = computed(() => {
  const map = new Map<number, AiCitation>()
  for (const citation of data.value?.citations ?? []) {
    map.set(citation.index, citation)
  }
  return map
})

type ConclusionPart
  = | { type: 'text', text: string }
    | { type: 'citation', index: number, text: string }

const conclusionParts = computed<ConclusionPart[]>(() => {
  const text = data.value?.conclusion ?? ''
  const citations = citationMap.value
  const parts: ConclusionPart[] = []
  const regex = /\[(\d+)\]/g
  let lastIndex = 0
  let match: RegExpExecArray | null = regex.exec(text)
  while (match) {
    if (match.index > lastIndex) {
      parts.push({ type: 'text', text: text.slice(lastIndex, match.index) })
    }
    const index = Number(match[1])
    if (citations.has(index)) {
      parts.push({ type: 'citation', index, text: match[0] })
    }
    else {
      parts.push({ type: 'text', text: match[0] })
    }
    lastIndex = regex.lastIndex
    match = regex.exec(text)
  }
  if (lastIndex < text.length) {
    parts.push({ type: 'text', text: text.slice(lastIndex) })
  }
  return parts
})

async function load() {
  const seq = ++requestSeq
  loading.value = true
  failed.value = false
  data.value = null
  const result = await getPostAiAnalysis(props.postId)
  if (disposed || seq !== requestSeq) {
    return
  }
  if (!result) {
    failed.value = true
  }
  else {
    data.value = result
  }
  loading.value = false
}

function openCitation(index: number) {
  const citation = citationMap.value.get(index)
  if (citation?.url) {
    router.push(citation.url)
  }
}

function postKey(item: AiRelatedPost) {
  return `post-${item.postId}`
}

watch(() => props.postId, load, { immediate: true })

onUnmounted(() => {
  disposed = true
  requestSeq++
})
</script>

<template>
  <section class="ai-card" aria-live="polite">
    <header class="ai-head">
      <div>
        <h2 class="ai-title">
          AI 分析
        </h2>
        <p class="ai-subtitle">
          基于站内历史帖子和回复检索生成
        </p>
      </div>
      <span class="ai-badge">RAG</span>
    </header>

    <div v-if="loading" class="ai-state">
      <span class="ai-spinner" aria-hidden="true" />
      正在检索站内历史讨论...
    </div>

    <div v-else-if="failed || showUnavailable" class="ai-state ai-state-error">
      <p>{{ failed ? 'AI 服务请求失败，不影响帖子浏览和评论。' : unavailableText }}</p>
      <button type="button" class="ai-retry" @click="load">
        重试
      </button>
    </div>

    <div v-else-if="showModelError" class="ai-state ai-state-error">
      <p>大模型调用失败，请稍后重试。帖子内容、评论和点赞不受影响。</p>
      <button type="button" class="ai-retry" @click="load">
        重试
      </button>
    </div>

    <div v-else-if="showDisabled" class="ai-state">
      AI 分析功能暂未启用。
    </div>

    <div v-else-if="showUnknown" class="ai-state ai-state-error">
      AI 返回了未知状态，请稍后重试。
    </div>

    <template v-else>
      <div v-if="showEmpty" class="ai-state ai-state-empty">
        {{ data?.conclusion || '站内暂无足够相关的历史讨论，AI 不会凭空回答。' }}
      </div>

      <section v-if="showReady && data?.conclusion" class="ai-section">
        <h3 class="ai-section-title">
          AI 总结
        </h3>
        <div class="ai-conclusion">
          <template v-for="(part, index) in conclusionParts" :key="index">
            <button
              v-if="part.type === 'citation'"
              type="button"
              class="ai-citation"
              @click="openCitation(part.index)"
            >
              {{ part.text }}
            </button>
            <template v-else>
              {{ part.text }}
            </template>
          </template>
        </div>
      </section>

      <section v-if="data?.relatedPosts?.length" class="ai-section">
        <h3 class="ai-section-title">
          相关历史帖子
        </h3>
        <div class="ai-post-list">
          <RouterLink
            v-for="item in data.relatedPosts"
            :key="postKey(item)"
            :to="item.url"
            class="ai-post-link"
          >
            {{ item.title }}
          </RouterLink>
        </div>
      </section>

      <p v-if="data?.notice" class="ai-notice">
        {{ data.notice }}
      </p>
    </template>

    <footer class="ai-follow-up">
      <input disabled placeholder="追问 AI（后续版本开放）">
      <button type="button" disabled>
        追问
      </button>
    </footer>
  </section>
</template>

<style scoped>
.ai-card {
  width: 100%;
  padding: 1rem 1.1rem;
  margin: 1rem auto;
  color: var(--color-text);
  background: var(--surface-1);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.ai-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  padding-bottom: 0.8rem;
  border-bottom: 1px solid var(--color-border);
}

.ai-title {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 700;
}

.ai-subtitle {
  margin: 0.25rem 0 0;
  font-size: 0.78rem;
  color: var(--color-text-secondary);
}

.ai-badge {
  flex-shrink: 0;
  padding: 0.15rem 0.55rem;
  font-size: 0.72rem;
  font-weight: 600;
  color: var(--accent);
  background: var(--accent-muted);
  border: 1px solid color-mix(in srgb, var(--accent) 35%, transparent);
  border-radius: 999px;
}

.ai-state {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 1.1rem 0.2rem 0.4rem;
  font-size: 0.88rem;
  color: var(--color-text-secondary);
}

.ai-state-error {
  flex-wrap: wrap;
  color: var(--color-error);
}

.ai-state-error p {
  margin: 0;
}

.ai-state-empty {
  color: var(--color-text-secondary);
}

.ai-spinner {
  width: 0.95rem;
  height: 0.95rem;
  border: 2px solid var(--color-border);
  border-top-color: var(--accent);
  border-radius: 50%;
  animation: ai-spin 0.8s linear infinite;
}

@keyframes ai-spin {
  to {
    transform: rotate(360deg);
  }
}

.ai-retry {
  min-height: 30px;
  padding: 0 0.7rem;
  margin: 0;
  font-size: 0.78rem;
  color: var(--color-text);
  background: var(--surface-2);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
}

.ai-retry:hover {
  color: var(--accent);
  border-color: var(--accent);
}

.ai-conclusion {
  padding: 0.85rem 1rem;
  margin-top: 0.9rem;
  font-size: 0.92rem;
  line-height: 1.75;
  white-space: pre-wrap;
  background: var(--surface-2);
  border-left: 3px solid var(--accent);
  border-radius: var(--radius-md);
}

.ai-citation {
  display: inline;
  padding: 0;
  margin: 0 0.08rem;
  font-size: 0.78rem;
  font-weight: 700;
  line-height: 1;
  color: var(--accent);
  vertical-align: super;
  background: transparent;
  border: none;
  cursor: pointer;
}

.ai-citation:hover {
  text-decoration: underline;
}

.ai-section {
  margin-top: 1.1rem;
}

.ai-section-title {
  margin: 0 0 0.5rem;
  font-size: 0.9rem;
  font-weight: 700;
  color: var(--color-text);
}

.ai-post-list {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.ai-post-link {
  overflow-wrap: anywhere;
  font-size: 0.88rem;
  line-height: 1.6;
  color: var(--accent);
  text-decoration: none;
}

.ai-post-link:hover {
  text-decoration: underline;
}

.ai-notice {
  margin: 1rem 0 0;
  font-size: 0.75rem;
  color: var(--color-text-secondary);
}

.ai-follow-up {
  display: flex;
  gap: 0.5rem;
  padding-top: 0.9rem;
  margin-top: 1rem;
  border-top: 1px dashed var(--color-border);
}

.ai-follow-up input {
  flex: 1;
  min-height: 34px;
  padding: 0 0.8rem;
  font-size: 0.82rem;
  color: var(--color-text-secondary);
  background: var(--surface-2);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}

.ai-follow-up button {
  min-height: 34px;
  padding: 0 0.9rem;
  margin: 0;
  font-size: 0.82rem;
  color: var(--color-text-secondary);
  background: var(--surface-2);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  cursor: not-allowed;
}
</style>
