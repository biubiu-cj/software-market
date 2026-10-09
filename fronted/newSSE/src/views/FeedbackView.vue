<script setup lang="ts">
import { useTemplateRef } from 'vue'
import { feedback } from '@/api/feedback/feedback'
import { showMsg } from '@/components/MessageBox'
import { debounceAsync } from '@/utils/debounced'

const feedbackContent = useTemplateRef<HTMLTextAreaElement>('feedbackContent')

// 这是防抖
const submitFeedbackDebounced = debounceAsync(submitFeedback)

async function submitFeedback() {
  const el = feedbackContent.value
  if (!el)
    return
  // 去除空格
  el.value = el.value.trim()
  if (el.value) {
    const res = await feedback(el.value, '')
    showMsg(res.msg)
  }
  else {
    showMsg('请输入反馈内容')
  }
}
</script>

<template>
  <div class="root page-section">
    <h1 class="page-heading">
      反馈
    </h1>
    <p class="feedback-lead">
      请在下方输入您的反馈信息
    </p>
    <textarea
      ref="feedbackContent"
      class="feedback-textarea"
      placeholder="请在此输入反馈..."
    />
    <button
      type="button"
      class="btn-primary feedback-submit"
      @click="submitFeedbackDebounced"
    >
      提交反馈
    </button>
  </div>
</template>

<style scoped lang="scss">
.root {
  color: var(--color-text);
}

.feedback-lead {
  margin: 0 0 1rem;
  font-size: 0.95rem;
  color: var(--shop-text-secondary);
  line-height: 1.5;
}

.feedback-textarea {
  width: 100%;
  max-width: 42rem;
  min-height: 10rem;
  display: block;
  padding: 0.85rem 1rem;
  font-size: 1rem;
  font-family: var(--font-sans);
  line-height: 1.55;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--surface-1);
  color: var(--color-text);
  resize: vertical;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease;
}

.feedback-textarea:focus {
  outline: none;
  border-color: var(--accent);
  box-shadow: 0 0 0 3px var(--accent-muted);
}

.feedback-submit {
  width: auto;
  min-width: 10rem;
  margin-top: 1.25rem;
}

body.dark-mode .feedback-textarea {
  background: var(--surface-2);
  color: var(--color-text);
  border-color: var(--color-border);
}
</style>
