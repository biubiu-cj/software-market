<script setup lang="ts">
import type { Product } from '@/api/shop/getProducts'
import { computed, onMounted, ref } from 'vue'
import { showImg } from '@/components/ImageShower'
import { getSellerName } from '@/utils/sellerNameMapper'
import { strHandler } from '@/utils/strHandler'

interface Props {
  product: Product
  layoutMode?: 'grid' | 'list'
}

const props = withDefaults(defineProps<Props>(), {
  layoutMode: 'grid',
})

const emit = defineEmits<{
  viewDetail: [product: Product]
}>()

// 响应式数据
const imageLoading = ref(true)
const imageError = ref(false)
const sellerName = ref<string>('')

// 计算属性
const hasImage = computed(() => props.product.photos && props.product.photos[0])
const resizedImageUrl = computed(() => {
  if (!hasImage.value)
    return ''
  return props.product.photos[0].replace('/uploads/', '/resized/')
})

const originalImageUrl = computed(() => {
  if (!hasImage.value)
    return ''
  return strHandler('postImg', props.product.photos[0])
})

const isSoldOut = computed(() => props.product.isSold)

// 方法
function viewDetail(): void {
  emit('viewDetail', props.product)
}

function viewOriginalImage(event: MouseEvent): void {
  event.stopPropagation()

  if (hasImage.value) {
    showImg(originalImageUrl.value)
  }
}

function handleImageLoad(): void {
  imageLoading.value = false
  imageError.value = false
}

function handleImageError(): void {
  imageLoading.value = false
  imageError.value = true
}

// 获取卖家名称
async function fetchSellerName(): Promise<void> {
  try {
    // 如果sellerId为0或无效，直接使用默认值
    if (!props.product.sellerId || props.product.sellerId === 0) {
      sellerName.value = props.product.seller || '匿名用户'
      return
    }

    const name = await getSellerName(props.product.sellerId)
    sellerName.value = name
  }
  catch (error) {
    console.error('获取卖家名称失败:', error)
    // 如果获取失败，使用原始数据或默认值
    sellerName.value = props.product.seller || (props.product.sellerId ? `用户${props.product.sellerId}` : '匿名用户')
  }
}

// 生命周期钩子
onMounted(() => {
  fetchSellerName()
})
</script>

<template>
  <div
    class="product-card"
    :class="{
      'sold-out': isSoldOut,
      'grid-mode': layoutMode === 'grid',
      'list-mode': layoutMode === 'list',
    }"
  >
    <!-- 网格模式布局 -->
    <template v-if="layoutMode === 'grid'">
      <div class="product-image" @click="viewOriginalImage">
        <!-- 图片加载状态 -->
        <div v-if="imageLoading" class="image-loading">
          <div class="loading-spinner" />
        </div>

        <!-- 商品图片 -->
        <img
          v-if="hasImage && !imageError"
          :src="resizedImageUrl"
          :alt="product.name"
          class="preserve-orientation"
          loading="lazy"
          @load="handleImageLoad"
          @error="handleImageError"
        >

        <!-- 默认图片 -->
        <img
          v-else
          src="/images/nophotos.png"
          :alt="product.name"
          @load="handleImageLoad"
          @error="handleImageError"
        >

        <!-- 售出标记 -->
        <div v-if="isSoldOut" class="sold-out-chip">
          已售出
        </div>

        <!-- 图片错误状态 -->
        <div v-if="imageError" class="image-error">
          <span class="error-text">图片加载失败</span>
        </div>
      </div>

      <div class="product-info">
        <h3 class="product-name" :title="product.name">
          {{ product.name }}
        </h3>
        <p class="product-seller">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
            <circle cx="12" cy="7" r="4" />
          </svg>
          {{ sellerName || product.seller || (product.sellerId ? `用户${product.sellerId}` : '匿名用户') }}
        </p>
        <p class="product-description" :title="product.description">
          {{ product.description }}
        </p>
        <div class="product-price">
          <span class="current-price">¥{{ product.price.toLocaleString() }}</span>
        </div>
      </div>

      <button
        class="detail-btn"
        :class="{ 'sold-out-btn': isSoldOut }"
        :disabled="isSoldOut"
        @click="viewDetail"
      >
        {{ isSoldOut ? '已售出' : '查看详情' }}
      </button>
    </template>

    <!-- 列表模式布局 -->
    <template v-else>
      <div class="list-layout-content">
        <div class="list-image-container" @click="viewOriginalImage">
          <!-- 图片加载状态 -->
          <div v-if="imageLoading" class="image-loading">
            <div class="loading-spinner" />
          </div>

          <!-- 商品图片 -->
          <img
            v-if="hasImage && !imageError"
            :src="resizedImageUrl"
            :alt="product.name"
            class="preserve-orientation"
            loading="lazy"
            @load="handleImageLoad"
            @error="handleImageError"
          >

          <!-- 默认图片 -->
          <img
            v-else
            src="/images/nophotos.png"
            :alt="product.name"
            @load="handleImageLoad"
            @error="handleImageError"
          >

          <!-- 售出标记 -->
          <div v-if="isSoldOut" class="sold-out-chip">
            已售出
          </div>

          <!-- 图片错误状态 -->
          <div v-if="imageError" class="image-error">
            <span class="error-text">图片加载失败</span>
          </div>
        </div>

        <div class="list-info-container">
          <div class="list-main-info">
            <h3 class="product-name" :title="product.name">
              {{ product.name }}
            </h3>
            <p class="product-description" :title="product.description">
              {{ product.description }}
            </p>
            <p class="product-seller">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                <circle cx="12" cy="7" r="4" />
              </svg>
              {{ sellerName || product.seller || (product.sellerId ? `用户${product.sellerId}` : '匿名用户') }}
            </p>
          </div>

          <div class="list-action-container">
            <div class="product-price">
              <span class="current-price">¥{{ product.price.toLocaleString() }}</span>
            </div>
            <button
              class="detail-btn"
              :class="{ 'sold-out-btn': isSoldOut }"
              :disabled="isSoldOut"
              @click="viewDetail"
            >
              {{ isSoldOut ? '已售出' : '查看详情' }}
            </button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
