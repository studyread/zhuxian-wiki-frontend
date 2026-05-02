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
        <router-link 
          v-for="cat in navCategories" 
          :key="cat.id" 
          :to="`/category/${cat.name}`" 
          class="nav-link"
        >
          {{ cat.name }}
        </router-link>
        <router-link to="/ai" class="nav-link nav-link-ai">AI 助手</router-link>
      </nav>

      <!-- 写攻略按钮（仅管理员可见） -->
      <router-link
        v-if="isAdmin"
        to="/article/edit"
        class="write-btn"
        title="发布攻略"
      >
        <span>写攻略</span>
      </router-link>

      <!-- 用户区域 -->
      <div class="header-user">
        <button class="user-btn" @click="goToAdmin" title="后台管理">
          <span class="user-icon">管</span>
        </button>
      </div>

      <!-- 移动端汉堡菜单按钮 -->
      <button class="mobile-menu-btn" @click="toggleMobileMenu" :class="{ active: showMobileMenu }">
        <span class="hamburger-line"></span>
        <span class="hamburger-line"></span>
        <span class="hamburger-line"></span>
      </button>
    </div>

    <!-- 移动端侧滑导航面板 -->
    <transition name="slide">
      <div v-if="showMobileMenu" class="mobile-nav-overlay" @click="closeMobileMenu">
        <div class="mobile-nav-panel" @click.stop>
          <div class="mobile-nav-header">
            <span class="mobile-nav-title">导航菜单</span>
            <button class="mobile-nav-close" @click="closeMobileMenu">×</button>
          </div>
          
          <!-- 移动端搜索 -->
          <div class="mobile-search">
            <input
              type="text"
              v-model="searchQuery"
              @keyup.enter="handleMobileSearch"
              placeholder="搜索词条..."
              class="mobile-search-input"
            />
            <button @click="handleMobileSearch" class="mobile-search-btn">搜索</button>
          </div>

          <!-- 移动端导航链接 -->
          <nav class="mobile-nav-list">
            <router-link to="/" class="mobile-nav-link" @click="closeMobileMenu">
              <span class="nav-icon">🏠</span>
              <span>首页</span>
            </router-link>
            <router-link 
              v-for="cat in navCategories" 
              :key="cat.id" 
              :to="`/category/${cat.name}`" 
              class="mobile-nav-link"
              @click="closeMobileMenu"
            >
              <span class="nav-icon">📁</span>
              <span>{{ cat.name }}</span>
            </router-link>
            <router-link to="/ai" class="mobile-nav-link mobile-nav-ai" @click="closeMobileMenu">
              <span class="nav-icon">🤖</span>
              <span>AI 助手</span>
            </router-link>
          </nav>

          <!-- 管理员功能 -->
          <div v-if="isAdmin" class="mobile-admin-section">
            <router-link to="/article/edit" class="mobile-write-btn" @click="closeMobileMenu">
              <span>✏️</span>
              <span>写攻略</span>
            </router-link>
            <button class="mobile-admin-btn" @click="goToAdmin">
              <span>⚙️</span>
              <span>后台管理</span>
            </button>
          </div>
        </div>
      </div>
    </transition>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { categoryApi } from '@/api/article'

const router = useRouter()
const searchQuery = ref('')
const navCategories = ref([])
const isAdmin = computed(() => !!localStorage.getItem('admin_token'))
const showMobileMenu = ref(false)

// 加载导航分类
const loadCategories = async () => {
  try {
    const res = await categoryApi.getAll()
    if (res.code === 200) {
      navCategories.value = (res.data || []).slice(0, 6)
    }
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({ path: '/search', query: { keyword: searchQuery.value.trim() } })
  }
}

// 移动端搜索
const handleMobileSearch = () => {
  if (searchQuery.value.trim()) {
    closeMobileMenu()
    router.push({ path: '/search', query: { keyword: searchQuery.value.trim() } })
  }
}

// 切换移动端菜单
const toggleMobileMenu = () => {
  showMobileMenu.value = !showMobileMenu.value
  if (showMobileMenu.value) {
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
  }
}

// 关闭移动端菜单
const closeMobileMenu = () => {
  showMobileMenu.value = false
  document.body.style.overflow = ''
}

// 写攻略按钮点击处理
const goToAdmin = () => {
  closeMobileMenu()
  window.location.href = '/admin.html'
}

// 点击外部关闭
const handleClickOutside = (e) => {
  if (showMobileMenu.value && !e.target.closest('.mobile-nav-panel') && !e.target.closest('.mobile-menu-btn')) {
    closeMobileMenu()
  }
}

// 路由变化时关闭菜单
const handleRouteChange = () => {
  closeMobileMenu()
}

onMounted(() => {
  loadCategories()
  document.addEventListener('click', handleClickOutside)
  router.afterEach(handleRouteChange)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  document.body.style.overflow = ''
})
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
}

.search-input:focus {
  outline: none;
  border-color: var(--color-ochre);
  background: var(--color-white);
}

.search-input::placeholder {
  color: var(--color-ink-muted);
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
}

