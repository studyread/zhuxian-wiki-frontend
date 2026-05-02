<template>
  <div class="home-page">
    <!-- Hero 区域 -->
    <section class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">诛仙世界 Wiki</h1>
        <p class="hero-desc">最全面的游戏攻略百科全书</p>
      </div>
    </section>

    <!-- 主内容区域 - 固定宽度居中 -->
    <div class="main-wrapper">
      <main class="main-content">
        <!-- 站点统计 -->
        <div class="stats-bar">
          <div class="stat-item">
            <span class="stat-value">{{ formatNumber(displayStats.userCount) }}</span>
            <span class="stat-label">用户数量</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ formatNumber(displayStats.knowledgeCount) }}</span>
            <span class="stat-label">知识词条</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ formatNumber(displayStats.categoryCount) }}</span>
            <span class="stat-label">内容分类</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ formatNumber(displayStats.totalViews) }}</span>
            <span class="stat-label">总浏览量</span>
          </div>
        </div>

        <!-- 热门攻略 -->
        <section class="content-section">
          <div class="section-header">
            <h2 class="section-title">热门攻略</h2>
            <router-link to="/category/all?sort=hot" class="section-more">查看全部</router-link>
          </div>
          <div class="article-grid">
            <article
              v-for="article in displayHotArticles"
              :key="article.id"
              class="article-card"
              @click="$router.push(`/article/${article.id}`)"
            >
              <div class="card-cover">
                <img :src="getCoverImage(article)" :alt="article.title" />
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
            <router-link to="/category/all?sort=latest" class="section-more">查看全部</router-link>
          </div>
          <div class="article-list">
            <article
              v-for="article in displayLatestArticles"
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
      </main>
    </div>

    <!-- 移动端底部导航 -->
    <nav class="mobile-bottom-nav">
      <router-link to="/" class="bottom-nav-item">
        <span class="bottom-nav-icon">🏠</span>
        <span class="bottom-nav-label">首页</span>
      </router-link>
      <router-link to="/search" class="bottom-nav-item">
        <span class="bottom-nav-icon">🔍</span>
        <span class="bottom-nav-label">搜索</span>
      </router-link>
      <router-link to="/ai" class="bottom-nav-item">
        <span class="bottom-nav-icon">🤖</span>
        <span class="bottom-nav-label">AI助手</span>
      </router-link>
      <router-link to="/user" class="bottom-nav-item">
        <span class="bottom-nav-icon">👤</span>
        <span class="bottom-nav-label">我的</span>
      </router-link>
    </nav>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { articleApi, statsApi } from '@/api/article'
import { formatDate } from '@/utils/format'

const hotArticles = ref([])
const latestArticles = ref([])

const displayHotArticles = computed(() => hotArticles.value.slice(0, 4))
const displayLatestArticles = computed(() => latestArticles.value.slice(0, 5))

// 站点统计数据（动画显示用）
const displayStats = ref({
  articleCount: 0,
  knowledgeCount: 0,
  categoryCount: 0,
  totalViews: 0
})

// 数字滚动动画函数
const animateNumber = (key, target, duration = 1500) => {
  const start = 0
  const startTime = performance.now()

  const updateNumber = (currentTime) => {
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / duration, 1)
    // easeOutQuart 缓动
    const easeProgress = 1 - Math.pow(1 - progress, 4)
    const current = Math.floor(start + (target - start) * easeProgress)

    displayStats.value[key] = current

    if (progress < 1) {
      requestAnimationFrame(updateNumber)
    } else {
      displayStats.value[key] = target
    }
  }

  requestAnimationFrame(updateNumber)
}

const formatNumber = (num) => {
  if (!num) return 0
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toLocaleString()
}

// 获取封面图片，使用固定默认图避免随机刷新
const getCoverImage = (article) => {
  if (article.coverImage) {
    return article.coverImage
  }
  // 使用固定的游戏风格默认图片（使用 Picsum 的固定种子）
  return `https://picsum.photos/seed/${article.id || 'default'}/400/240`
}

onMounted(async () => {
  try {
    // 分别请求，便于定位问题
    let hotRes, latestRes, statsRes

    try {
      hotRes = await articleApi.getHot()
      console.log('热门文章:', hotRes)
    } catch (e) {
      console.error('获取热门文章失败:', e)
    }

    try {
      latestRes = await articleApi.getLatest()
      console.log('最新文章:', latestRes)
    } catch (e) {
      console.error('获取最新文章失败:', e)
    }

    try {
      statsRes = await statsApi.getStatistics()
      console.log('统计数据:', statsRes)
    } catch (e) {
      console.error('获取统计数据失败:', e)
    }

    hotArticles.value = hotRes?.data || []
    latestArticles.value = latestRes?.data || []

    // 启动站点统计数字动画
    if (statsRes?.code === 200) {
      const stats = statsRes.data || {}
      setTimeout(() => {
        animateNumber('userCount', stats.userCount || 0)
        animateNumber('knowledgeCount', stats.knowledgeCount || 0)
        animateNumber('categoryCount', stats.categoryCount || 0)
        animateNumber('totalViews', stats.totalViews || 0)
      }, 300)
    }
  } catch (error) {
    console.error('获取数据失败:', error)
  }
})
</script>

