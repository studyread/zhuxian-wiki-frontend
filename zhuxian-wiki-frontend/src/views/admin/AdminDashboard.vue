<template>
  <div class="dashboard">
    <!-- 欢迎卡片 -->
    <div class="welcome-card">
      <div class="welcome-content">
        <h2>欢迎回来，{{ adminInfo?.nickname || '管理员' }}</h2>
        <p>今天是美好的一天，让我们开始管理工作吧</p>
      </div>
      <div class="welcome-actions">
        <button @click="handleLogout" class="logout-btn">
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
          退出登录
        </button>
        <div class="welcome-icon">
          <img src="/big_logo.webp" alt="Logo" />
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card" v-for="stat in stats" :key="stat.title">
        <div class="stat-icon-wrap" :style="{ background: stat.bgColor }">
          <span class="stat-icon-inner" v-html="stat.icon"></span>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stat.value }}</span>
          <span class="stat-title">{{ stat.title }}</span>
        </div>
        <div class="stat-trend" :class="stat.trend > 0 ? 'up' : stat.trend < 0 ? 'down' : 'neutral'">
          <span v-html="stat.trend > 0 ? arrowUpIcon : arrowDownIcon"></span>
          {{ stat.trend === 0 ? '持平' : Math.abs(stat.trend) + '%' }}
        </div>
      </div>
    </div>

    <!-- 图表与快捷操作 -->
    <div class="main-grid">
      <!-- 快捷操作 -->
      <div class="quick-actions">
        <h3 class="section-title">快捷操作</h3>
        <div class="action-grid">
          <router-link to="/knowledge/create" class="action-card">
            <div class="action-icon-wrap" style="background: rgba(196,92,72,0.15);">
              <span v-html="addIcon"></span>
            </div>
            <span class="action-text">添加词条</span>
          </router-link>
          <router-link to="/category" class="action-card">
            <div class="action-icon-wrap" style="background: rgba(99,102,241,0.15);">
              <span v-html="folderIcon"></span>
            </div>
            <span class="action-text">管理分类</span>
          </router-link>
          <router-link to="/knowledge" class="action-card">
            <div class="action-icon-wrap" style="background: rgba(16,185,129,0.15);">
              <span v-html="listIcon"></span>
            </div>
            <span class="action-text">查看词条</span>
          </router-link>
          <a href="/" class="action-card">
            <div class="action-icon-wrap" style="background: rgba(245,158,11,0.15);">
              <span v-html="homeIcon"></span>
            </div>
            <span class="action-text">访问前台</span>
          </a>
        </div>
      </div>

      <!-- 词条分类分布 -->
      <div class="chart-card">
        <h3 class="section-title">词条分类分布</h3>
        <div class="chart-placeholder">
          <div class="chart-bars">
            <div class="bar-item" v-for="item in categoryData" :key="item.name">
              <div class="bar-track">
                <div class="bar-fill" :style="{ width: item.percent + '%', background: item.color }"></div>
              </div>
              <span class="bar-label">{{ item.name }}</span>
              <span class="bar-value">{{ item.count }}篇</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 最近操作记录 -->
    <div class="recent-section">
      <h3 class="section-title">最近操作</h3>
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
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { knowledgeEntryApi, knowledgeCategoryApi, knowledgeLogApi } from '@/api/knowledge'

const adminInfo = ref(null)

const handleLogout = () => {
  localStorage.removeItem('admin_token')
  localStorage.removeItem('admin_info')
  localStorage.removeItem('admin_id')
  window.location.href = '/admin.html#/login'
}

// 统计数据
const stats = ref([
  { title: '知识词条', value: '0', icon: '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg>', bgColor: 'linear-gradient(135deg, #667eea, #764ba2)', trend: 0, loading: true },
  { title: '知识分类', value: '0', icon: '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>', bgColor: 'linear-gradient(135deg, #f093fb, #f5576c)', trend: 0, loading: true },
  { title: '总浏览量', value: '0', icon: '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>', bgColor: 'linear-gradient(135deg, #4facfe, #00f2fe)', trend: 0, loading: true },
  { title: '操作日志', value: '0', icon: '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>', bgColor: 'linear-gradient(135deg, #43e97b, #38f9d7)', trend: 0, loading: true }
])

const categoryData = ref([])
const recentLogs = ref([])

const arrowUpIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="18 15 12 9 6 15"/></svg>'
const arrowDownIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="6 9 12 15 18 9"/></svg>'
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

// 格式化数字
const formatNumber = (num) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

// 加载统计数据
const loadStats = async () => {
  try {
    // 加载词条数量
    const entryRes = await knowledgeEntryApi.list({ page: 1, size: 1 })
    if (entryRes.code === 200) {
      stats.value[0].value = formatNumber(entryRes.total || 0)
      stats.value[0].loading = false
    }

    // 加载分类数量
    const catRes = await knowledgeCategoryApi.list()
    if (catRes.code === 200) {
      stats.value[1].value = (catRes.data?.length || 0).toString()
      stats.value[1].loading = false
    }

    // 加载日志统计
    const logRes = await knowledgeLogApi.getStats()
    if (logRes.code === 200 && logRes.data) {
      stats.value[2].value = formatNumber(logRes.data.totalViews || 0)
      stats.value[3].value = formatNumber(logRes.data.totalLogs || 0)
      stats.value[2].loading = false
      stats.value[3].loading = false
    }
  } catch (e) {
    console.error('加载统计数据失败', e)
  }
}

