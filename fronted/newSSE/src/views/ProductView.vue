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
  img.src = 'https://via.placeholder.com/500x400?text=图片加载失败'
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
  <div class="product-detail-container page-section">
    <!-- 加载状态 -->
    <div v-if="isLoading" class="loading-container">
      <div class="loading-spinner" />
      <p>正在加载商品详情...</p>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error-container">
      <div class="error-content">
        <div class="error-icon">
          ⚠️
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
      <div class="back-button-container">
        <button class="back-button" @click="goBack">
          <img width="24" height="24" src="https://img.icons8.com/sf-black/24/return.png" alt="return">
          <span>返回商城</span>
        </button>

        <div v-if="isCurrentUser" class="action-buttons">
          <button
            v-if="!product.isSold"
            class="sale-button"
            :disabled="isSelling"
            @click="saleProduct"
          >
            <span v-if="isSelling">处理中...</span>
            <span v-else>标记售出</span>
          </button>

          <button
            class="delete-button"
            :disabled="isDeleting"
            @click="deleteProduct"
          >
            <span v-if="isDeleting">删除中...</span>
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
                  :alt="`商品图片 ${index + 1}`"
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
              @click="prevImage"
            >
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="15,18 9,12 15,6" />
              </svg>
            </button>
            <button
              v-if="hasImages && product.photos.length > 1"
              class="carousel-control next"
              aria-label="下一张"
              @click="nextImage"
            >
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
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
            <span class="seller-icon">👤</span>
            卖家: {{ sellerName || product.seller || `用户${product.sellerId}` }}
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
        <div class="action-buttons">
          <button
            v-if="!isCurrentUser && canInteract"
            class="chat-button primary"
            @click="chatWithSeller(false)"
          >
            💬 私聊商家
          </button>
          <button
            v-if="!isCurrentUser && canInteract"
            class="chat-button secondary"
            @click="chatWithSeller(true)"
          >
            🎭 匿名私聊
          </button>
          <div v-if="!canInteract && !isCurrentUser" class="sold-notice">
            <span class="notice-icon">⛔</span>
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
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
}

.product-detail-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 0;
}

.product-detail-container > * {
  position: relative;
  z-index: 1;
}

/* 加载和错误状态样式 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  padding: 40px;
  color: var(--color-text);
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid var(--shop-skeleton-bg);
  border-top: 4px solid var(--color-info);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
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
  min-height: 60vh;
  padding: 40px 20px;
}

.error-content {
  text-align: center;
  background: var(--shop-section-bg);
  padding: 40px;
  border-radius: 16px;
  box-shadow: 0 8px 30px var(--color-post-card-box-shadow);
  backdrop-filter: blur(10px);
  max-width: 400px;
  color: var(--color-text);
}

.error-icon {
  font-size: 48px;
  margin-bottom: 20px;
}

.error-content h3 {
  font-size: 24px;
  color: var(--color-error);
  margin-bottom: 12px;
  font-weight: 600;
}

.error-content p {
  color: var(--shop-text-secondary);
  margin-bottom: 24px;
  line-height: 1.5;
}

.retry-button {
  padding: 12px 24px;
  background: linear-gradient(135deg, #4c8baf 0%, #81b3e9 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s ease;
}

.retry-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(76, 139, 175, 0.3);
}

/* 返回按钮和操作按钮样式 */
.back-button-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background: var(--shop-section-bg);
  border-radius: 12px;
  box-shadow: 0 4px 15px var(--color-post-card-box-shadow);
  backdrop-filter: blur(10px);
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.back-button,
.delete-button {
  padding: 12px 20px;
  background: var(--shop-card-bg);
  border: 2px solid var(--color-border);
  border-radius: 8px;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  color: var(--color-text);
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 10px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px var(--color-post-card-box-shadow);
}

.back-button:hover {
  color: var(--color-info);
  border-color: var(--color-info);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px var(--color-hover);
}

.delete-button:hover {
  color: var(--color-error);
  border-color: var(--color-error);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px var(--color-hover);
}

.back-button img,
.delete-button img {
  width: 24px;
  height: 24px;
  object-fit: contain;
}

/* 图片轮播样式 */
.product-images-carousel {
  width: 100%;
  margin-bottom: 30px;
}

.carousel-container {
  box-shadow: 0 8px 30px var(--color-post-card-box-shadow);
  border-radius: 16px;
  overflow: hidden;
  background: var(--shop-card-bg);
  border: 1px solid var(--color-border);
}

.carousel {
  position: relative;
  height: 500px;
}

.carousel-inner {
  display: flex;
  transition: transform 0.5s ease;
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

/* 轮播控制按钮 */
.carousel-control {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  font-size: 18px;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
}

.carousel-control.prev {
  left: 15px;
}

.carousel-control.next {
  right: 15px;
}

/* 轮播指示器 */
.carousel-indicators {
  position: absolute;
  bottom: 20px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  gap: 8px;
}

.indicator {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
}

.indicator.active {
  background-color: white;
}

/* 商品信息样式 */
.product-info {
  background: var(--shop-section-bg);
  padding: 40px;
  border-radius: 20px;
  box-shadow: 0 20px 60px var(--color-post-card-box-shadow);
  border: 1px solid var(--color-border);
  position: relative;
  backdrop-filter: blur(20px);
  overflow: hidden;
}

@keyframes shimmer {
  0%,
  100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.product-title {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 28px;
  color: var(--color-text);
}

.product-seller {
  margin-bottom: 15px;
  color: var(--shop-text-secondary);
}

.product-price-info {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.current-price {
  font-size: 24px;
  font-weight: bold;
  color: var(--color-error);
  margin-right: 10px;
}

.original-price {
  font-size: 18px;
  color: #999;
  text-decoration: line-through;
}

.product-description {
  margin-bottom: 30px;
  line-height: 1.6;
  color: var(--color-text);
}

.product-description h3 {
  color: var(--color-text);
}

.product-description p {
  color: var(--shop-text-muted);
}

.action-buttons {
  display: flex;
  gap: 15px;
  margin-top: 20px;
}

.action-buttons button {
  padding: 14px 24px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  box-shadow: 0 4px 12px var(--color-post-card-box-shadow);
}
</style>
