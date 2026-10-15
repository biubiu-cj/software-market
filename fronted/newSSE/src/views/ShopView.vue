<script setup lang="ts">
import type { Product } from '@/api/shop/getProducts'
import { computed, inject, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductNum, getProducts } from '@/api/shop/getProducts'
import ProductCard from '@/components/card/ProductCard.vue'
import FloatingBall from '@/components/FloatingBall.vue'
import { preloadSellerNames } from '@/utils/sellerNameMapper'

interface CarouselItem {
  image: string
  title: string
  subtitle?: string
}

// 常量定义
const CAROUSEL_ITEMS: CarouselItem[] = [
  {
    image: '/images/product-wechat.jpg',
    title: '精选商品',
    subtitle: '品质保证，价格优惠',
  },
  {
    image: '/images/product-96618898.jpg',
    title: '热门推荐',
    subtitle: '人气爆款，限时特惠',
  },
  {
    image: '/images/product-121867383.jpg',
    title: '新品上市',
    subtitle: '最新款式，抢先体验',
  },
]

const AUTOPLAY_INTERVAL = 5000
const PRICE_RANGES = [
  { value: '', label: '全部' },
  { value: '0-5', label: '五元以内' },
  { value: '0-20', label: '二十元以内' },
  { value: '0-50', label: '五十元以内' },
  { value: '0-100', label: '一百元以内' },
  { value: '0-500', label: '五百元以内' },
  { value: '500-1000000', label: '五百元以上' },
] as const

// 路由和注入
const router = useRouter()
const route = useRoute()
const isPC = inject<boolean>('isPC', false)

// 响应式数据
const isLoading = ref(true)
const products = ref<Product[]>([])
const productCount = ref(0)
const selectedPriceRange = ref<string>('')
const currentIndex = ref(0)
const autoplay = ref(true)
const autoplayTimer = ref<number | null>(null)
const error = ref<string | null>(null)
const retryCount = ref(0)
const maxRetries = 3

// 布局相关状态
const layoutMode = ref<'grid' | 'list'>('grid') // 'grid' 为竖排，'list' 为横排

// 使用常量而不是响应式数据
const carouselItems = CAROUSEL_ITEMS

// 计算属性
const isMain = computed(() => /^\/shop\/?$/.test(route.fullPath))

const filteredProducts = computed<Product[]>(() => {
  if (!selectedPriceRange.value)
    return products.value

  const [min, max] = selectedPriceRange.value.split('-')
  const minPrice = Number.parseInt(min, 10)

  return products.value.filter((product) => {
    if (max === '+') {
      return product.price >= minPrice
    }
    const maxPrice = Number.parseInt(max, 10)
    return product.price >= minPrice && product.price <= maxPrice
  })
})

const hasProducts = computed(() => products.value.length > 0)
const hasFilteredProducts = computed(() => filteredProducts.value.length > 0)
const displayProductCount = computed(() => {
  if (selectedPriceRange.value)
    return filteredProducts.value.length
  return productCount.value
})

// 错误处理和重试机制
async function fetchProductsWithRetry(): Promise<void> {
  try {
    error.value = null
    const endpoint = isMain.value ? 'home' : 'history'
    products.value = await getProducts(endpoint)
    retryCount.value = 0

    // 预加载卖家名称
    if (products.value.length > 0) {
      const sellerIds = products.value.map(product => product.sellerId)
      await preloadSellerNames(sellerIds)
    }
  }
  catch (err) {
    retryCount.value++
    if (retryCount.value < maxRetries) {
      console.warn(`获取商品数据失败，正在重试 (${retryCount.value}/${maxRetries})`)
      setTimeout(() => fetchProductsWithRetry(), 1000 * retryCount.value)
    }
    else {
      error.value = '获取商品数据失败，请稍后重试'
      console.error('获取商品数据失败:', err)
    }
  }
}

// 初始化数据
async function initData(): Promise<void> {
  try {
    isLoading.value = true
    await fetchProductsWithRetry()
    if (!error.value) {
      // 获取商品总数
      const endpoint = isMain.value ? 'home' : 'history'
      productCount.value = await getProductNum(endpoint)
      setupAutoplay()
    }
  }
  finally {
    isLoading.value = false
  }
}

