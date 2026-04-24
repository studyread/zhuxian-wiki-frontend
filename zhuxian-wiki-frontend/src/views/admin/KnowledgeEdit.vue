<template>
  <div class="knowledge-edit">
      <div class="edit-header">
        <h2>{{ isEdit ? '编辑词条' : '新增词条' }}</h2>
        <router-link to="/admin/knowledge" class="btn-back">返回列表</router-link>
      </div>

      <form @submit.prevent="handleSubmit" class="edit-form">
        <div class="form-row">
          <div class="form-group flex-1">
            <label>词条标题 *</label>
            <input
              v-model="form.title"
              type="text"
              placeholder="请输入词条标题"
              required
            />
          </div>
          <div class="form-group">
            <label>所属分类</label>
            <select v-model="form.categoryId">
              <option value="">请选择分类</option>
              <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                {{ cat.name }}
              </option>
            </select>
          </div>
        </div>

        <div class="form-group">
          <label>词条摘要</label>
          <textarea
            v-model="form.summary"
            placeholder="请输入词条摘要（可选）"
            rows="2"
          ></textarea>
        </div>

        <div class="form-group">
          <label>词条内容 *</label>
          <div class="editor-toolbar">
            <button type="button" @click="insertMd('## ')">标题</button>
            <button type="button" @click="insertMd('**', '**')">加粗</button>
            <button type="button" @click="insertMd('- ')">列表</button>
            <button type="button" @click="insertMd('\n```\n', '\n```')">代码</button>
          </div>
          <textarea
            ref="contentRef"
            v-model="form.content"
            placeholder="请输入词条内容（支持 Markdown 格式）"
            rows="15"
            required
          ></textarea>
          <div class="editor-tip">支持 Markdown 格式，可使用 ## 标题、**加粗**、- 列表等语法</div>
        </div>

        <div class="form-row">
          <div class="form-group flex-1">
            <label>标签</label>
            <input
              v-model="form.tags"
              type="text"
              placeholder="多个标签用逗号分隔，如：青云门,法术,输出"
            />
          </div>
          <div class="form-group">
            <label>状态</label>
            <select v-model="form.status">
              <option :value="1">发布</option>
              <option :value="0">草稿</option>
            </select>
          </div>
        </div>

        <div class="form-group">
          <label>来源信息</label>
          <div class="source-row">
            <input
              v-model="form.sourceName"
              type="text"
              placeholder="来源名称"
              class="source-name"
            />
            <input
              v-model="form.sourceUrl"
              type="url"
              placeholder="来源URL"
              class="source-url"
            />
          </div>
        </div>

        <div class="form-actions">
          <button type="submit" class="btn-primary" :disabled="loading">
            {{ loading ? '保存中...' : '保存' }}
          </button>
          <button type="button" class="btn-secondary" @click="$router.back()">
            取消
          </button>
        </div>
      </form>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { knowledgeEntryApi, knowledgeCategoryApi } from '@/api/knowledge'

const route = useRoute()
const router = useRouter()

const contentRef = ref(null)
const loading = ref(false)
const categories = ref([])
const entryId = computed(() => route.params.id)

const isEdit = computed(() => !!entryId.value)

const form = reactive({
  title: '',
  summary: '',
  content: '',
  categoryId: '',
  tags: '',
  status: 1,
  sourceName: '',
  sourceUrl: ''
})

const loadCategories = async () => {
  const res = await knowledgeCategoryApi.list()
  if (res.code === 200) {
    categories.value = res.data
  }
}

const loadEntry = async () => {
  if (!entryId.value) return
  const res = await knowledgeEntryApi.getById(entryId.value)
  if (res.code === 200 && res.data) {
    Object.assign(form, res.data)
  }
}

const insertMd = (before, after = '') => {
  const textarea = contentRef.value
  if (!textarea) return
  
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const text = form.content
  const selected = text.substring(start, end)
  
  form.content = text.substring(0, start) + before + selected + after + text.substring(end)
}

const handleSubmit = async () => {
  if (!form.title || !form.content) {
    alert('请填写标题和内容')
    return
  }

  loading.value = true
  try {
    if (isEdit.value) {
      await knowledgeEntryApi.update(entryId.value, form)
    } else {
      await knowledgeEntryApi.create(form)
    }
    router.push('/admin/knowledge')
  } catch (error) {
    alert('保存失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCategories()
  if (isEdit.value) {
    loadEntry()
  }
})
</script>

<style scoped>
.knowledge-edit {
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

.edit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-shrink: 0;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--color-border-light);
}

.edit-header h2 {
  font-size: 16px;
  font-family: var(--font-serif);
  color: var(--color-ink);
}

.btn-back {
  padding: 7px 14px;
  background: var(--color-white);
  color: var(--color-ink);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 12px;
  text-decoration: none;
}

.edit-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
  overflow-y: auto;
  flex: 1;
  min-height: 0;
}

.form-row {
  display: flex;
  gap: 14px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group.flex-1 {
  flex: 1;
}

.form-group label {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-ink);
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 9px 11px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 13px;
  background: var(--color-white);
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: var(--color-cinnabar);
}

.form-group textarea {
  resize: vertical;
  font-family: inherit;
}

.editor-toolbar {
  display: flex;
  gap: 6px;
  padding: 8px;
  background: var(--color-paper);
  border: 1px solid var(--color-border);
  border-bottom: none;
  border-radius: var(--radius-sm) var(--radius-sm) 0 0;
}

.editor-toolbar button {
  padding: 4px 10px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 11px;
  cursor: pointer;
}

.editor-toolbar button:hover {
  background: var(--color-paper-dark);
}

.form-group textarea {
  border-radius: 0 0 var(--radius-sm) var(--radius-sm);
}

.editor-tip {
  font-size: 11px;
  color: var(--color-ink-muted);
  margin-top: 4px;
}

.source-row {
  display: flex;
  gap: 10px;
}

.source-name {
  width: 180px;
}

.source-url {
  flex: 1;
}

.form-actions {
  display: flex;
  gap: 10px;
  padding-top: 14px;
  border-top: 1px solid var(--color-border-light);
}

.btn-primary {
  padding: 9px 20px;
  background: var(--color-cinnabar);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 13px;
  cursor: pointer;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  padding: 9px 20px;
  background: var(--color-white);
  color: var(--color-ink);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 13px;
  cursor: pointer;
}
</style>
