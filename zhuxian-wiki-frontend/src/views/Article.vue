<template>
  <div class="article-detail-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb">
      <router-link to="/">首页</router-link>
      <span class="separator">/</span>
      <router-link v-if="article?.categoryId" :to="`/category/${article.categoryId}`">
        {{ article.categoryName || '攻略' }}
      </router-link>
      <span class="separator">/</span>
      <span class="current">{{ article?.title }}</span>
    </div>

    <!-- 文章内容 -->
    <div v-if="loading" class="loading-state">
      <div class="loading-icon">典</div>
      <p>典籍载入中...</p>
    </div>

    <div v-else-if="article" class="article-container">
      <!-- 文章头部 -->
      <header class="article-header">
        <h1 class="article-title">{{ article.title }}</h1>
        
        <div class="article-meta">
          <div class="meta-left">
            <span class="meta-item">
              <i class="icon">👁</i>
              {{ article.viewCount || 0 }} 次阅读
            </span>
            <span class="meta-item">
              <i class="icon">❤</i>
              {{ article.likeCount || 0 }} 点赞
            </span>
            <span class="meta-item">
              <i class="icon">📅</i>
              {{ formatDate(article.createdAt) }}
            </span>
          </div>
        </div>

        <div v-if="article.tags" class="article-tags">
          <router-link 
            v-for="tag in article.tags.split(',')" 
            :key="tag" 
            :to="`/search?keyword=${tag.trim()}`"
            class="tag"
          >
            {{ tag.trim() }}
          </router-link>
        </div>
      </header>

      <!-- 文章摘要 -->
      <div v-if="article.summary" class="article-summary">
        <p>{{ article.summary }}</p>
      </div>

      <!-- 文章正文 -->
      <article class="article-body" v-html="renderedContent"></article>

      <!-- 文章底部操作 -->
      <footer class="article-footer">
        <div class="action-bar">
          <button class="action-btn like-btn" :class="{ active: hasLiked }" @click="handleLike">
            <i class="icon">{{ hasLiked ? '❤' : '☆' }}</i>
            {{ article.likeCount || 0 }} 点赞
          </button>
          <button class="action-btn collect-btn" :class="{ active: hasCollected }" @click="handleCollect">
            <i class="icon">{{ hasCollected ? '★' : '☆' }}</i>
            {{ hasCollected ? '已收藏' : '收藏' }}
          </button>
          <button class="action-btn share-btn" @click="handleShare">
            <i class="icon">↗</i>
            分享
          </button>
        </div>

        <!-- 上下篇导航 -->
        <div class="article-nav">
          <router-link v-if="prevArticle" :to="`/article/${prevArticle.id}`" class="nav-link prev">
            <span class="nav-label">上一篇</span>
            <span class="nav-title">{{ prevArticle.title }}</span>
          </router-link>
          <div v-else class="nav-placeholder"></div>
          
          <router-link v-if="nextArticle" :to="`/article/${nextArticle.id}`" class="nav-link next">
            <span class="nav-label">下一篇</span>
            <span class="nav-title">{{ nextArticle.title }}</span>
          </router-link>
          <div v-else class="nav-placeholder"></div>
        </div>
      </footer>

      <!-- 相关推荐 -->
      <section class="related-articles">
        <h3 class="section-title">相关推荐</h3>
        <div class="related-list">
          <router-link 
            v-for="item in relatedArticles" 
            :key="item.id"
            :to="`/article/${item.id}`"
            class="related-item"
          >
            <div class="related-title">{{ item.title }}</div>
            <div class="related-meta">
              <span>阅读 {{ item.viewCount }}</span>
              <span>点赞 {{ item.likeCount }}</span>
            </div>
          </router-link>
        </div>
      </section>
    </div>

    <!-- 文章不存在 -->
    <div v-else class="not-found">
      <div class="not-found-icon">典</div>
      <h2>典籍未寻</h2>
      <p>此典籍不存在或已被删除</p>
      <router-link to="/" class="back-home">返回首页</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { articleApi } from '@/api/article'

const route = useRoute()
const router = useRouter()

