<template>
  <AdminPage title="知识分类">
    <div class="category-manage">
      <div class="manage-header">
        <h2>知识分类管理</h2>
        <button @click="showAddModal = true" class="btn-primary">新增分类</button>
      </div>

      <div class="category-grid">
        <div v-for="cat in categories" :key="cat.id" class="category-card">
          <div class="cat-icon">{{ cat.icon }}</div>
          <div class="cat-info">
            <h3>{{ cat.name }}</h3>
            <p>{{ cat.description || '暂无描述' }}</p>
          </div>
          <div class="cat-actions">
            <button @click="handleEdit(cat)">编辑</button>
            <button @click="handleDelete(cat.id)" class="danger">删除</button>
          </div>
        </div>
      </div>

      <!-- 新增/编辑弹窗 -->
      <div v-if="showAddModal" class="modal-overlay" @click.self="closeModal">
        <div class="modal">
          <div class="modal-header">
            <h3>{{ editingCategory ? '编辑分类' : '新增分类' }}</h3>
            <button @click="closeModal" class="modal-close">×</button>
          </div>
          <form @submit.prevent="handleSubmit" class="modal-body">
            <div class="form-group">
              <label>分类名称</label>
              <input v-model="form.name" type="text" placeholder="请输入分类名称" required />
            </div>
            <div class="form-group">
              <label>分类图标</label>
              <input v-model="form.icon" type="text" placeholder="单个字符，如：门" maxlength="2" />
            </div>
            <div class="form-group">
              <label>分类描述</label>
              <textarea v-model="form.description" placeholder="请输入分类描述" rows="3"></textarea>
            </div>
            <div class="modal-footer">
              <button type="submit" class="btn-primary">保存</button>
              <button type="button" @click="closeModal" class="btn-secondary">取消</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </AdminPage>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import AdminPage from '@/components/AdminPage.vue'
import { knowledgeCategoryApi } from '@/api/knowledge'

const categories = ref([])
const showAddModal = ref(false)
const editingCategory = ref(null)

const form = reactive({
  name: '',
  icon: '',
  description: ''
})

const loadData = async () => {
  const res = await knowledgeCategoryApi.list()
  if (res.code === 200) {
    categories.value = res.data
  }
}

const handleEdit = (cat) => {
  editingCategory.value = cat
  Object.assign(form, cat)
  showAddModal.value = true
}

const handleDelete = async (id) => {
  if (!confirm('确定删除该分类？')) return
  await knowledgeCategoryApi.delete(id)
  loadData()
}

const handleSubmit = async () => {
  if (editingCategory.value) {
    await knowledgeCategoryApi.update(editingCategory.value.id, form)
  } else {
    await knowledgeCategoryApi.create(form)
  }
  closeModal()
  loadData()
}

const closeModal = () => {
  showAddModal.value = false
  editingCategory.value = null
  Object.assign(form, { name: '', icon: '', description: '' })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.category-manage {
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-shrink: 0;
}

.manage-header h2 {
  font-size: 16px;
  font-family: var(--font-serif);
  color: var(--color-ink);
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

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 10px;
  overflow-y: auto;
  flex: 1;
  min-height: 0;
  align-content: flex-start;
}

.category-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px;
  background: var(--color-paper);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}

.cat-icon {
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  background: var(--color-white);
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}

.cat-info {
  flex: 1;
  min-width: 0;
}

.cat-info h3 {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-ink);
  margin-bottom: 3px;
}

.cat-info p {
  font-size: 11px;
  color: var(--color-ink-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cat-actions {
  display: flex;
  gap: 6px;
}

.cat-actions button {
  padding: 4px 10px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 11px;
  cursor: pointer;
}

.cat-actions button.danger {
  color: #dc2626;
}

.cat-actions button:hover {
  background: var(--color-paper-dark);
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
  width: 450px;
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
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-ink);
}

.form-group input,
.form-group textarea {
  padding: 9px 11px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 13px;
}

.modal-footer {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  padding-top: 14px;
  border-top: 1px solid var(--color-border-light);
}

.btn-secondary {
  padding: 8px 16px;
  background: var(--color-white);
  color: var(--color-ink);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 13px;
  cursor: pointer;
}
</style>
