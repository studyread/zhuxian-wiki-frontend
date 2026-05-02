<template>
  <div class="article-manage">
    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <!-- 搜索框 -->
        <div class="search-box">
          <input
            v-model="filters.keyword"
            type="text"
            placeholder="搜索文章标题..."
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
        <select v-model="filters.sort" class="filter-select" @change="loadData">
          <option value="">默认排序</option>
          <option value="hot">按热度</option>
          <option value="like">按点赞</option>
          <option value="top">置顶优先</option>
        </select>
      </div>
      <div class="toolbar-right">
        <button class="btn-primary" @click="handleAdd">新增文章</button>
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
            <th>浏览</th>
            <th>点赞</th>
            <th>置顶</th>
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
              <a :href="`/article/${item.id}`" target="_blank">
                {{ item.title }}
              </a>
            </td>
            <td>{{ getCategoryName(item.categoryId) }}</td>
            <td>{{ item.viewCount || 0 }}</td>
            <td>{{ item.likeCount || 0 }}</td>
            <td>
              <span :class="['top-badge', item.isTop === 1 ? 'is-top' : '']">
                {{ item.isTop === 1 ? '置顶' : '-' }}
              </span>
            </td>
            <td>
              <span :class="['status', item.status === 1 ? 'published' : 'draft']">
                {{ item.status === 1 ? '已发布' : '草稿' }}
              </span>
            </td>
            <td>{{ formatDate(item.updatedAt) }}</td>
            <td>
              <button class="btn-text" @click="handleEdit(item)">编辑</button>
              <button class="btn-text" @click="handleToggleTop(item)">
                {{ item.isTop === 1 ? '取消置顶' : '置顶' }}
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

    <!-- 新增/编辑弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal modal-lg">
        <div class="modal-header">
          <h3>{{ isEdit ? '编辑文章' : '新增文章' }}</h3>
          <button @click="showModal = false" class="modal-close">×</button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <div class="form-group">
              <label>标题 <span class="required">*</span></label>
              <input v-model="form.title" type="text" class="form-input" placeholder="请输入文章标题" maxlength="100" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>分类 <span class="required">*</span></label>
              <select v-model="form.categoryId" class="form-input">
                <option value="">请选择分类</option>
                <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                  {{ cat.name }}
                </option>
              </select>
            </div>
            <div class="form-group">
              <label>状态</label>
              <select v-model="form.status" class="form-input">
                <option :value="1">已发布</option>
                <option :value="0">草稿</option>
              </select>
            </div>
          </div>
          <div class="form-group">
            <label>标签</label>
            <input v-model="form.tags" type="text" class="form-input" placeholder="多个标签用逗号分隔，如：新手,攻略,PVP" />
          </div>
          <div class="form-group">
            <label>封面图片</label>
            <div class="cover-upload">
              <div v-if="form.coverImage" class="cover-preview">
                <img :src="form.coverImage" alt="封面预览" />
                <button class="cover-remove" @click="form.coverImage = ''">×</button>
              </div>
              <div v-else class="cover-placeholder" @click="triggerUpload">
                <span>点击上传封面</span>
              </div>
              <input ref="coverInput" type="file" accept="image/*" style="display:none" @change="handleCoverUpload" />
            </div>
          </div>
          <div class="form-group">
            <label>摘要</label>
            <textarea v-model="form.summary" class="form-textarea" placeholder="请输入文章摘要（选填）" rows="3" maxlength="500"></textarea>
          </div>
          <div class="form-group">
            <label>正文内容 <span class="required">*</span></label>
            <div class="editor-toolbar">
              <button type="button" class="toolbar-btn" @click="insertMarkdown('**', '**')" title="加粗"><strong>B</strong></button>
              <button type="button" class="toolbar-btn" @click="insertMarkdown('*', '*')" title="斜体"><em>I</em></button>
              <button type="button" class="toolbar-btn" @click="insertMarkdown('\n- ', '')" title="列表">•</button>
              <button type="button" class="toolbar-btn" @click="insertMarkdown('\n## ', '')" title="二级标题">H2</button>
              <button type="button" class="toolbar-btn" @click="insertMarkdown('\n### ', '')" title="三级标题">H3</button>
              <button type="button" class="toolbar-btn" @click="insertMarkdown('\n> ', '')" title="引用">"</button>
              <button type="button" class="toolbar-btn" @click="insertMarkdown('`', '`')" title="代码">&lt;&gt;</button>
            </div>
            <textarea ref="contentTextarea" v-model="form.content" class="form-textarea content-editor" placeholder="请输入正文内容..." rows="15"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showModal = false" class="btn-secondary">取消</button>
          <button @click="handleSave" class="btn-primary" :disabled="saving">
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { adminArticleApi } from '@/api/admin-article'
import { categoryApi } from '@/api/article'

const list = ref([])
const categories = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedIds = ref([])
const selectAll = ref(false)
const showModal = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const coverInput = ref(null)
const contentTextarea = ref(null)
const editingId = ref(null)

const filters = reactive({
  keyword: '',
  categoryId: '',
  status: '',
  sort: ''
})

const form = reactive({
  title: '',
  categoryId: '',
  tags: '',
  coverImage: '',
  summary: '',
  content: '',
  status: 1
})

const totalPages = computed(() => Math.ceil(total.value / pageSize.value) || 1)

