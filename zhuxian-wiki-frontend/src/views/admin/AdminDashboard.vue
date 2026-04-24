<template>
  <AdminPage>
    <div class="dashboard">
      <!-- 欢迎卡片 -->
      <div class="welcome-card">
        <div class="welcome-content">
          <h2>欢迎回来，{{ adminInfo?.nickname || '管理员' }}</h2>
          <p>今天是美好的一天，让我们开始管理工作吧</p>
        </div>
        <div class="welcome-icon">
          <img src="/big_logo.webp" alt="Logo" />
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-grid">
        <div class="stat-card" v-for="stat in stats" :key="stat.title">
          <div class="stat-icon" :style="{ background: stat.bgColor }">
            <span v-html="stat.icon"></span>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ stat.value }}</span>
            <span class="stat-title">{{ stat.title }}</span>
          </div>
          <div class="stat-trend" :class="stat.trend > 0 ? 'up' : 'down'">
            <span v-html="stat.trend > 0 ? arrowUpIcon : arrowDownIcon"></span>
            {{ Math.abs(stat.trend) }}%
          </div>
        </div>
      </div>

      <!-- 快捷操作 -->
      <div class="quick-actions">
        <h3>快捷操作</h3>
        <div class="action-grid">
          <router-link to="/knowledge/create" class="action-card">
            <span class="action-icon" v-html="addIcon"></span>
            <span class="action-text">添加词条</span>
          </router-link>
          <router-link to="/category" class="action-card">
            <span class="action-icon" v-html="folderIcon"></span>
            <span class="action-text">管理分类</span>
          </router-link>
          <router-link to="/knowledge" class="action-card">
            <span class="action-icon" v-html="listIcon"></span>
            <span class="action-text">查看词条</span>
          </router-link>
          <a href="/" class="action-card">
            <span class="action-icon" v-html="homeIcon"></span>
            <span class="action-text">访问前台</span>
          </a>
        </div>
      </div>

      <!-- 最近操作 -->
      <div class="recent-section">
        <h3>最近操作</h3>
        <div class="recent-list">
          <div v-if="recentLogs.length === 0" class="empty-state">
            暂无操作记录
          </div>
          <div v-else v-for="log in recentLogs" :key="log.id" class="log-item">
            <div class="log-icon" :class="log.operation">
              <span v-html="getLogIcon(log.operation)"></span>
            </div>
            <div class="log-content">
              <span class="log-title">{{ log.title || '系统操作' }}</span>
              <span class="log-desc">{{ log.description }}</span>
            </div>
            <span class="log-time">{{ formatTime(log.createdAt) }}</span>
          </div>
        </div>
      </div>
    </div>
  </AdminPage>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AdminPage from '@/components/AdminPage.vue'

const adminInfo = ref(null)

