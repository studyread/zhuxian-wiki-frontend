<template>
  <div class="about-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">关于诛仙世界 Wiki</h1>
      <p class="page-desc">一个由玩家共建的游戏攻略百科全书</p>
    </div>

    <!-- 关于内容 -->
    <div class="about-content">
      <!-- 项目介绍 -->
      <section class="about-section">
        <h2 class="section-title">关于项目</h2>
        <div class="section-body">
          <p>
            诛仙世界 Wiki 是一个由游戏爱好者共同维护的攻略百科网站。
            我们致力于收集、整理并分享《诛仙世界》游戏中的各类攻略、技巧和心得，
            帮助每一位玩家在游戏中获得更好的体验。
          </p>
          <p>
            无论是初入江湖的新手，还是经验丰富的老玩家，
            都能在这里找到有价值的信息。我们欢迎每一位玩家的参与和贡献。
          </p>
        </div>
      </section>

      <!-- 核心功能 -->
      <section class="about-section">
        <h2 class="section-title">核心功能</h2>
        <div class="feature-grid">
          <div class="feature-card">
            <div class="feature-icon">📜</div>
            <h3>攻略百科</h3>
            <p>收录全门派、副本、装备等攻略，助你快速上手</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">🔍</div>
            <h3>智能搜索</h3>
            <p>快速查找你需要的攻略内容</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">🤖</div>
            <h3>AI 助手</h3>
            <p>基于知识库的智能问答，24小时为你服务</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">⭐</div>
            <h3>收藏夹</h3>
            <p>收藏喜欢的攻略，随时查看</p>
          </div>
        </div>
      </section>

      <!-- 技术架构 -->
      <section class="about-section">
        <h2 class="section-title">技术架构</h2>
        <div class="tech-stack">
          <div class="tech-item">
            <span class="tech-label">前端技术</span>
            <span class="tech-value">Vue 3 + Vite + Axios</span>
          </div>
          <div class="tech-item">
            <span class="tech-label">后端技术</span>
            <span class="tech-value">Spring Boot + MyBatis-Plus</span>
          </div>
          <div class="tech-item">
            <span class="tech-label">数据库</span>
            <span class="tech-value">MySQL + Redis</span>
          </div>
          <div class="tech-item">
            <span class="tech-label">AI 服务</span>
            <span class="tech-value">Qwen 2.5 大语言模型</span>
          </div>
        </div>
      </section>

      <!-- 贡献指南 -->
      <section class="about-section">
        <h2 class="section-title">贡献指南</h2>
        <div class="contribute-guide">
          <div class="guide-step">
            <div class="step-number">1</div>
            <div class="step-content">
              <h4>注册账号</h4>
              <p>注册成为 Wiki 成员，开始你的江湖之旅</p>
            </div>
          </div>
          <div class="guide-step">
            <div class="step-number">2</div>
            <div class="step-content">
              <h4>撰写攻略</h4>
              <p>分享你的游戏心得和攻略技巧</p>
            </div>
          </div>
          <div class="guide-step">
            <div class="step-number">3</div>
            <div class="step-content">
              <h4>审核发布</h4>
              <p>提交后经管理员审核，即可发布</p>
            </div>
          </div>
          <div class="guide-step">
            <div class="step-number">4</div>
            <div class="step-content">
              <h4>获得积分</h4>
              <p>优质攻略可获得积分奖励，提升江湖地位</p>
            </div>
          </div>
        </div>
      </section>

      <!-- 联系我们 -->
      <section class="about-section">
        <h2 class="section-title">联系我们</h2>
        <div class="contact-list">
          <div class="contact-item">
            <div class="contact-icon">📧</div>
            <div class="contact-info">
              <span class="contact-label">邮箱</span>
              <span class="contact-value">contact@zhuxian-wiki.com</span>
            </div>
          </div>
          <div class="contact-item">
            <div class="contact-icon">💬</div>
            <div class="contact-info">
              <span class="contact-label">QQ群</span>
              <span class="contact-value">123456789</span>
            </div>
          </div>
          <div class="contact-item">
            <div class="contact-icon">🌐</div>
            <div class="contact-info">
              <span class="contact-label">官网</span>
              <span class="contact-value">www.zhuxian-wiki.com</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 版权声明 -->
      <section class="about-section copyright">
        <p>
          © 2026 诛仙世界 Wiki. 保留所有权利.<br />
          本网站内容仅供学习交流使用，如有侵权请联系删除。
        </p>
      </section>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.about-page {
  padding: 0;
}

