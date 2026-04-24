<template>
  <div class="home-page">
    <!-- Hero 区域 -->
    <section class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">诛仙世界 Wiki</h1>
        <p class="hero-desc">最全面的游戏攻略百科全书</p>
      </div>
    </section>

    <!-- 热门攻略 -->
    <section class="content-section">
      <div class="section-header">
        <h2 class="section-title">热门攻略</h2>
        <router-link to="/" class="section-more">查看全部</router-link>
      </div>
      <div class="article-grid">
        <article
          v-for="article in hotArticles"
          :key="article.id"
          class="article-card"
          @click="$router.push(`/article/${article.id}`)"
        >
          <div class="card-cover">
            <img :src="article.coverImage || 'https://picsum.photos/400/240'" :alt="article.title" />
          </div>
          <div class="card-body">
            <h3 class="card-title">{{ article.title }}</h3>
            <p class="card-summary">{{ article.summary }}</p>
            <div class="card-footer">
              <span class="card-tag">攻略</span>
              <span class="card-views">{{ article.viewCount }} 阅读</span>
            </div>
          </div>
        </article>
      </div>
    </section>

    <!-- 最新更新 -->
    <section class="content-section">
      <div class="section-header">
        <h2 class="section-title">最新攻略</h2>
        <router-link to="/" class="section-more">查看全部</router-link>
      </div>
      <div class="article-list">
        <article
          v-for="article in latestArticles"
          :key="article.id"
          class="article-item"
          @click="$router.push(`/article/${article.id}`)"
        >
          <div class="item-info">
            <h4 class="item-title">{{ article.title }}</h4>
            <p class="item-summary">{{ article.summary }}</p>
          </div>
          <span class="item-date">{{ formatDate(article.createdAt) }}</span>
        </article>
      </div>
    </section>

    <!-- 分类导航 -->
    <section class="content-section">
      <div class="section-header">
        <h2 class="section-title">攻略分类</h2>
      </div>
      <div class="category-grid">
        <router-link
          v-for="cat in categories"
          :key="cat.path"
          :to="cat.path"
          class="category-card"
        >
          <span class="cat-icon">{{ cat.icon }}</span>
          <span class="cat-name">{{ cat.name }}</span>
          <span class="cat-count">{{ cat.count }} 篇</span>
        </router-link>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { articleApi } from '@/api/article'
import { formatDate } from '@/utils/format'

const hotArticles = ref([])
const latestArticles = ref([])

const categories = ref([
  { name: '门派', icon: '门', path: '/category/门派', count: 24 },
  { name: '职业', icon: '职', path: '/category/职业', count: 18 },
  { name: '副本', icon: '副', path: '/category/副本', count: 32 },
  { name: '攻略', icon: '攻', path: '/category/攻略', count: 56 },
  { name: '装备', icon: '装', path: '/category/装备', count: 45 },
  { name: '宠物', icon: '宠', path: '/category/宠物', count: 21 },
  { name: '坐骑', icon: '骑', path: '/category/坐骑', count: 15 },
  { name: '任务', icon: '任', path: '/category/任务', count: 38 },
])

onMounted(async () => {
  try {
    const [hotRes, latestRes] = await Promise.all([
      articleApi.getHot(),
      articleApi.getLatest()
    ])
    hotArticles.value = hotRes.data || []
    latestArticles.value = latestRes.data || []
  } catch (error) {
    console.error('获取文章失败:', error)
  }
})
</script>

<style scoped>
.home-page {
  padding: 0;
}

/* Hero 区域 */
.hero-section {
  padding: 40px 0;
  text-align: center;
  background: var(--color-cream);
  border-bottom: 1px solid var(--color-border);
  margin-bottom: 32px;
}

.hero-content {
  max-width: 600px;
  margin: 0 auto;
  padding: 0 20px;
}

.hero-title {
  font-size: 28px;
  font-weight: 600;
  font-family: var(--font-serif);
  color: var(--color-ink);
  margin-bottom: 8px;
}

.hero-desc {
  font-size: 14px;
  color: var(--color-ink-light);
}

/* 内容区域 */
.content-section {
  margin-bottom: 32px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--color-border);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  font-family: var(--font-serif);
  color: var(--color-ink);
}

.section-more {
  font-size: 12px;
  color: var(--color-ink-muted);
  
  &:hover {
    color: var(--color-cinnabar);
  }
}

/* 文章网格 */
.article-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.article-card {
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  overflow: hidden;
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s;
  
  &:hover {
    border-color: var(--color-ochre);
    box-shadow: var(--shadow-md);
  }
}

.card-cover {
  aspect-ratio: 16/10;
  overflow: hidden;
  background: var(--color-paper-dark);
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

.card-body {
  padding: 12px 14px;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  font-family: var(--font-serif);
  color: var(--color-ink);
  margin-bottom: 6px;
  line-height: 1.4;
}

.card-summary {
  font-size: 12px;
  color: var(--color-ink-light);
  line-height: 1.5;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-tag {
  font-size: 11px;
  padding: 2px 8px;
  background: rgba(196, 92, 72, 0.1);
  color: var(--color-cinnabar);
  border: 1px solid rgba(196, 92, 72, 0.2);
  border-radius: var(--radius-sm);
}

.card-views {
  font-size: 11px;
  color: var(--color-ink-muted);
}

/* 文章列表 */
.article-list {
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.article-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid var(--color-border-light);
  cursor: pointer;
  transition: background 0.2s;
  
  &:last-child {
    border-bottom: none;
  }
  
  &:hover {
    background: var(--color-cream);
  }
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-ink);
  margin-bottom: 4px;
}

.item-summary {
  font-size: 12px;
  color: var(--color-ink-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 400px;
}

.item-date {
  font-size: 12px;
  color: var(--color-ink-muted);
  flex-shrink: 0;
  margin-left: 16px;
}

/* 分类网格 */
.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
}

.category-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px 12px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  text-decoration: none;
  transition: all 0.2s;
  
  &:hover {
    border-color: var(--color-ochre);
    background: var(--color-cream);
    text-decoration: none;
  }
}

.cat-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-family: var(--font-serif);
  background: var(--color-ink);
  color: var(--color-white);
  border-radius: var(--radius-sm);
}

.cat-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-ink);
}

.cat-count {
  font-size: 11px;
  color: var(--color-ink-muted);
}

/* 响应式 */
@media (max-width: 768px) {
  .article-grid {
    grid-template-columns: 1fr;
  }
  
  .item-summary {
    display: none;
  }
}
</style>
