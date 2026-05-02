<template>
  <div class="category-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <img src="/big_logo.webp" alt="Logo" class="header-icon" />
      <div class="header-content">
        <h1 class="page-title">{{ categoryName }}</h1>
        <p class="page-desc">{{ categoryDescription }}</p>
      </div>
    </div>

    <!-- 筛选和排序 -->
    <div class="filter-bar">
      <div class="filter-left">
        <span class="article-count">共 {{ total }} 篇攻略</span>
      </div>
      <div class="filter-right">
        <select v-model="sortBy" class="sort-select">
          <option value="latest">最新发布</option>
          <option value="hot">最多阅读</option>
          <option value="like">最多点赞</option>
        </select>
      </div>
    </div>

    <!-- 文章列表 -->
    <div v-if="loading" class="loading-state">
      <img src="/big_logo.webp" alt="Loading" class="loading-icon" />
      <p>典籍载入中...</p>
    </div>

    <div v-else-if="articles.length > 0" class="article-grid">
      <router-link
        v-for="article in articles"
        :key="article.id"
        :to="getArticleLink(article)"
        class="article-card"
      >
        <div class="card-cover">
          <img v-if="article.coverImage" :src="article.coverImage" :alt="article.title" />
          <img v-else src="/big_logo.webp" alt="Cover" class="cover-placeholder" />
        </div>
        <div class="card-body">
          <h3 class="card-title">{{ article.title }}</h3>
          <p class="card-summary">{{ article.summary || '暂无简介' }}</p>
          <div class="card-footer">
            <div class="card-meta">
              <span class="meta-item">
                <i class="icon">👁</i>
                {{ article.viewCount || 0 }}
              </span>
              <span class="meta-item">
                <i class="icon">❤</i>
                {{ article.likeCount || 0 }}
              </span>
            </div>
            <span class="card-date">{{ formatDate(article.createdAt) }}</span>
          </div>
        </div>
      </router-link>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <img src="/big_logo.webp" alt="Empty" class="empty-icon" />
      <h3>暂无词条</h3>
      <p>该分类下暂无词条内容，敬请期待</p>
    </div>

    <!-- 分页 -->
    <div v-if="totalPages > 1" class="pagination">
      <button 
        class="page-btn" 
        :disabled="currentPage <= 1"
        @click="goToPage(currentPage - 1)"
      >
        上一页
      </button>
      <div class="page-info">
        <span class="current">{{ currentPage }}</span>
        <span class="separator">/</span>
        <span class="total">{{ totalPages }}</span>
      </div>
      <button 
        class="page-btn" 
        :disabled="currentPage >= totalPages"
        @click="goToPage(currentPage + 1)"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { articleApi, categoryApi } from '@/api/article'

const route = useRoute()
const router = useRouter()

const categoryName = ref('攻略分类')
const categoryDescription = ref('精选游戏攻略')
const articles = ref([])
const loading = ref(true)
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const sortBy = ref('latest')