/* 页面标题 */
.page-header {
  text-align: center;
  padding: 40px 24px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-family: var(--font-serif);
  font-weight: 600;
  color: var(--color-ink);
  margin-bottom: 8px;
}

.page-desc {
  font-size: var(--text-base);
  color: var(--color-ink-muted);
  margin: 0;
}

/* 关于内容 */
.about-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.about-section {
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 24px;
}

.section-title {
  font-size: var(--text-lg);
  font-family: var(--font-serif);
  font-weight: 600;
  color: var(--color-ink);
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--color-border-light);
}

.section-body {
  font-size: var(--text-base);
  color: var(--color-ink-light);
  line-height: 1.8;
}

.section-body p {
  margin-bottom: 12px;
}

.section-body p:last-child {
  margin-bottom: 0;
}

/* 功能卡片 */
.feature-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.feature-card {
  padding: 20px;
  background: var(--color-paper);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  text-align: center;
  transition: all 0.2s;
}

.feature-card:hover {
  border-color: var(--color-cinnabar);
}

.feature-icon {
  font-size: 32px;
  margin-bottom: 12px;
}

.feature-card h3 {
  font-size: var(--text-md);
  font-family: var(--font-serif);
  color: var(--color-ink);
  margin-bottom: 8px;
}

.feature-card p {
  font-size: var(--text-sm);
  color: var(--color-ink-muted);
  margin: 0;
}

/* 技术栈 */
.tech-stack {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.tech-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 16px;
  background: var(--color-paper);
  border-radius: var(--radius-sm);
}

.tech-label {
  font-size: var(--text-sm);
  color: var(--color-ink-muted);
}

.tech-value {
  font-size: var(--text-sm);
  color: var(--color-ink);
  font-weight: 500;
}

/* 贡献指南 */
.contribute-guide {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.guide-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  position: relative;
}

.guide-step:not(:last-child)::after {
  content: '';
  position: absolute;
  top: 20px;
  right: -8px;
  width: 16px;
  height: 2px;
  background: var(--color-border);
}

.step-number {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-lg);
  font-weight: 600;
  color: var(--color-white);
  background: var(--color-cinnabar);
  border-radius: 50%;
  margin-bottom: 12px;
}

.step-content h4 {
  font-size: var(--text-base);
  font-family: var(--font-serif);
  color: var(--color-ink);
  margin-bottom: 4px;
}

.step-content p {
  font-size: var(--text-sm);
  color: var(--color-ink-muted);
  margin: 0;
}

/* 联系方式 */
.contact-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--color-paper);
  border-radius: var(--radius-sm);
}

.contact-icon {
  font-size: 24px;
}

.contact-info {
  display: flex;
  flex-direction: column;
}

.contact-label {
  font-size: var(--text-xs);
  color: var(--color-ink-muted);
}

.contact-value {
  font-size: var(--text-sm);
  color: var(--color-ink);
}

/* 版权 */
.copyright {
  text-align: center;
  padding: 20px;
}

.copyright p {
  font-size: var(--text-xs);
  color: var(--color-ink-muted);
  margin: 0;
  line-height: 1.8;
}

/* 响应式 */
@media (max-width: 768px) {
  .feature-grid,
  .contribute-guide,
  .contact-list {
    grid-template-columns: repeat(2, 1fr);
  }

  .tech-stack {
    grid-template-columns: 1fr;
  }

  .guide-step:not(:last-child)::after {
    display: none;
  }
}
</style>
