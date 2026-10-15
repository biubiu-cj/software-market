<script setup lang="ts">
import type { ProductCreation } from '@/api/shop/handleProduct'
import { ref, useTemplateRef } from 'vue'
import { useRouter } from 'vue-router'
import { uploadPhoto } from '@/api/editPostAndComment/utils'
import { handleProduct } from '@/api/shop/handleProduct'
import { showMsg } from '@/components/MessageBox'
import { useUserStore } from '@/store/userStore'

const { userInfo } = useUserStore()

const router = useRouter()

// 商品数据
const product = ref<ProductCreation & { tempFiles: File[] }>({
  name: '',
  price: 0,
  description: '',
  images: [], // 存储图片URL
  tempFiles: [], // 临时存储选择的文件，用于预览
})

// 错误信息
const errors = ref<{
  name: string
  price: string
  description: string
  images: string
}>({
  name: '',
  price: '',
  description: '',
  images: '',
})

// 提交状态
const isSubmitting = ref<boolean>(false)
const fileInput = useTemplateRef<HTMLInputElement>('fileInput')

// 触发文件选择
function triggerFileInput(): void {
  if (fileInput.value) {
    fileInput.value.click()
  }
}

// 处理文件选择
function handleFiles(event: Event): void {
  const target = event.target as HTMLInputElement
  const files = target.files
  if (!files)
    return

  for (const file of Array.from(files)) {
    if (!isImage(file))
      continue // 检查是否是图片

    if (product.value.tempFiles.length >= 5) {
      showMsg('最多只能上传5张图片')
      break
    }

    // 添加到临时文件列表
    product.value.tempFiles.push(file)

    // 预览图片
    const reader = new FileReader()
    reader.onload = (e: ProgressEvent<FileReader>): void => {
      if (e.target && e.target.result) {
        // 在 images 数组中添加临时预览URL（非服务器URL）
        product.value.images.push(e.target.result as string)
      }
    }
    reader.readAsDataURL(file)
  }

  // 清空文件输入，以便可以重新选择相同的文件
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

// 检查是否是图片
function isImage(file: File): boolean {
  return file.type.startsWith('image/')
}

// 删除图片
function deleteImage(index: number): void {
  product.value.tempFiles.splice(index, 1) // 从临时文件中删除
  product.value.images.splice(index, 1) // 从预览列表中删除
}

// 表单验证
function validateForm(): boolean {
  let isValid = true
  errors.value = {
    name: '',
    price: '',
    description: '',
    images: '',
  }

  if (!product.value.name) {
    errors.value.name = '商品名称不能为空'
    isValid = false
  }

  if (product.value.price === null || product.value.price <= 0) {
    errors.value.price = '请输入有效的商品价格'
    isValid = false
  }

  if (!product.value.description) {
    errors.value.description = '商品描述不能为空'
    isValid = false
  }

  // if (product.value.tempFiles.length === 0) {
  //   errors.value.images = '至少需要上传一张商品图片'
  //   isValid = false
  // }

  return isValid
}

// 提交商品
async function submitProduct(): Promise<void> {
  if (isSubmitting.value)
    return

  if (!validateForm()) {
    showMsg('请修正表单中的错误')
    return
  }
  isSubmitting.value = true

  try {
    // 收集所有图片的URL
    const imageUrls: string[] = []

    // 上传所有图片到服务器
    for (const file of product.value.tempFiles) {
      try {
        const data = await uploadPhoto(file)
        imageUrls.push(data.fileURL)
      }
      catch (error) {
        console.error('Error uploading file:', error)
        showMsg('部分图片上传失败，请重试')
        isSubmitting.value = false
        return
      }
    }

    // 将图片URL添加到商品对象
    product.value.images = imageUrls

    // 这里添加提交逻辑，可以调用API提交整个商品对象
    // console.log('商品发布成功，图片URL:', imageUrls)
    await handleProduct(product.value, userInfo.userID)
    await router.push('/shop')
    showMsg('商品发布成功')
  }
  catch (error) {
    console.error('Error submitting product:', error)
    showMsg('商品发布失败，请重试')
  }
  finally {
    isSubmitting.value = false
  }
}

// 取消发布
function cancelPublish(): void {
  if (confirm('确定要取消发布吗？已填写的信息将不会保存')) {
    router.push('/shop')
  }
}
</script>

<template>
  <div class="product-publish-container">
    <h2 class="page-heading">
      发布新商品
    </h2>

    <form class="product-form" @submit.prevent="submitProduct">
      <div class="form-row">
        <!-- 商品名称 -->
        <div class="form-group flex-1">
          <label for="productName">商品名称</label>
          <input
            id="productName"
            v-model.trim="product.name"
            type="text"
            placeholder="请输入商品名称"
            :class="{ error: errors.name }"
          >
          <div v-if="errors.name" class="error-message">
            {{ errors.name }}
          </div>
        </div>

        <!-- 商品价格 -->
        <div class="form-group flex-1">
          <label for="productPrice">商品价格 (¥)</label>
          <input
            id="productPrice"
            v-model.number="product.price"
            type="number"
            placeholder="请输入商品价格"
            :class="{ error: errors.price }"
          >
          <div v-if="errors.price" class="error-message">
            {{ errors.price }}
          </div>
        </div>
      </div>

      <!-- 商品图片上传 -->
      <div class="form-group">
        <label>商品图片 (最多上传5张)</label>
        <div class="images-upload-container">
          <div
            v-for="(image, index) in product.images"
            :key="index"
            class="image-upload-wrapper"
          >
            <img :src="image" alt="预览图" class="preview-image">
            <div class="image-actions">
              <button type="button" class="delete-image" aria-label="删除图片" title="删除图片" @click.stop="deleteImage(index)">
                ×
              </button>
            </div>
          </div>

          <div
            v-if="product.images.length < 5"
            class="upload-button-wrapper"
            @click="triggerFileInput"
          >
            <input
              ref="fileInput"
              type="file"
              accept="image/*"
              multiple
              style="display: none"
              @change="handleFiles"
            >
            <div class="upload-button">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                <line x1="12" y1="5" x2="12" y2="19" />
                <line x1="5" y1="12" x2="19" y2="12" />
              </svg>
              <span>添加图片</span>
            </div>
          </div>
        </div>
        <div v-if="errors.images" class="error-message">
          {{ errors.images }}
        </div>
      </div>

      <!-- 商品描述 -->
      <div class="form-group">
        <label for="productDescription">商品描述</label>
        <textarea
          id="productDescription"
          v-model="product.description"
          placeholder="请输入商品详细描述"
          rows="6"
          :class="{ error: errors.description }"
        />
        <div v-if="errors.description" class="error-message">
          {{ errors.description }}
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <button type="button" class="cancel-btn" @click="cancelPublish">
          取消
        </button>
        <button type="submit" class="publish-btn" :disabled="isSubmitting">
          {{ isSubmitting ? '发布中…' : '发布商品' }}
        </button>
      </div>
    </form>
  </div>
</template>

<style scoped>
.product-publish-container {
  width: 100%;
  max-width: 46rem;
  margin: 0 auto;
  padding-bottom: 1rem;
}

.product-form {
  background: var(--surface-1);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  padding: clamp(1.25rem, 3vw, 2rem);
  border: 1px solid var(--color-border);
}

.form-row {
  display: flex;
  gap: 1.25rem;
}

.form-group {
  margin-bottom: 1.25rem;
}

.flex-1 {
  flex: 1;
  min-width: 0;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: var(--color-text);
  font-size: 0.9rem;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 0.7rem 0.85rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 0.95rem;
  font-family: var(--font-sans);
  box-sizing: border-box;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    background-color 0.2s ease;
  background: var(--surface-2);
  color: var(--color-text);
}

.form-group textarea {
  resize: vertical;
  min-height: 8rem;
  line-height: 1.6;
}

.form-group input::placeholder,
.form-group textarea::placeholder {
  color: var(--color-text-secondary);
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: var(--accent);
  background: var(--surface-1);
  box-shadow: 0 0 0 3px var(--accent-muted);
}

.form-group input.error,
.form-group textarea.error {
  border-color: var(--color-error);
}

.error-message {
  color: var(--color-error);
  font-size: 0.82rem;
  margin-top: 0.35rem;
}

.images-upload-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.85rem;
  border: 1.5px dashed var(--color-border);
  border-radius: var(--radius-md);
  padding: 1rem;
  min-height: 150px;
  background: var(--surface-2);
  transition:
    border-color 0.2s ease,
    background-color 0.2s ease;
}

