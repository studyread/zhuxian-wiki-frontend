<template>
  <div class="log-manage">
      <div class="manage-header">
        <h2>操作日志</h2>
        <p class="log-tip">记录知识库的所有操作历史</p>
      </div>

      <div class="log-list">
        <div v-for="log in logs" :key="log.id" class="log-item">
          <div class="log-icon" :class="log.operation">
            {{ getOperationIcon(log.operation) }}
          </div>
          <div class="log-content">
            <div class="log-title">
              <span class="log-op">{{ getOperationName(log.operation) }}</span>
              <span v-if="log.title" class="log-title-text">{{ log.title }}</span>
            </div>
            <p v-if="log.description" class="log-desc">{{ log.description }}</p>
            <span class="log-time">{{ formatDate(log.createdAt) }}</span>
          </div>
        </div>

        <div v-if="logs.length === 0" class="empty-state">
          暂无操作记录
        </div>
      </div>

      <div v-if="total > pageSize" class="pagination">
        <button
          class="page-btn"
          :disabled="page <= 1"
          @click="page--; loadData()"
        >
          上一页
        </button>
        <span class="page-info">{{ page }} / {{ totalPages }}</span>
        <button
          class="page-btn"
          :disabled="page >= totalPages"
          @click="page++; loadData()"
        >
          下一页
        </button>
      </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

import { knowledgeLogApi } from '@/api/knowledge'

const logs = ref([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const getOperationIcon = (op) => {
  const icons = { ingest: '📥', query: '🔍', lint: '🧹' }
  return icons[op] || '📝'
}

const getOperationName = (op) => {
  const names = { ingest: '知识录入', query: '知识查询', lint: '知识整理' }
  return names[op] || '操作'
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

const loadData = async () => {
  const res = await knowledgeLogApi.list({ page: page.value, size: pageSize.value })
  if (res.code === 200) {
    logs.value = res.data
    total.value = res.total
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
  padding: 16px;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.manage-header {
  margin-bottom: 16px;
  flex-shrink: 0;
}

.manage-header h2 {
  font-size: 16px;
  font-family: var(--font-serif);
  color: var(--color-ink);
  margin-bottom: 6px;
}

.log-tip {
  font-size: 12px;
  color: var(--color-ink-muted);
}

.log-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  overflow-y: auto;
  flex: 1;
  min-height: 0;
}

.log-item {
  display: flex;
  gap: 14px;
  padding: 14px;
  background: var(--color-paper);
  border-radius: var(--radius-md);
}

.log-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-white);
  border-radius: var(--radius-sm);
  font-size: 16px;
  flex-shrink: 0;
}

.log-icon.ingest {
  background: #dbeafe;
}

.log-icon.query {
  background: #fef3c7;
}

.log-icon.lint {
  background: #dcfce7;
}

.log-content {
  flex: 1;
  min-width: 0;
}

.log-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.log-op {
  padding: 2px 6px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 11px;
  color: var(--color-ink);
}

.log-title-text {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-ink);
}

.log-desc {
  font-size: 12px;
  color: var(--color-ink-muted);
  margin-bottom: 6px;
}

.log-time {
  font-size: 11px;
  color: var(--color-ink-muted);
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: var(--color-ink-muted);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
}

.page-btn {
  padding: 7px 14px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-white);
  cursor: pointer;
  font-size: 12px;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 12px;
  color: var(--color-ink-muted);
}
</style>
