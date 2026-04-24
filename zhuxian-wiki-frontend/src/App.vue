<template>
  <div class="wiki-app">
    <WikiHeader />
    <div class="wiki-container">
      <WikiSidebarLeft />
      <main class="wiki-main-content">
        <router-view />
      </main>
      <WikiSidebarRight />
    </div>
    <WikiFooter />
  </div>
</template>

<script setup>
import WikiHeader from './components/layout/WikiHeader.vue'
import WikiSidebarLeft from './components/layout/WikiSidebarLeft.vue'
import WikiSidebarRight from './components/layout/WikiSidebarRight.vue'
import WikiFooter from './components/layout/WikiFooter.vue'
</script>

<style scoped>
.wiki-app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--color-paper);
}

.wiki-container {
  display: grid;
  grid-template-columns: 220px 1fr 280px;
  gap: 24px;
  max-width: 1280px;
  margin: 0 auto;
  padding: 24px;
  flex: 1;
  width: 100%;
}

.wiki-main-content {
  min-height: 500px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 20px;
}

@media (max-width: 1200px) {
  .wiki-container {
    grid-template-columns: 1fr;
  }

  :deep(.sidebar-left),
  :deep(.sidebar-right) {
    display: none;
  }
}
</style>
