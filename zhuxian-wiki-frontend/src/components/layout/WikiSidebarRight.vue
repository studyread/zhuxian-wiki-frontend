<template>
  <aside class="sidebar-right">
    <!-- AI 助手入口 -->
    <div class="sidebar-section ai-section">
      <div class="ai-header">
        <img src="/big_logo.webp" alt="Logo" class="ai-icon" />
        <div class="ai-info">
          <h4 class="ai-title">游戏百科助手</h4>
          <p class="ai-desc">AI 智能问答</p>
        </div>
      </div>
      <p class="ai-text">
        有什么问题？试试问问 AI 助手，获取游戏攻略、门派推荐等信息。
      </p>
      <router-link to="/ai" class="ai-btn">
        立即提问
      </router-link>
    </div>

    <!-- 快捷工具 -->
    <div class="sidebar-section">
      <h3 class="sidebar-title">快捷工具</h3>
      <div class="tool-list">
        <button
          v-for="tool in tools"
          :key="tool.name"
          class="tool-item"
          @click="handleTool(tool)"
        >
          <span class="tool-icon">{{ tool.icon }}</span>
          <span class="tool-name">{{ tool.name }}</span>
        </button>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const tools = ref([
  { name: '个人中心', icon: '我', action: 'user' },
  { name: '关于我们', icon: '关', action: 'about' },
])

const handleTool = (tool) => {
  if (tool.action === 'user') {
    router.push('/user')
  } else if (tool.action === 'about') {
    router.push('/about')
  }
}
</script>

<style scoped>
.sidebar-right {
  display: flex;
  flex-direction: column;
  gap: 20px;
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

/* AI 助手入口 */
.ai-section {
  background: linear-gradient(135deg, var(--color-cream) 0%, var(--color-white) 100%);
  border-color: var(--color-ochre);
}

.ai-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.ai-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  overflow: hidden;
}

.ai-icon img {
  width: 36px;
  height: 36px;
  object-fit: contain;
}

.ai-info {
  flex: 1;
}

.ai-title {
  font-size: 15px;
  font-weight: 600;
  font-family: var(--font-serif);
  color: var(--color-ink);
  margin-bottom: 2px;
}

.ai-desc {
  font-size: 12px;
  color: var(--color-ink-muted);
}

.ai-text {
  font-size: 13px;
  color: var(--color-ink-light);
  line-height: 1.6;
  margin-bottom: 12px;
}

.ai-btn {
  display: block;
  width: 100%;
  padding: 10px;
  text-align: center;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-white);
  background: var(--color-cinnabar);
  border: none;
  border-radius: var(--radius-sm);
  text-decoration: none;
  transition: background 0.2s;
  
  &:hover {
    background: var(--color-cinnabar-light);
    text-decoration: none;
  }
}

/* 快捷工具 */
.tool-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.tool-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 12px 8px;
  background: var(--color-paper);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    background: var(--color-ink);
    border-color: var(--color-ink);
    
    .tool-icon {
      background: var(--color-white);
      color: var(--color-ink);
    }
    
    .tool-name {
      color: var(--color-white);
    }
  }
}

.tool-icon {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-family: var(--font-serif);
  background: var(--color-paper-dark);
  border-radius: var(--radius-sm);
  color: var(--color-ink-light);
}

.tool-name {
  font-size: 11px;
  color: var(--color-ink-light);
}
</style>
