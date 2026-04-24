<template>
  <div class="search-page">
    <!-- 搜索框 -->
    <div class="search-header">
      <div class="search-box">
        <i class="search-icon">🔍</i>
        <input 
          v-model="keyword"
          type="text" 
          placeholder="搜索攻略、门派、副本..."
          class="search-input"
          @keyup.enter="handleSearch"
        />
        <button class="search-btn" @click="handleSearch">搜索</button>
      </div>
    </div>

    <!-- 搜索结果统计 -->
    <div v-if="hasSearched" class="result-meta">
      <span v-if="loading">搜索中...</span>
      <span v-else>找到 <strong>{{ results.length }}</strong> 篇相关攻略</span>
    </div>

    <!-- 热门标签 -->
    <div v-if="!hasSearched" class="hot-tags">
      <h3 class="section-title">热门搜索</h3>
      <div class="tags-list">
        <button 
          v-for="tag in hotTags" 
          :key="tag"
          class="hot-tag"
          @click="searchTag(tag)"
        >
          {{ tag }}
        </button>
      </div>
    </div>

    <!-- 搜索结果列表 -->
    <div v-if="loading" class="loading-state">
      <div class="loading-icon">典</div>
      <p>典籍检索中...</p>
    </div>

    <div v-else-if="hasSearched && results.length > 0" class="results-list">
      <router-link 
        v-for="article in results" 
        :key="article.id"
        :to="`/article/${article.id}`"
        class="result-item"
      >
        <div class="result-content">
          <h3 class="result-title">{{ article.title }}</h3>
          <p class="result-summary">{{ article.summary || '暂无简介' }}</p>
          <div class="result-tags" v-if="article.tags">
            <span v-for="tag in article.tags.split(',').slice(0, 3)" :key="tag" class="tag">
              {{ tag.trim() }}
            </span>
          </div>
        </div>
        <div class="result-meta">
          <div class="meta-stats">
            <span class="stat-item">
              <i class="icon">👁</i>
              {{ article.viewCount || 0 }}
            </span>
            <span class="stat-item">
              <i class="icon">❤</i>
              {{ article.likeCount || 0 }}
            </span>
          </div>
          <span class="result-date">{{ formatDate(article.createdAt) }}</span>
        </div>
      </router-link>
    </div>

    <!-- 无结果状态 -->
    <div v-else-if="hasSearched && !loading && results.length === 0" class="empty-state">
      <div class="empty-icon">典</div>
      <h3>未寻得典籍</h3>
      <p>没有找到与「{{ keyword }}」相关的攻略</p>
      <p class="suggestion">尝试更换关键词，或浏览分类目录</p>
      <div class="suggestion-tags">
        <router-link v-for="tag in hotTags.slice(0, 4)" :key="tag" :to="`/search?keyword=${tag}`" class="suggestion-tag">
          {{ tag }}
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { articleApi } from '@/api/article'

const route = useRoute()
const router = useRouter()

const keyword = ref('')
const results = ref([])
const loading = ref(false)
const hasSearched = ref(false)

