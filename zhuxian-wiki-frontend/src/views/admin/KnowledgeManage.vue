<template>
  <div class="knowledge-manage">
      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <router-link to="/dashboard" class="btn-back">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
              <path d="M10 12L6 8l4-4" stroke="currentColor" stroke-width="1.5" fill="none"/>
            </svg>
            返回
          </router-link>
          <!-- 搜索框 -->
          <div class="search-box">
            <input
              v-model="filters.keyword"
              type="text"
              placeholder="搜索词条标题..."
              @keyup.enter="handleSearch"
              class="search-input"
            />
            <button @click="handleSearch" class="search-btn">搜索</button>
          </div>
          <select v-model="filters.categoryId" class="filter-select" @change="loadData">
            <option value="">全部分类</option>
            <option v-for="cat in categories" :key="cat.id" :value="cat.id">
              {{ cat.name }}
            </option>
          </select>
          <select v-model="filters.status" class="filter-select" @change="loadData">
            <option value="">全部状态</option>
            <option value="1">已发布</option>
            <option value="0">草稿</option>
          </select>
        </div>
        <div class="toolbar-right">
          <button class="btn-export" @click="handleExport">导出知识库</button>
          <router-link to="/knowledge/create" class="btn-primary">新增词条</router-link>
        </div>
      </div>

      <!-- 批量操作栏 -->
      <div v-if="selectedIds.length > 0" class="batch-toolbar">
        <span class="batch-tip">已选择 {{ selectedIds.length }} 项</span>
        <button @click="handleBatchDelete" class="btn-batch danger">批量删除</button>
        <button @click="handleBatchPublish" class="btn-batch">批量发布</button>
        <button @click="handleBatchDraft" class="btn-batch">批量设为草稿</button>
        <button @click="selectedIds = []" class="btn-batch cancel">取消选择</button>
      </div>

      <!-- 列表 -->
      <div class="table-container">
        <table class="data-table">
          <thead>
            <tr>
              <th class="col-checkbox">
                <input type="checkbox" v-model="selectAll" @change="handleSelectAll" />
              </th>
              <th>ID</th>
              <th>标题</th>
              <th>分类</th>
              <th>标签</th>
              <th>浏览</th>
              <th>点赞</th>
              <th>状态</th>
              <th>更新时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in list" :key="item.id" :class="{ selected: selectedIds.includes(item.id) }">
              <td class="col-checkbox">
                <input type="checkbox" :value="item.id" v-model="selectedIds" />
              </td>
              <td>{{ item.id }}</td>
              <td class="title-cell">
                <a :href="`/knowledge/${item.id}`" target="_blank">
                  {{ item.title }}
                </a>
              </td>
              <td>{{ getCategoryName(item.categoryId) }}</td>
              <td>
                <span v-for="tag in getTags(item.tags)" :key="tag" class="tag">
                  {{ tag }}
                </span>
              </td>
              <td>{{ item.viewCount || 0 }}</td>
              <td>{{ item.likeCount || 0 }}</td>
              <td>
                <span :class="['status', item.status === 1 ? 'published' : 'draft']">
                  {{ item.status === 1 ? '已发布' : '草稿' }}
                </span>
              </td>
              <td>{{ formatDate(item.updatedAt) }}</td>
              <td>
                <button class="btn-text" @click="handleEdit(item.id)">编辑</button>
                <button class="btn-text" @click="handleToggleStatus(item)">
                  {{ item.status === 1 ? '撤销' : '发布' }}
                </button>
                <button class="btn-text danger" @click="handleDelete(item.id)">删除</button>
              </td>
            </tr>
            <tr v-if="list.length === 0">
              <td colspan="10" class="empty-cell">暂无数据</td>
            </tr>
          </tbody>
        </table>

        <!-- 分页 -->
        <div class="pagination">
          <span class="total-count">共 {{ total }} 条</span>
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

      <!-- 导出弹窗 -->
      <div v-if="showExport" class="modal-overlay" @click.self="showExport = false">
        <div class="modal">
          <div class="modal-header">
            <h3>导出知识库</h3>
            <button @click="showExport = false" class="modal-close">×</button>
          </div>
          <div class="modal-body">
            <textarea
              v-model="exportContent"
              class="export-textarea"
              rows="15"
              readonly
            ></textarea>
          </div>
          <div class="modal-footer">
            <button @click="downloadMd" class="btn-primary">下载 Markdown</button>
            <button @click="showExport = false" class="btn-secondary">关闭</button>
          </div>
        </div>
      </div>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'

