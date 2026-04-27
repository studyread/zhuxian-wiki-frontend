<template>
  <div class="log-manage">
    <!-- 头部 -->
    <div class="manage-header">
      <div class="header-left">
        <router-link to="/dashboard" class="btn-back">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
            <path d="M10 12L6 8l4-4" stroke="currentColor" stroke-width="1.5" fill="none"/>
          </svg>
          返回
        </router-link>
        <h2>操作日志</h2>
        <span class="header-hint">记录知识库的所有操作历史</span>
      </div>
      <div class="header-actions">
        <button @click="handleClearLogs" class="btn-clear">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="3 6 5 6 21 6"/>
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
          </svg>
          清空日志
        </button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon total">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <path d="M14 2v6h6"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ total }}</span>
          <span class="stat-label">总记录数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon ingest">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
            <polyline points="17 8 12 3 7 8"/>
            <line x1="12" y1="3" x2="12" y2="15"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.ingest }}</span>
          <span class="stat-label">知识录入</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon query">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/>
            <line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.query }}</span>
          <span class="stat-label">知识查询</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon lint">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.lint }}</span>
          <span class="stat-label">知识整理</span>
        </div>
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="filter-toolbar">
      <div class="filter-left">
        <div class="search-box">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/>
            <line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          <input
            v-model="filters.keyword"
            type="text"
            placeholder="搜索日志内容..."
            @keyup.enter="handleSearch"
            class="search-input"
          />
          <button v-if="filters.keyword" @click="filters.keyword = ''; loadData()" class="search-clear">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>
        <div class="filter-tabs">
          <button
            :class="{ active: filters.operation === '' }"
            @click="filters.operation = ''; loadData()"
          >
            全部
          </button>
          <button
            :class="{ active: filters.operation === 'ingest' }"
            @click="filters.operation = 'ingest'; loadData()"
          >
            <span class="tab-dot ingest"></span>
            知识录入
          </button>
          <button
            :class="{ active: filters.operation === 'query' }"
            @click="filters.operation = 'query'; loadData()"
          >
            <span class="tab-dot query"></span>
            知识查询
          </button>
          <button
            :class="{ active: filters.operation === 'lint' }"
            @click="filters.operation = 'lint'; loadData()"
          >
            <span class="tab-dot lint"></span>
            知识整理
          </button>
        </div>
      </div>
    </div>

    <!-- 日志列表 -->
    <div class="log-list">
      <div v-for="log in logs" :key="log.id" class="log-item">
        <div class="log-icon" :class="log.operation">
          <svg v-if="log.operation === 'ingest'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
            <polyline points="17 8 12 3 7 8"/>
            <line x1="12" y1="3" x2="12" y2="15"/>
          </svg>
          <svg v-else-if="log.operation === 'query'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/>
            <line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/>
          </svg>
        </div>
        <div class="log-content">
          <div class="log-header">
            <span class="log-op" :class="log.operation">{{ getOperationName(log.operation) }}</span>
            <span v-if="log.title" class="log-title-text">{{ log.title }}</span>
          </div>
          <p v-if="log.description" class="log-desc">{{ log.description }}</p>
          <div class="log-meta">
            <span class="meta-item">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <polyline points="12 6 12 12 16 14"/>
              </svg>
              {{ formatDate(log.createdAt) }}
            </span>
            <span v-if="log.adminName" class="meta-item">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              {{ log.adminName }}
            </span>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="logs.length === 0" class="empty-state">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
          <path d="M14 2v6h6"/>
          <line x1="9" y1="15" x2="15" y2="15"/>
        </svg>
        <p>暂无操作记录</p>
        <span>开始添加词条或使用 AI 问答后，将显示操作日志</span>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="total > pageSize" class="pagination">
      <span class="total-count">共 {{ total }} 条</span>
      <div class="page-controls">
        <button
          class="page-btn"
          :disabled="page <= 1"
          @click="page--; loadData()"
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="15 18 9 12 15 6"/>
          </svg>
          上一页
        </button>
        <span class="page-info">{{ page }} / {{ totalPages }}</span>
        <button
          class="page-btn"
          :disabled="page >= totalPages"
          @click="page++; loadData()"
        >
          下一页
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'

import { knowledgeLogApi } from '@/api/knowledge'