.search-btn:hover {
  background: var(--color-ink-light);
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
}

.nav-link:hover {
  color: var(--color-ink);
  background: var(--color-paper-dark);
  text-decoration: none;
}

.nav-link.router-link-active {
  color: var(--color-cinnabar);
  font-weight: 500;
}

.nav-link.nav-link-ai {
  color: var(--color-cinnabar);
  border: 1px solid var(--color-cinnabar);
}

.nav-link.nav-link-ai:hover {
  background: var(--color-cinnabar);
  color: var(--color-white);
}

/* 写攻略按钮 */
.write-btn {
  display: inline-flex;
  align-items: center;
  padding: 6px 14px;
  font-size: 13px;
  font-weight: 500;
  color: #fff;
  background: var(--color-cinnabar);
  border: 1px solid var(--color-cinnabar);
  border-radius: var(--radius-sm);
  text-decoration: none;
  transition: all 0.2s;
  flex-shrink: 0;
}

.write-btn:hover:not(.disabled) {
  background: var(--color-cinnabar-light);
  border-color: var(--color-cinnabar-light);
}

.write-btn.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  pointer-events: none;
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
}

.user-btn:hover {
  background: var(--color-paper-dark);
  border-color: var(--color-border-dark);
}

.user-icon {
  font-size: 14px;
  font-family: var(--font-serif);
  color: var(--color-ink-light);
}

/* ========== 移动端汉堡菜单 ========== */
.mobile-menu-btn {
  display: none;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 40px;
  height: 40px;
  padding: 8px;
  background: transparent;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  cursor: pointer;
  gap: 4px;
  flex-shrink: 0;
}

.hamburger-line {
  display: block;
  width: 20px;
  height: 2px;
  background: var(--color-ink);
  border-radius: 1px;
  transition: all 0.3s;
}

.mobile-menu-btn.active .hamburger-line:nth-child(1) {
  transform: rotate(45deg) translate(4px, 4px);
}

.mobile-menu-btn.active .hamburger-line:nth-child(2) {
  opacity: 0;
}

.mobile-menu-btn.active .hamburger-line:nth-child(3) {
  transform: rotate(-45deg) translate(4px, -4px);
}

/* 移动端导航遮罩层 */
.mobile-nav-overlay {
  position: fixed;
  top: 56px;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 99;
}

/* 移动端导航面板 */
.mobile-nav-panel {
  position: absolute;
  top: 0;
  left: 0;
  width: 280px;
  max-width: 85%;
  height: 100%;
  background: var(--color-white);
  box-shadow: 4px 0 20px rgba(0, 0, 0, 0.15);
  overflow-y: auto;
}

.mobile-nav-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid var(--color-border);
}

.mobile-nav-title {
  font-size: 16px;
  font-weight: 600;
  font-family: var(--font-serif);
  color: var(--color-ink);
}

.mobile-nav-close {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  font-size: 24px;
  color: var(--color-ink-muted);
  cursor: pointer;
}

/* 移动端搜索 */
.mobile-search {
  display: flex;
  padding: 12px 16px;
  gap: 8px;
  border-bottom: 1px solid var(--color-border);
}

.mobile-search-input {
  flex: 1;
  padding: 10px 12px;
  font-size: 14px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-cream);
}

.mobile-search-input:focus {
  outline: none;
  border-color: var(--color-ochre);
}

.mobile-search-btn {
  padding: 10px 16px;
  font-size: 14px;
  background: var(--color-ink);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
}

/* 移动端导航列表 */
.mobile-nav-list {
  padding: 8px 0;
}

.mobile-nav-link {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  font-size: 15px;
  color: var(--color-ink);
  text-decoration: none;
  transition: background 0.2s;
}

.mobile-nav-link:hover,
.mobile-nav-link.router-link-active {
  background: var(--color-paper);
}

.mobile-nav-link.router-link-active {
  color: var(--color-cinnabar);
  border-left: 3px solid var(--color-cinnabar);
}

.mobile-nav-ai {
  color: var(--color-cinnabar);
  font-weight: 500;
}

.nav-icon {
  font-size: 18px;
}

/* 管理员功能区域 */
.mobile-admin-section {
  padding: 16px;
  border-top: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: auto;
}

.mobile-write-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  font-size: 15px;
  font-weight: 500;
  color: white;
  background: var(--color-cinnabar);
  border-radius: var(--radius-sm);
  text-decoration: none;
}

.mobile-admin-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  font-size: 15px;
  color: var(--color-ink);
  background: var(--color-paper);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  cursor: pointer;
}

/* 侧滑动画 */
.slide-enter-active,
.slide-leave-active {
  transition: opacity 0.3s;
}

.slide-enter-active .mobile-nav-panel,
.slide-leave-active .mobile-nav-panel {
  transition: transform 0.3s;
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
}

.slide-enter-from .mobile-nav-panel,
.slide-leave-to .mobile-nav-panel {
  transform: translateX(-100%);
}

/* ========== 响应式 ========== */
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
  
  .write-btn {
    display: none;
  }
  
  .mobile-menu-btn {
    display: flex;
  }
}
</style>
