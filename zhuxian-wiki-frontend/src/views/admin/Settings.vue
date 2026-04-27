<template>
  <div class="admin-settings">
    <!-- 头部 -->
    <div class="settings-header">
      <h2>系统设置</h2>
    </div>

    <!-- 主内容 -->
    <div class="settings-main">
      <!-- 左侧Tab导航 -->
      <div class="settings-nav">
        <button
          v-for="tab in tabs"
          :key="tab.id"
          :class="['nav-item', { active: activeTab === tab.id }]"
          @click="activeTab = tab.id"
        >
          <component :is="tab.icon" />
          <span>{{ tab.name }}</span>
        </button>
      </div>

      <!-- 右侧内容区 -->
      <div class="settings-content">
        <!-- AI 配置 -->
        <div v-if="activeTab === 'ai'" class="setting-section">
          <div class="section-header">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 2a4 4 0 0 1 4 4v1h2a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V9a2 2 0 0 1 2-2h2V6a4 4 0 0 1 4-4z"/>
              <circle cx="9" cy="13" r="1"/>
              <circle cx="15" cy="13" r="1"/>
            </svg>
            <h3>AI 智能问答配置</h3>
          </div>
          <div class="setting-card">
            <div class="form-group">
              <label>API 地址</label>
              <input type="text" v-model="settings.aiApiUrl" placeholder="AI API 地址" />
              <p class="hint">通常为服务商提供的 API 端点地址</p>
            </div>
            <div class="form-group">
              <label>API Key</label>
              <input type="password" v-model="settings.aiApiKey" placeholder="API Key（保密）" />
              <p class="hint">您的 API 密钥，请妥善保管</p>
            </div>
            <div class="form-group">
              <label>模型名称</label>
              <input type="text" v-model="settings.aiModel" placeholder="模型名称" />
            </div>
            <div class="form-group">
              <label>温度参数</label>
              <div class="range-wrapper">
                <div class="range-header">
                  <input type="range" v-model.number="settings.aiTemperature" min="0" max="1" step="0.1" />
                  <span class="range-value">{{ settings.aiTemperature }}</span>
                </div>
                <div class="range-labels">
                  <span>精确</span>
                  <span>创意</span>
                </div>
              </div>
              <p class="hint">控制回答的随机性，较低值更确定性，较高值更有创造性</p>
            </div>
          </div>
        </div>

        <!-- 提示词配置 -->
        <div v-if="activeTab === 'prompt'" class="setting-section">
          <div class="section-header">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </svg>
            <h3>AI 提示词配置</h3>
          </div>
          <div class="setting-card">
            <div class="form-group">
              <label>系统提示词</label>
              <textarea v-model="settings.systemPrompt" rows="10" placeholder="AI 系统提示词"></textarea>
              <p class="hint">控制 AI 回答时的行为约束，知识库内容将作为主要回答依据</p>
            </div>
            <div class="form-group">
              <label>知识库搜索模式</label>
              <div class="radio-group">
                <label class="radio-item">
                  <input type="radio" v-model="settings.searchMode" value="semantic" />
                  <span class="radio-label">
                    <strong>语义搜索（推荐）</strong>
                    <small>理解问题含义，返回最相关的答案</small>
                  </span>
                </label>
                <label class="radio-item">
                  <input type="radio" v-model="settings.searchMode" value="keyword" />
                  <span class="radio-label">
                    <strong>关键词搜索</strong>
                    <small>根据关键词匹配，返回包含关键词的内容</small>
                  </span>
                </label>
                <label class="radio-item">
                  <input type="radio" v-model="settings.searchMode" value="hybrid" />
                  <span class="radio-label">
                    <strong>混合搜索</strong>
                    <small>结合语义和关键词，兼顾准确性和相关性</small>
                  </span>
                </label>
              </div>
            </div>
            <div class="form-group">
              <label>最大召回条数</label>
              <div class="number-input">
                <button type="button" @click="settings.maxResults = Math.max(1, settings.maxResults - 1)">-</button>
                <input type="number" v-model.number="settings.maxResults" min="1" max="20" />
                <button type="button" @click="settings.maxResults = Math.min(20, settings.maxResults + 1)">+</button>
              </div>
              <p class="hint">每次查询最多召回的知识条目数量（1-20）</p>
            </div>
          </div>
        </div>

        <!-- 网站信息 -->
        <div v-if="activeTab === 'site'" class="setting-section">
          <div class="section-header">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="2" y1="12" x2="22" y2="12"/>
              <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>
            </svg>
            <h3>网站信息配置</h3>
          </div>
          <div class="setting-card">
            <div class="form-group">
              <label>网站名称</label>
              <input type="text" v-model="settings.siteName" placeholder="诛仙世界 Wiki" />
            </div>
            <div class="form-group">
              <label>网站描述</label>
              <textarea v-model="settings.siteDescription" rows="3" placeholder="网站描述SEO信息"></textarea>
              <p class="hint">显示在搜索引擎结果中的描述文字</p>
            </div>
            <div class="form-group">
              <label>关键词</label>
              <input type="text" v-model="settings.siteKeywords" placeholder="诛仙,游戏,攻略" />
              <p class="hint">用逗号分隔，用于 SEO 优化</p>
            </div>
            <div class="form-group">
              <label>备案号</label>
              <input type="text" v-model="settings.icpNumber" placeholder="如：京ICP备xxxxxx号" />
            </div>
          </div>
        </div>

        <!-- 界面定制 -->
        <div v-if="activeTab === 'ui'" class="setting-section">
          <div class="section-header">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="3"/>
              <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/>
            </svg>
            <h3>界面定制</h3>
          </div>
          <div class="setting-card">
            <div class="form-group">
              <label>主题色</label>
              <div class="color-picker-wrapper">
                <input type="color" v-model="settings.themeColor" />
                <input type="text" v-model="settings.themeColor" class="color-text" />
                <div class="color-presets">
                  <button type="button" @click="settings.themeColor = '#c45c48'" class="preset" style="background:#c45c48" title="朱砂红"></button>
                  <button type="button" @click="settings.themeColor = '#2563eb'" class="preset" style="background:#2563eb" title="宝石蓝"></button>
                  <button type="button" @click="settings.themeColor = '#059669'" class="preset" style="background:#059669" title="翠玉绿"></button>
                  <button type="button" @click="settings.themeColor = '#7c3aed'" class="preset" style="background:#7c3aed" title="紫罗兰"></button>
                  <button type="button" @click="settings.themeColor = '#ea580c'" class="preset" style="background:#ea580c" title="琥珀橙"></button>
                </div>
              </div>
            </div>
            <div class="form-group">
              <label>显示统计</label>
              <div class="toggle-list">
                <label class="toggle-item">
                  <div class="toggle-info">
                    <span class="toggle-title">显示浏览量</span>
                    <span class="toggle-desc">在词条详情页显示浏览次数</span>
                  </div>
                  <input type="checkbox" v-model="settings.showViewCount" class="toggle-switch" />
                </label>
                <label class="toggle-item">
                  <div class="toggle-info">
                    <span class="toggle-title">显示点赞数</span>
                    <span class="toggle-desc">在词条详情页显示点赞次数</span>
                  </div>
                  <input type="checkbox" v-model="settings.showLikeCount" class="toggle-switch" />
                </label>
              </div>
            </div>
            <div class="form-group">
              <label>每页显示条数</label>
              <div class="segment-control">
                <button
                  v-for="size in [10, 20, 30, 50]"
                  :key="size"
                  :class="{ active: settings.pageSize === size }"
                  @click="settings.pageSize = size"
                  type="button"
                >
                  {{ size }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部操作栏 -->
    <div class="setting-actions">
      <button @click="handleReset" class="btn-reset">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/>
          <path d="M3 3v5h5"/>
        </svg>
        恢复默认
      </button>
      <button @click="handleSave" class="btn-save">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/>
          <polyline points="17 21 17 13 7 13 7 21"/>
          <polyline points="7 3 7 8 15 8"/>
        </svg>
        保存设置
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h } from 'vue'

