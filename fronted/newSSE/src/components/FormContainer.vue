<script setup lang="ts">
import type { CustomFormData } from '@/composables/FormExam'

import FormInput from '@/components/FormInput.vue'

defineProps({
  formName: {
    type: String,
    required: false,
    default: '提交',
  },
  disabled: {
    type: Boolean,
    required: false,
    default: false,
  },
  formData: {
    // 要传递数组
    type: Array<CustomFormData>,
    required: true,
  },
})

defineEmits(['submitForm'])
</script>

<template>
  <form
    class="form-container"
    @submit.prevent="$emit('submitForm')"
  >
    <FormInput
      v-for="item in formData"
      :id="item.id"
      :key="item.id"
      v-model="item.value"
      :label="item.label"
      :type="item.type || ''"
      :autocomplete="item.autocomplete || 'off'"
    />
    <slot />
    <button
      :disabled="disabled"
      class="btn-primary"
      type="submit"
    >
      {{ disabled ? '请填写完整信息' : formName }}
    </button>
  </form>
</template>

<style scoped lang="scss">
.form-container {
  width: 100%;
  margin: 0;
  padding: 0;
}
</style>