// 加载分类分布
const loadCategoryDistribution = async () => {
  try {
    const [catRes, entryRes] = await Promise.all([
      knowledgeCategoryApi.list(),
      knowledgeEntryApi.list({ page: 1, size: 1000 })
    ])

    if (catRes.code === 200 && entryRes.code === 200) {
      const categories = catRes.data || []
      const entries = entryRes.data || []
      const total = entries.length

      const colors = ['#667eea', '#f093fb', '#4facfe', '#43e97b', '#f5576c', '#f59e0b', '#38f9d7', '#667eea']
      categoryData.value = categories.slice(0, 6).map((cat, i) => {
        const count = entries.filter(e => e.categoryId === cat.id).length
        const percent = total > 0 ? Math.round((count / total) * 100) : 0
        return {
          name: cat.name,
          count,
          percent,
          color: colors[i % colors.length]
        }
      })
    }
  } catch (e) {
    console.error('加载分类分布失败', e)
  }
}

// 加载最近操作
const loadRecentLogs = async () => {
  try {
    const res = await knowledgeLogApi.list({ page: 1, size: 5 })
    if (res.code === 200) {
      recentLogs.value = res.data || []
    }
  } catch (e) {
    console.error('加载最近操作失败', e)
  }
}

onMounted(async () => {
  const stored = localStorage.getItem('admin_info')
  if (stored) {
    adminInfo.value = JSON.parse(stored)
  }

  // 并行加载所有数据
  await Promise.all([
    loadStats(),
    loadCategoryDistribution(),
    loadRecentLogs()
  ])
})
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 欢迎卡片 */
.welcome-card {
  background: linear-gradient(135deg, #c45c48 0%, #e07b6d 100%);
  border-radius: 12px;
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #fff;
}

.welcome-content h2 {
  font-size: 16px;
  font-family: var(--font-serif);
  margin: 0 0 4px 0;
}

.welcome-content p {
  margin: 0;
  opacity: 0.8;
  font-size: 12px;
}

.welcome-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: rgba(255,255,255,0.15);
  border: 1px solid rgba(255,255,255,0.25);
  color: #fff;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: rgba(255,255,255,0.25);
  border-color: rgba(255,255,255,0.4);
}

.welcome-icon {
  width: 44px;
  height: 44px;
  background: rgba(255,255,255,0.2);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
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
  gap: 12px;
}

.stat-card {
  background: #1e1e32;
  border-radius: 12px;
  padding: 14px;
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid rgba(255,255,255,0.06);
  transition: transform 0.2s ease, border-color 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  border-color: rgba(196, 92, 72, 0.3);
}

.stat-icon-wrap {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon-inner {
  color: #fff;
  display: flex;
  align-items: center;
}

.stat-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
}

.stat-title {
  font-size: 11px;
  color: rgba(255,255,255,0.4);
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 10px;
  font-weight: 600;
  padding: 3px 6px;
  border-radius: 20px;
  flex-shrink: 0;
}

.stat-trend.up {
  background: rgba(16, 185, 129, 0.15);
  color: #10b981;
}

.stat-trend.down {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
}

.stat-trend.neutral {
  background: rgba(255,255,255,0.08);
  color: rgba(255,255,255,0.4);
}

.stat-trend :deep(svg) {
  width: 9px;
  height: 9px;
}

/* 主体网格 */
.main-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.section-title {
  font-size: 13px;
  color: rgba(255,255,255,0.8);
  margin: 0 0 12px 0;
  font-weight: 600;
}

/* 快捷操作 */
.quick-actions {
  background: #1e1e32;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid rgba(255,255,255,0.06);
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.action-card {
  background: rgba(255,255,255,0.04);
  border-radius: 10px;
  padding: 14px 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: rgba(255,255,255,0.7);
  border: 1px solid rgba(255,255,255,0.06);
  transition: all 0.2s ease;
}

.action-card:hover {
  background: rgba(196, 92, 72, 0.12);
  border-color: rgba(196, 92, 72, 0.3);
  color: #e07b6d;
  transform: translateY(-1px);
}

.action-icon-wrap {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255,255,255,0.8);
}

.action-card:hover .action-icon-wrap {
  color: #e07b6d;
}

.action-text {
  font-size: 12px;
  font-weight: 500;
}

/* 图表卡片 */
.chart-card {
  background: #1e1e32;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid rgba(255,255,255,0.06);
}

.chart-bars {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.bar-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.bar-track {
  flex: 1;
  height: 6px;
  background: rgba(255,255,255,0.06);
  border-radius: 3px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.6s ease;
}

.bar-label {
  font-size: 11px;
  color: rgba(255,255,255,0.5);
  width: 36px;
  text-align: right;
  flex-shrink: 0;
}

.bar-value {
  font-size: 11px;
  color: rgba(255,255,255,0.3);
  width: 36px;
  flex-shrink: 0;
}

/* 最近操作 */
.recent-section {
  background: #1e1e32;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid rgba(255,255,255,0.06);
}

.recent-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.empty-state {
  text-align: center;
  padding: 24px;
  color: rgba(255,255,255,0.25);
  font-size: 12px;
}

.log-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: rgba(255,255,255,0.03);
  border-radius: 8px;
  transition: background 0.15s ease;
}

.log-item:hover {
  background: rgba(255,255,255,0.06);
}

.log-icon {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.log-icon.ingest {
  background: rgba(16, 185, 129, 0.15);
  color: #10b981;
}

.log-icon.edit {
  background: rgba(99, 102, 241, 0.15);
  color: #6366f1;
}

.log-icon.delete {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
}

.log-icon :deep(svg) {
  width: 12px;
  height: 12px;
}

.log-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.log-title {
  font-size: 12px;
  font-weight: 500;
  color: rgba(255,255,255,0.85);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.log-desc {
  font-size: 10px;
  color: rgba(255,255,255,0.35);
}

.log-time {
  font-size: 10px;
  color: rgba(255,255,255,0.25);
  flex-shrink: 0;
}
</style>
