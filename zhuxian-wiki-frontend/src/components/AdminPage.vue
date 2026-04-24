<template>
  <div class="admin-page-wrapper">
    <slot />
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: ''
  }
})
</script>

<style scoped>
.admin-page-wrapper {
  /* 子页面通用内边距，与 AdminLayout 保持一致 */
}
</style>