import { knowledgeEntryApi, knowledgeCategoryApi } from '@/api/knowledge'

const router = useRouter()

const list = ref([])
const categories = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const showExport = ref(false)
const exportContent = ref('')
const selectedIds = ref([])
const selectAll = ref(false)

const filters = reactive({
  categoryId: '',
  status: '',
  keyword: ''
})

const totalPages = computed(() => Math.ceil(total.value / pageSize.value) || 1)

const getCategoryName = (id) => {
  const cat = categories.value.find(c => c.id === id)
  return cat?.name || '-'
}

const getTags = (tags) => {
  if (!tags) return []
  return tags.split(',').filter(t => t.trim())
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

const loadData = async () => {
  const params = {
    page: page.value,
    size: pageSize.value
  }
  if (filters.categoryId) {
    params.categoryId = filters.categoryId
  }
  if (filters.status !== '') {
    params.status = parseInt(filters.status)
  }
  if (filters.keyword) {
    params.keyword = filters.keyword
  }

  try {
    const res = await knowledgeEntryApi.list(params)
    if (res.code === 200) {
      list.value = res.data || []
      total.value = res.total || 0
    }
  } catch (e) {
    console.error('加载数据失败', e)
  }
}

const loadCategories = async () => {
  try {
    const res = await knowledgeCategoryApi.list()
    if (res.code === 200) {
      categories.value = res.data || []
    }
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

const handleSearch = () => {
  page.value = 1
  loadData()
}

const handleEdit = (id) => {
  router.push(`/knowledge/edit/${id}`)
}

const handleDelete = async (id) => {
  if (!confirm('确定删除该词条？')) return
  try {
    await knowledgeEntryApi.delete(id)
    loadData()
  } catch (e) {
    alert('删除失败')
  }
}

const handleToggleStatus = async (item) => {
  const newStatus = item.status === 1 ? 0 : 1
  try {
    await knowledgeEntryApi.update(item.id, { status: newStatus })
    loadData()
  } catch (e) {
    alert('操作失败')
  }
}

// 批量选择
const handleSelectAll = () => {
  if (selectAll.value) {
    selectedIds.value = list.value.map(item => item.id)
  } else {
    selectedIds.value = []
  }
}

watch(selectedIds, () => {
  selectAll.value = list.value.length > 0 && selectedIds.value.length === list.value.length
})

// 批量删除
const handleBatchDelete = async () => {
  if (!confirm(`确定删除选中的 ${selectedIds.value.length} 个词条？`)) return
  try {
    await Promise.all(selectedIds.value.map(id => knowledgeEntryApi.delete(id)))
    selectedIds.value = []
    loadData()
  } catch (e) {
    alert('批量删除失败')
  }
}

// 批量发布
const handleBatchPublish = async () => {
  try {
    await Promise.all(selectedIds.value.map(id => knowledgeEntryApi.update(id, { status: 1 })))
    selectedIds.value = []
    loadData()
  } catch (e) {
    alert('批量发布失败')
  }
}

// 批量设为草稿
const handleBatchDraft = async () => {
  try {
    await Promise.all(selectedIds.value.map(id => knowledgeEntryApi.update(id, { status: 0 })))
    selectedIds.value = []
    loadData()
  } catch (e) {
    alert('批量操作失败')
  }
}

const handleExport = async () => {
  try {
    const res = await knowledgeEntryApi.export()
    if (res.code === 200) {
      exportContent.value = res.data
      showExport.value = true
    }
  } catch (e) {
    alert('导出失败')
  }
}

const downloadMd = () => {
  const blob = new Blob([exportContent.value], { type: 'text/markdown' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `诛仙世界知识库_${Date.now()}.md`
  a.click()
  URL.revokeObjectURL(url)
}

onMounted(() => {
  loadData()
  loadCategories()
})
</script>

<style scoped>
.knowledge-manage {
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

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 10px;
  flex-shrink: 0;
}

.toolbar-left {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

/* 搜索框 */
.search-box {
  display: flex;
  gap: 0;
}

.search-input {
  padding: 7px 10px;
  border: 1px solid var(--color-border);
  border-right: none;
  border-radius: var(--radius-sm) 0 0 var(--radius-sm);
  font-size: 12px;
  width: 180px;
  outline: none;
}

.search-input:focus {
  border-color: var(--color-cinnabar);
}

.search-btn {
  padding: 7px 12px;
  background: var(--color-cinnabar);
  color: white;
  border: 1px solid var(--color-cinnabar);
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
  font-size: 12px;
  cursor: pointer;
}

.filter-select {
  padding: 7px 10px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 12px;
  background: var(--color-white);
  cursor: pointer;
}

.filter-select:focus {
  outline: none;
  border-color: var(--color-cinnabar);
}

.toolbar-right {
  display: flex;
  gap: 10px;
}

/* 批量操作栏 */
.batch-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: #fef3c7;
  border-radius: var(--radius-sm);
  margin-bottom: 12px;
  flex-shrink: 0;
}

.batch-tip {
  font-size: 12px;
  color: #92400e;
  font-weight: 500;
}

.btn-batch {
  padding: 4px 10px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 11px;
  cursor: pointer;
  color: var(--color-ink);
}

.btn-batch.danger {
  background: #ef4444;
  color: white;
  border-color: #ef4444;
}

.btn-batch.danger:hover {
  background: #dc2626;
}

.btn-batch.cancel {
  color: var(--color-ink-muted);
}

.btn-primary {
  padding: 7px 14px;
  background: var(--color-cinnabar);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 12px;
  cursor: pointer;
  text-decoration: none;
}

.btn-export {
  padding: 7px 14px;
  background: var(--color-white);
  color: var(--color-ink);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 12px;
  cursor: pointer;
}

.table-container {
  flex: 1;
  overflow: auto;
  min-height: 0;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 10px 8px;
  text-align: left;
  border-bottom: 1px solid var(--color-border-light);
  font-size: 12px;
}

.data-table th {
  font-weight: 600;
  color: var(--color-ink);
  background: var(--color-paper);
  position: sticky;
  top: 0;
}

.col-checkbox {
  width: 40px;
  text-align: center;
}

.data-table tr.selected {
  background: #fef3c7;
}

.title-cell a {
  color: var(--color-ink);
  text-decoration: none;
}

.title-cell a:hover {
  color: var(--color-cinnabar);
}

.tag {
  display: inline-block;
  padding: 2px 6px;
  margin: 2px;
  background: var(--color-paper);
  border-radius: var(--radius-sm);
  font-size: 10px;
  color: var(--color-ink-muted);
}

.status {
  padding: 2px 6px;
  border-radius: var(--radius-sm);
  font-size: 11px;
}

.status.published {
  background: #dcfce7;
  color: #16a34a;
}

.status.draft {
  background: #fef3c7;
  color: #d97706;
}

.btn-text {
  padding: 3px 6px;
  background: none;
  border: none;
  color: var(--color-ink-muted);
  cursor: pointer;
  font-size: 12px;
}

.btn-text:hover {
  color: var(--color-cinnabar);
}

.btn-text.danger:hover {
  color: #dc2626;
}

.empty-cell {
  text-align: center;
  color: var(--color-ink-muted);
  padding: 30px !important;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
  flex-shrink: 0;
}

.total-count {
  font-size: 12px;
  color: var(--color-ink-muted);
}

.page-btn {
  padding: 6px 12px;
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

/* 弹窗 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  width: 700px;
  max-height: 80vh;
  background: var(--color-white);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.modal-header {
  padding: 14px 18px;
  border-bottom: 1px solid var(--color-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  font-size: 15px;
  font-weight: 600;
}

.modal-close {
  width: 28px;
  height: 28px;
  border: none;
  background: none;
  font-size: 22px;
  cursor: pointer;
  color: var(--color-ink-muted);
}

.modal-body {
  padding: 16px;
  max-height: 60vh;
  overflow-y: auto;
}

.export-textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 11px;
  font-family: monospace;
  resize: none;
}

.modal-footer {
  padding: 14px 18px;
  border-top: 1px solid var(--color-border);
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn-secondary {
  padding: 7px 14px;
  background: var(--color-white);
  color: var(--color-ink);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 12px;
  cursor: pointer;
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
