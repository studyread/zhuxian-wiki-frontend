<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
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
      <header class="admin-header">
        <div class="header-left">
          <h1>{{ currentTitle }}</h1>
          <span class="header-time">{{ currentTime }}</span>
        </div>
        <div class="header-right">
          <a href="/" class="home-link">
            <span v-html="homeIcon"></span>
            返回首页
          </a>
        </div>
      </header>

      <div class="admin-content">
        <slot />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const props = defineProps({
  title: {
    type: String,
    default: '仪表盘'
  }
})

const route = useRoute()
const router = useRouter()

const adminInfo = ref(null)
const currentTime = ref('')

const knowledgeMenu = [
  { name: '知识库管理', icon: '📚', path: '/knowledge' },
  { name: '知识分类', icon: '📁', path: '/category' },
  { name: '知识录入', icon: '✏️', path: '/knowledge/create' },
  { name: '操作日志', icon: '📋', path: '/logs' }
]

const systemMenu = [
  { name: '仪表盘', icon: '📊', path: '/dashboard' },
  { name: '系统设置', icon: '⚙️', path: '/settings' }
]

const homeIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>'
const logoutIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>'

const currentTitle = computed(() => {
  if (props.title !== '仪表盘') return props.title
  
  const exactMatch = [...knowledgeMenu, ...systemMenu].find(m => route.path === m.path)
  if (exactMatch) return exactMatch.name

  if (route.path.startsWith('/knowledge/edit')) {
    return '知识库管理'
  }

  const prefixMatch = [...knowledgeMenu, ...systemMenu].find(m => route.path.startsWith(m.path) && m.path !== '/')
  return prefixMatch?.name || '仪表盘'
})

const isActive = (path) => {
  if (path === '/dashboard') {
    return route.path === path
  }
  return route.path.startsWith(path)
}

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

let timeInterval = null

const handleLogout = () => {
  localStorage.removeItem('admin_token')
  localStorage.removeItem('admin_info')
  localStorage.removeItem('admin_id')
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
  if (timeInterval) {
    clearInterval(timeInterval)
  }
})
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: #f5f6f8;
}

/* 侧边栏 */
.admin-sidebar {
  width: 220px;
  min-width: 220px;
  background: #fff;
  color: #333;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #e8e8e8;
}

.sidebar-header {
  padding: 16px 14px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid #f0e6d3;
}

.sidebar-logo {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  object-fit: contain;
  background: transparent;
  padding: 0;
}

.sidebar-title {
  display: flex;
  flex-direction: column;
}

.title-main {
  font-size: 14px;
  font-weight: 600;
  font-family: var(--font-serif);
  letter-spacing: 1px;
}

.title-sub {
  font-size: 10px;
  color: #999;
  margin-top: 2px;
}

.sidebar-nav {
  flex: 1;
  padding: 14px 8px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.nav-section {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-label {
  font-size: 10px;
  text-transform: uppercase;
  letter-spacing: 1.2px;
  color: #999;
  padding: 0 10px;
  margin-bottom: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 10px;
  color: #666;
  text-decoration: none;
  border-radius: 6px;
  transition: all 0.2s ease;
  font-size: 13px;
}

.nav-item:hover {
  background: #faf8f3;
  color: #c45c48;
}

.nav-item.active {
  background: linear-gradient(135deg, #c45c48, #e07b6d);
  color: #fff;
}

.nav-icon {
  font-size: 15px;
  width: 16px;
  text-align: center;
}

.nav-icon :deep(svg) {
  width: 14px;
  height: 14px;
}

.sidebar-footer {
  padding: 12px;
  border-top: 1px solid #f0e6d3;
}

.admin-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background: #faf8f3;
  border-radius: 6px;
  margin-bottom: 8px;
}

.admin-avatar {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  overflow: hidden;
  background: transparent;
}

.admin-avatar img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.admin-detail {
  display: flex;
  flex-direction: column;
}

.admin-name {
  font-size: 12px;
  font-weight: 500;
}

.admin-role {
  font-size: 10px;
  color: #999;
  margin-top: 2px;
}

.logout-btn {
  width: 100%;
  padding: 9px;
  background: #fff;
  border: 1px solid #e8e8e8;
  color: #666;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: #fee;
  border-color: #c45c48;
  color: #c45c48;
}

/* 主内容区 */
.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow-x: hidden;
}

.admin-header {
  padding: 14px 20px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.admin-header h1 {
  font-size: 16px;
  font-family: var(--font-serif);
  color: #c45c48;
  margin: 0;
}

.header-time {
  font-size: 11px;
  color: #999;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.home-link {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 7px 12px;
  background: #faf8f3;
  color: #666;
  text-decoration: none;
  border-radius: 6px;
  font-size: 12px;
  transition: all 0.2s;
}

.home-link:hover {
  background: #c45c48;
  color: #fff;
}

.home-link :deep(svg) {
  width: 14px;
  height: 14px;
}

.admin-content {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  overflow-x: hidden;
}
</style>