const hotTags = [
  '青云门', '天音寺', '云梦宗', '新手攻略', 
  '副本', '装备强化', 'PVP', '加点'
]

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const search = async (kw) => {
  if (!kw?.trim()) return
  
  loading.value = true
  hasSearched.value = true
  keyword.value = kw.trim()
  
  try {
    const res = await articleApi.search(kw.trim())
    if (res.code === 200) {
      results.value = res.data || []
    } else {
      results.value = []
    }
  } catch (error) {
    console.error('搜索失败:', error)
    results.value = []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  if (keyword.value.trim()) {
    router.push({ path: '/search', query: { keyword: keyword.value.trim() } })
  }
}

const searchTag = (tag) => {
  router.push({ path: '/search', query: { keyword: tag } })
}

onMounted(() => {
  const queryKeyword = route.query.keyword
  if (queryKeyword) {
    search(queryKeyword)
  }
})

watch(() => route.query.keyword, (newKeyword) => {
  if (newKeyword) {
    search(newKeyword)
  } else {
    hasSearched.value = false
    results.value = []
  }
})
</script>

<style scoped>
.search-page {
  padding: 0;
}

/* 搜索框 */
.search-header {
  margin-bottom: 24px;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 8px 8px 16px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  transition: border-color 0.2s;
}

.search-box:focus-within {
  border-color: var(--color-cinnabar);
}

.search-icon {
  font-size: var(--text-lg);
  opacity: 0.6;
}

.search-input {
  flex: 1;
  padding: 8px 0;
  font-size: var(--text-md);
  border: none;
  background: transparent;
  color: var(--color-ink);
}

.search-input:focus {
  outline: none;
}

.search-input::placeholder {
  color: var(--color-ink-muted);
}

.search-btn {
  padding: 10px 24px;
  font-size: var(--text-sm);
  font-weight: 500;
  background: var(--color-cinnabar);
  color: var(--color-white);
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background 0.2s;
}

.search-btn:hover {
  background: var(--color-cinnabar-light);
}

/* 结果统计 */
.result-meta {
  padding: 12px 0;
  margin-bottom: 16px;
  font-size: var(--text-sm);
  color: var(--color-ink-muted);
  border-bottom: 1px solid var(--color-border-light);
}

.result-meta strong {
  color: var(--color-cinnabar);
  font-weight: 600;
}

/* 热门标签 */
.hot-tags {
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
  margin-bottom: 16px;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.hot-tag {
  padding: 8px 16px;
  font-size: var(--text-sm);
  background: var(--color-paper);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  color: var(--color-ink-light);
  cursor: pointer;
  transition: all 0.2s;
}

.hot-tag:hover {
  background: var(--color-cinnabar);
  border-color: var(--color-cinnabar);
  color: var(--color-white);
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
  font-size: 32px;
  font-family: var(--font-serif);
  background: var(--color-ink);
  color: var(--color-white);
  margin: 0 auto 16px;
}

.loading-state p {
  color: var(--color-ink-muted);
}

/* 结果列表 */
.results-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.result-item {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 20px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  text-decoration: none;
  transition: all 0.2s;
}

.result-item:hover {
  border-color: var(--color-cinnabar);
  box-shadow: var(--shadow-sm);
  text-decoration: none;
}

.result-content {
  flex: 1;
  min-width: 0;
}

.result-title {
  font-size: var(--text-md);
  font-family: var(--font-serif);
  font-weight: 600;
  color: var(--color-ink);
  margin-bottom: 8px;
  transition: color 0.2s;
}

.result-item:hover .result-title {
  color: var(--color-cinnabar);
}

.result-summary {
  font-size: var(--text-sm);
  color: var(--color-ink-muted);
  line-height: 1.6;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.result-tags {
  display: flex;
  gap: 8px;
}

.tag {
  padding: 2px 8px;
  font-size: var(--text-xs);
  background: var(--color-paper);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  color: var(--color-ink-muted);
}

.result-meta {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: flex-end;
  flex-shrink: 0;
}

.meta-stats {
  display: flex;
  gap: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: var(--text-xs);
  color: var(--color-ink-muted);
}

.stat-item .icon {
  font-size: var(--text-sm);
}

.result-date {
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
  font-size: 40px;
  font-family: var(--font-serif);
  background: var(--color-paper);
  border: 1px solid var(--color-border);
  color: var(--color-ink-muted);
  margin: 0 auto 20px;
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
  margin-bottom: 8px;
}

.suggestion {
  margin-top: 16px;
  margin-bottom: 8px;
}

.suggestion-tags {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
}

.suggestion-tag {
  padding: 6px 12px;
  font-size: var(--text-sm);
  background: var(--color-paper);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  color: var(--color-ink-light);
  text-decoration: none;
  transition: all 0.2s;
}

.suggestion-tag:hover {
  background: var(--color-cinnabar);
  border-color: var(--color-cinnabar);
  color: var(--color-white);
  text-decoration: none;
}

/* 响应式 */
@media (max-width: 768px) {
  .result-item {
    flex-direction: column;
    gap: 12px;
  }

  .result-meta {
    flex-direction: row;
    align-items: center;
  }
}
</style>
