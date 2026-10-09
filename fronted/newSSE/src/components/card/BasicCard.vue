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
    box-shadow 0.35s ease,
    transform 0.35s ease,
    border-color 0.2s ease;
}

p {
  text-indent: 2rem;
}
</style>
