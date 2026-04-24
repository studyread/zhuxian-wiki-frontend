<template>
  <div class="article-detail-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb">
      <router-link to="/">首页</router-link>
      <span class="separator">/</span>
      <router-link v-if="article.categoryId" :to="`/category/${article.categoryId}`">
        {{ article.categoryName || '攻略' }}
      </router-link>
      <span class="separator">/</span>
      <span class="current">{{ article.title }}</span>
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
  // 简单的 Markdown 渲染
  return article.value.content
    .replace(/\n\n/g, '</p><p>')
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/`(.*?)`/g, '<code>$1</code>')
    .replace(/^/, '<p>')
    .replace(/$/, '</p>')
})

const loadArticle = async (id) => {
  loading.value = true
  try {
    const res = await articleApi.getById(id)
    if (res.code === 200) {
      article.value = res.data
      
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

const handleLike = () => {
  hasLiked.value = !hasLiked.value
  if (article.value) {
    article.value.likeCount = (article.value.likeCount || 0) + (hasLiked.value ? 1 : -1)
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
  .article-container {
    padding: 20px;
  }

  .article-title {
    font-size: 22px;
  }

  .article-nav {
    grid-template-columns: 1fr;
  }

  .related-list {
    grid-template-columns: 1fr;
  }
}
</style>
