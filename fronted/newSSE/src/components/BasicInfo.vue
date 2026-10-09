<script lang="ts" setup>
const { isDense, browse, comment, isLike = false, like = 0, isDeny = false, deny = 0, noDeny = false } = defineProps<{
  isDense?: boolean
  browse?: number
  comment?: number
  nocomment?: boolean
  isLike?: boolean
  like?: number
  isDeny?: boolean
  deny?: number
  noDeny?: boolean
  time: string
}>()
const emit = defineEmits(['likeChange', 'denyChange'])

function LikeChange() {
  emit('likeChange')
}
function DenyChange() {
  emit('denyChange')
}
</script>

<template>
  <div>
    <div v-show="!isDense">
      <span class="text-3">{{
        new Date(time).toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit',
        })
      }}</span>
    </div>
    <div class="mt-3 flex justify-around">
      <span v-if="browse || browse === 0">
        {{ browse }}
        <svg
          viewBox="0 0 16 16"
          width="1em"
          height="1em"
          fill="currentColor"
        >
          <path
            d="M10.5 8a2.5 2.5 0 1 1-5 0 2.5 2.5 0 0 1 5 0z"
          />
          <path
            d="M0 8s3-5.5 8-5.5S16 8 16 8s-3 5.5-8 5.5S0 8 0 8zm8 3.5a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7z"
          />
        </svg>
      </span>
      <span v-if="(comment || comment === 0) && !nocomment">
        {{ comment < 0 ? 0 : comment }}
        <svg
          viewBox="0 0 16 16"
          width="1em"
          height="1em"
          fill="currentColor"
        >
          <path
            d="M16 8c0 3.866-3.582 7-8 7a9.06 9.06 0 0 1-2.347-.306c-.584.296-1.925.864-4.181 1.234-.2.032-.352-.176-.273-.362.354-.836.674-1.95.77-2.966C.744 11.37 0 9.76 0 8c0-3.866 3.582-7 8-7s8 3.134 8 7zM5 8a1 1 0 1 0-2 0 1 1 0 0 0 2 0zm4 0a1 1 0 1 0-2 0 1 1 0 0 0 2 0zm3 1a1 1 0 1 0 0-2 1 1 0 0 0 0 2z"
          />
        </svg>
      </span>
      <span
        class="cursor-pointer *:pointer-events-none" :class="isLike ? 'like' : ''" @click.prevent="LikeChange"
      >
        {{ like < 0 ? 0 : like }}
        <svg
          viewBox="0 0 16 16"
          width="1em"
          height="1em"
          fill="currentColor"
        >
          <path
            d="m8 2.748-.717-.737C5.6.281 2.514.878 1.4 3.053c-.523 1.023-.641 2.5.314 4.385.92 1.815 2.834 3.989 6.286 6.357 3.452-2.368 5.365-4.542 6.286-6.357.955-1.886.838-3.362.314-4.385C13.486.878 10.4.28 8.717 2.01L8 2.748zM8 15C-7.333 4.868 3.279-3.04 7.824 1.143c.06.055.119.112.176.171a3.12 3.12 0 0 1 .176-.17C12.72-3.042 23.333 4.867 8 15z"
          />
        </svg>
      </span>
      <span
        v-if="!noDeny" class="cursor-pointer *:pointer-events-none" :class="isDeny ? 'deny' : ''" @click.prevent="DenyChange"
      >
        {{ deny < 0 ? 0 : deny }}
        <svg
          viewBox="0 0 256 256"
          width="1em"
          height="1em"
          fill="currentColor"
        >
          <path d="M239.8 157.1a29.5 29.5 0 0 1-15.1 20.8 30.1 30.1 0 0 1-13 2.1H174a6 6 0 0 0-5.3 3.2l-42.3 87.2a30 30 0 0 1-25.9 16.9 6.02 6.02 0 0 1-5.7-4 29.8 29.8 0 0 1-1.5-20.8l13.1-44.7a6 6 0 0 0-5.8-7.6H54.2a30.2 30.2 0 0 1-24.7-12.8 29.4 29.4 0 0 1-4.6-26.5l28.5-110A30.1 30.1 0 0 1 82.4 38h110a6 6 0 0 1 6 6v102a6 6 0 0 1-6 6h-12a30.2 30.2 0 0 0 16.9 3.6 29.8 29.8 0 0 0 18.9-10.7 6 6 0 0 1 4.6-2.1h4.4a29.9 29.9 0 0 1 14.6 14.3Z"/>
        </svg>
      </span>
    </div>
  </div>
</template>

<style lang="scss">
.like {
  color: #ff0000;
  transition: all 0.3s ease;
}
.deny {
  color: #6b6b6b;
  transition: all 0.3s ease;
}
</style>
