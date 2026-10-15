<script lang="ts" setup>
const partitions = [

  {
    name: '优质贴',
    src: '/images/star.png',
  },
  {
    name: '课程交流',
    src: '/images/icons8-open-book-48.png',
  },
  {
    name: '日常吐槽',
    src: '/images/icons8-chat-message-48.png',
  },
  {
    name: '打听求助',
    src: '/images/icons8-unverified-account-48.png',
  },
  {
    name: '学习交流',
    src: '/images/icons8-open-book-48.png',
  },
  {
    name: '院务',
    src: '/images/icons8-graduation-cap-48.png',
  },
  {
    name: '求职招募',
    src: '/images/icons8-commercial-48.png',
  },
  {
    name: '其他',
    src: '/images/icons8-link-48.png',
  },
]
</script>

<template>
  <div
    class="partition-strip mb-4 w-full flex overflow-x-auto overflow-y-hidden"
  >
    <RouterLink to="/partitions" class="partition w-1/5 flex flex-shrink-0 flex-col cursor-pointer items-center overflow-hidden p-2">
      <div class="bright-icon m-1 h-7.5 w-7.5 bg-contain bg-no-repeat" style="background-image: url(/images/more.png);" />
      <div class="whitespace-nowrap text-center text-2.5 font-800">
        全部分区
      </div>
    </RouterLink>
    <RouterLink
      v-for="p in partitions"
      :key="p.name"
      :to="`/partition/${p.name}`"
      class="partition w-1/5 flex flex-shrink-0 flex-col items-center overflow-hidden p-2"
    >
      <div
        class="bright-icon m-1 h-7.5 w-7.5 bg-contain bg-no-repeat"
        :style="{
          backgroundImage: `url(${p.src})`,
        }"
      />
      <div class="whitespace-nowrap text-center text-2.5 font-800">
        {{ p.name }}
      </div>
    </RouterLink>
  </div>
</template>

<style lang="scss">
.partition-strip {
  scrollbar-width: none;

  &::-webkit-scrollbar {
    display: none;
  }
}

.partition {
  border-radius: var(--radius-md);
  transition:
    background-color 0.2s ease,
    transform 0.15s ease;
}

.partition:hover,
.partition:active {
  background: var(--accent-muted);
  transform: translateY(-1px);
}

.partition.router-link-active {
  background: var(--accent-muted);
  color: var(--color-link);
}

body.dark-mode .bright-icon {
  filter: invert(1) opacity(0.85);
}
</style>