<style scoped>
.home-page {
  padding: 0;
  padding-bottom: 70px; /* 为底部导航留出空间 */
}

/* 主内容区域 - 固定宽度居中 */
.main-wrapper {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 主内容 */
.main-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 站点统计条 */
.stats-bar {
  display: flex;
  justify-content: space-around;
  align-items: center;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 20px 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 16px;
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  font-family: var(--font-serif);
  color: var(--color-cinnabar);
  line-height: 1.2;
}

.stat-label {
  font-size: 11px;
  color: var(--color-ink-muted);
  margin-top: 4px;
}

/* Hero 区域 */
.hero-section {
  padding: 40px 0;
  text-align: center;
  background: var(--color-cream);
  border-bottom: 1px solid var(--color-border);
  margin-bottom: 24px;
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
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 16px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--color-border-light);
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  font-family: var(--font-serif);
  color: var(--color-ink);
}

.section-more {
  font-size: 12px;
  color: var(--color-ink-muted);
}

.section-more:hover {
  color: var(--color-cinnabar);
}

/* 文章网格 */
.article-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.article-card {
  background: var(--color-paper);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  overflow: hidden;
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.article-card:hover {
  border-color: var(--color-ochre);
  box-shadow: var(--shadow-md);
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
  padding: 10px 12px;
}

.card-title {
  font-size: 13px;
  font-weight: 600;
  font-family: var(--font-serif);
  color: var(--color-ink);
  margin-bottom: 4px;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-summary {
  font-size: 11px;
  color: var(--color-ink-light);
  line-height: 1.4;
  margin-bottom: 8px;
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
  font-size: 10px;
  padding: 2px 6px;
  background: rgba(196, 92, 72, 0.1);
  color: var(--color-cinnabar);
  border: 1px solid rgba(196, 92, 72, 0.2);
  border-radius: var(--radius-sm);
}

.card-views {
  font-size: 10px;
  color: var(--color-ink-muted);
}

/* 文章列表 */
.article-list {
  background: var(--color-paper);
  border-radius: var(--radius-sm);
}

.article-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border-bottom: 1px solid var(--color-border-light);
  cursor: pointer;
  transition: background 0.2s;
}

.article-item:last-child {
  border-bottom: none;
}

.article-item:hover {
  background: var(--color-cream);
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-ink);
  margin-bottom: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-summary {
  font-size: 11px;
  color: var(--color-ink-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-date {
  font-size: 11px;
  color: var(--color-ink-muted);
  flex-shrink: 0;
  margin-left: 12px;
}

/* ========== 移动端底部导航栏 ========== */
.mobile-bottom-nav {
  display: none;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 56px;
  background: var(--color-white);
  border-top: 1px solid var(--color-border);
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  z-index: 100;
  padding: 0 8px;
}

.bottom-nav-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  color: var(--color-ink-muted);
  gap: 2px;
  padding: 6px 0;
  transition: color 0.2s;
}

.bottom-nav-item.router-link-active {
  color: var(--color-cinnabar);
}

.bottom-nav-icon {
  font-size: 20px;
  line-height: 1;
}

.bottom-nav-label {
  font-size: 10px;
}

/* ========== 响应式 ========== */
@media (max-width: 768px) {
  .main-wrapper {
    padding: 0 12px;
  }

  /* Hero 区域优化 */
  .hero-section {
    padding: 24px 0;
    margin-bottom: 16px;
  }

  .hero-title {
    font-size: 22px;
  }

  .hero-desc {
    font-size: 13px;
  }

  /* 统计栏移动端优化 */
  .stats-bar {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
    padding: 12px;
  }

  .stat-item {
    padding: 8px;
  }

  .stat-value {
    font-size: 18px;
  }

  .stat-label {
    font-size: 10px;
  }

  /* 文章网格移动端单列 */
  .article-grid {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .article-card {
    display: flex;
    flex-direction: row;
  }

  .card-cover {
    width: 100px;
    min-width: 100px;
    aspect-ratio: 1;
  }

  .card-body {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
  }

  .card-title {
    white-space: normal;
    display: -webkit-box;
    -webkit-line-clamp: 2;
  }

  .card-summary {
    display: none;
  }

  /* 最新攻略列表优化 */
  .article-item {
    padding: 12px 10px;
  }

  .item-title {
    font-size: 14px;
  }

  .item-summary {
    display: none;
  }

  .item-date {
    font-size: 10px;
  }

  /* 显示底部导航 */
  .mobile-bottom-nav {
    display: flex;
  }
}

/* 超小屏幕优化 */
@media (max-width: 375px) {
  .hero-section {
    padding: 20px 0;
  }

  .hero-title {
    font-size: 20px;
  }

  .stats-bar {
    gap: 8px;
    padding: 10px;
  }

  .stat-value {
    font-size: 16px;
  }

  .card-cover {
    width: 80px;
    min-width: 80px;
  }

  .mobile-bottom-nav {
    height: 50px;
  }

  .bottom-nav-icon {
    font-size: 18px;
  }

  .bottom-nav-label {
    font-size: 9px;
  }
}
</style>
