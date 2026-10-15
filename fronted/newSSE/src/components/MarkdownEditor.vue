<!-- eslint-disable vue/html-self-closing -->
<script setup lang="ts">
import { nextTick, ref, useTemplateRef } from 'vue'

import { uploadPhoto } from '@/api/editPostAndComment/utils'

import { showMsg } from '@/components/MessageBox'
import MarkdownContainer from './MarkdownContainer.vue'

defineEmits(['send'])

const fileInputId = `fileInput-${Math.random().toString(36).slice(2, 9)}`

const isPreview = ref(false)
const textarea = useTemplateRef<HTMLTextAreaElement>('textarea')

const modelValue = defineModel({
  type: String,
  required: true,
})

function autoResize() {
  textarea.value!.style.height = '450px'
  textarea.value!.style.height = `${textarea.value!.scrollHeight}px`
}

function handleInput(event: Event) {
  const top = document.body.scrollTop
  const el = event.target as HTMLTextAreaElement
  modelValue.value = el.value
  autoResize()
  nextTick(() => {
    document.body.scrollTop = top
  })
}

async function upload(file: File) {
  if (file) {
    try {
      const data = await uploadPhoto(file)
      showMsg(data.message)
      // modelValue.value += `<p><img src="${data.fileURL}" alt="${file.name}" loading="lazy" /></p>`
      // 适配Markdown渲染
      modelValue.value += `![${file.name}](${data.fileURL})`
      autoResize()
    }
    catch (error) {
      console.error('Error uploading file:', error)
      showMsg('上传失败，请重试')
    }
  }
  else {
    console.error('No file selected')
    showMsg('请选择一个文件')
  }
}

async function uploadFile(event: Event) {
  const el = event.target as HTMLInputElement
  const file = el.files![0]
  await upload(file)
}

async function handlePaste(event: ClipboardEvent) {
  const items = event.clipboardData?.items
  if (!items)
    return
  for (let i = 0; i < items.length; ++i) {
    const item = items[i]
    if (item.kind === 'file') {
      const file = item.getAsFile()
      event.preventDefault()
      if (!file)
        return
      await upload(file)
    }
  }
}

type EditType = '标题' | '粗体' | '斜体' | '删除线' | '引用' | '无序列表' | '有序列表' | '表格' | '分割线' | '代码块'
function editContent(type: EditType) {
  let insertion = ''
  let cursorOffset = 0

  switch (type) {
    case '标题':
      insertion = '### 标题\n'
      cursorOffset = -1
      break
    case '粗体':
      insertion = '**粗体**'
      cursorOffset = -2
      break
    case '斜体':
      insertion = '*斜体*'
      cursorOffset = -1
      break
    case '删除线':
      insertion = '~~删除线~~'
      cursorOffset = -2
      break
    case '引用':
      insertion = '> 引用\n'
      cursorOffset = -1
      break
    case '无序列表':
      insertion = '- 无序列表'
      cursorOffset = 0
      break
    case '有序列表':
      insertion = '1. 有序列表'
      cursorOffset = 0
      break
    case '表格':
      insertion
				= '\n| 列1 | 列2 | 列3 |\n| --- | --- | --- |\n| 内容1 | 内容2 | 内容3 |\n'
      cursorOffset = 0
      break
    case '分割线':
      insertion = '\n---\n'
      cursorOffset = 0
      break
    case '代码块':
      insertion = '```\n代码块\n```'
      cursorOffset = -4
      break
    default:
      return
  }

  const start = textarea.value?.selectionStart
  const end = textarea.value?.selectionEnd
  const newValue = modelValue.value.slice(0, start) + insertion + modelValue.value.slice(end)

  modelValue.value = newValue

  // 在下一个事件循环中设置光标位置
  nextTick(() => {
    textarea.value?.focus()
    textarea.value?.setSelectionRange(
      start! + insertion.length + cursorOffset,
      start! + insertion.length + cursorOffset,
    )
  })
}
</script>