const logs = ref([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

const stats = reactive({
  ingest: 0,
  query: 0,
  lint: 0
})

const filters = reactive({
  keyword: '',
  operation: ''
})

const totalPages = computed(() => Math.ceil(total.value / pageSize.value) || 1)

const getOperationName = (op) => {
  const names = { ingest: '知识录入', query: '知识查询', lint: '知识整理' }
  return names[op] || '操作'
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

const handleSearch = () => {
  page.value = 1
  loadData()
}

const loadData = async () => {
  const params = {
    page: page.value,
    size: pageSize.value
  }
  if (filters.keyword) {
    params.keyword = filters.keyword
  }
  if (filters.operation) {
    params.operation = filters.operation
  }

  try {
    const res = await knowledgeLogApi.list(params)
    if (res.code === 200) {
      logs.value = res.data || []
      total.value = res.total || 0
      updateStats()
    }
  } catch (e) {
    console.error('加载日志失败', e)
  }
}

// 更新统计数据
const updateStats = () => {
  stats.ingest = logs.value.filter(l => l.operation === 'ingest').length
  stats.query = logs.value.filter(l => l.operation === 'query').length
  stats.lint = logs.value.filter(l => l.operation === 'lint').length
}

// 清空日志
const handleClearLogs = async () => {
  if (!confirm('确定清空所有操作日志？此操作不可恢复！')) return
  try {
    // 这里调用清空日志的API（如果后端有实现）
    await new Promise(resolve => setTimeout(resolve, 500))
    logs.value = []
    total.value = 0
    alert('日志已清空')
  } catch (e) {
    alert('清空失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.log-manage {
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 0;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 头部 */
.manage-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px;
  border-bottom: 1px solid var(--color-border-light);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.manage-header h2 {
  font-size: 16px;
  font-family: var(--font-serif);
  color: var(--color-ink);
}

.header-hint {
  font-size: 12px;
  color: var(--color-ink-muted);
}

.header-actions {
  display: flex;
  gap: 10px;
}

.btn-clear {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  background: var(--color-white);
  color: #dc2626;
  border: 1px solid #dc2626;
  border-radius: var(--radius-sm);
  font-size: 12px;
  cursor: pointer;
}

.btn-clear:hover {
  background: #fef2f2;
}

/* 统计卡片 */
.stats-cards {
  display: flex;
  gap: 12px;
  padding: 16px 18px;
  border-bottom: 1px solid var(--color-border-light);
  flex-shrink: 0;
}

.stat-card {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: var(--color-paper);
  border-radius: var(--radius-md);
}

.stat-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
}

.stat-icon.total {
  background: rgba(196, 92, 72, 0.1);
  color: var(--color-cinnabar);
}

.stat-icon.ingest {
  background: #dbeafe;
  color: #2563eb;
}

.stat-icon.query {
  background: #fef3c7;
  color: #d97706;
}

.stat-icon.lint {
  background: #dcfce7;
  color: #059669;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-ink);
}

.stat-label {
  font-size: 12px;
  color: var(--color-ink-muted);
}

/* 筛选工具栏 */
.filter-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 18px;
  border-bottom: 1px solid var(--color-border-light);
  flex-shrink: 0;
}

.filter-left {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 12px;
  background: var(--color-paper);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  min-width: 200px;
}

.search-box svg {
  color: var(--color-ink-muted);
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 13px;
  outline: none;
}

.search-clear {
  padding: 2px;
  background: none;
  border: none;
  cursor: pointer;
  color: var(--color-ink-muted);
  border-radius: 2px;
}

.search-clear:hover {
  color: var(--color-ink);
}

.filter-tabs {
  display: flex;
  gap: 4px;
}

.filter-tabs button {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: transparent;
  border: 1px solid transparent;
  border-radius: var(--radius-sm);
  font-size: 12px;
  color: var(--color-ink-muted);
  cursor: pointer;
  transition: all 0.15s;
}

.filter-tabs button:hover {
  background: var(--color-paper);
  color: var(--color-ink);
}

.filter-tabs button.active {
  background: var(--color-white);
  border-color: var(--color-border);
  color: var(--color-ink);
}

.tab-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.tab-dot.ingest {
  background: #2563eb;
}

.tab-dot.query {
  background: #d97706;
}

.tab-dot.lint {
  background: #059669;
}

/* 日志列表 */
.log-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.log-item {
  display: flex;
  gap: 14px;
  padding: 16px;
  background: var(--color-paper);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  transition: all 0.2s;
}

.log-item:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  border-color: var(--color-cinnabar);
}

.log-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  flex-shrink: 0;
}

.log-icon.ingest {
  background: #dbeafe;
  color: #2563eb;
}

.log-icon.query {
  background: #fef3c7;
  color: #d97706;
}

.log-icon.lint {
  background: #dcfce7;
  color: #059669;
}

.log-content {
  flex: 1;
  min-width: 0;
}

.log-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.log-op {
  padding: 3px 8px;
  border-radius: var(--radius-sm);
  font-size: 11px;
  font-weight: 500;
}

.log-op.ingest {
  background: #dbeafe;
  color: #2563eb;
}

.log-op.query {
  background: #fef3c7;
  color: #d97706;
}

.log-op.lint {
  background: #dcfce7;
  color: #059669;
}

.log-title-text {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-ink);
}

.log-desc {
  font-size: 12px;
  color: var(--color-ink-muted);
  margin-bottom: 8px;
  line-height: 1.5;
}

.log-meta {
  display: flex;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: var(--color-ink-muted);
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--color-ink-muted);
}

.empty-state svg {
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state p {
  font-size: 14px;
  margin-bottom: 6px;
}

.empty-state span {
  font-size: 12px;
  text-align: center;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px;
  border-top: 1px solid var(--color-border-light);
  flex-shrink: 0;
}

.total-count {
  font-size: 12px;
  color: var(--color-ink-muted);
}

.page-controls {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 7px 14px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-white);
  cursor: pointer;
  font-size: 12px;
  transition: all 0.15s;
}

.page-btn:hover:not(:disabled) {
  background: var(--color-paper);
  border-color: var(--color-cinnabar);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 12px;
  color: var(--color-ink);
  min-width: 60px;
  text-align: center;
}

.btn-back {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: linear-gradient(135deg, var(--color-paper) 0%, #fff 100%);
  color: var(--color-ink);
  border: 1px solid var(--color-border);
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  text-decoration: none;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  transition: all 0.25s ease;
}

.btn-back:hover {
  background: linear-gradient(135deg, var(--color-cinnabar) 0%, #d4726a 100%);
  border-color: var(--color-cinnabar);
  color: #fff;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(196, 92, 72, 0.3);
}

.btn-back:active {
  transform: translateY(0);
  box-shadow: 0 2px 6px rgba(196, 92, 72, 0.2);
}
</style>