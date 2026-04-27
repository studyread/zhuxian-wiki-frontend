<template>
  <div class="category-manage">
    <!-- 头部 -->
    <div class="manage-header">
      <div class="header-left">
        <router-link to="/dashboard" class="btn-back">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
            <path d="M10 12L6 8l4-4" stroke="currentColor" stroke-width="1.5" fill="none"/>
          </svg>
          返回
        </router-link>
        <h2>知识分类管理</h2>
        <span class="stat-count">共 {{ categories.length }} 个分类</span>
      </div>
      <div class="header-actions">
        <select v-model="sortBy" class="sort-select">
          <option value="order">按顺序</option>
          <option value="name">按名称</option>
          <option value="newest">最新创建</option>
        </select>
        <button @click="openAddModal()" class="btn-primary">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="5" x2="12" y2="19"/>
            <line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          新增分类
        </button>
      </div>
    </div>

    <!-- 分类网格 -->
    <div class="category-grid">
      <div v-for="(cat, index) in sortedCategories" :key="cat.id" class="category-card">
        <div class="card-order">
          <span class="order-num">{{ index + 1 }}</span>
        </div>
        <div class="card-icon">
          <svg v-if="!cat.icon" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
          </svg>
          <span v-else class="icon-emoji">{{ cat.icon }}</span>
        </div>
        <div class="card-content">
          <h3 class="card-title">{{ cat.name }}</h3>
          <p class="card-desc">{{ cat.description || '暂无描述' }}</p>
          <div class="card-meta">
            <span class="meta-badge">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <path d="M14 2v6h6"/>
              </svg>
              {{ getEntryCount(cat.id) }} 个词条
            </span>
          </div>
        </div>
        <div class="card-actions">
          <button @click="handleMoveUp(cat, index)" :disabled="index === 0" class="btn-move" title="上移">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="18 15 12 9 6 15"/>
            </svg>
          </button>
          <button @click="handleMoveDown(cat, index)" :disabled="index === sortedCategories.length - 1" class="btn-move" title="下移">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="6 9 12 15 18 9"/>
            </svg>
          </button>
          <button @click="openEditModal(cat)" class="btn-edit" title="编辑">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
              <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
            </svg>
          </button>
          <button @click="handleDelete(cat.id)" class="btn-delete" title="删除">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="3 6 5 6 21 6"/>
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
            </svg>
          </button>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="categories.length === 0" class="empty-state">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
          <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
        </svg>
        <p>暂无分类</p>
        <span>点击上方按钮添加第一个分类</span>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ editingCategory ? '编辑分类' : '新增分类' }}</h3>
          <button @click="closeModal" class="modal-close">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>
        <form @submit.prevent="handleSubmit" class="modal-body">
          <div class="form-group">
            <label>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
              </svg>
              分类名称 <span class="required">*</span>
            </label>
            <input v-model="form.name" type="text" placeholder="请输入分类名称" required />
          </div>
          <div class="form-group">
            <label>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="3" width="18" height="18" rx="2"/>
                <circle cx="8.5" cy="8.5" r="1.5"/>
                <path d="M21 15l-5-5L5 21"/>
              </svg>
              分类图标
            </label>
            <div class="icon-picker">
              <input v-model="form.icon" type="text" placeholder="单个字符或emoji" maxlength="4" />
              <div class="icon-presets">
                <button type="button" @click="form.icon = '门'" :class="{ active: form.icon === '门' }">门</button>
                <button type="button" @click="form.icon = '📖'" :class="{ active: form.icon === '📖' }">📖</button>
                <button type="button" @click="form.icon = '⚔️'" :class="{ active: form.icon === '⚔️' }">⚔️</button>
                <button type="button" @click="form.icon = '🎮'" :class="{ active: form.icon === '🎮' }">🎮</button>
                <button type="button" @click="form.icon = '📜'" :class="{ active: form.icon === '📜' }">📜</button>
                <button type="button" @click="form.icon = '🏔️'" :class="{ active: form.icon === '🏔️' }">🏔️</button>
              </div>
            </div>
          </div>
          <div class="form-group">
            <label>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="4" y1="6" x2="20" y2="6"/>
                <line x1="4" y1="12" x2="20" y2="12"/>
                <line x1="4" y1="18" x2="20" y2="18"/>
              </svg>
              分类描述
            </label>
            <textarea v-model="form.description" placeholder="请输入分类描述" rows="3"></textarea>
          </div>
          <div class="form-group">
            <label>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="4" y1="12" x2="20" y2="12"/>
                <polyline points="11 5 4 12 11 19"/>
              </svg>
              排序权重
            </label>
            <input v-model.number="form.sortOrder" type="number" placeholder="数字越小越靠前" min="0" />
            <p class="form-hint">设置分类的显示顺序，数值越小排序越靠前</p>
          </div>
          <div class="modal-footer">
            <button type="button" @click="closeModal" class="btn-cancel">取消</button>
            <button type="submit" class="btn-confirm">
              {{ editingCategory ? '保存修改' : '创建分类' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'

import { knowledgeCategoryApi, knowledgeEntryApi } from '@/api/knowledge'

const categories = ref([])
const entryCounts = ref({})
const showModal = ref(false)
const editingCategory = ref(null)
const sortBy = ref('order')

const form = reactive({
  name: '',
  icon: '',
  description: '',
  sortOrder: 0
})

// 排序后的分类
const sortedCategories = computed(() => {
  const cats = [...categories.value]
  switch (sortBy.value) {
    case 'order':
      return cats.sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
    case 'name':
      return cats.sort((a, b) => a.name.localeCompare(b.name, 'zh-CN'))
    case 'newest':
      return cats.sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0))
    default:
      return cats
  }
})