// 分类图标映射
const iconMap = {
  1: '门', 2: '寺', 3: '宗', 4: '派',
  5: '装', 6: '武', 7: '副', 8: '任',
  9: '宠', 10: '坐'
}

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}-${date.getDate()}`
}

const loadCategory = async (categoryId) => {
  // 处理"全部"分类
  if (categoryId === 'all') {
    categoryName.value = '全部攻略'
    categoryDescription.value = '所有游戏攻略'
    return
  }
  
  try {
    const isNumeric = /^\d+$/.test(categoryId)
    const catRes = isNumeric
      ? await categoryApi.getById(categoryId)
      : await categoryApi.getByName(categoryId)

    if (catRes.code === 200 && catRes.data) {
      categoryName.value = catRes.data.name || categoryId
      categoryDescription.value = catRes.data.description || '精选游戏攻略'
    } else {
      categoryName.value = isNumeric ? '攻略分类' : categoryId
      categoryDescription.value = '精选游戏攻略'
    }
  } catch (error) {
    console.error('加载分类信息失败:', error)
    categoryName.value = /^\d+$/.test(categoryId) ? '攻略分类' : categoryId
  }
}

const loadArticles = async (categoryId) => {
  loading.value = true
  try {
    // 处理"全部"分类 - 不传分类ID
    if (categoryId === 'all') {
      const params = {
        page: currentPage.value,
        size: pageSize.value
      }
      const res = await articleApi.getList(params)
      if (res.code === 200) {
        articles.value = res.data || []
        total.value = res.total || 0
      } else {
        articles.value = []
        total.value = 0
      }
      loading.value = false
      return
    }

    let realId = categoryId

    // 如果是中文名，先转成数字 ID
    if (!/^\d+$/.test(categoryId)) {
      try {
        const catRes = await categoryApi.getByName(categoryId)
        if (catRes.code === 200 && catRes.data) {
          realId = catRes.data.id
        }
      } catch (e) { /* 找不到分类，用原参数 */ }
    }

    const params = {
      categoryId: realId,
      page: currentPage.value,
      size: pageSize.value
    }

    const res = await articleApi.getList(params)
    if (res.code === 200) {
      articles.value = res.data || []
      total.value = res.total || 0
    } else {
      articles.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('加载文章列表失败:', error)
    articles.value = []
  } finally {
    loading.value = false
  }
}

const loadData = async (categoryId) => {
  await Promise.all([
    loadCategory(categoryId),
    loadArticles(categoryId)
  ])
}

const goToPage = (page) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  loadArticles(route.params.id)
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const getArticleLink = (article) => {
  return `/article/${article.id}`
}

onMounted(() => {
  // 从 URL query 参数读取排序方式
  if (route.query.sort) {
    sortBy.value = route.query.sort
  }
  loadData(route.params.id)
})

watch(() => route.params.id, (newId) => {
  if (newId) {
    currentPage.value = 1
    loadData(newId)
  }
})

watch(() => route.query.sort, (newSort) => {
  if (newSort) {
    sortBy.value = newSort
  }
})

watch(sortBy, () => {
  loadArticles(route.params.id)
})
</script>

<style scoped>
.category-page {
  padding: 0;
}

/* 页面标题 */
.page-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  margin-bottom: 20px;
}

.header-icon {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  flex-shrink: 0;
}

.header-icon img {
  width: 60px;
  height: 60px;
  object-fit: contain;
}

.header-content {
  flex: 1;
}

.page-title {
  font-size: 24px;
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

/* 筛选栏 */
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  margin-bottom: 16px;
  border-bottom: 1px solid var(--color-border-light);
}

.article-count {
  font-size: var(--text-sm);
  color: var(--color-ink-muted);
}

.sort-select {
  padding: 6px 12px;
  font-size: var(--text-sm);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-white);
  color: var(--color-ink);
  cursor: pointer;
}

.sort-select:focus {
  outline: none;
  border-color: var(--color-ochre);
}

/* 加载状态 */
.loading-state {
  text-align: center;
  padding: 80px 20px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}

.loading-icon {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.loading-icon img {
  width: 60px;
  height: 60px;
  object-fit: contain;
}

.loading-state p {
  color: var(--color-ink-muted);
}

/* 文章卡片网格 */
.article-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}

.article-card {
  display: flex;
  flex-direction: column;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  overflow: hidden;
  text-decoration: none;
  transition: all 0.2s ease;
}

.article-card:hover {
  border-color: var(--color-cinnabar);
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
  text-decoration: none;
}

.card-cover {
  height: 160px;
  background: var(--color-paper);
  overflow: hidden;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.article-card:hover .card-cover img {
  transform: scale(1.05);
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-placeholder img {
  width: 80px;
  height: 80px;
  object-fit: contain;
}

.card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 16px;
}

.card-title {
  font-size: var(--text-md);
  font-family: var(--font-serif);
  font-weight: 600;
  color: var(--color-ink);
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-summary {
  flex: 1;
  font-size: var(--text-sm);
  color: var(--color-ink-muted);
  line-height: 1.5;
  margin-bottom: 12px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid var(--color-border-light);
}

.card-meta {
  display: flex;
  gap: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: var(--text-xs);
  color: var(--color-ink-muted);
}

.meta-item .icon {
  font-size: var(--text-sm);
}

.card-date {
  font-size: var(--text-xs);
  color: var(--color-ink-muted);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}

.empty-icon {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
}

.empty-icon img {
  width: 76px;
  height: 76px;
  object-fit: contain;
}

.empty-state h3 {
  font-size: var(--text-xl);
  font-family: var(--font-serif);
  color: var(--color-ink);
  margin-bottom: 8px;
}

.empty-state p {
  font-size: var(--text-sm);
  color: var(--color-ink-muted);
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--color-border-light);
}

.page-btn {
  padding: 8px 16px;
  font-size: var(--text-sm);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-white);
  color: var(--color-ink);
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  border-color: var(--color-cinnabar);
  color: var(--color-cinnabar);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: var(--text-sm);
  color: var(--color-ink-muted);
}

.page-info .current {
  color: var(--color-cinnabar);
  font-weight: 600;
}

/* 响应式 */
@media (max-width: 768px) {
  .category-page {
    padding-bottom: 70px;
  }

  .page-header {
    flex-direction: column;
    text-align: center;
    padding: 16px;
    gap: 8px;
  }

  .category-title {
    font-size: 20px;
  }

  .category-desc {
    font-size: 12px;
  }

  .filter-bar {
    padding: 12px;
    overflow-x: auto;
  }

  .filter-btn {
    padding: 6px 12px;
    font-size: 12px;
    white-space: nowrap;
  }

  .article-grid {
    grid-template-columns: 1fr;
    gap: 12px;
    padding: 12px;
  }

  .article-card {
    display: flex;
    flex-direction: row;
  }

  .card-cover {
    width: 90px;
    min-width: 90px;
    aspect-ratio: 1;
  }

  .card-body {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    padding: 10px;
  }

  .card-title {
    font-size: 14px;
    white-space: normal;
    display: -webkit-box;
    -webkit-line-clamp: 2;
  }

  .card-summary {
    display: none;
  }

  .pagination {
    padding: 16px;
  }

  .page-info {
    font-size: 12px;
  }

  .pagination-btn {
    padding: 6px 12px;
    font-size: 12px;
  }
}
</style>