const activeTab = ref('ai')

// 图标组件
const AiIcon = () => h('svg', { width: 18, height: 18, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
  h('path', { d: 'M12 2a4 4 0 0 1 4 4v1h2a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V9a2 2 0 0 1 2-2h2V6a4 4 0 0 1 4-4z' })
])

const PromptIcon = () => h('svg', { width: 18, height: 18, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
  h('path', { d: 'M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z' })
])

const SiteIcon = () => h('svg', { width: 18, height: 18, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
  h('circle', { cx: 12, cy: 12, r: 10 }),
  h('line', { x1: 2, y1: 12, x2: 22, y2: 12 }),
  h('path', { d: 'M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z' })
])

const UiIcon = () => h('svg', { width: 18, height: 18, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
  h('circle', { cx: 12, cy: 12, r: 3 }),
  h('path', { d: 'M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z' })
])

const tabs = [
  { id: 'ai', name: 'AI 配置', icon: AiIcon },
  { id: 'prompt', name: '提示词', icon: PromptIcon },
  { id: 'site', name: '网站信息', icon: SiteIcon },
  { id: 'ui', name: '界面定制', icon: UiIcon }
]

const defaultSettings = {
  aiApiUrl: 'https://api.siliconflow.cn/v1/chat/completions',
  aiApiKey: '',
  aiModel: 'Qwen/Qwen2.5-7B-Instruct',
  aiTemperature: 0.7,
  systemPrompt: `你是诛仙世界游戏攻略助手，你的职责是：

1. **优先使用知识库回答**：当用户提问时，首先从知识库中检索相关内容进行回答
2. **知识库无法回答时**：明确告知用户"当前知识库暂无相关信息"，不要编造答案
3. **禁止胡编乱造**：如果知识库没有相关内容，必须如实回答，不要试图"推理"或"猜测"答案
4. **保持回答简洁**：直接回答问题，避免过多废话

回答格式：
- 使用 Markdown 格式化
- 如有相关词条，提供链接
- 标注信息来源
`,
  searchMode: 'semantic',
  maxResults: 5,
  siteName: '诛仙世界 Wiki',
  siteDescription: '诛仙世界游戏攻略知识库',
  siteKeywords: '诛仙,游戏,攻略,职业,门派',
  icpNumber: '',
  themeColor: '#c45c48',
  showViewCount: true,
  showLikeCount: true,
  pageSize: 20
}

