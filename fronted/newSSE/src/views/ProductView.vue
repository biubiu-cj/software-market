<script setup lang="ts">
import type { ProductDetail } from '@/api/shop/getProducts'
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
// 由于与本地声明冲突，改为重命名导入
import { deleteProduct as apiDeleteProduct, saleProduct as apiSaleProduct } from '@/api/shop/controlProduct'
import { getProductByID } from '@/api/shop/getProducts'
import { showMsg } from '@/components/MessageBox'
import { useChat } from '@/composables/useChat'
import { useUserStore } from '@/store/userStore'
import { getSellerName } from '@/utils/sellerNameMapper'

// 路由和状态管理
const router = useRouter()
const route = useRoute()
const { userInfo } = useUserStore()
const { setAnonymousMode } = useChat()

// 响应式数据
const product = ref<ProductDetail>({
  sellerId: 0,
  productId: 0,
  seller: '',
  price: 0,
  name: '',
  description: '',
  photos: [],
  isSold: false,
  isAnonymous: false,
})

const imageIndex = ref(0)
const isLoading = ref(true)
const error = ref<string | null>(null)
const isDeleting = ref(false)
const isSelling = ref(false)
const sellerName = ref<string>('')

// 计算属性
const isCurrentUser = computed(() => product.value.sellerId === userInfo.userID)
const hasImages = computed(() => product.value.photos && product.value.photos.length > 0)
const canInteract = computed(() => !product.value.isSold && !isDeleting.value && !isSelling.value)

// 获取商品详情的异步函数
async function fetchProductDetail(ProductID: number): Promise<void> {
  try {
    isLoading.value = true
    error.value = null
    const response = await getProductByID(ProductID)
    product.value = response

    // 获取卖家名称
    await fetchSellerName()
  }
  catch (err) {
    error.value = '获取商品详情失败，请稍后重试'
    console.error('Failed to fetch product detail:', err)
  }
  finally {
    isLoading.value = false
  }
}

// 获取卖家名称
async function fetchSellerName(): Promise<void> {
  try {
    const name = await getSellerName(product.value.sellerId)
    sellerName.value = name
  }
  catch (error) {
    console.error('获取卖家名称失败:', error)
    // 如果获取失败，使用原始数据或默认值
    sellerName.value = product.value.seller || `用户${product.value.sellerId}`
  }
}

// 轮播图控制
function nextImage(): void {
  if (hasImages.value) {
    imageIndex.value = (imageIndex.value + 1) % product.value.photos.length
  }
}

function prevImage(): void {
  if (hasImages.value) {
    imageIndex.value = (imageIndex.value - 1 + product.value.photos.length) % product.value.photos.length
  }
}

function goToImage(index: number): void {
  if (hasImages.value && index >= 0 && index < product.value.photos.length) {
    imageIndex.value = index
  }
}

// 私聊功能
function chatWithSeller(isAnonymous: boolean): void {
  if (!canInteract.value)
    return

  // 设置匿名模式
  setAnonymousMode(isAnonymous)

  if (isAnonymous) {
    showMsg('正在跳转到匿名私聊页面')
  }
  else {
    showMsg('正在跳转到私聊页面')
  }

  navigateChat()
}

// 返回商城主界面
function goBack(): void {
  router.push('/shop')
}

// 卖出商品
async function saleProduct(): Promise<void> {
  if (!canInteract.value)
    return

  if (confirm('确定要标记此商品为已售出吗？')) {
    try {
      isSelling.value = true
      // 调用后端API标记商品为已售出
      await apiSaleProduct(Number(product.value.productId))
      product.value.isSold = true
      showMsg('商品已标记为售出')
    }
    catch (err) {
      showMsg('操作失败，请稍后重试')
      console.error('Sale product error:', err)
    }
    finally {
      isSelling.value = false
    }
  }
}

