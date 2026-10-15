<template>
  <div class="root card-root mx-a flex flex-col p-3">
    <slot />
  </div>
</template>

<style lang="scss">
.card-root {
  position: relative;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--surface-1);
  box-shadow: var(--shadow-sm);
  transition:
    box-shadow 0.25s ease,
    transform 0.25s ease,
    border-color 0.2s ease;
}

@media screen and (min-width: 768px) {
  .card-root:hover {
    box-shadow: var(--shadow-md);
    transform: translateY(-2px);
    border-color: color-mix(in srgb, var(--accent) 30%, var(--color-border));
  }
}

/* 正文自然段首行缩进（仅作用于卡片内部，避免影响其他页面） */
.card-root p:not(.no-indent) {
  text-indent: 2rem;
}
</style>