// 重试获取数据
function retryFetch(): void {
  retryCount.value = 0
  initData()
}

// 轮播图控制
function setupAutoplay(): void {
  if (autoplay.value && carouselItems.length > 1) {
    startAutoplay()
  }
}

function startAutoplay(): void {
  stopAutoplay() // 确保没有重复的定时器
  autoplayTimer.value = setInterval(() => {
    nextSlide()
  }, AUTOPLAY_INTERVAL)
}

function stopAutoplay(): void {
  if (autoplayTimer.value) {
    clearInterval(autoplayTimer.value)
    autoplayTimer.value = null
  }
}

function nextSlide(): void {
  currentIndex.value = (currentIndex.value + 1) % carouselItems.length
}

function prevSlide(): void {
  currentIndex.value = (currentIndex.value - 1 + carouselItems.length) % carouselItems.length
}

function goToSlide(index: number): void {
  if (index >= 0 && index < carouselItems.length) {
    currentIndex.value = index
  }
}

// 轮播图交互控制
function handleCarouselMouseEnter(): void {
  stopAutoplay()
}

function handleCarouselMouseLeave(): void {
  if (autoplay.value) {
    startAutoplay()
  }
}

// 商品相关方法
function handleViewDetail(product: Product): void {
  router.push(`/shop/productdetail/${product.productId}`)
}

// 价格筛选
function handlePriceRangeChange(range: string): void {
  selectedPriceRange.value = range
}

// 设置布局模式
function setLayoutMode(mode: 'grid' | 'list'): void {
  layoutMode.value = mode
}

// 图片错误处理
function handleImageError(event: Event): void {
  const img = event.target as HTMLImageElement
  img.src = '/images/nophotos.png'
}

// 监听路由变化，重新获取数据
watch(() => route.fullPath, () => {
  if (route.path.startsWith('/shop')) {
    initData()
  }
}, { immediate: false })

// 监听页面可见性，控制轮播
watch(() => document.visibilityState, (visibilityState) => {
  if (visibilityState === 'visible' && autoplay.value) {
    startAutoplay()
  }
  else {
    stopAutoplay()
  }
})

// 生命周期钩子
onMounted(async () => {
  await initData()
})

onBeforeUnmount(() => {
  stopAutoplay()
})
</script>

