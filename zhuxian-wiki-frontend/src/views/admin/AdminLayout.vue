<template>
  <div class="admin-layout">
    <!-- 侧边栏 - 深色主题 -->
    <aside class="admin-sidebar">
      <div class="sidebar-header">
        <img src="/big_logo.webp" alt="Logo" class="sidebar-logo" />
        <div class="sidebar-title">
          <span class="title-main">诛仙Wiki</span>
          <span class="title-sub">管理后台</span>
        </div>
      </div>

      <nav class="sidebar-nav">
        <div class="nav-section">
          <span class="nav-label">知识库</span>
          <router-link
            v-for="item in knowledgeMenu"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            :class="{ active: isActive(item.path) }"
          >
            <span class="nav-icon" v-html="item.icon"></span>
            <span class="nav-text">{{ item.name }}</span>
          </router-link>
        </div>

        <div class="nav-section">
          <span class="nav-label">系统</span>
          <router-link
            v-for="item in systemMenu"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            :class="{ active: isActive(item.path) }"
          >
            <span class="nav-icon" v-html="item.icon"></span>
            <span class="nav-text">{{ item.name }}</span>
          </router-link>
        </div>
      </nav>

      <div class="sidebar-footer">
        <div class="admin-info">
          <div class="admin-avatar">
            <img src="/big_logo.webp" alt="Avatar" />
          </div>
          <div class="admin-detail">
            <span class="admin-name">{{ adminInfo?.nickname || '管理员' }}</span>
            <span class="admin-role">{{ adminInfo?.role === 'super_admin' ? '超级管理员' : '普通管理员' }}</span>
          </div>
        </div>
        <button @click="handleLogout" class="logout-btn">
          <span v-html="logoutIcon"></span>
          退出登录
        </button>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="admin-main">
      <!-- 顶部导航栏 -->
      <header class="admin-header">
        <div class="header-left">
          <h1 class="page-title">{{ currentTitle }}</h1>
          <span class="header-time">{{ currentTime }}</span>
        </div>
        <div class="header-right">
          <div class="search-box">
            <span class="search-icon" v-html="searchIcon"></span>
            <input type="text" placeholder="搜索词条..." v-model="searchKeyword" @keyup.enter="handleSearch" />
          </div>
          <a href="/" class="home-link">
            <span v-html="homeIcon"></span>
            返回首页
          </a>
          <div class="admin-avatar-mini">
            <img src="/big_logo.webp" alt="Avatar" />
          </div>
        </div>
      </header>

      <!-- 内容区域 -->
      <div class="admin-content">
        <div class="content-wrapper">
          <router-view />
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const adminInfo = ref(null)
const currentTime = ref('')
const searchKeyword = ref('')

const knowledgeMenu = [
  { name: '仪表盘', icon: '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>', path: 'dashboard' },
  { name: '知识库管理', icon: '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg>', path: 'knowledge' },
  { name: '知识分类', icon: '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>', path: 'category' },
  { name: '知识录入', icon: '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>', path: 'knowledge/create' },
  { name: '操作日志', icon: '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>', path: 'logs' }
]

const systemMenu = [
  { name: '系统设置', icon: '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>', path: 'settings' }
]

const homeIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>'
const logoutIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>'
const searchIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>'

const allMenu = computed(() => [...knowledgeMenu, ...systemMenu])

// hash 模式下 route.path 是 /#/dashboard，去掉 # 前缀得到路由段
const hashPath = computed(() => route.path.replace(/^#\//, '/') || '/')

const currentTitle = computed(() => {
  const path = hashPath.value
  const exactMatch = allMenu.value.find(m => '/' + m.path === path)
  if (exactMatch) return exactMatch.name
  // /knowledge/edit/123 → 知识库管理
  if (path.startsWith('/knowledge/edit')) return '知识库管理'
  const prefixMatch = allMenu.value.find(m => path === '/' + m.path || path.startsWith('/' + m.path + '/'))
  return prefixMatch?.name || '仪表盘'
})

const isActive = (path) => {
  const pathStr = '/' + path
  const current = hashPath.value
  if (path === 'dashboard') return current === '/dashboard' || current === '/'
  // 知识录入页（叶子节点）：精确匹配
  if (path === 'knowledge/create') return current === '/knowledge/create'
  // 知识库管理：匹配 /knowledge 本身 及 /knowledge/edit/... 编辑子路径
  if (path === 'knowledge') return current === '/knowledge' || current.startsWith('/knowledge/edit')
  // 其余菜单：路径完全相等，或匹配直接子路径
  return current === pathStr || current.startsWith(pathStr + '/')
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    window.location.href = `/?keyword=${encodeURIComponent(searchKeyword.value.trim())}`
  }
}

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'
  })
}

let timeInterval = null