const stats = ref([
  { title: '知识词条', value: '156', icon: '📚', bgColor: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', trend: 12 },
  { title: '知识分类', value: '8', icon: '📁', bgColor: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', trend: 0 },
  { title: '总浏览量', value: '12.8k', icon: '👁', bgColor: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', trend: 8 },
  { title: '操作日志', value: '42', icon: '📋', bgColor: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)', trend: 5 }
])

const recentLogs = ref([])

const arrowUpIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="18 15 12 9 6 15"/></svg>'
const arrowDownIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>'
const addIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>'
const folderIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>'
const listIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg>'
const homeIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>'
const editIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>'
const deleteIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>'
const addLogIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>'

const getLogIcon = (operation) => {
  switch(operation) {
    case 'ingest': return addLogIcon
    case 'edit': return editIcon
    case 'delete': return deleteIcon
    default: return listIcon
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return date.toLocaleDateString('zh-CN')
}

onMounted(() => {
  const stored = localStorage.getItem('admin_info')
  if (stored) {
    adminInfo.value = JSON.parse(stored)
  }

  recentLogs.value = [
    { id: 1, operation: 'ingest', title: '青云门职业指南', description: '新增知识词条', createdAt: new Date(Date.now() - 3600000) },
    { id: 2, operation: 'edit', title: '合欢派技能详解', description: '更新词条内容', createdAt: new Date(Date.now() - 7200000) },
    { id: 3, operation: 'ingest', title: '装备强化攻略', description: '新增知识词条', createdAt: new Date(Date.now() - 86400000) }
  ]
})
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 14px;
  height: 100%;
  box-sizing: border-box;
}

/* 欢迎卡片 */
.welcome-card {
  background: linear-gradient(135deg, #c45c48 0%, #e07b6d 100%);
  border-radius: 8px;
  padding: 14px 18px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #fff;
}

.welcome-content h2 {
  font-size: 15px;
  font-family: var(--font-serif);
  margin: 0 0 3px 0;
}

.welcome-content p {
  margin: 0;
  opacity: 0.85;
  font-size: 11px;
}

.welcome-icon {
  width: 42px;
  height: 42px;
  background: rgba(255,255,255,0.2);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.welcome-icon img {
  width: 32px;
  height: 32px;
  object-fit: contain;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  border: 1px solid #f0e6d3;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 3px 8px rgba(196, 92, 72, 0.1);
  border-color: #c45c48;
}

.stat-icon {
  width: 36px;
  height: 36px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1px;
  min-width: 0;
}

.stat-value {
  font-size: 16px;
  font-weight: 700;
  color: #1a1a2e;
}

.stat-title {
  font-size: 10px;
  color: #888;
  white-space: nowrap;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 1px;
  font-size: 9px;
  font-weight: 500;
  padding: 2px 5px;
  border-radius: 20px;
  flex-shrink: 0;
}

.stat-trend.up {
  background: #e6f7ed;
  color: #10b981;
}

.stat-trend.down {
  background: #fee2e2;
  color: #ef4444;
}

.stat-trend :deep(svg) {
  width: 9px;
  height: 9px;
}

/* 快捷操作 */
.quick-actions h3,
.recent-section h3 {
  font-size: 13px;
  color: #1a1a2e;
  margin: 0 0 8px 0;
  font-weight: 600;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.action-card {
  background: #fff;
  border-radius: 6px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  text-decoration: none;
  color: #666;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  border: 1px solid #f0e6d3;
  transition: transform 0.15s ease, box-shadow 0.15s ease, border-color 0.15s ease;
}

.action-card:hover {
  border-color: #c45c48;
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(196, 92, 72, 0.12);
}

.action-icon {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #fef3f2, #fee2e0);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c45c48;
}

.action-icon :deep(svg) {
  width: 16px;
  height: 16px;
}

.action-text {
  font-size: 11px;
  font-weight: 500;
}

/* 最近操作 */
.recent-section {
  background: #fff;
  border-radius: 8px;
  padding: 14px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  border: 1px solid #f0e6d3;
}

.recent-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.empty-state {
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 12px;
}

.log-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background: #faf8f3;
  border-radius: 5px;
  transition: background 0.15s ease;
}

.log-item:hover {
  background: #f0ece4;
}

.log-icon {
  width: 24px;
  height: 24px;
  border-radius: 5px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.log-icon.ingest {
  background: #e6f7ed;
  color: #10b981;
}

.log-icon.edit {
  background: #e0e7ff;
  color: #6366f1;
}

.log-icon.delete {
  background: #fee2e2;
  color: #ef4444;
}

.log-icon :deep(svg) {
  width: 11px;
  height: 11px;
}

.log-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1px;
  min-width: 0;
}

.log-title {
  font-size: 11px;
  font-weight: 500;
  color: #1a1a2e;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.log-desc {
  font-size: 9px;
  color: #888;
}

.log-time {
  font-size: 9px;
  color: #999;
  flex-shrink: 0;
}
</style>