// 获取词条数量
const getEntryCount = (categoryId) => {
  return entryCounts.value[categoryId] || 0
}

// 加载分类数据
const loadData = async () => {
  try {
    const res = await knowledgeCategoryApi.list()
    if (res.code === 200) {
      categories.value = res.data || []
    }
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

// 加载词条统计
const loadEntryCounts = async () => {
  try {
    const res = await knowledgeEntryApi.list({ page: 1, size: 1000 })
    if (res.code === 200) {
      const counts = {}
      res.data.forEach(entry => {
        if (entry.categoryId) {
          counts[entry.categoryId] = (counts[entry.categoryId] || 0) + 1
        }
      })
      entryCounts.value = counts
    }
  } catch (e) {
    console.error('加载词条统计失败', e)
  }
}

// 打开新增弹窗
const openAddModal = () => {
  editingCategory.value = null
  Object.assign(form, { name: '', icon: '', description: '', sortOrder: 0 })
  showModal.value = true
}

// 打开编辑弹窗
const openEditModal = (cat) => {
  editingCategory.value = cat
  Object.assign(form, {
    name: cat.name,
    icon: cat.icon,
    description: cat.description,
    sortOrder: cat.sortOrder || 0
  })
  showModal.value = true
}

// 关闭弹窗
const closeModal = () => {
  showModal.value = false
  editingCategory.value = null
}

// 删除分类
const handleDelete = async (id) => {
  const count = getEntryCount(id)
  if (count > 0) {
    alert(`该分类下有 ${count} 个词条，请先移动或删除词条后再删除分类`)
    return
  }
  if (!confirm('确定删除该分类？')) return
  try {
    await knowledgeCategoryApi.delete(id)
    loadData()
  } catch (e) {
    alert('删除失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    if (editingCategory.value) {
      await knowledgeCategoryApi.update(editingCategory.value.id, form)
    } else {
      await knowledgeCategoryApi.create(form)
    }
    closeModal()
    loadData()
  } catch (e) {
    alert('保存失败')
  }
}

// 上移
const handleMoveUp = async (cat, index) => {
  if (index === 0) return
  const prev = sortedCategories.value[index - 1]
  const tempOrder = prev.sortOrder || index
  await knowledgeCategoryApi.update(prev.id, { sortOrder: cat.sortOrder || index + 1 })
  await knowledgeCategoryApi.update(cat.id, { sortOrder: tempOrder })
  loadData()
}

// 下移
const handleMoveDown = async (cat, index) => {
  if (index === sortedCategories.value.length - 1) return
  const next = sortedCategories.value[index + 1]
  const tempOrder = next.sortOrder || index + 2
  await knowledgeCategoryApi.update(next.id, { sortOrder: cat.sortOrder || index + 1 })
  await knowledgeCategoryApi.update(cat.id, { sortOrder: tempOrder })
  loadData()
}

onMounted(() => {
  loadData()
  loadEntryCounts()
})
</script>

<style scoped>
.category-manage {
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

.stat-count {
  font-size: 12px;
  color: var(--color-ink-muted);
  padding: 4px 10px;
  background: var(--color-paper);
  border-radius: var(--radius-sm);
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.sort-select {
  padding: 7px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 12px;
  background: var(--color-white);
  cursor: pointer;
  min-width: 110px;
}

.btn-primary {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  background: var(--color-cinnabar);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 12px;
  cursor: pointer;
}

.btn-primary:hover {
  opacity: 0.9;
}

/* 分类网格 */
.category-grid {
  flex: 1;
  overflow-y: auto;
  padding: 16px 18px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 14px;
  align-content: flex-start;
}

.category-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  transition: all 0.2s;
}

.category-card:hover {
  box-shadow: 0 3px 12px rgba(0,0,0,0.08);
  border-color: var(--color-cinnabar);
}

.card-order {
  flex-shrink: 0;
}

.order-num {
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-cinnabar);
  color: white;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;
}

.card-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-paper);
  border-radius: var(--radius-md);
  flex-shrink: 0;
  color: var(--color-cinnabar);
}

