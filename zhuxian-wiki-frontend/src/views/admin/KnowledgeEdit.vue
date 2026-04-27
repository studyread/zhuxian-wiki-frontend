<template>
  <div class="knowledge-edit">
    <!-- 头部 -->
    <div class="edit-header">
      <div class="header-left">
        <router-link to="/dashboard" class="btn-back">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
            <path d="M10 12L6 8l4-4" stroke="currentColor" stroke-width="1.5" fill="none"/>
          </svg>
          返回列表
        </router-link>
        <h2>{{ isEdit ? '编辑词条' : '新增词条' }}</h2>
      </div>
      <div class="header-right">
        <label class="preview-toggle">
          <input type="checkbox" v-model="showPreview" />
          <span>实时预览</span>
        </label>
      </div>
    </div>

    <!-- 主内容区：左右分栏 -->
    <div class="edit-main" :class="{ 'with-preview': showPreview }">
      <!-- 左侧：编辑区 -->
      <div class="edit-panel">
        <!-- 基本信息卡片 -->
        <div class="form-card">
          <div class="card-header">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 2L2 7l10 5 10-5-10-5z"/>
              <path d="M2 17l10 5 10-5"/>
              <path d="M2 12l10 5 10-5"/>
            </svg>
            <span>基本信息</span>
          </div>
          <div class="card-body">
            <div class="form-row">
              <div class="form-group flex-2">
                <label>
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M4 7V4h16v3"/>
                    <path d="M9 20h6"/>
                    <path d="M12 4v16"/>
                  </svg>
                  词条标题 <span class="required">*</span>
                </label>
                <input
                  v-model="form.title"
                  type="text"
                  placeholder="请输入词条标题"
                  required
                />
              </div>
              <div class="form-group">
                <label>
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="3" y="3" width="18" height="18" rx="2"/>
                    <path d="M9 3v18"/>
                  </svg>
                  所属分类
                </label>
                <select v-model="form.categoryId">
                  <option value="">请选择分类</option>
                  <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                    {{ cat.name }}
                  </option>
                </select>
              </div>
            </div>
            <div class="form-group">
              <label>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <path d="M14 2v6h6"/>
                  <line x1="16" y1="13" x2="8" y2="13"/>
                  <line x1="16" y1="17" x2="8" y2="17"/>
                </svg>
                词条摘要
              </label>
              <textarea
                v-model="form.summary"
                placeholder="请输入词条摘要（可选），将显示在词条列表和搜索结果中"
                rows="2"
              ></textarea>
            </div>
          </div>
        </div>

        <!-- 内容编辑卡片 -->
        <div class="form-card">
          <div class="card-header">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
              <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
            </svg>
            <span>内容编辑</span>
            <div class="header-actions">
              <button type="button" @click="showFullPreview = true" class="btn-preview-modal">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                  <circle cx="12" cy="12" r="3"/>
                </svg>
                全屏预览
              </button>
            </div>
          </div>
          <div class="card-body">
            <div class="editor-wrapper">
              <div class="editor-toolbar">
                <div class="toolbar-group">
                  <button type="button" @click="insertMd('# ')">H1</button>
                  <button type="button" @click="insertMd('## ')">H2</button>
                  <button type="button" @click="insertMd('### ')">H3</button>
                </div>
                <div class="toolbar-divider"></div>
                <div class="toolbar-group">
                  <button type="button" @click="insertMd('**', '**')" title="粗体">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M15.6 10.79c.97-.67 1.65-1.77 1.65-2.79 0-2.26-1.75-4-4-4H7v14h7.04c2.09 0 3.71-1.7 3.71-3.79 0-1.52-.86-2.82-2.15-3.42zM10 6.5h3c.83 0 1.5.67 1.5 1.5s-.67 1.5-1.5 1.5h-3v-3zm3.5 9H10v-3h3.5c.83 0 1.5.67 1.5 1.5s-.67 1.5-1.5 1.5z"/>
                    </svg>
                  </button>
                  <button type="button" @click="insertMd('*', '*')" title="斜体">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M10 4v3h2.21l-3.42 8H6v3h8v-3h-2.21l3.42-8H18V4z"/>
                    </svg>
                  </button>
                  <button type="button" @click="insertMd('~~', '~~')" title="删除线">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M17.3 4.9c-2.3-.6-4.4-1-6-.9-2.7 0-5.3.7-5.3 3.6 0 1.5 1.8 3.3 3.6 3.9h.2"/>
                      <path d="M8.7 19.1c2.3.6 4.4 1 6 .9 2.7 0 5.3-.7 5.3-3.6 0-1.5-1.8-3.3-3.6-3.9h-.2"/>
                    </svg>
                  </button>
                </div>
                <div class="toolbar-divider"></div>
                <div class="toolbar-group">
                  <button type="button" @click="insertMd('- ')" title="无序列表">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M4 10.5c-.83 0-1.5.67-1.5 1.5s.67 1.5 1.5 1.5 1.5-.67 1.5-1.5-.67-1.5-1.5-1.5zm0-6c-.83 0-1.5.67-1.5 1.5S3.17 7.5 4 7.5 5.5 6.83 5.5 6 4.83 4.5 4 4.5zm0 12c-.83 0-1.5.68-1.5 1.5s.68 1.5 1.5 1.5 1.5-.68 1.5-1.5-.67-1.5-1.5-1.5zM7 19h14v-2H7v2zm0-6h14v-2H7v2zm0-8v2h14V5H7z"/>
                    </svg>
                  </button>
                  <button type="button" @click="insertMd('1. ')" title="有序列表">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M2 17h2v.5H3v1h1v.5H2v1h3v-4H2v1zm1-9h1V4H2v1h1v3zm-1 3h1.8L2 13.1v.9h3v-1H3.2L5 10.9V10H2v1zm5-6v2h14V5H7zm0 14h14v-2H7v2zm0-6h14v-2H7v2z"/>
                    </svg>
                  </button>
                  <button type="button" @click="insertMd('- [ ] ')" title="任务列表">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <rect x="3" y="5" width="6" height="6" rx="1"/>
                      <path d="M5 8l1 1 2-2"/>
                      <path d="M12 7h7"/>
                      <path d="M12 11h7"/>
                      <path d="M3 17h18"/>
                      <path d="M3 21h10"/>
                    </svg>
                  </button>
                </div>
                <div class="toolbar-divider"></div>
                <div class="toolbar-group">
                  <button type="button" @click="insertMd('\n```\n', '\n```')" title="代码块">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M9.4 16.6L4.8 12l4.6-4.6L8 6l-6 6 6 6 1.4-1.4zm5.2 0l4.6-4.6-4.6-4.6L16 6l6 6-6 6-1.4-1.4z"/>
                    </svg>
                  </button>
                  <button type="button" @click="insertMd('> ')" title="引用">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M6 17h3l2-4V7H5v6h3zm8 0h3l2-4V7h-6v6h3z"/>
                    </svg>
                  </button>
                  <button type="button" @click="insertMd('[', '](url)')" title="链接">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
                      <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
                    </svg>
                  </button>
                  <button type="button" @click="handleImageUpload" title="图片">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <rect x="3" y="3" width="18" height="18" rx="2"/>
                      <circle cx="8.5" cy="8.5" r="1.5"/>
                      <path d="M21 15l-5-5L5 21"/>
                    </svg>
                  </button>
                </div>
                <div class="toolbar-divider"></div>
                <div class="toolbar-group">
                  <button type="button" @click="insertMd('\n---\n')" title="分割线">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <line x1="3" y1="12" x2="21" y2="12"/>
                      <circle cx="12" cy="12" r="2"/>
                      <circle cx="5" cy="12" r="1"/>
                      <circle cx="19" cy="12" r="1"/>
                    </svg>
                  </button>
                  <button type="button" @click="insertMd('\n| 表头1 | 表头2 |\n|------|------|\n| ', ' | 内容2 |')" title="表格">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <rect x="3" y="3" width="18" height="18" rx="2"/>
                      <path d="M3 9h18"/>
                      <path d="M3 15h18"/>
                      <path d="M9 3v18"/>
                    </svg>
                  </button>
                </div>
              </div>
              <textarea
                ref="contentRef"
                v-model="form.content"
                placeholder="请输入词条内容（支持 Markdown 格式）&#10;&#10;快捷键：&#10;• Ctrl+B 加粗&#10;• Ctrl+I 斜体&#10;• Ctrl+K 链接"
                rows="15"
                required
              ></textarea>
            </div>
            <div class="editor-tip">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <path d="M12 16v-4"/>
                <path d="M12 8h.01"/>
              </svg>
              <span>支持 Markdown 格式 | 快捷键: Ctrl+B 加粗, Ctrl+I 斜体</span>
            </div>
          </div>
        </div>

        <!-- 其他信息卡片 -->
        <div class="form-card">
          <div class="card-header">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
              <line x1="7" y1="7" x2="7.01" y2="7"/>
            </svg>
            <span>附加信息</span>
          </div>
          <div class="card-body">
            <div class="form-row">
              <div class="form-group flex-1">
                <label>
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
                    <line x1="7" y1="7" x2="7.01" y2="7"/>
                  </svg>
                  标签
                </label>
                <input
                  v-model="form.tags"
                  type="text"
                  placeholder="多个标签用逗号分隔，如：青云门,法术,输出"
                />
              </div>
              <div class="form-group">
                <label>
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"/>
                    <polyline points="12 6 12 12 16 14"/>
                  </svg>
                  状态
                </label>
                <div class="status-toggle">
                  <button
                    :class="{ active: form.status === 1 }"
                    @click="form.status = 1"
                    type="button"
                  >
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/>
                    </svg>
                    发布
                  </button>
                  <button
                    :class="{ active: form.status === 0 }"
                    @click="form.status = 0"
                    type="button"
                  >
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
                    </svg>
                    草稿
                  </button>
                </div>
              </div>
            </div>
            <div class="form-group">
              <label>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
                  <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
                </svg>
                来源信息
              </label>
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
          </div>
        </div>

        <!-- 图片上传卡片 -->
        <div class="form-card">
          <div class="card-header">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="18" height="18" rx="2"/>
              <circle cx="8.5" cy="8.5" r="1.5"/>
              <path d="M21 15l-5-5L5 21"/>
            </svg>
            <span>图片上传</span>
            <span class="header-hint">拖拽或点击上传</span>
          </div>
          <div class="card-body">
            <div class="image-upload-area" @dragover.prevent @drop.prevent="handleDrop">
              <input
                type="file"
                ref="imageInputRef"
                accept="image/*"
                multiple
                @change="handleFileSelect"
                style="display: none"
              />
              <div v-if="!uploadedImages.length" class="upload-placeholder">
                <div class="upload-icon-wrapper">
                  <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                    <polyline points="17 8 12 3 7 8"/>
                    <line x1="12" y1="3" x2="12" y2="15"/>
                  </svg>
                </div>
                <p class="upload-text">拖拽图片到这里，或</p>
                <button type="button" @click="imageInputRef.click()" class="upload-btn">
                  点击选择文件
                </button>
                <p class="upload-tip">支持 JPG、PNG、WebP 格式，单张不超过 5MB</p>
              </div>
              <div v-else class="uploaded-images">
                <div v-for="(img, idx) in uploadedImages" :key="idx" class="uploaded-item">
                  <div class="image-preview">
                    <img :src="img.url" :alt="img.name" />
                  </div>
                  <div class="image-info">
                    <span class="image-name">{{ img.name }}</span>
                  </div>
                  <div class="image-actions">
                    <button type="button" @click="insertImage(img)" class="btn-insert" title="插入到内容">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
                        <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
                      </svg>
                    </button>
                    <button type="button" @click="removeImage(idx)" class="btn-delete" title="删除">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <polyline points="3 6 5 6 21 6"/>
                        <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                      </svg>
                    </button>
                  </div>
                </div>
                <button type="button" class="add-more-btn" @click="imageInputRef.click()">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <line x1="12" y1="5" x2="12" y2="19"/>
                    <line x1="5" y1="12" x2="19" y2="12"/>
                  </svg>
                  添加更多
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：预览区 -->
      <div v-if="showPreview" class="preview-panel">
        <div class="preview-header">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
            <circle cx="12" cy="12" r="3"/>
          </svg>
          <span>实时预览</span>
          <button type="button" @click="showPreview = false" class="close-preview">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>
        <div class="preview-body" v-html="renderedContent"></div>
      </div>
    </div>

    <!-- 底部操作栏 -->
    <div class="form-actions">
      <div class="actions-left">
        <button type="button" class="btn-draft" @click="form.status = 0" v-if="form.status === 1">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/>
            <polyline points="17 21 17 13 7 13 7 21"/>
            <polyline points="7 3 7 8 15 8"/>
          </svg>
          存为草稿
        </button>
      </div>
      <div class="actions-right">
        <button type="button" class="btn-secondary" @click="$router.back()">
          取消
        </button>
        <button type="submit" class="btn-primary" :disabled="loading" @click="handleSubmit">
          <svg v-if="loading" width="14" height="14" viewBox="0 0 24 24" class="spinner">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="3" fill="none" stroke-dasharray="32" stroke-linecap="round"/>
          </svg>
          <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/>
            <polyline points="17 21 17 13 7 13 7 21"/>
            <polyline points="7 3 7 8 15 8"/>
          </svg>
          {{ loading ? '保存中...' : (isEdit ? '保存修改' : '创建词条') }}
        </button>
      </div>
    </div>

    <!-- 全屏预览弹窗 -->
    <div v-if="showFullPreview" class="modal-overlay" @click.self="showFullPreview = false">
      <div class="preview-modal">
        <div class="preview-modal-header">
          <h3>内容预览</h3>
          <button @click="showFullPreview = false" class="modal-close">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>
        <div class="preview-modal-body" v-html="renderedContent"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { knowledgeEntryApi, knowledgeCategoryApi } from '@/api/knowledge'