const handleLogout = () => {
  localStorage.removeItem('admin_token')
  localStorage.removeItem('admin_info')
  localStorage.removeItem('admin_id')
  // hash 模式下跳转到登录页
  window.location.href = '/admin.html#/login'
}

onMounted(() => {
  const stored = localStorage.getItem('admin_info')
  if (stored) {
    adminInfo.value = JSON.parse(stored)
  }
  updateTime()
  timeInterval = setInterval(updateTime, 60000)
})

onUnmounted(() => {
  if (timeInterval) clearInterval(timeInterval)
})
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: #0f0f1a;
}

/* 侧边栏 - 深色主题 */
.admin-sidebar {
  width: 240px;
  min-width: 240px;
  background: #1a1a2e;
  color: #a0a0b0;
  display: flex;
  flex-direction: column;
  border-right: 1px solid rgba(255,255,255,0.05);
}

.sidebar-header {
  padding: 20px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}

.sidebar-logo {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  object-fit: contain;
  background: transparent;
  padding: 0;
}

.sidebar-title {
  display: flex;
  flex-direction: column;
}

.title-main {
  font-size: 15px;
  font-weight: 600;
  font-family: var(--font-serif);
  color: #fff;
  letter-spacing: 1px;
}

.title-sub {
  font-size: 10px;
  color: rgba(255,255,255,0.35);
  margin-top: 2px;
}

.sidebar-nav {
  flex: 1;
  padding: 12px 8px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.nav-section {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.nav-label {
  font-size: 10px;
  text-transform: uppercase;
  letter-spacing: 1.5px;
  color: rgba(255,255,255,0.25);
  padding: 0 12px;
  margin-bottom: 6px;
  font-weight: 500;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 12px;
  color: rgba(255,255,255,0.6);
  text-decoration: none;
  border-radius: 8px;
  transition: all 0.2s ease;
  font-size: 13px;
}

.nav-item:hover {
  background: rgba(255,255,255,0.06);
  color: #fff;
}

.nav-item.active {
  background: rgba(196, 92, 72, 0.25);
  color: #e07b6d;
}

.nav-item.active .nav-icon {
  color: #c45c48;
}

.nav-icon {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  opacity: 0.8;
}

.nav-icon :deep(svg) {
  width: 18px;
  height: 18px;
}

.sidebar-footer {
  padding: 14px;
  border-top: 1px solid rgba(255,255,255,0.06);
}

.admin-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  background: rgba(255,255,255,0.04);
  border-radius: 8px;
  margin-bottom: 8px;
}

.admin-avatar {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  overflow: hidden;
  background: transparent;
  flex-shrink: 0;
}

.admin-avatar img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.admin-detail {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.admin-name {
  font-size: 12px;
  font-weight: 500;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.admin-role {
  font-size: 10px;
  color: rgba(255,255,255,0.35);
  margin-top: 1px;
}

.logout-btn {
  width: 100%;
  padding: 10px 12px;
  background: rgba(239, 68, 68, 0.08);
  border: 1px solid rgba(239, 68, 68, 0.2);
  color: rgba(239, 68, 68, 0.8);
  border-radius: 8px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: rgba(239, 68, 68, 0.15);
  border-color: rgba(239, 68, 68, 0.4);
  color: #ef4444;
  transform: translateY(-1px);
}

/* 主内容区 */
.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
}

.admin-header {
  padding: 0 24px;
  background: #1a1a2e;
  border-bottom: 1px solid rgba(255,255,255,0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
  height: 56px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.page-title {
  font-size: 14px;
  font-family: var(--font-serif);
  color: #fff;
  margin: 0;
  font-weight: 600;
}

.header-time {
  font-size: 10px;
  color: rgba(255,255,255,0.3);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255,255,255,0.06);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 8px;
  padding: 6px 12px;
  width: 200px;
  transition: all 0.2s;
}

.search-box:focus-within {
  border-color: rgba(196, 92, 72, 0.4);
  background: rgba(255,255,255,0.08);
}

.search-icon {
  color: rgba(255,255,255,0.3);
  display: flex;
  align-items: center;
}

.search-box input {
  background: none;
  border: none;
  outline: none;
  color: rgba(255,255,255,0.8);
  font-size: 12px;
  width: 100%;
}

.search-box input::placeholder {
  color: rgba(255,255,255,0.25);
}

.home-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: rgba(255,255,255,0.05);
  color: rgba(255,255,255,0.5);
  text-decoration: none;
  border-radius: 8px;
  font-size: 12px;
  transition: all 0.2s;
}

.home-link:hover {
  background: rgba(196, 92, 72, 0.2);
  color: #e07b6d;
}

.admin-avatar-mini {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid rgba(255,255,255,0.1);
}

.admin-avatar-mini img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

/* 内容区域 */
.admin-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #0f0f1a;
}

.content-wrapper {
  min-height: 100%;
}
</style>