// 删除商品
async function deleteProduct(): Promise<void> {
  if (!canInteract.value)
    return

  if (confirm('确定要删除此商品吗？此操作不可恢复！')) {
    try {
      isDeleting.value = true
      await apiDeleteProduct(Number(product.value.productId))
      showMsg('商品已删除')
      router.push('/shop')
    }
    catch (err) {
      showMsg('删除失败，请稍后重试')
      console.error('Delete product error:', err)
    }
    finally {
      isDeleting.value = false
    }
  }
}

function navigateChat(): void {
  if (product.value.sellerId > 0) {
    router.push({ name: 'Chat', query: { user: product.value.sellerId } })
  }
}

// 重试获取数据
function retryFetch(): void {
  const ProductID = route.params.ProductID
  if (ProductID) {
    fetchProductDetail(Number(ProductID))
  }
}

// 图片错误处理
function handleImageError(event: Event): void {
  const img = event.target as HTMLImageElement
  img.src = '/images/nophotos.png'
}

// 监听路由变化
watch(() => route.params.ProductID, (newId) => {
  if (newId) {
    fetchProductDetail(Number(newId))
  }
}, { immediate: true })

// 生命周期钩子
onMounted(() => {
  const ProductID = route.params.ProductID
  if (ProductID) {
    fetchProductDetail(Number(ProductID))
  }
})
</script>

<template>
  <div class="product-detail-container">
    <!-- 加载状态 -->
    <div v-if="isLoading" class="loading-container">
      <div class="loading-spinner" />
      <p>正在加载商品详情…</p>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error-container">
      <div class="error-content">
        <div class="error-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
            <line x1="12" y1="9" x2="12" y2="13" />
            <line x1="12" y1="17" x2="12.01" y2="17" />
          </svg>
        </div>
        <h3>加载失败</h3>
        <p>{{ error }}</p>
        <button class="retry-button" @click="retryFetch">
          重试
        </button>
      </div>
    </div>

    <!-- 商品详情内容 -->
    <template v-else>
      <!-- 返回按钮和操作按钮 -->
      <div class="top-bar">
        <button class="back-button" @click="goBack">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M19 12H5" />
            <polyline points="12 19 5 12 12 5" />
          </svg>
          <span>返回商城</span>
        </button>

        <div v-if="isCurrentUser" class="owner-actions">
          <button
            v-if="!product.isSold"
            class="sale-button"
            :disabled="isSelling"
            @click="saleProduct"
          >
            <span v-if="isSelling">处理中…</span>
            <span v-else>标记售出</span>
          </button>

          <button
            class="delete-button"
            :disabled="isDeleting"
            @click="deleteProduct"
          >
            <span v-if="isDeleting">删除中…</span>
            <span v-else>删除商品</span>
          </button>
        </div>
      </div>

      <!-- 商品图片轮播 -->
      <div class="product-images-carousel">
        <div class="carousel-container">
          <div class="carousel">
            <div class="carousel-inner" :style="{ transform: `translateX(-${imageIndex * 100}%)` }">
              <div v-for="(image, index) in product.photos" :key="`image-${index}`" class="carousel-item">
                <img
                  :src="image"
                  :alt="`${product.name} - 商品图片 ${index + 1}`"
                  loading="lazy"
                  @error="handleImageError"
                >
              </div>
            </div>

            <!-- 控制按钮 -->
            <button
              v-if="hasImages && product.photos.length > 1"
              class="carousel-control prev"
              aria-label="上一张"
              title="上一张"
              @click="prevImage"
            >
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="15,18 9,12 15,6" />
              </svg>
            </button>
            <button
              v-if="hasImages && product.photos.length > 1"
              class="carousel-control next"
              aria-label="下一张"
              title="下一张"
              @click="nextImage"
            >
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="9,18 15,12 9,6" />
              </svg>
            </button>

            <!-- 指示器 -->
            <div v-if="hasImages && product.photos.length > 1" class="carousel-indicators">
              <span
                v-for="(image, index) in product.photos"
                :key="`indicator-${index}`"
                class="indicator"
                :class="{ active: index === imageIndex }"
                :aria-label="`跳转到第${index + 1}张图片`"
                @click="goToImage(index)"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- 商品信息 -->
      <div class="product-info">
        <div class="product-header">
          <h1 class="product-title">
            {{ product.name }}
          </h1>
          <div v-if="product.isSold" class="sold-badge">
            已售出
          </div>
        </div>

        <div class="product-meta">
          <p class="product-seller">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
              <circle cx="12" cy="7" r="4" />
            </svg>
            卖家：{{ sellerName || product.seller || `用户${product.sellerId}` }}
          </p>
          <div class="product-price-info">
            <span class="current-price">¥{{ product.price.toLocaleString() }}</span>
          </div>
        </div>

        <div class="product-description">
          <h3>商品描述</h3>
          <p>{{ product.description }}</p>
        </div>

        <!-- 操作按钮 -->
        <div class="chat-actions">
          <button
            v-if="!isCurrentUser && canInteract"
            class="primary chat-button"
            @click="chatWithSeller(false)"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
            </svg>
            私聊商家
          </button>
          <button
            v-if="!isCurrentUser && canInteract"
            class="chat-button secondary"
            @click="chatWithSeller(true)"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
              <circle cx="9" cy="7" r="4" />
              <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
              <path d="M16 3.13a4 4 0 0 1 0 7.75" />
            </svg>
            匿名私聊
          </button>
          <div v-if="!canInteract && !isCurrentUser" class="sold-notice">
            此商品已售出，无法进行交易
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.product-detail-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  padding-top: 0.25rem;
  min-height: 60vh;
}

