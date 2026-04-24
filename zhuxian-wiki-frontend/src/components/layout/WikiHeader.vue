<template>
  <header class="wiki-header">
    <div class="header-content">
      <!-- Logo 区域 -->
      <div class="header-brand">
        <router-link to="/" class="logo-link">
          <img src="/big_logo.webp" alt="Logo" class="logo-icon" />
          <span class="logo-text">诛仙世界</span>
        </router-link>
        <span class="logo-subtitle">Wiki</span>
      </div>

      <!-- 搜索区域 -->
      <div class="header-search">
        <input
          type="text"
          v-model="searchQuery"
          @keyup.enter="handleSearch"
          placeholder="搜索词条..."
          class="search-input"
        />
        <button @click="handleSearch" class="search-btn">搜索</button>
      </div>

      <!-- 导航菜单 -->
      <nav class="header-nav">
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link to="/category/门派" class="nav-link">门派</router-link>
        <router-link to="/category/职业" class="nav-link">职业</router-link>
        <router-link to="/category/攻略" class="nav-link">攻略</router-link>
        <router-link to="/ai" class="nav-link nav-link-ai">AI 助手</router-link>
      </nav>

      <!-- 用户区域 -->
      <div class="header-user">
        <button class="user-btn" @click="goToAdmin" title="后台管理">
          <span class="user-icon">管</span>
        </button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const searchQuery = ref('')

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({ path: '/search', query: { keyword: searchQuery.value.trim() } })
  }
}

const goToAdmin = () => {
  // 管理后台是独立入口，必须用 window.location.href 而非前台 router.push
  window.location.href = '/admin.html/login'
}
</script>

<style scoped>
.wiki-header {
  background: var(--color-white);
  border-bottom: 1px solid var(--color-border);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
  height: 56px;
  display: flex;
  align-items: center;
  gap: 32px;
}

/* Logo 区域 */
.header-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.logo-link {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
}

.logo-icon {
  width: 36px;
  height: 36px;
  object-fit: contain;
  border-radius: var(--radius-sm);
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  font-family: var(--font-serif);
  color: var(--color-ink);
}

.logo-subtitle {
  font-size: 12px;
  color: var(--color-ink-muted);
  padding: 2px 6px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
}

/* 搜索区域 */
.header-search {
  flex: 1;
  max-width: 400px;
  display: flex;
  gap: 0;
}

.search-input {
  flex: 1;
  padding: 8px 12px;
  font-size: 13px;
  border: 1px solid var(--color-border);
  border-right: none;
  border-radius: var(--radius-sm) 0 0 var(--radius-sm);
  background: var(--color-cream);
  color: var(--color-ink);
  
  &:focus {
    outline: none;
    border-color: var(--color-ochre);
    background: var(--color-white);
  }
  
  &::placeholder {
    color: var(--color-ink-muted);
  }
}

.search-btn {
  padding: 8px 16px;
  font-size: 13px;
  font-weight: 500;
  border: 1px solid var(--color-border);
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
  background: var(--color-ink);
  color: var(--color-white);
  cursor: pointer;
  transition: background 0.2s;
  
  &:hover {
    background: var(--color-ink-light);
  }
}

/* 导航菜单 */
.header-nav {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-link {
  padding: 8px 12px;
  font-size: 14px;
  color: var(--color-ink-light);
  text-decoration: none;
  border-radius: var(--radius-sm);
  transition: all 0.2s;
  
  &:hover {
    color: var(--color-ink);
    background: var(--color-paper-dark);
    text-decoration: none;
  }
  
  &.router-link-active {
    color: var(--color-cinnabar);
    font-weight: 500;
  }
  
  &.nav-link-ai {
    color: var(--color-cinnabar);
    border: 1px solid var(--color-cinnabar);
    
    &:hover {
      background: var(--color-cinnabar);
      color: var(--color-white);
    }
  }
}

/* 用户区域 */
.header-user {
  flex-shrink: 0;
}

.user-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-white);
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    background: var(--color-paper-dark);
    border-color: var(--color-border-dark);
  }
}

.user-icon {
  font-size: 14px;
  font-family: var(--font-serif);
  color: var(--color-ink-light);
}

/* 响应式 */
@media (max-width: 768px) {
  .header-content {
    padding: 0 16px;
    gap: 16px;
  }
  
  .header-search {
    display: none;
  }
  
  .header-nav {
    display: none;
  }
}
</style>
