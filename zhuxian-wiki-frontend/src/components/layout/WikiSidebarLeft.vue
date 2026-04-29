<template>
  <aside class="sidebar-left">
    <!-- 分类导航 -->
    <div class="sidebar-section">
      <h3 class="sidebar-title">导航分类</h3>
      <nav class="category-nav">
        <router-link
          v-for="cat in categories"
          :key="cat.path"
          :to="cat.path"
          class="category-link"
          :class="{ active: isActiveCategory(cat.path) }"
        >
          <span class="category-icon">{{ cat.icon }}</span>
          <span class="category-name">{{ cat.name }}</span>
        </router-link>
      </nav>
    </div>

    <!-- 热门标签 -->
    <div class="sidebar-section">
      <h3 class="sidebar-title">热门标签</h3>
      <div class="tag-list">
        <router-link
          v-for="tag in hotTags"
          :key="tag"
          :to="`/search?keyword=${tag}`"
          class="sidebar-tag"
        >
          {{ tag }}
        </router-link>
      </div>
    </div>

    <!-- 最近更新 -->
    <div class="sidebar-section">
      <h3 class="sidebar-title">最近更新</h3>
      <ul class="recent-list">
        <li v-for="item in recentArticles" :key="item.id" class="recent-item">
          <router-link :to="`/article/${item.id}`" class="recent-link">
            {{ item.title }}
          </router-link>
          <span class="recent-time">{{ item.time }}</span>
        </li>
      </ul>
    </div>
  </aside>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { articleApi } from '@/api/article'

const route = useRoute()

const categories = ref([
  { name: '全部', path: '/', icon: '全' },
  { name: '门派', path: '/category/1', icon: '门' },
  { name: '职业', path: '/category/2', icon: '职' },
  { name: '攻略', path: '/category/3', icon: '攻' },
  { name: '副本', path: '/category/4', icon: '副' },
  { name: '装备', path: '/category/5', icon: '装' },
  { name: '宠物', path: '/category/6', icon: '宠' },
  { name: '坐骑', path: '/category/7', icon: '骑' },
])

const hotTags = ref(['新手入门', '升级攻略', '门派推荐', 'PVP', '副本攻略', '装备打造'])

const recentArticles = ref([])

// 格式化时间
const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

// 加载最近更新
const loadRecentArticles = async () => {
  try {
    const res = await articleApi.getLatest()
    if (res.code === 200 && res.data) {
      recentArticles.value = res.data.slice(0, 5).map(article => ({
        id: article.id,
        title: article.title,
        time: formatTime(article.createdAt || article.updatedAt)
      }))
    }
  } catch (error) {
    console.error('加载最近更新失败:', error)
  }
}

const isActiveCategory = (path) => {
  if (path === '/') {
    return route.path === '/' && !route.query.category
  }
  return route.path === path || route.query.category === path.split('/')[2]
}

onMounted(() => {
  loadRecentArticles()
})
</script>

<style scoped>
.sidebar-left {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.sidebar-section {
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 16px;
}

.sidebar-title {
  font-size: 14px;
  font-weight: 600;
  font-family: var(--font-serif);
  color: var(--color-ink);
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--color-border-light);
}

/* 分类导航 */
.category-nav {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.category-link {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  color: var(--color-ink-light);
  text-decoration: none;
  border-radius: var(--radius-sm);
  transition: all 0.2s;
  
  &:hover {
    background: var(--color-paper-dark);
    color: var(--color-ink);
    text-decoration: none;
  }
  
  &.active {
    background: var(--color-ink);
    color: var(--color-white);
    
    .category-icon {
      background: var(--color-white);
      color: var(--color-ink);
    }
  }
}

.category-icon {
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-family: var(--font-serif);
  background: var(--color-paper-dark);
  border-radius: var(--radius-sm);
  color: var(--color-ink-light);
}

.category-name {
  font-size: 13px;
}

/* 标签列表 */
.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.sidebar-tag {
  display: inline-block;
  padding: 4px 10px;
  font-size: 12px;
  color: var(--color-ink-light);
  background: var(--color-paper);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  text-decoration: none;
  transition: all 0.2s;
  
  &:hover {
    background: var(--color-ink);
    color: var(--color-white);
    border-color: var(--color-ink);
    text-decoration: none;
  }
}

/* 最近更新 */
.recent-list {
  list-style: none;
}

.recent-item {
  padding: 8px 0;
  border-bottom: 1px dashed var(--color-border-light);
  
  &:last-child {
    border-bottom: none;
  }
}

.recent-link {
  display: block;
  font-size: 13px;
  color: var(--color-ink);
  text-decoration: none;
  margin-bottom: 2px;
  
  &:hover {
    color: var(--color-cinnabar);
  }
}

.recent-time {
  font-size: 11px;
  color: var(--color-ink-muted);
}
</style>
