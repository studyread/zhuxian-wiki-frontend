<template>
  <div class="not-found-page">
    <div class="not-found-content">
      <!-- 大数字 -->
      <div class="error-code">404</div>
      
      <!-- 古风图标 -->
      <div class="error-icon">
        <img src="/big_logo.webp" alt="Logo" />
      </div>
      
      <!-- 错误消息 -->
      <h1 class="error-title">典籍未寻</h1>
      <p class="error-desc">
        所寻典籍不在此间，或是已被岁月掩埋<br />
        请返回首页重新寻觅
      </p>
      
      <!-- 操作按钮 -->
      <div class="error-actions">
        <router-link to="/" class="btn-home">
          返回首页
        </router-link>
        <button class="btn-back" @click="$router.back()">
          返回上页
        </button>
      </div>
      
      <!-- 快捷导航 -->
      <div class="quick-nav">
        <p class="nav-hint">或浏览以下内容</p>
        <div class="nav-links">
          <router-link to="/" class="nav-link">首页</router-link>
          <router-link to="/ai" class="nav-link">AI 助手</router-link>
          <router-link to="/search" class="nav-link">搜索攻略</router-link>
          <router-link to="/user" class="nav-link">个人中心</router-link>
        </div>
      </div>
    </div>

    <!-- 装饰元素 -->
    <div class="decorative-line top-left"></div>
    <div class="decorative-line bottom-right"></div>
  </div>
</template>

<script setup>
// 静态页面，无需特殊逻辑
</script>

<style scoped>
.not-found-page {
  min-height: calc(100vh - 200px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  position: relative;
  overflow: hidden;
}

.not-found-content {
  text-align: center;
  max-width: 500px;
  position: relative;
  z-index: 1;
}

/* 错误代码 */
.error-code {
  font-size: 120px;
  font-family: var(--font-serif);
  font-weight: 700;
  color: var(--color-paper-dark);
  line-height: 1;
  margin-bottom: -20px;
  letter-spacing: -8px;
}

/* 错误图标 */
.error-icon {
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  margin: 0 auto 24px;
  position: relative;
  overflow: hidden;
}

.error-icon img {
  width: 90px;
  height: 90px;
  object-fit: contain;
}

.error-icon::before {
  content: '';
  position: absolute;
  top: -8px;
  left: -8px;
  right: -8px;
  bottom: -8px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}

/* 标题和描述 */
.error-title {
  font-size: 32px;
  font-family: var(--font-serif);
  font-weight: 600;
  color: var(--color-ink);
  margin-bottom: 12px;
}

.error-desc {
  font-size: var(--text-base);
  color: var(--color-ink-muted);
  line-height: 1.8;
  margin-bottom: 32px;
}

/* 操作按钮 */
.error-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 40px;
}

.btn-home,
.btn-back {
  padding: 12px 28px;
  font-size: var(--text-base);
  border-radius: var(--radius-sm);
  text-decoration: none;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-home {
  background: var(--color-cinnabar);
  color: var(--color-white);
  border: 1px solid var(--color-cinnabar);
}

.btn-home:hover {
  background: var(--color-cinnabar-light);
  text-decoration: none;
}

.btn-back {
  background: var(--color-white);
  color: var(--color-ink);
  border: 1px solid var(--color-border);
}

.btn-back:hover {
  border-color: var(--color-cinnabar);
  color: var(--color-cinnabar);
}

/* 快捷导航 */
.quick-nav {
  padding-top: 24px;
  border-top: 1px solid var(--color-border-light);
}

.nav-hint {
  font-size: var(--text-sm);
  color: var(--color-ink-muted);
  margin-bottom: 12px;
}

.nav-links {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 12px;
}

.nav-link {
  padding: 8px 16px;
  font-size: var(--text-sm);
  background: var(--color-paper);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  color: var(--color-ink-light);
  text-decoration: none;
  transition: all 0.2s;
}

.nav-link:hover {
  background: var(--color-cinnabar);
  border-color: var(--color-cinnabar);
  color: var(--color-white);
  text-decoration: none;
}

/* 装饰线 */
.decorative-line {
  position: absolute;
  width: 200px;
  height: 200px;
  border: 1px solid var(--color-border-light);
}

.decorative-line.top-left {
  top: 40px;
  left: 40px;
  border-right: none;
  border-bottom: none;
}

.decorative-line.bottom-right {
  bottom: 40px;
  right: 40px;
  border-left: none;
  border-top: none;
}

/* 响应式 */
@media (max-width: 768px) {
  .error-code {
    font-size: 80px;
    letter-spacing: -4px;
  }

  .error-icon {
    width: 80px;
    height: 80px;
    font-size: 36px;
  }

  .error-title {
    font-size: 24px;
  }

  .error-actions {
    flex-direction: column;
  }

  .decorative-line {
    display: none;
  }
}
</style>