const article = ref({})
const loading = ref(true)
const hasLiked = ref(false)
const hasCollected = ref(false)
const relatedArticles = ref([])
const prevArticle = ref(null)
const nextArticle = ref(null)

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const renderedContent = computed(() => {
  if (!article.value?.content) return ''
  let content = article.value.content

  // HTML转义（防XSS）
  const escapeHtml = (str) => str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')

  // 处理代码块（先提取保护，最后还原）
  const codeBlocks = []
  content = content.replace(/```([\w]*)\n?([\s\S]*?)```/g, (_, lang, code) => {
    const idx = codeBlocks.length
    codeBlocks.push(`<pre class="article-code-block"><code>${escapeHtml(code.trim())}</code></pre>`)
    return `\x00CODE${idx}\x00`
  })

  // 行内代码
  const inlineCodes = []
  content = content.replace(/`([^`\n]+)`/g, (_, code) => {
    const idx = inlineCodes.length
    inlineCodes.push(`<code class="article-inline-code">${escapeHtml(code)}</code>`)
    return `\x00INLINE${idx}\x00`
  })

  // 按行处理
  const lines = content.split('\n')
  const output = []
  let inList = false
  let listItems = []

  const flushList = () => {
    if (listItems.length > 0) {
      output.push(`<ul class="article-ul">${listItems.join('')}</ul>`)
      listItems = []
      inList = false
    }
  }

  for (let i = 0; i < lines.length; i++) {
    let line = lines[i]

    // 代码块占位符直接输出
    if (line.match(/^\x00CODE\d+\x00$/)) {
      flushList()
      const idx = parseInt(line.replace(/\x00CODE(\d+)\x00/, '$1'))
      output.push(codeBlocks[idx])
      continue
    }

    // 标题
    const h4 = line.match(/^####\s+(.+)$/)
    const h3 = line.match(/^###\s+(.+)$/)
    const h2 = line.match(/^##\s+(.+)$/)
    const h1 = line.match(/^#\s+(.+)$/)
    if (h4) { flushList(); output.push(`<h4 class="article-h4">${processInline(h4[1], inlineCodes)}</h4>`); continue }
    if (h3) { flushList(); output.push(`<h3 class="article-h3">${processInline(h3[1], inlineCodes)}</h3>`); continue }
    if (h2) { flushList(); output.push(`<h2 class="article-h2">${processInline(h2[1], inlineCodes)}</h2>`); continue }
    if (h1) { flushList(); output.push(`<h1 class="article-h1">${processInline(h1[1], inlineCodes)}</h1>`); continue }

    // 分割线
    if (line.match(/^[-*_]{3,}$/)) { flushList(); output.push('<hr class="article-hr">'); continue }

    // 引用块
    const bq = line.match(/^>\s*(.*)$/)
    if (bq) { flushList(); output.push(`<blockquote class="article-blockquote">${processInline(bq[1], inlineCodes)}</blockquote>`); continue }

    // 有序列表
    const olMatch = line.match(/^\d+\.\s+(.+)$/)
    if (olMatch) {
      if (!inList) { inList = true; listItems = [] }
      listItems.push(`<li>${processInline(olMatch[1], inlineCodes)}</li>`)
      continue
    }

    // 无序列表
    const ulMatch = line.match(/^[-*+]\s+(.+)$/)
    if (ulMatch) {
      if (!inList) { inList = true; listItems = [] }
      listItems.push(`<li>${processInline(ulMatch[1], inlineCodes)}</li>`)
      continue
    }

    // 空行：结束列表或段落
    if (line.trim() === '') {
      flushList()
      continue
    }

    // 普通段落
    flushList()
    output.push(`<p class="article-p">${processInline(line, inlineCodes)}</p>`)
  }
  flushList()

  // 还原内联代码占位符
  let result = output.join('\n')
  inlineCodes.forEach((code, idx) => {
    result = result.replace(`\x00INLINE${idx}\x00`, code)
  })

  return result
})

// 处理行内Markdown（粗体、斜体、内联代码占位符）
const processInline = (text, inlineCodes) => {
  // 还原内联代码占位符
  text = text.replace(/\x00INLINE(\d+)\x00/g, (_, idx) => inlineCodes[parseInt(idx)] || '')
  // 粗斜体
  text = text.replace(/\*\*\*(.+?)\*\*\*/g, '<strong><em>$1</em></strong>')
  // 粗体
  text = text.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  // 斜体
  text = text.replace(/\*(.+?)\*/g, '<em>$1</em>')
  // 链接
  text = text.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener">$1</a>')
  return text
}

const loadArticle = async (id) => {
  loading.value = true
  try {
    const res = await articleApi.getById(id)
    if (res.code === 200) {
      article.value = res.data
      
      // 从后端检查用户点赞状态
      await checkLikeStatus(id)
      
      // 从后端检查用户收藏状态
      await checkCollectStatus(id)
      
      // 加载相关文章
      if (article.value.tags) {
        const tag = article.value.tags.split(',')[0]
        const relatedRes = await articleApi.search(tag.trim())
        relatedArticles.value = (relatedRes.data || [])
          .filter(a => a.id !== article.value.id)
          .slice(0, 4)
      }
    } else {
      article.value = null
    }
  } catch (error) {
    console.error('加载文章失败:', error)
    article.value = null
  } finally {
    loading.value = false
  }
}

// 检查用户点赞状态
const checkLikeStatus = async (articleId) => {
  const userInfo = localStorage.getItem('user_info')
  if (userInfo) {
    try {
      const res = await articleApi.getLikeStatus(articleId)
      if (res.code === 200) {
        hasLiked.value = res.data
      }
    } catch (error) {
      console.error('获取点赞状态失败:', error)
    }
  }
}

// 检查用户收藏状态
const checkCollectStatus = async (articleId) => {
  const userInfo = localStorage.getItem('user_info')
  if (userInfo) {
    try {
      const res = await articleApi.getCollectStatus(articleId)
      if (res.code === 200) {
        hasCollected.value = res.data
      }
    } catch (error) {
      console.error('获取收藏状态失败:', error)
    }
  }
}

// 处理收藏
const handleCollect = async () => {
  const userInfo = localStorage.getItem('user_info')
  if (!userInfo) {
    alert('请先登录后再收藏')
    return
  }

  const userId = JSON.parse(userInfo).id
  if (!userId) {
    alert('请先登录后再收藏')
    return
  }

  try {
    const res = await articleApi.collect(article.value.id)
    if (res.code === 200) {
      hasCollected.value = res.data.collected
      alert(res.data.collected ? '收藏成功' : '已取消收藏')
    } else {
      console.error('收藏失败:', res.message)
    }
  } catch (error) {
    console.error('收藏失败:', error)
  }
}

const handleLike = async () => {
  // 检查是否已登录
  const userInfo = localStorage.getItem('user_info')
  if (!userInfo) {
    alert('请先登录后再点赞')
    return
  }

  const userId = JSON.parse(userInfo).id
  if (!userId) {
    alert('请先登录后再点赞')
    return
  }

  try {
    const res = await articleApi.likeWithUser(article.value.id)
    if (res.code === 200) {
      hasLiked.value = res.data.liked
      article.value.likeCount = res.data.likeCount
    } else {
      console.error('点赞失败:', res.message)
    }
  } catch (error) {
    console.error('点赞失败:', error)
  }
}

const handleShare = () => {
  const url = window.location.href
  if (navigator.clipboard) {
    navigator.clipboard.writeText(url)
    alert('链接已复制到剪贴板')
  }
}

onMounted(() => {
  loadArticle(route.params.id)
})

watch(() => route.params.id, (newId) => {
  if (newId) loadArticle(newId)
})
</script>

<style scoped>
.article-detail-page {
  padding: 0;
}

/* 面包屑 */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 0;
  font-size: var(--text-sm);
  color: var(--color-ink-muted);
  border-bottom: 1px solid var(--color-border-light);
  margin-bottom: 24px;
}

.breadcrumb a {
  color: var(--color-ink-muted);
  transition: color 0.2s;
}

.breadcrumb a:hover {
  color: var(--color-cinnabar);
  text-decoration: none;
}

.breadcrumb .separator {
  color: var(--color-border-dark);
}

.breadcrumb .current {
  color: var(--color-ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
}

/* 加载状态 */
.loading-state {
  text-align: center;
  padding: 80px 20px;
}

.loading-icon {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-family: var(--font-serif);
  background: var(--color-ink);
  color: var(--color-white);
  margin: 0 auto 16px;
}

.loading-state p {
  color: var(--color-ink-muted);
}

/* 文章容器 */
.article-container {
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 32px;
}

/* 文章头部 */
.article-header {
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--color-border-light);
}

.article-title {
  font-size: 28px;
  font-family: var(--font-serif);
  font-weight: 600;
  color: var(--color-ink);
  line-height: 1.4;
  margin-bottom: 16px;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.meta-left {
  display: flex;
  gap: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: var(--text-sm);
  color: var(--color-ink-muted);
}

.meta-item .icon {
  font-size: var(--text-base);
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.article-tags .tag {
  padding: 4px 12px;
  font-size: var(--text-xs);
  background: var(--color-paper);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  color: var(--color-ink-light);
  transition: all 0.2s;
}

.article-tags .tag:hover {
  background: var(--color-cinnabar);
  border-color: var(--color-cinnabar);
  color: var(--color-white);
  text-decoration: none;
}

/* 文章摘要 */
.article-summary {
  padding: 16px 20px;
  background: var(--color-paper);
  border-left: 3px solid var(--color-cinnabar);
  border-radius: var(--radius-sm);
  margin-bottom: 24px;
}

.article-summary p {
  font-size: var(--text-md);
  color: var(--color-ink-light);
  line-height: 1.7;
  margin: 0;
}

/* 文章正文 */
.article-body {
  font-size: var(--text-md);
  line-height: 1.9;
  color: var(--color-ink);
  padding: 24px 0;
}

.article-body :deep(p) {
  margin-bottom: 16px;
}

.article-body :deep(strong) {
  font-weight: 600;
  color: var(--color-ink);
}

.article-body :deep(em) {
  font-style: italic;
  color: var(--color-ink-light);
}

.article-body :deep(code) {
  padding: 2px 6px;
  background: var(--color-paper);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-family: monospace;
  font-size: var(--text-sm);
}

/* Markdown 渲染样式 */
.article-body :deep(.article-p) {
  margin: 0 0 14px 0;
  line-height: 1.9;
  color: var(--color-ink);
}

.article-body :deep(.article-h1) {
  font-size: 24px;
  font-family: var(--font-serif);
  font-weight: 700;
  color: var(--color-ink);
  margin: 28px 0 14px 0;
  padding-bottom: 10px;
  border-bottom: 2px solid var(--color-cinnabar);
}

.article-body :deep(.article-h2) {
  font-size: 20px;
  font-family: var(--font-serif);
  font-weight: 600;
  color: var(--color-cinnabar);
  margin: 24px 0 12px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(196, 92, 72, 0.25);
}

.article-body :deep(.article-h3) {
  font-size: 17px;
  font-family: var(--font-serif);
  font-weight: 600;
  color: var(--color-ink);
  margin: 18px 0 10px 0;
}

.article-body :deep(.article-h4) {
  font-size: 15px;
  font-family: var(--font-serif);
  font-weight: 600;
  color: var(--color-ink-light);
  margin: 14px 0 8px 0;
}

.article-body :deep(.article-blockquote) {
  margin: 14px 0;
  padding: 12px 16px;
  background: rgba(196, 92, 72, 0.05);
  border-left: 4px solid var(--color-cinnabar);
  border-radius: 0 8px 8px 0;
  color: var(--color-ink-light);
  font-size: 14px;
  line-height: 1.7;
}

.article-body :deep(.article-ul) {
  margin: 10px 0 14px 0;
  padding-left: 8px;
  list-style: none;
}

.article-body :deep(.article-ul li) {
  position: relative;
  padding: 4px 0 4px 22px;
  line-height: 1.7;
  color: var(--color-ink);
}

.article-body :deep(.article-ul li)::before {
  content: '';
  position: absolute;
  left: 6px;
  top: 12px;
  width: 6px;
  height: 6px;
  background: var(--color-cinnabar);
  border-radius: 50%;
}

.article-body :deep(.article-hr) {
  margin: 20px 0;
  border: none;
  border-top: 1px dashed var(--color-border);
}

.article-body :deep(.article-code-block) {
  margin: 14px 0;
  padding: 16px;
  background: #1e1e1e;
  border-radius: 8px;
  overflow-x: auto;
}

.article-body :deep(.article-code-block code) {
  font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
  font-size: 13px;
  color: #d4d4d4;
  background: transparent;
  border: none;
  padding: 0;
  line-height: 1.6;
}

.article-body :deep(.article-inline-code) {
  padding: 2px 6px;
  background: rgba(196, 92, 72, 0.08);
  color: var(--color-cinnabar);
  border-radius: 4px;
  font-family: monospace;
  font-size: 13px;
  border: 1px solid rgba(196, 92, 72, 0.2);
}

.article-body :deep(a) {
  color: var(--color-cinnabar);
  text-decoration: underline;
  transition: color 0.2s;
}

.article-body :deep(a:hover) {
  color: var(--color-cinnabar-light);
}

/* 文章底部 */
.article-footer {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--color-border-light);
}

.action-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  font-size: var(--text-sm);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-white);
  color: var(--color-ink-light);
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  border-color: var(--color-cinnabar);
  color: var(--color-cinnabar);
}

.action-btn.like-btn.active {
  background: rgba(196, 92, 72, 0.1);
  border-color: var(--color-cinnabar);
  color: var(--color-cinnabar);
}

.action-btn.collect-btn.active {
  background: rgba(255, 193, 7, 0.1);
  border-color: #ffc107;
  color: #ffc107;
}

.action-btn .icon {
  font-size: var(--text-base);
}

/* 上下篇导航 */
.article-nav {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 32px;
}

.nav-placeholder {
  /* 占位 */
}

.nav-link {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 16px;
  background: var(--color-paper);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  text-decoration: none;
  transition: all 0.2s;
}

.nav-link:hover {
  border-color: var(--color-cinnabar);
  text-decoration: none;
}

.nav-link.next {
  text-align: right;
}

.nav-label {
  font-size: var(--text-xs);
  color: var(--color-ink-muted);
}

.nav-title {
  font-size: var(--text-sm);
  color: var(--color-ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 相关推荐 */
.related-articles {
  padding-top: 24px;
  border-top: 1px solid var(--color-border-light);
}

.section-title {
  font-size: var(--text-lg);
  font-family: var(--font-serif);
  color: var(--color-ink);
  margin-bottom: 16px;
}

.related-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.related-item {
  padding: 16px;
  background: var(--color-paper);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  text-decoration: none;
  transition: all 0.2s;
}

.related-item:hover {
  border-color: var(--color-cinnabar);
  text-decoration: none;
}

.related-title {
  font-size: var(--text-sm);
  color: var(--color-ink);
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.related-meta {
  display: flex;
  gap: 12px;
  font-size: var(--text-xs);
  color: var(--color-ink-muted);
}

/* 404 状态 */
.not-found {
  text-align: center;
  padding: 80px 20px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}

.not-found-icon {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  font-family: var(--font-serif);
  background: var(--color-paper);
  border: 1px solid var(--color-border);
  color: var(--color-ink-muted);
  margin: 0 auto 20px;
}

.not-found h2 {
  font-size: var(--text-xl);
  font-family: var(--font-serif);
  color: var(--color-ink);
  margin-bottom: 8px;
}

.not-found p {
  font-size: var(--text-sm);
  color: var(--color-ink-muted);
  margin-bottom: 24px;
}

.back-home {
  display: inline-flex;
  padding: 10px 24px;
  background: var(--color-cinnabar);
  color: var(--color-white);
  border-radius: var(--radius-sm);
  text-decoration: none;
  transition: background 0.2s;
}

.back-home:hover {
  background: var(--color-cinnabar-light);
  text-decoration: none;
}

/* 响应式 */
@media (max-width: 768px) {
  .article-detail-page {
    padding-bottom: 70px;
  }

  .article-container {
    padding: 16px;
    border-radius: 0;
    border-left: none;
    border-right: none;
  }

  .article-title {
    font-size: 20px;
    margin-bottom: 12px;
  }

  .article-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .meta-left {
    flex-wrap: wrap;
    gap: 12px;
  }

  .article-summary {
    padding: 12px 16px;
  }

  .article-body {
    padding: 16px 0;
    font-size: 14px;
  }

  .article-body :deep(.article-h2) {
    font-size: 17px;
  }

  .article-body :deep(.article-h3) {
    font-size: 15px;
  }

  .article-body :deep(.article-code-block) {
    margin: 12px -16px;
    border-radius: 0;
    padding: 12px 16px;
    font-size: 12px;
  }

  .article-body :deep(.article-blockquote) {
    margin: 12px -16px;
    border-radius: 0;
    padding: 10px 16px;
  }

  .action-bar {
    flex-wrap: wrap;
  }

  .action-btn {
    flex: 1;
    min-width: 100px;
    padding: 12px 16px;
    justify-content: center;
  }

  .article-nav {
    grid-template-columns: 1fr;
  }

  .nav-link {
    padding: 12px;
  }

  .related-list {
    grid-template-columns: 1fr;
  }

  .related-item {
    padding: 12px;
  }

  .related-meta {
    font-size: 10px;
  }

  .breadcrumb .current {
    max-width: 120px;
  }
}

/* 超小屏幕优化 */
@media (max-width: 375px) {
  .article-container {
    padding: 12px;
  }

  .article-title {
    font-size: 18px;
  }

  .meta-left {
    gap: 8px;
  }

  .meta-item {
    font-size: 11px;
  }

  .action-btn {
    min-width: 80px;
    padding: 10px 12px;
    font-size: 12px;
  }
}
</style>
