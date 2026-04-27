<template>
  <div class="user-articles-page">
    <div class="page-header">
      <button @click="goBack" class="back-btn">
        <span v-html="backIcon"></span>
        返回
      </button>
      <h1>我的文章</h1>
    </div>

    <div class="articles-list" v-if="articles.length > 0">
      <article
        v-for="article in articles"
        :key="article.id"
        class="article-item"
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
            <span class="meta-item">
              <span v-html="likeIcon"></span>
              {{ article.likeCount || 0 }}
            </span>
            <span class="meta-date">{{ formatDate(article.createdAt) }}</span>
          </div>
        </div>
        <div class="article-actions">
          <button @click.stop="editArticle(article.id)" class="action-btn edit" title="编辑">
            <span v-html="editIcon"></span>
          </button>
          <button @click.stop="deleteArticle(article.id)" class="action-btn delete" title="删除">
            <span v-html="deleteIcon"></span>
          </button>
        </div>
      </article>
    </div>

    <div v-else class="empty-state">
      <span v-html="emptyIcon"></span>
      <p>还没有发布任何文章</p>
      <router-link to="/article/edit" class="write-btn">写一篇攻略</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { articleApi } from '@/api/article'
import { formatDate } from '@/utils/format'

const router = useRouter()
const articles = ref([])

const backIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>'
const viewIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>'
const likeIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>'
const editIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>'
const deleteIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>'
const emptyIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" opacity="0.3"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>'

const goBack = () => {
  router.push('/user')
}

const goToArticle = (id) => {
  router.push(`/article/${id}`)
}

const editArticle = (id) => {
  router.push(`/article/edit/${id}`)
}

const deleteArticle = async (id) => {
  if (!confirm('确定要删除这篇文章吗？')) return

  try {
    const res = await articleApi.delete(id)
    if (res.code === 200) {
      articles.value = articles.value.filter(a => a.id !== id)
      alert('删除成功')
    } else {
      alert(res.message || '删除失败')
    }
  } catch (error) {
    console.error('Delete error:', error)
    alert('删除失败，请重试')
  }
}

const fetchArticles = async () => {
  const userInfo = JSON.parse(localStorage.getItem('user_info') || '{}')
  if (!userInfo.id) {
    router.push('/user')
    return
  }

  try {
    const res = await articleApi.getUserArticles(userInfo.id)
    if (res.code === 200) {
      articles.value = res.data || []
    }
  } catch (error) {
    console.error('Fetch articles error:', error)
  }
}

onMounted(() => {
  fetchArticles()
})
</script>

<style scoped>
.user-articles-page {
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

.articles-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.article-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.2s;
}

.article-item:hover {
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

.article-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.action-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-paper);
  color: var(--color-ink-light);
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  background: var(--color-ink);
  border-color: var(--color-ink);
  color: white;
}

.action-btn.delete:hover {
  background: var(--color-cinnabar);
  border-color: var(--color-cinnabar);
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

.write-btn {
  display: inline-block;
  padding: 10px 24px;
  background: var(--color-cinnabar);
  color: white;
  border-radius: var(--radius-sm);
  text-decoration: none;
  font-size: 14px;
  transition: background 0.2s;
}

.write-btn:hover {
  background: #b34d3d;
  color: white;
}

@media (max-width: 600px) {
  .article-cover {
    width: 80px;
    height: 60px;
  }

  .article-actions {
    display: none;
  }
}
</style>
