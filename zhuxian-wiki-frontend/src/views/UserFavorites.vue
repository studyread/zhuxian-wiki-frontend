<template>
  <div class="user-favorites-page">
    <div class="page-header">
      <button @click="goBack" class="back-btn">
        <span v-html="backIcon"></span>
        返回
      </button>
      <h1>我的收藏</h1>
    </div>

    <div class="favorites-list" v-if="favorites.length > 0">
      <article
        v-for="article in favorites"
        :key="article.id"
        class="favorite-item"
        @click="goToArticle(article.id)"
      >
        <div class="article-cover">
          <img :src="article.coverImage || 'https://picsum.photos/200/120'" :alt="article.title" />
        </div>
        <div class="article-info">
          <h3 class="article-title">{{ article.title }}</h3>
          <p class="article-summary">{{ article.summary || '暂无描述' }}</p>
          <div class="article-meta">
            <span class="meta-item">
              <span v-html="viewIcon"></span>
              {{ article.viewCount || 0 }}
            </span>
            <span class="meta-date">{{ formatDate(article.createdAt) }}</span>
          </div>
        </div>
        <button @click.stop="removeFavorite(article.id)" class="unfavorite-btn" title="取消收藏">
          <span v-html="starFilledIcon"></span>
        </button>
      </article>
    </div>

    <div v-else class="empty-state">
      <span v-html="emptyIcon"></span>
      <p>还没有收藏任何文章</p>
      <router-link to="/" class="browse-btn">去逛逛</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { articleApi } from '@/api/article'
import { formatDate } from '@/utils/format'

const router = useRouter()
const favorites = ref([])

const backIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>'
const viewIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>'
const starFilledIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="currentColor" stroke="currentColor" stroke-width="2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>'
const emptyIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" opacity="0.3"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>'

const goBack = () => {
  router.push('/user')
}

const goToArticle = (id) => {
  router.push(`/article/${id}`)
}

const removeFavorite = async (articleId) => {
  if (!confirm('确定要取消收藏吗？')) return

  try {
    const res = await articleApi.collect(articleId)
    if (res.code === 200) {
      favorites.value = favorites.value.filter(a => a.id !== articleId)
      alert('已取消收藏')
    } else {
      alert(res.message || '操作失败')
    }
  } catch (error) {
    console.error('Remove favorite error:', error)
    alert('操作失败，请重试')
  }
}

const fetchFavorites = async () => {
  try {
    const res = await articleApi.getUserCollects()
    if (res.code === 200) {
      favorites.value = res.data || []
    }
  } catch (error) {
    console.error('Fetch favorites error:', error)
  }
}

onMounted(() => {
  fetchFavorites()
})
</script>

<style scoped>
.user-favorites-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 16px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  color: var(--color-ink);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.back-btn:hover {
  background: var(--color-cream);
}

.page-header h1 {
  font-size: 20px;
  font-family: var(--font-serif);
  color: var(--color-ink);
  margin: 0;
}

.favorites-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.favorite-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.2s;
}

.favorite-item:hover {
  border-color: var(--color-ochre);
  box-shadow: var(--shadow-md);
}

.article-cover {
  width: 120px;
  height: 80px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  flex-shrink: 0;
}

.article-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.article-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.article-title {
  font-size: 15px;
  font-weight: 600;
  font-family: var(--font-serif);
  color: var(--color-ink);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.article-summary {
  font-size: 12px;
  color: var(--color-ink-light);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: auto;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: var(--color-ink-muted);
}

.meta-date {
  font-size: 11px;
  color: var(--color-ink-muted);
  margin-left: auto;
}

.unfavorite-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--color-border);
  border-radius: 50%;
  background: var(--color-paper);
  color: var(--color-cinnabar);
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
  align-self: center;
}

.unfavorite-btn:hover {
  background: var(--color-cinnabar);
  border-color: var(--color-cinnabar);
  color: white;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: var(--color-white);
  border-radius: var(--radius-md);
}

.empty-state svg {
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 14px;
  color: var(--color-ink-muted);
  margin-bottom: 20px;
}

.browse-btn {
  display: inline-block;
  padding: 10px 24px;
  background: var(--color-cinnabar);
  color: white;
  border-radius: var(--radius-sm);
  text-decoration: none;
  font-size: 14px;
  transition: background 0.2s;
}

.browse-btn:hover {
  background: #b34d3d;
  color: white;
}

@media (max-width: 600px) {
  .article-cover {
    width: 80px;
    height: 60px;
  }
}
</style>