<template>
  <div class="shop-view w-full">
    <!-- 加载中骨架屏 -->
    <template v-if="isLoading">
      <div class="skeleton-screen">
        <div class="skeleton-top-section">
          <div class="skeleton-carousel" />
          <div class="skeleton-filter" />
        </div>
        <div class="skeleton-bottom-section">
          <h2 class="skeleton-title" />
          <div class="skeleton-product-list">
            <div v-for="i in 8" :key="i" class="skeleton-product-card" />
          </div>
        </div>
      </div>
    </template>

    <!-- 错误状态 -->
    <template v-else-if="error">
      <div class="error-container">
        <div class="error-content">
          <div class="error-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
              <line x1="12" y1="9" x2="12" y2="13" />
              <line x1="12" y1="17" x2="12.01" y2="17" />
            </svg>
          </div>
          <h3 class="error-title">
            加载失败
          </h3>
          <p class="error-message">
            {{ error }}
          </p>
          <button class="retry-button" @click="retryFetch">
            重试
          </button>
        </div>
      </div>
    </template>

    <!-- 实际内容 -->
    <template v-else>
      <!-- 轮播图 + 筛选 -->
      <div class="top-section">
        <div class="carousel-container">
          <div
            class="carousel"
            @mouseenter="handleCarouselMouseEnter"
            @mouseleave="handleCarouselMouseLeave"
          >
            <div class="carousel-inner" :style="{ transform: `translateX(-${currentIndex * 100}%)` }">
              <div
                v-for="(item, index) in carouselItems"
                :key="`carousel-${index}`"
                class="carousel-item"
              >
                <img
                  :src="item.image"
                  :alt="item.title"
                  loading="lazy"
                  @error="handleImageError"
                >
                <div class="carousel-overlay">
                  <h2 class="carousel-title">
                    {{ item.title }}
                  </h2>
                  <p class="carousel-subtitle">
                    {{ item.subtitle || '精选商品推荐' }}
                  </p>
                </div>
              </div>
            </div>

            <!-- 控制按钮 -->
            <button
              v-if="carouselItems.length > 1"
              class="carousel-control prev"
              aria-label="上一张"
              title="上一张"
              @click="prevSlide"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="15,18 9,12 15,6" />
              </svg>
            </button>
            <button
              v-if="carouselItems.length > 1"
              class="carousel-control next"
              aria-label="下一张"
              title="下一张"
              @click="nextSlide"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="9,18 15,12 9,6" />
              </svg>
            </button>

            <!-- 指示器 -->
            <div v-if="carouselItems.length > 1" class="carousel-indicators">
              <span
                v-for="(item, index) in carouselItems"
                :key="`indicator-${index}`"
                class="indicator"
                :class="{ active: index === currentIndex }"
                :aria-label="`跳转到第${index + 1}张`"
                @click="goToSlide(index)"
              />
            </div>
          </div>
        </div>

        <!-- 筛选条件 -->
        <aside class="filter-container">
          <div class="filter-section">
            <h3 class="filter-title">
              筛选
            </h3>
            <label class="price-label" for="priceRange">价格区间</label>
            <select
              id="priceRange"
              v-model="selectedPriceRange"
              class="price-select"
              @change="handlePriceRangeChange(selectedPriceRange)"
            >
              <option
                v-for="range in PRICE_RANGES"
                :key="range.value"
                :value="range.value"
              >
                {{ range.label }}
              </option>
            </select>
          </div>
        </aside>
      </div>

      <!-- 商品展示 -->
      <div class="bottom-section">
        <div class="product-list-header">
          <div class="header-left">
            <h2 class="product-list-title">
              {{ isMain ? '在售商品' : '我的商品' }}
            </h2>
            <div class="product-count">
              共 {{ displayProductCount }} 件商品
            </div>
          </div>

          <!-- 布局切换 -->
          <div class="layout-controls">
            <div class="layout-toggle">
              <button
                class="layout-btn"
                :class="{ active: layoutMode === 'grid' }"
                title="网格布局"
                aria-label="网格布局"
                @click="setLayoutMode('grid')"
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="3" width="7" height="7" />
                  <rect x="14" y="3" width="7" height="7" />
                  <rect x="14" y="14" width="7" height="7" />
                  <rect x="3" y="14" width="7" height="7" />
                </svg>
              </button>
              <button
                class="layout-btn"
                :class="{ active: layoutMode === 'list' }"
                title="列表布局"
                aria-label="列表布局"
                @click="setLayoutMode('list')"
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="8" y1="6" x2="21" y2="6" />
                  <line x1="8" y1="12" x2="21" y2="12" />
                  <line x1="8" y1="18" x2="21" y2="18" />
                  <line x1="3" y1="6" x2="3.01" y2="6" />
                  <line x1="3" y1="12" x2="3.01" y2="12" />
                  <line x1="3" y1="18" x2="3.01" y2="18" />
                </svg>
              </button>
            </div>
          </div>
        </div>

        <!-- 商品列表 -->
        <div class="product-list-wrapper">
          <div
            v-if="hasFilteredProducts"
            class="product-list"
            :class="{
              'grid-layout': layoutMode === 'grid',
              'list-layout': layoutMode === 'list',
            }"
          >
            <ProductCard
              v-for="product in filteredProducts"
              :key="product.productId"
              :product="product"
              :layout-mode="layoutMode"
              @view-detail="handleViewDetail"
            />
          </div>
          <div v-else-if="hasProducts" class="empty-state">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <circle cx="11" cy="11" r="8" />
              <line x1="21" y1="21" x2="16.65" y2="16.65" />
            </svg>
            <h3>没有找到符合条件的商品</h3>
            <p>请尝试调整筛选条件</p>
            <button class="clear-filter-btn" @click="selectedPriceRange = ''">
              清除筛选
            </button>
          </div>
          <div v-else class="empty-state">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z" />
              <line x1="3" y1="6" x2="21" y2="6" />
              <path d="M16 10a4 4 0 0 1-8 0" />
            </svg>
            <h3>暂无商品</h3>
            <p>{{ isMain ? '热门商品即将上线，敬请期待！' : '您还没有发布任何商品' }}</p>
          </div>
        </div>
      </div>
    </template>
    <template v-if="!isPC">
      <FloatingBall />
    </template>
  </div>
