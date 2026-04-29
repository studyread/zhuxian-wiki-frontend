<template>
  <div class="article-edit-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">{{ isEdit ? '编辑攻略' : '发布攻略' }}</h1>
      <p class="page-desc">{{ isEdit ? '修改已有攻略内容' : '分享你的游戏心得' }}</p>
    </div>

    <!-- 编辑表单 -->
    <div class="edit-form">
      <!-- 基本信息 -->
      <div class="form-section">
        <h3 class="section-title">基本信息</h3>
        
        <div class="form-group">
          <label class="form-label">
            标题
            <span class="required">*</span>
          </label>
          <input 
            v-model="form.title" 
            type="text" 
            class="form-input" 
            placeholder="请输入攻略标题，简洁明了"
            maxlength="100"
          />
          <span class="form-hint">{{ form.title.length }}/100</span>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label">
              分类
              <span class="required">*</span>
            </label>
            <select v-model="form.categoryId" class="form-input">
              <option value="">请选择分类</option>
              <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                {{ cat.name }}
              </option>
            </select>
          </div>
          
          <div class="form-group">
            <label class="form-label">标签</label>
            <input 
              v-model="form.tags" 
              type="text" 
              class="form-input" 
              placeholder="多个标签用逗号分隔，如：青云门,新手,攻略"
            />
            <!-- 热门标签快速选择 -->
            <div class="hot-tags">
              <span class="hot-tags-label">热门标签：</span>
              <span 
                v-for="tag in hotTags" 
                :key="tag"
                class="hot-tag-item"
                @click="addTag(tag)"
              >{{ tag }}</span>
            </div>
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">封面图片</label>
          <div class="cover-upload">
            <div v-if="form.coverImage" class="cover-preview">
              <img :src="form.coverImage" alt="封面预览" />
              <button class="cover-remove" @click="form.coverImage = ''">×</button>
            </div>
            <div v-else class="cover-placeholder" @click="triggerUpload">
              <i class="upload-icon">📷</i>
              <span>点击上传封面</span>
            </div>
            <input 
              ref="coverInput" 
              type="file" 
              accept="image/*" 
              style="display: none"
              @change="handleCoverUpload"
            />
          </div>
        </div>
      </div>

      <!-- 摘要 -->
      <div class="form-section">
        <h3 class="section-title">内容摘要</h3>
        <div class="form-group">
          <textarea 
            v-model="form.summary" 
            class="form-textarea" 
            placeholder="简要描述攻略内容，帮助读者快速了解（选填）"
            rows="3"
            maxlength="500"
          ></textarea>
          <span class="form-hint">{{ form.summary.length }}/500</span>
        </div>
      </div>

      <!-- 正文 -->
      <div class="form-section">
        <h3 class="section-title">
          正文内容
          <span class="required">*</span>
        </h3>
        <div class="editor-toolbar">
          <button type="button" class="toolbar-btn" @click="insertMarkdown('**', '**')" title="加粗">
            <strong>B</strong>
          </button>
          <button type="button" class="toolbar-btn" @click="insertMarkdown('*', '*')" title="斜体">
            <em>I</em>
          </button>
          <button type="button" class="toolbar-btn" @click="insertMarkdown('\n- ', '')" title="列表">
            •
          </button>
          <button type="button" class="toolbar-btn" @click="insertMarkdown('\n## ', '')" title="二级标题">
            H2
          </button>
          <button type="button" class="toolbar-btn" @click="insertMarkdown('\n### ', '')" title="三级标题">
            H3
          </button>
          <button type="button" class="toolbar-btn" @click="insertMarkdown('\n> ', '')" title="引用">
            "
          </button>
          <button type="button" class="toolbar-btn" @click="insertMarkdown('`', '`')" title="代码">
            &lt;&gt;
          </button>
        </div>
        <div class="form-group">
          <textarea 
            ref="contentTextarea"
            v-model="form.content" 
            class="form-textarea content-editor" 
            placeholder="请输入攻略正文内容...