/* 加载与错误状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  min-height: 50vh;
  color: var(--color-text-secondary);
  font-size: 0.9rem;
}

.loading-spinner {
  width: 32px;
  height: 32px;
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

.error-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 50vh;
  padding: 2rem 1rem;
}

.error-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  text-align: center;
  background: var(--surface-1);
  padding: 2.5rem 2rem;
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
  max-width: 400px;
  color: var(--color-text);
}

.error-icon {
  color: var(--color-text-secondary);
}

.error-icon svg {
  width: 40px;
  height: 40px;
  opacity: 0.5;
}

.error-content h3 {
  font-size: 1.1rem;
  color: var(--color-text);
  margin: 0;
  font-weight: 600;
}

.error-content p {
  color: var(--color-text-secondary);
  margin: 0;
  font-size: 0.9rem;
  line-height: 1.5;
}

.retry-button {
  margin-top: 0.75rem;
  padding: 0.55rem 1.5rem;
  background: var(--accent);
  color: var(--accent-foreground);
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  font-weight: 600;
  font-size: 0.9rem;
  transition: background-color 0.2s ease;
}

.retry-button:hover {
  background: var(--accent-hover);
}

/* 顶部操作栏 */
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.85rem;
  flex-wrap: wrap;
}

.owner-actions {
  display: flex;
  gap: 0.6rem;
}

.back-button,
.sale-button,
.delete-button {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  min-height: 40px;
  padding: 0 1rem;
  border-radius: var(--radius-md);
  background: var(--surface-1);
  border: 1px solid var(--color-border);
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  color: var(--color-text);
  font-size: 0.9rem;
  font-weight: 500;
  transition:
    background-color 0.2s ease,
    border-color 0.2s ease,
    color 0.2s ease;
}

.back-button svg {
  width: 18px;
  height: 18px;
}

.back-button:hover {
  border-color: var(--accent);
  color: var(--accent);
}

.sale-button:hover:not(:disabled) {
  border-color: var(--accent);
  color: var(--accent);
  background: var(--accent-muted);
}

.delete-button:hover:not(:disabled) {
  border-color: color-mix(in srgb, var(--color-error) 55%, transparent);
  color: var(--color-error);
  background: color-mix(in srgb, var(--color-error) 8%, transparent);
}