.images-upload-container:hover {
  border-color: var(--accent);
}

.image-upload-wrapper {
  position: relative;
  width: 128px;
  height: 128px;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
}

.image-actions {
  position: absolute;
  top: 5px;
  right: 5px;
}

.delete-image {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  margin: 0;
  background-color: rgba(15, 23, 42, 0.65);
  color: #fff;
  border: none;
  border-radius: 50%;
  font-size: 15px;
  line-height: 1;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
}

.delete-image:hover {
  background-color: var(--color-error);
}

.upload-button-wrapper {
  width: 128px;
  height: 128px;
  display: flex;
  justify-content: center;
  align-items: center;
  background: var(--surface-1);
  border-radius: var(--radius-md);
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  transition:
    border-color 0.2s ease,
    color 0.2s ease;
  border: 1.5px dashed var(--color-border);
  color: var(--color-text-secondary);
}

.upload-button-wrapper:hover {
  border-color: var(--accent);
  color: var(--accent);
}

.upload-button {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.82rem;
}

.upload-button svg {
  width: 22px;
  height: 22px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 1.75rem;
}

.cancel-btn {
  min-height: 44px;
  padding: 0 1.5rem;
  background: var(--surface-1);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  font-size: 0.95rem;
  font-weight: 500;
  color: var(--color-text);
  transition:
    border-color 0.2s ease,
    background-color 0.2s ease;
}

.cancel-btn:hover {
  background: var(--surface-2);
}

.publish-btn {
  min-height: 44px;
  padding: 0 1.75rem;
  background: var(--accent);
  color: var(--accent-foreground);
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  font-size: 0.95rem;
  font-weight: 600;
  transition: background-color 0.2s ease;
}

.publish-btn:hover:not(:disabled) {
  background: var(--accent-hover);
}

.publish-btn:disabled {
  background-color: var(--surface-2);
  color: var(--color-text-secondary);
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .form-row {
    flex-direction: column;
    gap: 0;
  }

  .form-actions {
    justify-content: stretch;
  }

  .form-actions button {
    flex: 1;
  }
}
</style>