</template>

<style scoped>
.shop-view {
  width: 100%;
}

/* ---------- 骨架屏 ---------- */
.skeleton-screen {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.skeleton-top-section {
  display: flex;
  gap: 1rem;
}

.skeleton-carousel {
  flex: 1;
  height: clamp(180px, 24vw, 260px);
  border-radius: var(--radius-lg);
  background: var(--shop-skeleton-bg);
  animation: skeleton-pulse 1.6s ease-in-out infinite;
}

.skeleton-filter {
  width: 220px;
  flex-shrink: 0;
  height: 140px;
  border-radius: var(--radius-lg);
  background: var(--shop-skeleton-bg);
  animation: skeleton-pulse 1.6s ease-in-out infinite;
}

.skeleton-bottom-section {
  padding-top: 0.5rem;
}

.skeleton-title {
  width: 160px;
  height: 26px;
  border-radius: var(--radius-sm);
  background: var(--shop-skeleton-bg);
  margin: 0 0 1rem;
  animation: skeleton-pulse 1.6s ease-in-out infinite;
}

.skeleton-product-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
  gap: 1rem;
}

.skeleton-product-card {
  aspect-ratio: 3 / 4;
  border-radius: var(--radius-lg);
  background: var(--shop-skeleton-bg);
  animation: skeleton-pulse 1.6s ease-in-out infinite;
}

@keyframes skeleton-pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.55;
  }
}

/* ---------- 错误状态 ---------- */
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
  text-align: center;
  gap: 0.5rem;
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

.error-title {
  font-size: 1.1rem;
  color: var(--color-text);
  margin: 0;
  font-weight: 600;
}

.error-message {
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

/* ---------- 顶部：轮播 + 筛选 ---------- */
.top-section {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.25rem;
}

.carousel-container {
  flex: 1;
  min-width: 0;
}

.filter-container {
  width: 220px;
  flex-shrink: 0;
}

/* 轮播图 */
.carousel {
  position: relative;
  height: clamp(180px, 24vw, 260px);
  overflow: hidden;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  background: var(--surface-1);
  border: 1px solid var(--color-border);
}

.carousel-inner {
  display: flex;
  transition: transform 0.5s ease;
  height: 100%;
}

.carousel-item {
  min-width: 100%;
  height: 100%;
  position: relative;
}

.carousel-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.carousel-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(180deg, transparent 0%, rgba(0, 0, 0, 0.35) 70%, rgba(0, 0, 0, 0.65) 100%);
  color: #fff;
  padding: 1.5rem 1.25rem 1rem;
  text-align: left;
}

.carousel-title {
  font-size: clamp(1.05rem, 2vw, 1.35rem);
  font-weight: 700;
  margin: 0 0 0.2rem;
  color: #fff;
}

.carousel-subtitle {
  font-size: 0.85rem;
  margin: 0;
  opacity: 0.92;
  font-weight: 400;
}

/* 轮播控制按钮 */
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
  transition:
    background-color 0.2s ease,
    box-shadow 0.2s ease;
  box-shadow: var(--shadow-sm);
}

.carousel-control:hover {
  box-shadow: var(--shadow-md);
  color: var(--accent);
}

.carousel-control.prev {
  left: 12px;
}

.carousel-control.next {
  right: 12px;
}

/* 轮播指示器 */
.carousel-indicators {
  position: absolute;
  bottom: 12px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  gap: 6px;
  padding: 8px;
}

.indicator {
  width: 8px;
  height: 8px;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.45);
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  transition: all 0.25s ease;
}

.indicator:hover {
  background: rgba(255, 255, 255, 0.75);
}

.indicator.active {
  width: 20px;
  background: #fff;
}

