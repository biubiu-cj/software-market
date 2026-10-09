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
        <div v-if="isSoldOut" class="sold-out-mark">
          <span class="sold-out-text">已售出</span>
        </div>

        <!-- 图片错误状态 -->
        <div v-if="imageError" class="image-error">
          <span class="error-icon">📷</span>
          <span class="error-text">图片加载失败</span>
        </div>
      </div>

      <div class="product-info">
        <h3 class="product-name" :title="product.name">
          {{ product.name }}
        </h3>
        <p class="product-seller">
          {{ sellerName || product.seller || (product.sellerId ? `用户${product.sellerId}` : '匿名用户') }}
        </p>
        <div class="product-price">
          <span class="current-price">¥{{ product.price.toLocaleString() }}</span>
        </div>
        <p class="product-description" :title="product.description">
          {{ product.description }}
        </p>
      </div>

      <button
        class="add-to-cart"
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
          <div v-if="isSoldOut" class="sold-out-mark">
            <span class="sold-out-text">已售出</span>
          </div>

          <!-- 图片错误状态 -->
          <div v-if="imageError" class="image-error">
            <span class="error-icon">📷</span>
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
              {{ sellerName || product.seller || (product.sellerId ? `用户${product.sellerId}` : '匿名用户') }}
            </p>
          </div>

          <div class="list-action-container">
            <div class="product-price">
              <span class="current-price">¥{{ product.price.toLocaleString() }}</span>
            </div>
            <button
              class="add-to-cart"
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
/* 商品卡片样式 */
.product-card {
  background: var(--shop-card-bg);
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  border: 1px solid var(--color-border);
  position: relative;
}

.product-card:hover {
  transform: translateY(-2px);
}

.product-image {
  height: 220px;
  overflow: hidden;
  position: relative;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  border-radius: 12px 12px 0 0;
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
  background: var(--shop-card-bg);
  z-index: 2;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--shop-skeleton-bg);
  border-top: 3px solid var(--color-info);
  border-radius: 50%;
  animation: spin 1s linear infinite;
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
  background: var(--shop-card-bg);
  color: var(--shop-text-muted);
  z-index: 2;
}

.error-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.error-text {
  font-size: 12px;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  filter: brightness(1) contrast(1);
}

/* Add image orientation preservation */
.preserve-orientation {
  image-orientation: from-image;
}

.product-card:hover .product-image img {
  transform: scale(1.08);
  filter: brightness(1.1) contrast(1.1);
}

.product-info {
  padding: 24px;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  position: relative;
  z-index: 2;
}

.product-name {
  font-size: 18px;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: var(--color-text);
  font-weight: 700;
  line-height: 1.4;
  text-align: center;
}

.product-seller {
  font-size: 13px;
  color: var(--shop-text-muted);
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.product-price {
  margin: 8px 0;
  display: flex;
  align-items: center;
}

.current-price {
  font-size: 22px;
  font-weight: 800;
  color: var(--color-error);
  margin: 0;
  position: relative;
}

.current-price::before {
  content: '¥';
  position: absolute;
  left: -15px;
  top: 0;
  font-size: 16px;
  opacity: 0.7;
}

.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
}

.product-description {
  font-size: 13px;
  color: var(--shop-text-secondary);
  margin: 8px 0;
  flex-grow: 1;
  display: -webkit-box;
  /* -webkit-line-clamp: 2; */
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.add-to-cart {
  padding: 14px 20px;
  background: #2d5016;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: auto;
  font-weight: 600;
  font-size: 15px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  position: relative;
}

.add-to-cart:hover {
  background: #3a6b1a;
  transform: translateY(-1px);
}

.add-to-cart:active {
  transform: translateY(-1px);
}

/* 已售出商品的样式 */
.sold-out {
  opacity: 0.7;
  /* 整体稍微变暗 */
}

.sold-out .product-image img {
  filter: grayscale(100%);
  /* 图片变灰 */
}

.sold-out-mark {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3;
}

.sold-out-text {
  font-size: 20px;
  font-weight: bold;
  transform: rotate(-35deg);
  transform-origin: center;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
  background: linear-gradient(45deg, #e74c3c, #c0392b);
  padding: 8px 16px;
  border-radius: 8px;
  border: 2px solid white;
}

/* 已售出商品的按钮样式 */
.sold-out-btn {
  background: linear-gradient(135deg, #95a5a6 0%, #7f8c8d 100%) !important;
  cursor: not-allowed !important;
  opacity: 0.7;
}

.sold-out-btn:hover {
  background: linear-gradient(135deg, #95a5a6 0%, #7f8c8d 100%) !important;
  transform: none !important;
}

/* 列表模式样式 */
.product-card.list-mode {
  height: 160px;
  flex-direction: row;
  padding: 0;
}

.list-layout-content {
  display: flex;
  width: 100%;
  height: 100%;
  align-items: center;
  gap: 20px;
  padding: 20px;
}

.list-image-container {
  width: 120px;
  height: 120px;
  flex-shrink: 0;
  border-radius: 8px;
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
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  filter: brightness(1) contrast(1);
}

.list-image-container:hover img {
  transform: scale(1.05);
  filter: brightness(1.1) contrast(1.1);
}

.list-info-container {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.list-main-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-right: 20px;
}

.list-main-info .product-name {
  font-size: 18px;
  margin: 0;
  color: var(--color-text);
  font-weight: 700;
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: center;
}

.list-main-info .product-description {
  font-size: 14px;
  color: var(--shop-text-secondary);
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
}

.list-main-info .product-seller {
  font-size: 13px;
  color: var(--shop-text-muted);
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.list-action-container {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
  flex-shrink: 0;
}

.list-action-container .product-price {
  margin: 0;
}

.list-action-container .current-price {
  font-size: 20px;
  font-weight: 800;
  color: var(--color-error);
  margin: 0;
}

.list-action-container .add-to-cart {
  padding: 10px 20px;
  background: #2d5016;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 600;
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 0.3px;
  position: relative;
  min-width: 100px;
}

.list-action-container .add-to-cart:hover {
  background: #3a6b1a;
  transform: translateY(-1px);
}

/* 列表模式下的售出标记 */
.list-mode .sold-out-mark {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3;
}

.list-mode .sold-out-text {
  font-size: 16px;
  font-weight: bold;
  transform: rotate(-35deg);
  transform-origin: center;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
  background: linear-gradient(45deg, #e74c3c, #c0392b);
  padding: 6px 12px;
  border-radius: 6px;
  border: 2px solid white;
}

/* 列表模式下的已售出按钮 */
.list-mode .sold-out-btn {
  background: linear-gradient(135deg, #95a5a6 0%, #7f8c8d 100%) !important;
  cursor: not-allowed !important;
  opacity: 0.7;
}

.list-mode .sold-out-btn:hover {
  background: linear-gradient(135deg, #95a5a6 0%, #7f8c8d 100%) !important;
  transform: none !important;
}
</style>
