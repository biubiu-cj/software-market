<script setup lang="ts">
import { reactive, ref } from 'vue'
import { adminPost } from '@/api/admin'
import { showMsg } from '@/components/MessageBox'

const PARTITION_OPTIONS = [
  '优质贴',
  '课程交流',
  '日常吐槽',
  '打听求助',
  '学习交流',
  '院务',
  '求职招募',
  '其他',
]

const loading = ref(false)

const form = reactive({
  title: '',
  content: '',
  partition: '',
  photos: '',
  tagList: '',
})

const errors = reactive({
  title: '',
  content: '',
  partition: '',
})

function validate(): boolean {
  errors.title = ''
  errors.content = ''
  errors.partition = ''
  let valid = true

  if (!form.title.trim()) {
    errors.title = '请输入标题'
    valid = false
  }

  if (!form.content.trim()) {
    errors.content = '请输入内容'
    valid = false
  }

  if (!form.partition) {
    errors.partition = '请选择板块'
    valid = false
  }

  return valid
}

async function handleSubmit() {
  if (!validate()) return

  loading.value = true
  try {
    const res = await adminPost({
      title: form.title,
      content: form.content,
      partition: form.partition,
      photos: form.photos || undefined,
      tagList: form.tagList || undefined,
    })

    if (res.code === 200) {
      showMsg('发帖成功')
      form.title = ''
      form.content = ''
      form.partition = ''
      form.photos = ''
      form.tagList = ''
    } else {
      showMsg(res.msg || '发帖失败')
    }
  } catch {
    showMsg('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="form-page">
    <div class="page-top">
      <h1 class="page-top__title">管理员发帖</h1>
      <p class="page-top__desc">以官方管理员身份发布帖子</p>
    </div>

    <div class="form-card">
      <form @submit.prevent="handleSubmit">
        <div class="form-row">
          <div class="field">
            <label class="field-label">标题</label>
            <input
              v-model="form.title"
              class="field-input"
              type="text"
              placeholder="输入帖子标题"
              autocomplete="off"
              :class="{ 'field-input--error': errors.title }"
            >
            <p v-if="errors.title" class="field-error">{{ errors.title }}</p>
          </div>
          <div class="field">
            <label class="field-label">板块</label>
            <select
              v-model="form.partition"
              class="field-select"
              :class="{ 'field-input--error': errors.partition }"
            >
              <option value="" disabled>请选择板块</option>
              <option v-for="p in PARTITION_OPTIONS" :key="p" :value="p">{{ p }}</option>
            </select>
            <p v-if="errors.partition" class="field-error">{{ errors.partition }}</p>
          </div>
        </div>

        <div class="field">
          <label class="field-label">内容</label>
          <textarea
            v-model="form.content"
            class="field-textarea"
            placeholder="输入帖子内容..."
            rows="6"
            :class="{ 'field-input--error': errors.content }"
          ></textarea>
          <p v-if="errors.content" class="field-error">{{ errors.content }}</p>
        </div>

        <div class="form-row">
          <div class="field">
            <label class="field-label">图片URL（可选）</label>
            <input
              v-model="form.photos"
              class="field-input"
              type="text"
              placeholder="https://..."
              autocomplete="off"
            >
          </div>
          <div class="field">
            <label class="field-label">标签（可选）</label>
            <input
              v-model="form.tagList"
              class="field-input"
              type="text"
              placeholder="用 | 分隔多个标签"
              autocomplete="off"
            >
          </div>
        </div>

        <button class="submit-btn" type="submit" :disabled="loading">
          {{ loading ? '发布中...' : '发布帖子' }}
        </button>
      </form>
    </div>
  </div>
</template>

<style scoped lang="scss">
.form-page {
  max-width: 680px;
}

.page-top {
  margin-bottom: 1.5rem;
}

.page-top__title {
  font-size: 1.35rem;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.02em;
  margin: 0;

  :global(body.dark-mode) & {
    color: #f1f5f9;
  }
}

.page-top__desc {
  margin: 0.2rem 0 0;
  font-size: 0.85rem;
  color: #94a3b8;
}

.form-card {
  background: #ffffff;
  border: 1px solid rgba(15, 23, 42, 0.06);
  border-radius: 12px;
  padding: 1.75rem;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);

  :global(body.dark-mode) & {
    background: #111827;
    border-color: rgba(255, 255, 255, 0.06);
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  }
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 1rem;
}

@media screen and (max-width: 480px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}

.field {
  margin-bottom: 1.25rem;
}

.field-label {
  display: block;
  font-size: 0.825rem;
  font-weight: 600;
  color: #334155;
  margin-bottom: 0.4rem;

  :global(body.dark-mode) & {
    color: #e2e8f0;
  }
}

.field-input {
  width: 100%;
  height: 42px;
  padding: 0 0.85rem;
  border: 1px solid rgba(15, 23, 42, 0.1);
  border-radius: 10px;
  background: #ffffff;
  color: #0f172a;
  font-size: 0.9rem;
  font-family: inherit;
  transition: border-color 0.15s, box-shadow 0.15s;

  &::placeholder {
    color: #cbd5e1;
  }

  &:focus {
    outline: none;
    border-color: #0d9488;
    box-shadow: 0 0 0 3px rgba(13, 148, 136, 0.1);
  }

  &--error {
    border-color: #ef4444;

    &:focus {
      box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
    }
  }

  :global(body.dark-mode) & {
    background: #0f172a;
    border-color: rgba(255, 255, 255, 0.1);
    color: #e2e8f0;

    &:focus {
      border-color: #0d9488;
      box-shadow: 0 0 0 3px rgba(13, 148, 136, 0.15);
    }
  }
}

.field-select {
  width: 100%;
  height: 42px;
  padding: 0 2rem 0 0.85rem;
  border: 1px solid rgba(15, 23, 42, 0.1);
  border-radius: 10px;
  background: #ffffff;
  color: #0f172a;
  font-size: 0.9rem;
  font-family: inherit;
  cursor: pointer;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg width='10' height='6' viewBox='0 0 10 6' fill='none' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M1 1l4 4 4-4' stroke='%2394a3b8' stroke-width='1.5' stroke-linecap='round'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 0.75rem center;
  transition: border-color 0.15s, box-shadow 0.15s;

  &:focus {
    outline: none;
    border-color: #0d9488;
    box-shadow: 0 0 0 3px rgba(13, 148, 136, 0.1);
  }

  :global(body.dark-mode) & {
    background: #0f172a;
    border-color: rgba(255, 255, 255, 0.1);
    color: #e2e8f0;

    &:focus {
      border-color: #0d9488;
      box-shadow: 0 0 0 3px rgba(13, 148, 136, 0.15);
    }
  }
}

.field-textarea {
  width: 100%;
  padding: 0.75rem 0.85rem;
  border: 1px solid rgba(15, 23, 42, 0.1);
  border-radius: 10px;
  background: #ffffff;
  color: #0f172a;
  font-size: 0.9rem;
  font-family: inherit;
  resize: vertical;
  transition: border-color 0.15s, box-shadow 0.15s;

  &::placeholder {
    color: #cbd5e1;
  }

  &:focus {
    outline: none;
    border-color: #0d9488;
    box-shadow: 0 0 0 3px rgba(13, 148, 136, 0.1);
  }

  &--error {
    border-color: #ef4444;

    &:focus {
      box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
    }
  }

  :global(body.dark-mode) & {
    background: #0f172a;
    border-color: rgba(255, 255, 255, 0.1);
    color: #e2e8f0;

    &:focus {
      border-color: #0d9488;
      box-shadow: 0 0 0 3px rgba(13, 148, 136, 0.15);
    }
  }
}

.field-error {
  margin: 0.3rem 0 0;
  font-size: 0.78rem;
  color: #ef4444;
}

.submit-btn {
  width: 100%;
  height: 44px;
  margin-top: 0.5rem;
  border: none;
  border-radius: 10px;
  background: #0d9488;
  color: #ffffff;
  font-size: 0.95rem;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: background 0.15s, transform 0.1s;

  &:hover:not(:disabled) {
    background: #0f766e;
  }

  &:active:not(:disabled) {
    transform: scale(0.99);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}
</style>
