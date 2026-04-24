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
import { ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const categories = ref([
  { name: '全部', path: '/', icon: '全' },
  { name: '门派', path: '/category/门派', icon: '门' },
  { name: '职业', path: '/category/职业', icon: '职' },
  { name: '攻略', path: '/category/攻略', icon: '攻' },
  { name: '副本', path: '/category/副本', icon: '副' },
  { name: '装备', path: '/category/装备', icon: '装' },
  { name: '宠物', path: '/category/宠物', icon: '宠' },
  { name: '坐骑', path: '/category/坐骑', icon: '骑' },
])

const hotTags = ref(['新手入门', '升级攻略', '门派推荐', 'PVP', '副本攻略', '装备打造'])

const recentArticles = ref([
  { id: 1, title: '青云门新手入门指南', time: '2小时前' },
  { id: 2, title: '天音寺职业攻略', time: '5小时前' },
  { id: 3, title: '副本通关技巧分享', time: '1天前' },
  { id: 4, title: '装备强化成功率分析', time: '2天前' },
])

const isActiveCategory = (path) => {
  if (path === '/') {
    return route.path === '/' && !route.query.category
  }
  return route.path === path || route.query.category === path.split('/')[2]
}
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
