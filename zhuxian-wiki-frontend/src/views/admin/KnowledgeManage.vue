<template>
  <AdminPage title="知识库管理">
    <div class="knowledge-manage">
      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <select v-model="filters.categoryId" class="filter-select">
            <option value="">全部分类</option>
            <option v-for="cat in categories" :key="cat.id" :value="cat.id">
              {{ cat.name }}
            </option>
          </select>
          <select v-model="filters.status" class="filter-select">
            <option value="1">已发布</option>
            <option value="0">草稿</option>
            <option value="">全部</option>
          </select>
        </div>
        <div class="toolbar-right">
          <button class="btn-export" @click="handleExport">导出知识库</button>
          <router-link to="/admin/knowledge/create" class="btn-primary">新增词条</router-link>
        </div>
      </div>

      <!-- 列表 -->
      <div class="table-container">
        <table class="data-table">
          <thead>
            <tr>
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
            <tr v-for="item in list" :key="item.id">
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
                <button class="btn-text danger" @click="handleDelete(item.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- 分页 -->
        <div class="pagination">
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
          </div>
        </div>
      </div>
    </div>
  </AdminPage>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import AdminPage from '@/components/AdminPage.vue'
import { knowledgeEntryApi, knowledgeCategoryApi } from '@/api/knowledge'

const router = useRouter()

const list = ref([])
const categories = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const showExport = ref(false)
const exportContent = ref('')

const filters = reactive({
  categoryId: '',
  status: 1
})

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

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
    size: pageSize.value,
    status: filters.status || undefined
  }
  if (filters.categoryId) {
    params.categoryId = filters.categoryId
  }
  const res = await knowledgeEntryApi.list(params)
  if (res.code === 200) {
    list.value = res.data
    total.value = res.total
  }
}

const loadCategories = async () => {
  const res = await knowledgeCategoryApi.list()
  if (res.code === 200) {
    categories.value = res.data
  }
}

const handleEdit = (id) => {
  router.push(`/admin/knowledge/edit/${id}`)
}

const handleDelete = async (id) => {
  if (!confirm('确定删除该词条？')) return
  await knowledgeEntryApi.delete(id)
  loadData()
}

const handleExport = async () => {
  const res = await knowledgeEntryApi.export()
  if (res.code === 200) {
    exportContent.value = res.data
    showExport.value = true
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
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.toolbar-left {
  display: flex;
  gap: 10px;
}

.filter-select {
  padding: 7px 10px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 12px;
  background: var(--color-white);
}

.toolbar-right {
  display: flex;
  gap: 10px;
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

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
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
}
</style>