const route = useRoute()
const router = useRouter()

const contentRef = ref(null)
const imageInputRef = ref(null)
const loading = ref(false)
const categories = ref([])
const uploadedImages = ref([])
const showPreview = ref(false)
const showFullPreview = ref(false)
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
  sourceUrl: '',
  images: []
})

// 简单的 Markdown 转 HTML 渲染
const renderedContent = computed(() => {
  if (!form.content) return '<p class="empty-preview">预览区域</p>'
  let html = form.content
    // 标题
    .replace(/^### (.+)$/gm, '<h3>$1</h3>')
    .replace(/^## (.+)$/gm, '<h2>$1</h2>')
    .replace(/^# (.+)$/gm, '<h1>$1</h1>')
    // 粗体
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    // 斜体
    .replace(/\*(.+?)\*/g, '<em>$1</em>')
    // 删除线
    .replace(/~~(.+?)~~/g, '<del>$1</del>')
    // 列表
    .replace(/^- (.+)$/gm, '<li>$1</li>')
    .replace(/^[\*\-] \[ \] (.+)$/gm, '<li><input type="checkbox" disabled /> $1</li>')
    .replace(/^[\*\-] \[x\] (.+)$/gm, '<li><input type="checkbox" checked disabled /> $1</li>')
    // 数字列表
    .replace(/^\d+\. (.+)$/gm, '<li>$1</li>')
    // 引用
    .replace(/^> (.+)$/gm, '<blockquote>$1</blockquote>')
    // 代码块
    .replace(/```([\s\S]+?)```/g, '<pre><code>$1</code></pre>')
    // 行内代码
    .replace(/`(.+?)`/g, '<code>$1</code>')
    // 链接
    .replace(/\[(.+?)\]\((.+?)\)/g, '<a href="$2" target="_blank">$1</a>')
    // 图片
    .replace(/!\[(.+?)\]\((.+?)\)/g, '<img src="$2" alt="$1" style="max-width:100%"/>')
    // 分割线
    .replace(/^---$/gm, '<hr>')
    // 表格
    .replace(/\|(.+)\|/g, (match) => {
      const cells = match.split('|').filter(c => c.trim())
      if (cells.some(c => /^-+$/.test(c.trim()))) return ''
      return '<tr>' + cells.map(c => `<td>${c.trim()}</td>`).join('') + '</tr>'
    })
    // 段落
    .replace(/\n\n/g, '</p><p>')

  // 处理表格包裹
  if (html.includes('<tr>')) {
    html = html.replace(/(<tr>.*<\/tr>)+/gs, '<table>$&</table>')
  }

  return `<div class="markdown-body"><p>${html}</p></div>`
})

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

const loadEntry = async () => {
  if (!entryId.value) return
  try {
    const res = await knowledgeEntryApi.getById(entryId.value)
    if (res.code === 200 && res.data) {
      Object.assign(form, res.data)
    }
  } catch (e) {
    console.error('加载词条失败', e)
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

  // 聚焦回文本框
  setTimeout(() => {
    textarea.focus()
    textarea.setSelectionRange(start + before.length, start + before.length + selected.length)
  }, 0)
}

// 图片上传处理
const handleFileSelect = (e) => {
  const files = e.target.files
  if (files) {
    processFiles(Array.from(files))
  }
  e.target.value = '' // 重置 input
}

// 拖拽处理
const handleDrop = (e) => {
  const files = Array.from(e.dataTransfer.files).filter(f => f.type.startsWith('image/'))
  if (files.length) {
    processFiles(files)
  }
}

// 处理文件
const processFiles = (files) => {
  files.forEach(file => {
    // 检查文件大小 (5MB)
    if (file.size > 5 * 1024 * 1024) {
      alert(`文件 ${file.name} 超过 5MB 限制`)
      return
    }
    const reader = new FileReader()
    reader.onload = (e) => {
      uploadedImages.value.push({
        name: file.name,
        url: e.target.result
      })
    }
    reader.readAsDataURL(file)
  })
}

// 插入图片到内容
const insertImage = (img) => {
  const markdown = `\n![${img.name}](${img.url})\n`
  form.content += markdown
}

// 删除图片
const removeImage = (idx) => {
  uploadedImages.value.splice(idx, 1)
}

// 图片上传按钮
const handleImageUpload = () => {
  imageInputRef.value.click()
}

const handleSubmit = async () => {
  if (!form.title || !form.content) {
    alert('请填写标题和内容')
    return
  }

  loading.value = true
  try {
    // 收集所有图片
    form.images = uploadedImages.value.map(img => img.url)

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

// 快捷键支持
const handleKeydown = (e) => {
  if (e.ctrlKey || e.metaKey) {
    if (e.key === 'b') {
      e.preventDefault()
      insertMd('**', '**')
    } else if (e.key === 'i') {
      e.preventDefault()
      insertMd('*', '*')
    } else if (e.key === 'k') {
      e.preventDefault()
      insertMd('[', '](url)')
    }
  }
}

onMounted(() => {
  loadCategories()
  if (isEdit.value) {
    loadEntry()
  }
  // 监听快捷键
  document.addEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
.knowledge-edit {
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
.edit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px;
  border-bottom: 1px solid var(--color-border-light);
  flex-shrink: 0;
  background: var(--color-white);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-left h2 {
  font-size: 16px;
  font-family: var(--font-serif);
  color: var(--color-ink);
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

.preview-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 13px;
  color: var(--color-ink);
}

.preview-toggle input {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

/* 主内容区 */
.edit-main {
  flex: 1;
  display: flex;
  overflow: hidden;
  min-height: 0;
}

.edit-panel {
  flex: 1;
  overflow-y: auto;
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.edit-main.with-preview .edit-panel {
  flex: 1;
  min-width: 0;
}

.preview-panel {
  width: 380px;
  border-left: 1px solid var(--color-border);
  background: var(--color-paper);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.preview-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-bottom: 1px solid var(--color-border-light);
  font-size: 13px;
  font-weight: 500;
  color: var(--color-ink);
}

.close-preview {
  margin-left: auto;
  padding: 4px;
  background: none;
  border: none;
  cursor: pointer;
  color: var(--color-ink-muted);
  border-radius: var(--radius-sm);
}

.close-preview:hover {
  background: var(--color-paper-dark);
  color: var(--color-ink);
}

.preview-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

/* 卡片样式 */
.form-card {
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: var(--color-paper);
  border-bottom: 1px solid var(--color-border-light);
  font-size: 13px;
  font-weight: 500;
  color: var(--color-ink);
}

.card-header svg {
  color: var(--color-cinnabar);
}

.header-actions {
  margin-left: auto;
}

.header-hint {
  margin-left: auto;
  font-size: 11px;
  color: var(--color-ink-muted);
  font-weight: normal;
}

.btn-preview-modal {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 11px;
  color: var(--color-ink);
  cursor: pointer;
}

.btn-preview-modal:hover {
  border-color: var(--color-cinnabar);
  color: var(--color-cinnabar);
}

.card-body {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* 表单样式 */
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

.form-group.flex-2 {
  flex: 2;
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
.form-group select,
.form-group textarea {
  padding: 9px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 13px;
  background: var(--color-white);
  transition: border-color 0.2s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: var(--color-cinnabar);
}

.form-group textarea {
  resize: vertical;
  min-height: 60px;
}

/* 编辑器 */
.editor-wrapper {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.editor-toolbar {
  display: flex;
  gap: 2px;
  padding: 8px;
  background: var(--color-paper);
  border-bottom: 1px solid var(--color-border);
  flex-wrap: wrap;
}

.toolbar-group {
  display: flex;
  gap: 2px;
}

.toolbar-divider {
  width: 1px;
  height: 24px;
  background: var(--color-border);
  margin: 0 6px;
  align-self: center;
}

.editor-toolbar button {
  padding: 6px 8px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 11px;
  font-weight: 500;
  cursor: pointer;
  color: var(--color-ink);
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 28px;
  height: 28px;
  transition: all 0.15s;
}

.editor-toolbar button:hover {
  background: var(--color-paper-dark);
  border-color: var(--color-cinnabar);
  color: var(--color-cinnabar);
}

.editor-wrapper textarea {
  width: 100%;
  border: none;
  border-radius: 0;
  resize: vertical;
  min-height: 350px;
  font-family: inherit;
  line-height: 1.6;
}

.editor-wrapper textarea:focus {
  outline: none;
  border-color: transparent;
}

.editor-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: var(--color-ink-muted);
}

/* 状态切换 */
.status-toggle {
  display: flex;
  gap: 0;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.status-toggle button {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 8px 12px;
  background: var(--color-white);
  border: none;
  font-size: 12px;
  cursor: pointer;
  color: var(--color-ink-muted);
  transition: all 0.2s;
}

.status-toggle button:first-child {
  border-right: 1px solid var(--color-border);
}

.status-toggle button.active {
  background: var(--color-cinnabar);
  color: white;
}

/* 来源信息 */
.source-row {
  display: flex;
  gap: 10px;
}

.source-name {
  width: 150px;
}

.source-url {
  flex: 1;
}

/* 图片上传 */
.image-upload-area {
  border: 2px dashed var(--color-border);
  border-radius: var(--radius-md);
  padding: 24px;
  background: var(--color-paper);
  min-height: 120px;
  transition: border-color 0.2s;
}

.image-upload-area:hover {
  border-color: var(--color-cinnabar);
}

.upload-placeholder {
  text-align: center;
}

.upload-icon-wrapper {
  margin-bottom: 12px;
  color: var(--color-ink-muted);
}

.upload-text {
  font-size: 13px;
  color: var(--color-ink-muted);
  margin-bottom: 8px;
}

.upload-btn {
  padding: 8px 16px;
  background: var(--color-cinnabar);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 13px;
  cursor: pointer;
  transition: opacity 0.2s;
}

.upload-btn:hover {
  opacity: 0.9;
}

.upload-tip {
  font-size: 11px;
  color: var(--color-ink-muted);
  margin-top: 12px;
}

.uploaded-images {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.uploaded-item {
  width: 140px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.image-preview {
  height: 90px;
  overflow: hidden;
  background: var(--color-paper);
}

.uploaded-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-info {
  padding: 6px 8px;
  border-bottom: 1px solid var(--color-border-light);
}

.image-name {
  font-size: 11px;
  color: var(--color-ink);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block;
}

.image-actions {
  display: flex;
  padding: 4px;
  gap: 4px;
}

.image-actions button {
  flex: 1;
  padding: 6px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-white);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s;
}

.btn-insert {
  color: var(--color-cinnabar);
  border-color: var(--color-cinnabar);
}

.btn-insert:hover {
  background: var(--color-cinnabar);
  color: white;
}

.btn-delete {
  color: #dc2626;
  border-color: #dc2626;
}

.btn-delete:hover {
  background: #dc2626;
  color: white;
}

.add-more-btn {
  width: 140px;
  height: 90px;
  border: 2px dashed var(--color-border);
  border-radius: var(--radius-sm);
  background: transparent;
  cursor: pointer;
  font-size: 12px;
  color: var(--color-ink-muted);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.2s;
}

.add-more-btn:hover {
  border-color: var(--color-cinnabar);
  color: var(--color-cinnabar);
}

/* 底部操作栏 */
.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px;
  border-top: 1px solid var(--color-border-light);
  background: var(--color-white);
  flex-shrink: 0;
}

.actions-right {
  display: flex;
  gap: 10px;
}

.btn-primary {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 9px 20px;
  background: var(--color-cinnabar);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 13px;
  cursor: pointer;
  transition: opacity 0.2s;
}

.btn-primary:hover {
  opacity: 0.9;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 9px 20px;
  background: var(--color-white);
  color: var(--color-ink);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 13px;
  cursor: pointer;
}

.btn-secondary:hover {
  background: var(--color-paper);
}

.btn-draft {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 9px 16px;
  background: var(--color-paper);
  color: var(--color-ink);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 13px;
  cursor: pointer;
}

.btn-draft:hover {
  background: var(--color-paper-dark);
}

.spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 预览弹窗 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.preview-modal {
  width: 900px;
  max-height: 85vh;
  background: var(--color-white);
  border-radius: var(--radius-lg);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.preview-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border);
}

.preview-modal-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-ink);
}

.modal-close {
  padding: 6px;
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

.preview-modal-body {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}

/* Markdown 预览样式 */
.preview-body :deep(.empty-preview),
.preview-modal-body :deep(.empty-preview) {
  color: var(--color-ink-muted);
  text-align: center;
  padding: 40px;
}

.preview-body :deep(.markdown-body),
.preview-modal-body :deep(.markdown-body) {
  font-size: 14px;
  line-height: 1.8;
}

.preview-body :deep(h1),
.preview-modal-body :deep(h1) {
  font-size: 24px;
  margin: 16px 0;
  font-family: var(--font-serif);
  border-bottom: 2px solid var(--color-cinnabar);
  padding-bottom: 8px;
}

.preview-body :deep(h2),
.preview-modal-body :deep(h2) {
  font-size: 20px;
  margin: 14px 0;
  font-family: var(--font-serif);
  border-bottom: 1px solid var(--color-border);
  padding-bottom: 6px;
}

.preview-body :deep(h3),
.preview-modal-body :deep(h3) {
  font-size: 16px;
  margin: 12px 0;
  font-weight: 600;
}

.preview-body :deep(p),
.preview-modal-body :deep(p) {
  margin: 8px 0;
}

.preview-body :deep(blockquote),
.preview-modal-body :deep(blockquote) {
  border-left: 4px solid var(--color-cinnabar);
  padding-left: 16px;
  margin: 12px 0;
  color: var(--color-ink-muted);
  background: var(--color-paper);
  padding: 8px 16px;
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
}

.preview-body :deep(pre),
.preview-modal-body :deep(pre) {
  background: #1e1e32;
  color: #fff;
  padding: 14px;
  border-radius: var(--radius-sm);
  overflow-x: auto;
  margin: 12px 0;
}

.preview-body :deep(code),
.preview-modal-body :deep(code) {
  background: var(--color-paper);
  padding: 2px 6px;
  border-radius: 3px;
  font-family: monospace;
  font-size: 13px;
}

.preview-body :deep(pre code),
.preview-modal-body :deep(pre code) {
  background: none;
  padding: 0;
}

.preview-body :deep(li),
.preview-modal-body :deep(li) {
  margin: 4px 0;
  padding-left: 8px;
}

.preview-body :deep(ul),
.preview-modal-body :deep(ul),
.preview-body :deep(ol),
.preview-modal-body :deep(ol) {
  margin: 8px 0;
  padding-left: 24px;
}

.preview-body :deep(table),
.preview-modal-body :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 12px 0;
}

.preview-body :deep(td),
.preview-modal-body :deep(td),
.preview-body :deep(th),
.preview-modal-body :deep(th) {
  border: 1px solid var(--color-border);
  padding: 8px 12px;
  text-align: left;
}

.preview-body :deep(th),
.preview-modal-body :deep(th) {
  background: var(--color-paper);
  font-weight: 600;
}

.preview-body :deep(hr),
.preview-modal-body :deep(hr) {
  border: none;
  border-top: 2px dashed var(--color-border);
  margin: 20px 0;
}

.preview-body :deep(a),
.preview-modal-body :deep(a) {
  color: var(--color-cinnabar);
  text-decoration: none;
}

.preview-body :deep(a:hover),
.preview-modal-body :deep(a:hover) {
  text-decoration: underline;
}

.preview-body :deep(img),
.preview-modal-body :deep(img) {
  max-width: 100%;
  border-radius: var(--radius-sm);
  margin: 8px 0;
}

.preview-body :deep(input[type="checkbox"]),
.preview-modal-body :deep(input[type="checkbox"]) {
  margin-right: 6px;
}
</style>