const settings = reactive({ ...defaultSettings })

const handleSave = () => {
  localStorage.setItem('wiki_settings', JSON.stringify(settings))
  // 应用主题色
  document.documentElement.style.setProperty('--color-cinnabar', settings.themeColor)
  alert('设置已保存')
}

const handleReset = () => {
  if (confirm('确定恢复所有设置为默认值？')) {
    Object.assign(settings, defaultSettings)
    localStorage.removeItem('wiki_settings')
    alert('已恢复默认设置')
  }
}

onMounted(() => {
  const saved = localStorage.getItem('wiki_settings')
  if (saved) {
    try {
      const parsed = JSON.parse(saved)
      Object.assign(settings, parsed)
      // 应用保存的主题色
      document.documentElement.style.setProperty('--color-cinnabar', settings.themeColor)
    } catch (e) {
      console.error('加载设置失败', e)
    }
  }
})
</script>

<style scoped>
.admin-settings {
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 0;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 头部 */
.settings-header {
  padding: 14px 20px;
  border-bottom: 1px solid var(--color-border-light);
  flex-shrink: 0;
}

.settings-header h2 {
  font-size: 16px;
  font-family: var(--font-serif);
  color: var(--color-ink);
}

/* 主内容 */
.settings-main {
  flex: 1;
  display: flex;
  overflow: hidden;
  min-height: 0;
}

/* 左侧导航 */
.settings-nav {
  width: 180px;
  padding: 16px;
  background: var(--color-paper);
  border-right: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex-shrink: 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: transparent;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 13px;
  color: var(--color-ink);
  cursor: pointer;
  text-align: left;
  transition: all 0.15s;
}

.nav-item:hover {
  background: var(--color-white);
  color: var(--color-cinnabar);
}

.nav-item.active {
  background: var(--color-white);
  color: var(--color-cinnabar);
  font-weight: 500;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.nav-item svg {
  flex-shrink: 0;
}

/* 右侧内容 */
.settings-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.setting-section {
  max-width: 640px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.section-header svg {
  color: var(--color-cinnabar);
}

.section-header h3 {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-ink);
}

.setting-card {
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group > label {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-ink);
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 10px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 13px;
  font-family: inherit;
  transition: border-color 0.2s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: var(--color-cinnabar);
}

.form-group textarea {
  resize: vertical;
}

.hint {
  font-size: 11px;
  color: var(--color-ink-muted);
  margin-top: 2px;
}

/* 范围滑块 */
.range-wrapper {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.range-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.range-header input[type="range"] {
  flex: 1;
  height: 6px;
  -webkit-appearance: none;
  background: var(--color-border);
  border-radius: 3px;
}

.range-header input[type="range"]::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 18px;
  height: 18px;
  background: var(--color-cinnabar);
  border-radius: 50%;
  cursor: pointer;
  border: 2px solid white;
  box-shadow: 0 1px 3px rgba(0,0,0,0.2);
}

.range-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-cinnabar);
  min-width: 30px;
}

