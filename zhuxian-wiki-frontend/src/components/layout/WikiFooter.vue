<template>
  <footer class="wiki-footer">
    <div class="footer-content">
      <p>© 2024 诛仙世界Wiki - 专注游戏攻略分享</p>
      <p class="ICP">ICP备案号: xxxxxxx</p>
    </div>
  </footer>
</template>

<style scoped>
.wiki-footer {
  background: var(--bg-secondary);
  border-top: 1px solid var(--border);
  padding: 30px 20px;
  margin-top: auto;
}

.footer-content {
  max-width: 1400px;
  margin: 0 auto;
  text-align: center;
  color: var(--text-secondary);
}

.footer-content p {
  margin: 5px 0;
}

.ICP {
  font-size: 12px;
}

/* 移动端隐藏 */
@media (max-width: 768px) {
  .wiki-footer {
    display: none;
  }
}
</style>
