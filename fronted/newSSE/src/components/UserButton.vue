<script lang="ts" setup>
const { noSave = false, showDelete = false } = defineProps<{
  noSave?: boolean
  isSaved?: boolean
  isSelf: boolean
  showDelete?: boolean
}>()
const emits = defineEmits(['userAction'])
function handleSave() {
  emits('userAction', 'save')
}
function handleDelete() {
  emits('userAction', 'delete')
}
</script>

<template>
  <div class="userButtons ml-a mt-0 h-fit flex flex-row items-center">
    <button
      v-if="!noSave"
      type="button"
      class="icon-btn"
      :class="{ saved: isSaved }"
      :title="isSaved ? '取消收藏' : '收藏'"
      :aria-label="isSaved ? '取消收藏' : '收藏'"
      @click.stop.prevent="handleSave"
    >
      <svg
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
        aria-hidden="true"
      >
        <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" />
      </svg>
    </button>

    <button
      v-if="isSelf && showDelete"
      type="button"
      class="delete-btn"
      @click.stop.prevent="handleDelete"
    >
      删除
    </button>
  </div>
</template>

<style lang="scss">
.userButtons {
  gap: 4px;

  .icon-btn svg {
    width: 20px;
    height: 20px;
  }

  .icon-btn.saved {
    color: var(--accent);

    svg {
      fill: var(--accent-muted);
    }
  }

  .delete-btn {
    min-height: 36px;
    padding: 0 12px;
    border: 1px solid var(--color-border);
    border-radius: var(--radius-md);
    background: transparent;
    color: var(--color-text-secondary);
    font-size: 0.85rem;
    font-weight: 500;
  }

  .delete-btn:hover {
    color: var(--color-error);
    border-color: color-mix(in srgb, var(--color-error) 45%, transparent);
    background: color-mix(in srgb, var(--color-error) 8%, transparent);
  }
}
</style>