<template>
  <div
    id="mdRoot"
  >
    <div class="editorButton">
      <button @click="editContent('标题')">
        标题
      </button>
      <button @click="editContent('粗体')">
        粗体
      </button>
      <button @click="editContent('斜体')">
        斜体
      </button>
      <button @click="editContent('删除线')">
        删除线
      </button>
      <button @click="editContent('引用')">
        引用
      </button>
      <button @click="editContent('无序列表')">
        无序列表
      </button>
      <button @click="editContent('有序列表')">
        有序列表
      </button>
      <button @click="editContent('表格')">
        表格
      </button>
      <button @click="editContent('分割线')">
        分割线
      </button>
      <button @click="editContent('代码块')">
        代码块
      </button>
      <button @click="isPreview = !isPreview">
        {{ isPreview ? '不想看了' : '看看效果' }}
      </button>
    </div>
    <div class="container">
      <textarea
        ref="textarea"
        :value="modelValue"
        placeholder="请输入正文"
        @input="handleInput"
        @paste="handlePaste"
      />
      <MarkdownContainer
        v-show="isPreview"
        :markdown-content="modelValue"
      />
    </div>
    <div class="buttons">
      <label
        :for="fileInputId"
        class="button"
      >选择图片</label>
      <div
        class="button primary"
        role="button"
        tabindex="0"
        @click="$emit('send')"
        @keydown.enter.prevent="$emit('send')"
      >
        发送
      </div>
      <input
        :id="fileInputId"
        type="file"
        accept="image/*"
        style="display: none"
        @input="uploadFile"
      >
    </div>
  </div>
</template>

<style scoped>
#mdRoot {
  width: 100%;
}

/* 工具栏：统一胶囊按钮 */
.editorButton {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 0.75rem;

  button {
    min-height: 32px;
    margin: 0;
    padding: 0 0.7rem;
    background-color: var(--surface-2);
    color: var(--color-text);
    border: 1px solid var(--color-border);
    border-radius: var(--radius-md);
    font-size: 0.82rem;
    font-weight: 500;
    cursor: pointer;
    -webkit-tap-highlight-color: transparent;
    transition:
      background-color 0.15s ease,
      color 0.15s ease,
      border-color 0.15s ease;

    &:hover {
      background-color: var(--accent-muted);
      color: var(--accent);
      border-color: color-mix(in srgb, var(--accent) 35%, var(--color-border));
    }
  }
}

.container {
  display: flex;
  flex-direction: row;
  gap: 1rem;
  width: 100%;

  > div {
    width: 100%;
    min-height: 100%;
    border-radius: var(--radius-md);
    padding: 0.9rem 1rem;
    border: 1px solid var(--color-border);
    background: var(--surface-1);
    overflow-y: auto;
    max-height: 620px;
  }

  textarea {
    width: 100%;
    height: 450px;
    min-height: 320px;
    padding: 0.9rem 1rem;
    border: 1px solid var(--color-border);
    border-radius: var(--radius-md);
    background: var(--surface-2);
    color: var(--color-text);
    font-family: inherit;
    font-size: 0.95rem;
    line-height: 1.7;
    resize: none;
    overflow: hidden;
    transition:
      border-color 0.2s ease,
      box-shadow 0.2s ease,
      background-color 0.2s ease;
  }

  textarea::placeholder {
    color: var(--color-text-secondary);
  }

  textarea:focus {
    outline: none;
    border-color: var(--accent);
    background: var(--surface-1);
    box-shadow: 0 0 0 3px var(--accent-muted);
  }
}

/* 底部操作 */
.buttons {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-end;
  gap: 0.75rem;
  width: 100%;
  margin-top: 1rem;

  .button {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-height: 42px;
    margin: 0;
    padding: 0 1.5rem;
    border-radius: var(--radius-md);
    background-color: var(--surface-1);
    border: 1px solid var(--color-border);
    color: var(--color-text);
    font-size: 0.9rem;
    font-weight: 500;
    text-align: center;
    cursor: pointer;
    -webkit-tap-highlight-color: transparent;
    transition:
      background-color 0.2s ease,
      border-color 0.2s ease,
      color 0.2s ease;

    &:hover {
      border-color: var(--accent);
      color: var(--accent);
      background-color: var(--surface-1);
    }

    &.primary {
      background-color: var(--accent);
      border-color: var(--accent);
      color: var(--accent-foreground);

      &:hover {
        background-color: var(--accent-hover);
        border-color: var(--accent-hover);
        color: var(--accent-foreground);
      }
    }
  }
}

@media screen and (max-width: 768px) {
  .editorButton {
    width: 100%;
    gap: 8px;
  }

  .container {
    flex-direction: column;
  }

  .buttons {
    justify-content: stretch;

    .button {
      flex: 1;
    }
  }
}
</style>