.range-labels {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: var(--color-ink-muted);
}

/* 单选组 */
.radio-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.radio-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.15s;
}

.radio-item:hover {
  border-color: var(--color-cinnabar);
}

.radio-item:has(input:checked) {
  border-color: var(--color-cinnabar);
  background: rgba(196, 92, 72, 0.05);
}

.radio-item input {
  margin-top: 3px;
}

.radio-label {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.radio-label strong {
  font-size: 13px;
  color: var(--color-ink);
}

.radio-label small {
  font-size: 11px;
  color: var(--color-ink-muted);
}

/* 数字输入 */
.number-input {
  display: flex;
  align-items: center;
  gap: 0;
  width: fit-content;
}

.number-input button {
  width: 36px;
  height: 36px;
  border: 1px solid var(--color-border);
  background: var(--color-paper);
  font-size: 18px;
  cursor: pointer;
  transition: all 0.15s;
}

.number-input button:first-child {
  border-radius: var(--radius-sm) 0 0 var(--radius-sm);
}

.number-input button:last-child {
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
}

.number-input button:hover {
  background: var(--color-cinnabar);
  border-color: var(--color-cinnabar);
  color: white;
}

.number-input input {
  width: 60px;
  height: 36px;
  border: 1px solid var(--color-border);
  border-left: none;
  border-right: none;
  text-align: center;
  font-size: 14px;
  font-weight: 600;
}

/* 颜色选择器 */
.color-picker-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.color-picker-wrapper input[type="color"] {
  width: 44px;
  height: 36px;
  padding: 2px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  cursor: pointer;
}

.color-text {
  width: 100px;
}

.color-presets {
  display: flex;
  gap: 6px;
}

.color-presets .preset {
  width: 28px;
  height: 28px;
  border: 2px solid transparent;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.15s;
}

.color-presets .preset:hover {
  transform: scale(1.1);
}

/* 开关列表 */
.toggle-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.toggle-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.15s;
}

.toggle-item:hover {
  border-color: var(--color-cinnabar);
}

.toggle-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.toggle-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-ink);
}

.toggle-desc {
  font-size: 11px;
  color: var(--color-ink-muted);
}

.toggle-switch {
  width: 44px;
  height: 24px;
  -webkit-appearance: none;
  background: var(--color-border);
  border-radius: 12px;
  position: relative;
  cursor: pointer;
  transition: background 0.2s;
}

.toggle-switch::after {
  content: '';
  position: absolute;
  top: 2px;
  left: 2px;
  width: 20px;
  height: 20px;
  background: white;
  border-radius: 50%;
  transition: transform 0.2s;
  box-shadow: 0 1px 3px rgba(0,0,0,0.2);
}

.toggle-switch:checked {
  background: var(--color-cinnabar);
}

.toggle-switch:checked::after {
  transform: translateX(20px);
}

/* 分段控件 */
.segment-control {
  display: flex;
  gap: 0;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  overflow: hidden;
  width: fit-content;
}

.segment-control button {
  padding: 8px 16px;
  background: var(--color-white);
  border: none;
  border-right: 1px solid var(--color-border);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s;
}

.segment-control button:last-child {
  border-right: none;
}

.segment-control button:hover {
  background: var(--color-paper);
}

.segment-control button.active {
  background: var(--color-cinnabar);
  color: white;
}

/* 底部操作栏 */
.setting-actions {
  display: flex;
  justify-content: space-between;
  padding: 14px 20px;
  border-top: 1px solid var(--color-border-light);
  background: var(--color-white);
  flex-shrink: 0;
}

.btn-reset {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 9px 16px;
  background: var(--color-white);
  color: var(--color-ink);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 13px;
  cursor: pointer;
}

.btn-reset:hover {
  background: var(--color-paper);
}

.btn-save {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 9px 20px;
  background: var(--color-cinnabar);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 13px;
  cursor: pointer;
}

.btn-save:hover {
  opacity: 0.9;
}
</style>