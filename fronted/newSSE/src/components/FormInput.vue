<script setup lang="ts">
defineOptions({
  inheritAttrs: false,
})
const info = defineModel({
  type: String,
  required: true,
})
</script>

<template>
  <div class="field">
    <input
      v-model="info"
      class="field-input peer"
      placeholder=" "
      v-bind="$attrs"
      required
    >
    <label
      class="field-label"
      :for="$attrs.id as string"
    >{{ $attrs.label }}</label>
  </div>
</template>

<style scoped lang="scss">
.field {
  position: relative;
  margin-bottom: 1.15rem;
}

.field-input {
  width: 100%;
  min-height: 2.85rem;
  padding: 1.1rem 0.85rem 0.45rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--surface-2);
  color: var(--color-text);
  font-size: 1rem;
  font-family: var(--font-sans);
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    background-color 0.2s ease;
}

.field-input::placeholder {
  color: transparent;
}

.field-input:hover {
  border-color: rgba(13, 148, 136, 0.35);
}

.field-input:focus {
  outline: none;
  border-color: var(--accent);
  background: var(--surface-1);
  box-shadow: 0 0 0 3px var(--accent-muted);
}

.field-label {
  position: absolute;
  left: 0.85rem;
  top: 50%;
  transform: translateY(-50%);
  font-size: 0.95rem;
  color: var(--shop-text-secondary, #64748b);
  pointer-events: none;
  transition:
    transform 0.2s ease,
    font-size 0.2s ease,
    color 0.2s ease,
    top 0.2s ease;
  transform-origin: left center;
}

.field-input:focus ~ .field-label,
.field-input:not(:placeholder-shown) ~ .field-label {
  top: 0.45rem;
  transform: translateY(0) scale(0.78);
  color: var(--accent);
  font-weight: 600;
}

.field-input:-webkit-autofill ~ .field-label {
  top: 0.45rem;
  transform: translateY(0) scale(0.78);
  color: var(--accent);
  font-weight: 600;
}

body.dark-mode .field-input {
  background: var(--surface-2);
}

body.dark-mode .field-input:focus {
  background: var(--surface-1);
}
</style>