/* 商品卡片 */
.product-card {
  background: var(--surface-1);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition:
    box-shadow 0.25s ease,
    transform 0.2s ease,
    border-color 0.2s ease;
  display: flex;
  flex-direction: column;
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
  position: relative;
}

.product-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: color-mix(in srgb, var(--accent) 30%, var(--color-border));
}

.product-image {
  aspect-ratio: 4 / 3;
  overflow: hidden;
  position: relative;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  background: var(--shop-skeleton-bg);
}

/* 图片加载状态 */
.image-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--shop-skeleton-bg);
  z-index: 2;
}

.loading-spinner {
  width: 26px;
  height: 26px;
  border: 3px solid var(--color-border);
  border-top-color: var(--accent);
  border-radius: 50%;
  animation: spin 0.9s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* 图片错误状态 */
.image-error {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: var(--shop-skeleton-bg);
  color: var(--shop-text-muted);
  z-index: 2;
}

.error-text {
  font-size: 12px;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.preserve-orientation {
  image-orientation: from-image;
}

.product-card:hover .product-image img {
  transform: scale(1.04);
}

.sold-out .product-image img {
  filter: grayscale(100%);
}

/* 售出标记 */
.sold-out-chip {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 3;
  padding: 3px 10px;
  border-radius: var(--radius-full);
  background: rgba(15, 23, 42, 0.7);
  color: #fff;
  font-size: 0.72rem;
  font-weight: 600;
  backdrop-filter: blur(4px);
}

.product-info {
  padding: 0.9rem 1rem 0.35rem;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.product-name {
  font-size: 0.95rem;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: var(--color-text);
  font-weight: 600;
  line-height: 1.4;
}

.product-seller {
  font-size: 0.78rem;
  color: var(--shop-text-muted);
  margin: 0;
  display: flex;
  align-items: center;
  gap: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-seller svg {
  width: 13px;
  height: 13px;
  flex-shrink: 0;
}

.product-price {
  margin: 0.15rem 0 0;
  display: flex;
  align-items: baseline;
  gap: 0.5rem;
}

.current-price {
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--color-price);
  letter-spacing: -0.01em;
}

.product-description {
  font-size: 0.8rem;
  color: var(--shop-text-secondary);
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
}

/* 查看详情按钮 */
.detail-btn {
  margin: 0.75rem 1rem 1rem;
  min-height: 38px;
  background: var(--accent);
  color: var(--accent-foreground);
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background-color 0.2s ease;
  font-weight: 600;
  font-size: 0.875rem;
}

.detail-btn:hover:not(:disabled) {
  background: var(--accent-hover);
}

.sold-out {
  opacity: 0.85;
}

.sold-out-btn {
  background: var(--surface-2) !important;
  color: var(--color-text-secondary) !important;
  cursor: not-allowed !important;
}

/* 列表模式 */
.product-card.list-mode {
  flex-direction: row;
  padding: 0.75rem;
  gap: 1rem;
  align-items: stretch;
}

.list-layout-content {
  display: flex;
  width: 100%;
  align-items: center;
  gap: 1rem;
}

.list-image-container {
  width: 110px;
  height: 110px;
  flex-shrink: 0;
  border-radius: var(--radius-md);
  overflow: hidden;
  position: relative;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  background: var(--shop-skeleton-bg);
}

.list-image-container img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.list-image-container:hover img {
  transform: scale(1.05);
}

.list-info-container {
  flex: 1;
  min-width: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
}

.list-main-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.list-main-info .product-description {
  -webkit-line-clamp: 2;
  line-clamp: 2;
}

.list-action-container {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.6rem;
  flex-shrink: 0;
}

.list-action-container .product-price {
  margin: 0;
}

.list-action-container .detail-btn {
  margin: 0;
  min-width: 104px;
  padding-inline: 1rem;
}

@media (max-width: 600px) {
  .product-card.list-mode {
    align-items: stretch;
  }

  .list-image-container {
    width: 96px;
    height: 96px;
  }

  .list-action-container {
    align-items: flex-end;
  }
}
</style>