/* 筛选卡片 */
.filter-section {
  background: var(--surface-1);
  border-radius: var(--radius-lg);
  padding: 1.1rem 1rem;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border);
}

.filter-title {
  margin: 0 0 0.9rem;
  font-size: 0.95rem;
  color: var(--color-text);
  font-weight: 600;
}

.price-label {
  display: block;
  font-weight: 500;
  color: var(--color-text-secondary);
  font-size: 0.82rem;
  margin-bottom: 0.45rem;
}

.price-select {
  width: 100%;
  height: 2.5rem;
  padding: 0 2.25rem 0 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--surface-1);
  color: var(--color-text);
  font-size: 0.9rem;
  font-family: var(--font-sans);
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease;
  appearance: none;
  background-image: url("data:image/svg+xml;charset=UTF-8,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%2364748b' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3e%3cpolyline points='6,9 12,15 18,9'%3e%3c/polyline%3e%3c/svg%3e");
  background-repeat: no-repeat;
  background-position: right 10px center;
  background-size: 15px;
}

.price-select:hover {
  border-color: color-mix(in srgb, var(--accent) 45%, var(--color-border));
}

.price-select:focus {
  outline: none;
  border-color: var(--accent);
  box-shadow: 0 0 0 3px var(--accent-muted);
}

/* ---------- 商品列表 ---------- */
.product-list-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 1rem;
  margin-bottom: 1rem;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.product-list-title {
  margin: 0;
  font-size: 1.3rem;
  color: var(--color-text);
  font-weight: 700;
  letter-spacing: -0.01em;
}

.product-count {
  color: var(--color-text-secondary);
  font-size: 0.82rem;
}

.layout-controls {
  display: flex;
  align-items: center;
}

.layout-toggle {
  display: flex;
  gap: 2px;
  background: var(--surface-2);
  border-radius: var(--radius-md);
  padding: 3px;
  border: 1px solid var(--color-border);
}

.layout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: none;
  border-radius: calc(var(--radius-md) - 3px);
  background: transparent;
  color: var(--color-text-secondary);
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  transition:
    background-color 0.2s ease,
    color 0.2s ease,
    box-shadow 0.2s ease;
}

.layout-btn:hover {
  color: var(--color-text);
}

.layout-btn.active {
  background: var(--surface-1);
  color: var(--accent);
  box-shadow: var(--shadow-xs);
}

/* 空状态内的清除筛选按钮 */
.clear-filter-btn {
  margin-top: 0.75rem;
  padding: 0.5rem 1.25rem;
  background: var(--accent);
  color: var(--accent-foreground);
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  font-weight: 600;
  font-size: 0.875rem;
  transition: background-color 0.2s ease;
}

.clear-filter-btn:hover {
  background: var(--accent-hover);
}

.product-list-wrapper {
  min-height: 300px; /* 防止内容为空时区域骤缩 */
}

.product-list {
  display: grid;
  gap: 1rem;
}

.product-list.grid-layout {
  grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
}

.product-list.list-layout {
  grid-template-columns: 1fr;
  gap: 0.75rem;
}

/* 超宽屏限制 4 列，避免卡片过窄 */
@media screen and (min-width: 1300px) {
  .product-list.grid-layout {
    grid-template-columns: repeat(4, 1fr);
  }
}

/* 移动端单列 */
@media screen and (max-width: 600px) {
  .product-list.grid-layout {
    grid-template-columns: 1fr;
  }
}

/* ---------- 响应式：移动端 ---------- */
@media (max-width: 767px) {
  .skeleton-top-section {
    flex-direction: column;
  }

  .skeleton-carousel {
    height: 170px;
  }

  .skeleton-filter {
    width: 100%;
    height: 120px;
  }

  .top-section {
    flex-direction: column;
    gap: 0.75rem;
    margin-bottom: 1rem;
  }

  .carousel {
    height: 170px;
  }

  .carousel-overlay {
    padding: 1rem 0.9rem 0.8rem;
  }

  .filter-container {
    width: 100%;
  }

  .product-list-header {
    align-items: center;
  }

  .layout-controls {
    display: none; /* 移动端隐藏布局切换，统一单列 */
  }
}
</style>