.icon-emoji {
  font-size: 22px;
}

.card-content {
  flex: 1;
  min-width: 0;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-ink);
  margin-bottom: 4px;
}

.card-desc {
  font-size: 12px;
  color: var(--color-ink-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 8px;
}

.card-meta {
  display: flex;
  gap: 8px;
}

.meta-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: var(--color-cinnabar);
  padding: 3px 8px;
  background: rgba(196, 92, 72, 0.1);
  border-radius: var(--radius-sm);
}

.card-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

.card-actions button {
  padding: 6px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  cursor: pointer;
  color: var(--color-ink);
  transition: all 0.15s;
}

.btn-move {
  color: var(--color-ink-muted);
}

.btn-move:hover:not(:disabled) {
  background: var(--color-paper);
  color: var(--color-ink);
}

.btn-move:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.btn-edit:hover {
  background: var(--color-cinnabar);
  border-color: var(--color-cinnabar);
  color: white;
}

.btn-delete:hover {
  background: #dc2626;
  border-color: #dc2626;
  color: white;
}

/* 空状态 */
.empty-state {
  grid-column: 1 / -1;
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
  width: 480px;
  background: var(--color-white);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0,0,0,0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border);
}

.modal-header h3 {
  font-size: 16px;
  font-weight: 600;
}

.modal-close {
  padding: 4px;
  background: none;
  border: none;
  cursor: pointer;
  color: var(--color-ink-muted);
  border-radius: var(--radius-sm);
}

.modal-close:hover {
  background: var(--color-paper);
  color: var(--color-ink);
}

.modal-body {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-ink);
}

.form-group label svg {
  color: var(--color-ink-muted);
}

.required {
  color: var(--color-cinnabar);
}

.form-group input,
.form-group textarea {
  padding: 10px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 13px;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: var(--color-cinnabar);
}

.form-hint {
  font-size: 11px;
  color: var(--color-ink-muted);
  margin-top: 4px;
}

/* 图标选择器 */
.icon-picker {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.icon-presets {
  display: flex;
  gap: 6px;
}

.icon-presets button {
  width: 36px;
  height: 36px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-white);
  font-size: 16px;
  cursor: pointer;
  transition: all 0.15s;
}

.icon-presets button:hover {
  border-color: var(--color-cinnabar);
}

.icon-presets button.active {
  background: var(--color-cinnabar);
  border-color: var(--color-cinnabar);
  color: white;
}

/* 弹窗底部 */
.modal-footer {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid var(--color-border-light);
  margin-top: 6px;
}

.btn-cancel {
  padding: 9px 18px;
  background: var(--color-white);
  color: var(--color-ink);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 13px;
  cursor: pointer;
}

.btn-cancel:hover {
  background: var(--color-paper);
}

.btn-confirm {
  padding: 9px 18px;
  background: var(--color-cinnabar);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 13px;
  cursor: pointer;
}

.btn-confirm:hover {
  opacity: 0.9;
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