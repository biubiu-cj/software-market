<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const partitions = ref([
  { name: '日常吐槽', description: '言短情长，抒发心声。', src: '/images/daily.jpg' },
  { name: '优质贴', description: '优质内容，分享交流。', src: '/images/secondhand.jpg' },
  { name: '课程交流', description: '课程内容，分享交流。', src: '/images/study.jpg' },
  { name: '打听求助', description: '疑虑相询，忧难互助。', src: '/images/help.jpg' },
  { name: '学习交流', description: '知识碰撞，学术思辨。', src: '/images/study.jpg' },
  { name: '院务', description: '院内事务，信息发布。', src: 'https://pic2.zhimg.com/v2-4fd432574e38d33d52e35041a8e7fd52_720w.jpg?source=172ae18b' },
  { name: '求职招募', description: '梦想职路，共创未来。', src: '/images/recruit.jpg' },
  { name: '其他', description: '畅所欲言，创意无限。', src: '/images/else.jpg' },
])

function sendPartition(p: string) {
  router.push(`/partition/${encodeURIComponent(p)}`)
}
</script>

<template>
  <div class="page-section partition-page">
    <h2 class="page-heading">
      选择分区
    </h2>
    <div class="partitions-grid">
      <article
        v-for="(p, index) in partitions"
        :key="index"
        class="partition-card"
      >
        <div class="partition-card-media">
          <img
            :src="p.src"
            alt=""
            class="partition-card-img"
          >
        </div>
        <div class="partition-card-body">
          <h3 class="partition-card-title">
            {{ p.name }}
          </h3>
          <p class="partition-card-desc">
            {{ p.description }}
          </p>
          <button
            type="button"
            class="partition-card-btn"
            @click="sendPartition(p.name)"
          >
            进入分区
          </button>
        </div>
      </article>
    </div>
  </div>
</template>

<style scoped lang="scss">
.partition-page {
  color: var(--color-text);
  padding-bottom: 2rem;
}

.partitions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(min(100%, 280px), 1fr));
  gap: 1.25rem;
  width: 100%;
  margin-top: 0.5rem;
}

.partition-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  background: var(--surface-1);
  box-shadow: var(--shadow-sm);
  transition:
    box-shadow 0.25s ease,
    transform 0.2s ease,
    border-color 0.2s ease;
}

.partition-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-3px);
  border-color: rgba(13, 148, 136, 0.35);
}

.partition-card-media {
  aspect-ratio: 16 / 10;
  overflow: hidden;
  background: var(--surface-2);
}

.partition-card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.partition-card-body {
  padding: 1rem 1.1rem 1.15rem;
  display: flex;
  flex-direction: column;
  flex: 1;
  gap: 0.35rem;
}

.partition-card-title {
  margin: 0;
  font-size: 1.15rem;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.partition-card-desc {
  margin: 0 0 0.5rem;
  font-size: 0.875rem;
  line-height: 1.5;
  color: var(--shop-text-secondary);
  flex: 1;
}

.partition-card-btn {
  margin-top: auto;
  align-self: flex-start;
  min-height: 38px;
  padding: 0.5rem 1.1rem;
  border-radius: var(--radius-md);
  border: none;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  background: var(--accent);
  color: var(--accent-foreground) !important;
  box-shadow: var(--shadow-xs);
  transition:
    background-color 0.2s ease,
    transform 0.15s ease;
}

.partition-card-btn:hover {
  background: var(--accent-hover);
  transform: translateY(-1px);
}

body.dark-mode .partition-card {
  background: var(--surface-1);
}

body.dark-mode .partition-card-img {
  opacity: 0.95;
}
</style>