支持 Markdown 格式：
- 使用 **文字** 表示加粗
- 使用 *文字* 表示斜体
- 使用 # 标题表示章节
- 使用 - 列表表示要点"
            rows="20"
          ></textarea>
        </div>
      </div>

      <!-- 提交按钮 -->
      <div class="form-actions">
        <button type="button" class="btn-cancel" @click="$router.back()">
          取消
        </button>
        <button type="button" class="btn-save" @click="handleSave" :disabled="saving">
          {{ saving ? '保存中...' : (isEdit ? '保存修改' : '发布攻略') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { articleApi, categoryApi } from '@/api/article'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const saving = ref(false)
const coverInput = ref(null)
const contentTextarea = ref(null)

// 导航分类 - 与首页导航分类对应
const categories = ref([
  { id: 1, name: '门派' },
  { id: 2, name: '职业' },
  { id: 3, name: '攻略' },
  { id: 4, name: '副本' },
  { id: 5, name: '装备' },
  { id: 6, name: '宠物' },
  { id: 7, name: '坐骑' }
])

// 热门标签预设
const hotTags = ['新手入门', '升级攻略', '门派推荐', 'PVP', '副本攻略', '装备打造']

// 快速添加标签
const addTag = (tag) => {
  const currentTags = form.tags ? form.tags.split(',').map(t => t.trim()).filter(t => t) : []
  if (!currentTags.includes(tag)) {
    currentTags.push(tag)
    form.tags = currentTags.join(', ')
  }
}

const form = reactive({
  title: '',
  categoryId: '',
  tags: '',
  coverImage: '',
  summary: '',
  content: ''
})

// 加载分类列表
const loadCategories = async () => {
  try {
    const res = await categoryApi.getAll()
    if (res.code === 200) {
      categories.value = res.data || []
    }
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 加载文章内容（编辑模式）
const loadArticle = async (id) => {
  try {
    const res = await articleApi.getById(id)
    if (res.code === 200) {
      const article = res.data
      form.title = article.title || ''
      form.categoryId = article.categoryId || ''
      form.tags = article.tags || ''
      form.coverImage = article.coverImage || ''
      form.summary = article.summary || ''
      form.content = article.content || ''
    }
  } catch (error) {
    console.error('加载文章失败:', error)
    alert('加载文章失败')
    router.push('/')
  }
}

// 上传封面
const triggerUpload = () => {
  coverInput.value?.click()
}

const handleCoverUpload = (e) => {
  const file = e.target.files?.[0]
  if (!file) return

  if (file.size > 50 * 1024 * 1024) {
    alert('图片大小不能超过 50MB')
    return
  }

  const reader = new FileReader()
  reader.onload = (event) => {
    const base64 = event.target.result
    // Base64 编码会增加约33%体积，允许最大7MB的base64（对应原图约5MB）
    const base64Size = new Blob([base64]).size
    if (base64Size > 7 * 1024 * 1024) {
      alert('图片转码后太大（' + Math.round(base64Size / 1024) + 'KB），请选择不超过 5MB 的图片')
      return
    }
    form.coverImage = base64
  }
  reader.readAsDataURL(file)
}

// 插入 Markdown
const insertMarkdown = (before, after) => {
  const textarea = contentTextarea.value
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = form.content.substring(start, end)
  
  const newText = before + (selectedText || '文字') + after
  form.content = form.content.substring(0, start) + newText + form.content.substring(end)
  
  // 设置光标位置
  setTimeout(() => {
    textarea.focus()
    textarea.setSelectionRange(start + before.length, start + before.length + (selectedText || '文字').length)
  }, 0)
}

// 保存文章
const handleSave = async () => {
  // 验证
  if (!form.title.trim()) {
    alert('请输入攻略标题')
    return
  }
  if (!form.categoryId) {
    alert('请选择攻略分类')
    return
  }
  if (!form.content.trim()) {
    alert('请输入攻略正文内容')
    return
  }

  // 检查内容大小（MySQL默认限制1MB，留一些余量）
  const contentSize = new Blob([form.content]).size
  if (contentSize > 900 * 1024) {
    alert('文章内容太大（' + Math.round(contentSize / 1024) + 'KB），请精简内容或拆分多篇发布')
    return
  }

  saving.value = true

  try {
    const data = {
      title: form.title.trim(),
      categoryId: parseInt(form.categoryId),
      tags: form.tags.trim(),
      coverImage: form.coverImage || null,
      summary: form.summary.trim() || null,
      content: form.content.trim()
    }

    let res
    if (isEdit.value) {
      res = await articleApi.update(route.params.id, data)
    } else {
      res = await articleApi.save(data)
    }

    if (res.code === 200) {
      alert(isEdit.value ? '修改成功' : '发布成功')
      router.push(isEdit.value ? `/article/${route.params.id}` : '/')
    } else {
      alert(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存文章失败:', error)
    alert('保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadCategories()
  if (isEdit.value) {
    loadArticle(route.params.id)
  }
})
</script>

<style scoped>
.article-edit-page {
  padding: 0;
}

/* 页面标题 */
.page-header {
  padding: 24px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  margin-bottom: 24px;
}

.page-title {
  font-size: 22px;
  font-family: var(--font-serif);
  font-weight: 600;
  color: var(--color-ink);
  margin-bottom: 4px;
}

.page-desc {
  font-size: var(--text-sm);
  color: var(--color-ink-muted);
  margin: 0;
}

/* 表单 */
.edit-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-section {
  padding: 24px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}

.section-title {
  font-size: var(--text-md);
  font-family: var(--font-serif);
  font-weight: 600;
  color: var(--color-ink);
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--color-border-light);
}

.required {
  color: var(--color-cinnabar);
}

.form-group {
  margin-bottom: 20px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.form-label {
  display: block;
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--color-ink);
  margin-bottom: 8px;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 10px 12px;
  font-size: var(--text-base);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-white);
  color: var(--color-ink);
  font-family: var(--font-sans);
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: var(--color-ochre);
}

.form-textarea {
  resize: vertical;
  line-height: 1.6;
}

.content-editor {
  min-height: 400px;
  font-family: monospace;
}

.form-hint {
  display: block;
  font-size: var(--text-xs);
  color: var(--color-ink-muted);
  text-align: right;
  margin-top: 4px;
}

/* 封面上传 */
.cover-upload {
  max-width: 300px;
}

.cover-preview {
  position: relative;
  width: 200px;
  height: 150px;
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
  top: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
}

.cover-placeholder {
  width: 200px;
  height: 150px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: var(--color-paper);
  border: 1px dashed var(--color-border);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s;
}

.cover-placeholder:hover {
  border-color: var(--color-cinnabar);
}

.upload-icon {
  font-size: 32px;
}

.cover-placeholder span {
  font-size: var(--text-sm);
  color: var(--color-ink-muted);
}

/* 编辑器工具栏 */
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
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-sm);
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s;
}

.toolbar-btn:hover {
  background: var(--color-cinnabar);
  border-color: var(--color-cinnabar);
  color: white;
}

/* 热门标签 */
.hot-tags {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.hot-tags-label {
  font-size: var(--text-xs);
  color: var(--color-ink-muted);
}

.hot-tag-item {
  display: inline-block;
  padding: 4px 10px;
  font-size: var(--text-xs);
  color: var(--color-ochre);
  background: rgba(196, 92, 72, 0.08);
  border: 1px solid rgba(196, 92, 72, 0.2);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.hot-tag-item:hover {
  background: var(--color-cinnabar);
  color: white;
  border-color: var(--color-cinnabar);
}

/* 提交按钮 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid var(--color-border-light);
}

.btn-cancel,
.btn-save {
  padding: 12px 28px;
  font-size: var(--text-base);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel {
  background: var(--color-white);
  color: var(--color-ink);
  border: 1px solid var(--color-border);
}

.btn-cancel:hover {
  border-color: var(--color-cinnabar);
  color: var(--color-cinnabar);
}

.btn-save {
  background: var(--color-cinnabar);
  color: var(--color-white);
  border: 1px solid var(--color-cinnabar);
}

.btn-save:hover:not(:disabled) {
  background: var(--color-cinnabar-light);
}

.btn-save:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 响应式 */
@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }

  .editor-toolbar {
    flex-wrap: wrap;
  }
}
</style>
