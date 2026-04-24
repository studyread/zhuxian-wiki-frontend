<template>
  <div class="admin-settings">
      <div class="settings-header">
        <h2>系统设置</h2>
      </div>

      <div class="settings-content">
        <div class="setting-group">
          <h3>AI 配置</h3>
          <div class="setting-item">
            <label>API 地址</label>
            <input type="text" v-model="settings.aiApiUrl" placeholder="AI API 地址" />
          </div>
          <div class="setting-item">
            <label>API Key</label>
            <input type="password" v-model="settings.aiApiKey" placeholder="AI API Key" />
          </div>
          <div class="setting-item">
            <label>模型名称</label>
            <input type="text" v-model="settings.aiModel" placeholder="模型名称" />
          </div>
        </div>

        <div class="setting-group">
          <h3>提示词配置</h3>
          <div class="setting-item">
            <label>系统提示词</label>
            <textarea v-model="settings.systemPrompt" rows="6" placeholder="AI 系统提示词"></textarea>
            <p class="setting-tip">控制 AI 回答时的行为约束，知识库内容将作为主要回答依据</p>
          </div>
        </div>

        <div class="setting-actions">
          <button @click="handleSave" class="btn-primary">保存设置</button>
        </div>
      </div>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'


const settings = reactive({
  aiApiUrl: 'https://api.siliconflow.cn/v1/chat/completions',
  aiApiKey: '',
  aiModel: 'Qwen/Qwen2.5-7B-Instruct',
  systemPrompt: `你是诛仙世界游戏攻略助手，你的职责是：

1. **优先使用知识库回答**：当用户提问时，首先从知识库中检索相关内容进行回答
2. **知识库无法回答时**：明确告知用户"当前知识库暂无相关信息"，不要编造答案
3. **禁止胡编乱造**：如果知识库没有相关内容，必须如实回答，不要试图"推理"或"猜测"答案
4. **保持回答简洁**：直接回答问题，避免过多废话

回答格式：
- 使用 Markdown 格式化
- 如有相关词条，提供链接
- 标注信息来源
`
})

const handleSave = () => {
  localStorage.setItem('wiki_settings', JSON.stringify(settings))
  alert('设置已保存')
}

onMounted(() => {
  const saved = localStorage.getItem('wiki_settings')
  if (saved) {
    Object.assign(settings, JSON.parse(saved))
  }
})
</script>

<style scoped>
.admin-settings {
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 16px;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.settings-header {
  margin-bottom: 16px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--color-border-light);
  flex-shrink: 0;
}

.settings-header h2 {
  font-size: 16px;
  font-family: var(--font-serif);
  color: var(--color-ink);
}

.settings-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow-y: auto;
  flex: 1;
  min-height: 0;
}

.setting-group {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.setting-group h3 {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-ink);
}

.setting-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.setting-item label {
  font-size: 13px;
  color: var(--color-ink);
}

.setting-item input,
.setting-item textarea {
  padding: 9px 11px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 13px;
  font-family: inherit;
}

.setting-item textarea {
  resize: vertical;
}

.setting-tip {
  font-size: 11px;
  color: var(--color-ink-muted);
  margin-top: 4px;
}

.setting-actions {
  padding-top: 14px;
  border-top: 1px solid var(--color-border-light);
}

.btn-primary {
  padding: 9px 20px;
  background: var(--color-cinnabar);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 13px;
  cursor: pointer;
}
</style>