.sale-button:disabled,
.delete-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 图片轮播 */
.product-images-carousel {
  width: 100%;
  margin-bottom: 1rem;
}

.carousel-container {
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: var(--surface-2);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
}

.carousel {
  position: relative;
  height: clamp(240px, 38vw, 420px);
}

.carousel-inner {
  display: flex;
  transition: transform 0.4s ease;
  height: 100%;
}

.carousel-item {
  min-width: 100%;
  height: 100%;
}

.carousel-item img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.carousel-control {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: var(--surface-1);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  width: 36px;
  height: 36px;
  border-radius: 50%;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-sm);
  transition:
    color 0.2s ease,
    box-shadow 0.2s ease;
}

.carousel-control:hover {
  color: var(--accent);
  box-shadow: var(--shadow-md);
}

.carousel-control.prev {
  left: 12px;
}

.carousel-control.next {
  right: 12px;
}

.carousel-indicators {
  position: absolute;
  bottom: 12px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  gap: 6px;
}

.indicator {
  width: 8px;
  height: 8px;
  border-radius: var(--radius-full);
  background: rgba(100, 116, 139, 0.4);
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  transition: all 0.25s ease;
}

.indicator.active {
  width: 20px;
  background: var(--accent);
}

/* 商品信息卡片 */
.product-info {
  background: var(--surface-1);
  padding: clamp(1.25rem, 3vw, 2rem);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border);
}

.product-header {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
}

.product-title {
  margin: 0;
  font-size: clamp(1.25rem, 2.5vw, 1.6rem);
  font-weight: 700;
  letter-spacing: -0.01em;
  color: var(--color-text);
  line-height: 1.35;
}

.sold-badge {
  flex-shrink: 0;
  margin-top: 0.3rem;
  padding: 3px 10px;
  border-radius: var(--radius-full);
  background: var(--color-hover);
  color: var(--color-text-secondary);
  font-size: 0.75rem;
  font-weight: 600;
}

.product-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
  padding-bottom: 0.9rem;
  margin-bottom: 1rem;
  border-bottom: 1px solid var(--color-border);
}

.product-seller {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 0.9rem;
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
}

.product-seller svg {
  width: 16px;
  height: 16px;
}

.product-price-info {
  display: flex;
  align-items: baseline;
}

.current-price {
  font-size: 1.6rem;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--color-price);
}

.product-description {
  margin-bottom: 1.25rem;
  line-height: 1.7;
  color: var(--color-text);
}

.product-description h3 {
  margin: 0 0 0.5rem;
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--color-text);
}

.product-description p {
  margin: 0;
  color: var(--color-text-secondary);
  white-space: pre-wrap;
  word-break: break-word;
}

.chat-actions {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
  margin-top: 1.5rem;
}

.chat-button {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  min-height: 44px;
  padding: 0 1.4rem;
  border-radius: var(--radius-md);
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  font-size: 0.95rem;
  font-weight: 600;
  transition:
    background-color 0.2s ease,
    border-color 0.2s ease,
    color 0.2s ease;
}

.chat-button svg {
  width: 18px;
  height: 18px;
}

.chat-button.primary {
  background: var(--accent);
  border: 1px solid var(--accent);
  color: var(--accent-foreground);
}

.chat-button.primary:hover {
  background: var(--accent-hover);
  border-color: var(--accent-hover);
}

.chat-button.secondary {
  background: transparent;
  border: 1px solid var(--color-border);
  color: var(--color-text);
}

.chat-button.secondary:hover {
  border-color: var(--accent);
  color: var(--accent);
  background: var(--accent-muted);
}

.sold-notice {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  min-height: 44px;
  padding: 0 1.1rem;
  border-radius: var(--radius-md);
  background: var(--color-hover);
  color: var(--color-text-secondary);
  font-size: 0.9rem;
}
</style>