const getCategoryName = (id) => {
  const cat = categories.value.find(c => c.id === id)
  return cat?.name || '-'
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
  if (filters.keyword) params.keyword = filters.keyword
  if (filters.categoryId) params.categoryId = filters.categoryId
  if (filters.status !== '') params.status = parseInt(filters.status)
  if (filters.sort) params.sort = filters.sort

  try {
    const res = await adminArticleApi.list(params)
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
    const res = await categoryApi.getAll()
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

const handleAdd = () => {
  isEdit.value = false
  editingId.value = null
  Object.assign(form, {
    title: '',
    categoryId: '',
    tags: '',
    coverImage: '',
    summary: '',
    content: '',
    status: 1
  })
  showModal.value = true
}

const handleEdit = (item) => {
  isEdit.value = true
  editingId.value = item.id
  Object.assign(form, {
    title: item.title || '',
    categoryId: item.categoryId || '',
    tags: item.tags || '',
    coverImage: item.coverImage || '',
    summary: item.summary || '',
    content: item.content || '',
    status: item.status || 1
  })
  showModal.value = true
}

const handleSave = async () => {
  if (!form.title.trim()) {
    alert('请输入文章标题')
    return
  }
  if (!form.categoryId) {
    alert('请选择文章分类')
    return
  }
  if (!form.content.trim()) {
    alert('请输入正文内容')
    return
  }

  saving.value = true
  try {
    const data = {
      title: form.title.trim(),
      categoryId: parseInt(form.categoryId),
      tags: form.tags.trim() || null,
      coverImage: form.coverImage || null,
      summary: form.summary.trim() || null,
      content: form.content.trim(),
      status: form.status
    }

    let res
    if (isEdit.value) {
      res = await adminArticleApi.update(editingId.value, data)
    } else {
      res = await adminArticleApi.create(data)
    }

    if (res.code === 200) {
      alert(isEdit.value ? '修改成功' : '创建成功')
      showModal.value = false
      loadData()
    } else {
      alert(res.message || '保存失败')
    }
  } catch (e) {
    alert('保存失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = async (id) => {
  if (!confirm('确定删除该文章？')) return
  try {
    await adminArticleApi.delete(id)
    loadData()
  } catch (e) {
    alert('删除失败')
  }
}

const handleToggleTop = async (item) => {
  try {
    if (item.isTop === 1) {
      await adminArticleApi.untoggleTop(item.id)
    } else {
      await adminArticleApi.toggleTop(item.id)
    }
    loadData()
  } catch (e) {
    alert('操作失败')
  }
}

const triggerUpload = () => {
  coverInput.value?.click()
}

const handleCoverUpload = (e) => {
  const file = e.target.files?.[0]
  if (!file) return

  const reader = new FileReader()
  reader.onload = (event) => {
    form.coverImage = event.target.result
  }
  reader.readAsDataURL(file)
}

const insertMarkdown = (before, after) => {
  const textarea = contentTextarea.value
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = form.content.substring(start, end)

  const newText = before + (selectedText || '文字') + after
  form.content = form.content.substring(0, start) + newText + form.content.substring(end)

  setTimeout(() => {
    textarea.focus()
    textarea.setSelectionRange(
      start + before.length,
      start + before.length + (selectedText || '文字').length
    )
  }, 0)
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
  if (!confirm(`确定删除选中的 ${selectedIds.value.length} 篇文章？`)) return
  try {
    await Promise.all(selectedIds.value.map(id => adminArticleApi.delete(id)))
    selectedIds.value = []
    loadData()
  } catch (e) {
    alert('批量删除失败')
  }
}

// 批量发布
const handleBatchPublish = async () => {
  try {
    await Promise.all(selectedIds.value.map(id => adminArticleApi.update(id, { status: 1 })))
    selectedIds.value = []
    loadData()
  } catch (e) {
    alert('批量发布失败')
  }
}

// 批量设为草稿
const handleBatchDraft = async () => {
  try {
    await Promise.all(selectedIds.value.map(id => adminArticleApi.update(id, { status: 0 })))
    selectedIds.value = []
    loadData()
  } catch (e) {
    alert('批量操作失败')
  }
}

onMounted(() => {
  loadData()
  loadCategories()
})
</script>

<style scoped>
.article-manage {
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

.btn-primary {
  padding: 7px 14px;
  background: var(--color-cinnabar);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 12px;
  cursor: pointer;
}

.btn-primary:hover {
  background: var(--color-cinnabar-light);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

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

.top-badge {
  color: var(--color-ink-muted);
}

.top-badge.is-top {
  color: var(--color-cinnabar);
  font-weight: 500;
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

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: var(--color-white);
  border-radius: var(--radius-md);
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.modal-lg {
  max-width: 800px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border-light);
}

.modal-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-ink);
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  color: var(--color-ink-muted);
  cursor: pointer;
  line-height: 1;
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 20px;
  border-top: 1px solid var(--color-border-light);
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

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-size: 12px;
  font-weight: 500;
  color: var(--color-ink);
  margin-bottom: 6px;
}

.required {
  color: var(--color-cinnabar);
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-family: inherit;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: var(--color-cinnabar);
}

.form-textarea {
  resize: vertical;
  line-height: 1.6;
}

.content-editor {
  min-height: 250px;
  font-family: monospace;
}

.editor-toolbar {
  display: flex;
  gap: 4px;
  padding: 8px;
  background: var(--color-paper);
  border: 1px solid var(--color-border);
  border-bottom: none;
  border-radius: var(--radius-sm) var(--radius-sm) 0 0;
}

.toolbar-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  cursor: pointer;
}

.toolbar-btn:hover {
  background: var(--color-cinnabar);
  border-color: var(--color-cinnabar);
  color: white;
}

.cover-upload {
  max-width: 200px;
}

.cover-preview {
  position: relative;
  width: 200px;
  height: 120px;
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.cover-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-remove {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  font-size: 14px;
}

.cover-placeholder {
  width: 200px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-paper);
  border: 1px dashed var(--color-border);
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: 12px;
  color: var(--color-ink-muted);
}

.cover-placeholder:hover {
  border-color: var(--color-cinnabar);
  color: var(--color-cinnabar);
}
</style>
