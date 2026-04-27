<template>
  <div class="ai-chat-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">游戏百科助手</h1>
        <p class="page-desc">基于游戏知识库，AI 智能解答各类问题</p>
      </div>
      <!-- 登录用户显示会话列表按钮 -->
      <button v-if="isLoggedIn" class="session-btn" @click="showSessionList = !showSessionList">
        <span class="session-icon">📋</span>
        会话列表
      </button>
    </div>

    <!-- 会话列表侧边栏 -->
    <div v-if="showSessionList && isLoggedIn" class="session-sidebar">
      <div class="session-header">
        <h3>我的会话</h3>
        <button class="new-session-btn" @click="createNewSession">+ 新会话</button>
      </div>
      <div class="session-list">
        <div
          v-for="session in sessionList"
          :key="session.sessionId"
          class="session-item"
          :class="{ active: sessionId === session.sessionId }"
          @click="switchSession(session.sessionId)"
        >
          <div class="session-title">{{ session.title }}</div>
          <div class="session-meta">
            <span class="session-count">{{ session.messageCount }}条消息</span>
            <button class="delete-btn" @click.stop="deleteSession(session.sessionId)">删除</button>
          </div>
        </div>
        <div v-if="sessionList.length === 0" class="no-sessions">
          暂无会话记录
        </div>
      </div>
    </div>

    <!-- 聊天区域 -->
    <div class="chat-container" :class="{ 'with-sidebar': showSessionList && isLoggedIn }">
      <!-- 消息列表 -->
      <div class="chat-messages" ref="messagesContainer">
        <!-- 欢迎消息 -->
        <div v-if="messages.length === 0" class="welcome-message">
          <div class="welcome-icon">典</div>
          <h3>欢迎使用游戏百科助手</h3>
          <p>我可以帮你解答关于游戏攻略、门派推荐、职业选择等问题</p>
          <div class="quick-questions">
            <span class="quick-label">试试问我：</span>
            <button
              v-for="q in quickQuestions"
              :key="q"
              class="quick-question"
              @click="sendQuickQuestion(q)"
            >
              {{ q }}
            </button>
          </div>
        </div>

        <!-- 聊天消息 -->
        <div
          v-for="(msg, index) in messages"
          :key="index"
          class="message"
          :class="msg.role"
        >
          <div class="message-avatar">
            <span v-if="msg.role === 'user'">我</span>
            <span v-else>典</span>
          </div>
          <div class="message-content">
            <div class="message-bubble">
              <p v-if="msg.role === 'assistant' && msg.loading" class="loading-text">
                <span class="loading-dot"></span>
                <span class="loading-dot"></span>
                <span class="loading-dot"></span>
              </p>
              <div v-else v-html="formatMessage(msg.content)"></div>
            </div>
            <span class="message-time">{{ msg.time }}</span>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input-area">
        <div class="input-container">
          <textarea
            v-model="inputMessage"
            @keydown.enter.exact.prevent="sendMessage"
            placeholder="输入你的问题..."
            class="chat-input"
            rows="1"
          ></textarea>
          <button
            class="send-btn"
            @click="sendMessage"
            :disabled="!inputMessage.trim() || loading"
          >
            发送
          </button>
        </div>
        <p class="input-hint">按 Enter 发送，Shift + Enter 换行</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, watch } from 'vue'
import { aiApi } from '@/api/article'

const SESSION_KEY = 'ai_chat_session_id'
const MAX_HISTORY = 20  // 保留10轮对话（20条消息：10条用户 + 10条AI）

const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const messagesContainer = ref(null)
const sessionId = ref('')
const userId = ref(null)
const isLoggedIn = ref(false)
const showSessionList = ref(false)  // 控制会话列表显示
const sessionList = ref([])  // 会话列表

const quickQuestions = [
  '推荐一个适合新手的门派',
  '青云门怎么加点',
  '60级副本攻略',
  '装备强化技巧',
]

// 优化消息格式化 - 更美观的渲染
const formatMessage = (content) => {
  if (!content) return ''

  let html = content

  // 转义 HTML 特殊字符（但保留已格式化的标签）
  const escapeHtml = (str) => {
    return str
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
  }

  // 分割文本，区分代码块和其他内容
  const segments = []
  let lastIndex = 0
  const codeBlockRegex = /```[\s\S]*?```/g
  let match

  while ((match = codeBlockRegex.exec(content)) !== null) {
    // 添加代码块之前的普通文本
    if (match.index > lastIndex) {
      segments.push({
        type: 'text',
        content: content.slice(lastIndex, match.index)
      })
    }
    // 添加代码块
    segments.push({
      type: 'code',
      content: match[0]
    })
    lastIndex = match.index + match[0].length
  }

  // 添加剩余的文本
  if (lastIndex < content.length) {
    segments.push({
      type: 'text',
      content: content.slice(lastIndex)
    })
  }

  // 处理每个片段
  const processedSegments = segments.map(segment => {
    if (segment.type === 'code') {
      // 代码块：直接保留，不转义
      const codeContent = segment.content
        .replace(/^```(\w*)/, '')
        .replace(/```$/, '')
      return `<pre class="msg-code"><code>${escapeHtml(codeContent)}</code></pre>`
    } else {
      // 普通文本：转义并处理 Markdown
      let text = escapeHtml(segment.content)

      // 行内代码（要转义）
      text = text.replace(/`([^`]+)`/g, '<code class="msg-inline-code">$1</code>')

      // 标题处理
      text = text.replace(/^### (.+)$/gm, '<h4 class="msg-h4">$1</h4>')
      text = text.replace(/^## (.+)$/gm, '<h3 class="msg-h3">$1</h3>')
      text = text.replace(/^# (.+)$/gm, '<h2 class="msg-h2">$1</h2>')

      // 引用块
      text = text.replace(/^&gt; (.+)$/gm, '<blockquote class="msg-quote">$1</blockquote>')

      // 列表处理
      text = text.replace(/^[-*] (.+)$/gm, '<li class="msg-li">$1</li>')
      text = text.replace(/(<li class="msg-li">.+?<\/li>\n?)+/g, '<ul class="msg-ul">$&</ul>')

      // 加粗和斜体
      text = text.replace(/\*\*\*(.+?)\*\*\*/g, '<strong class="msg-bold-italic">$1</strong>')
      text = text.replace(/\*\*(.+?)\*\*/g, '<strong class="msg-bold">$1</strong>')
      text = text.replace(/\*(.+?)\*/g, '<em class="msg-italic">$1</em>')

      // 分割线
      text = text.replace(/^---+$/gm, '<hr class="msg-hr">')

      // 换行处理
      text = text.replace(/\n\n/g, '</p><p class="msg-p">')
      text = text.replace(/\n/g, '<br>')

      // 包裹段落
      if (!text.startsWith('<')) {
        text = '<p class="msg-p">' + text + '</p>'
      }

      return text
    }
  })

  return processedSegments.join('')
}

const getCurrentTime = () => {
  const now = new Date()
  return `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const sendQuickQuestion = (question) => {
  inputMessage.value = question
  sendMessage()
}

// 获取用户信息
const getUserInfo = () => {
  const userInfo = localStorage.getItem('user_info')
  if (userInfo) {
    try {
      const user = JSON.parse(userInfo)
      userId.value = user.id
      isLoggedIn.value = true
    } catch (e) {
      isLoggedIn.value = false
    }
  }
}

// 加载历史消息
const loadHistory = async () => {
  getUserInfo()

  // 获取或创建 sessionId
  let sid = localStorage.getItem(SESSION_KEY)
  if (!sid) {
    sid = 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
    localStorage.setItem(SESSION_KEY, sid)
  }
  sessionId.value = sid

  try {
    if (isLoggedIn.value && userId.value) {
      // 登录用户：加载会话列表
      await loadSessionList()
    } else {
      // 游客：加载指定会话的消息
      await loadSessionMessages(sid)
    }
  } catch (error) {
    console.error('加载历史消息失败:', error)
  }
}

// 加载用户的会话列表
const loadSessionList = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/ai/history', {
      headers: {
        'X-User-Id': userId.value.toString()
      }
    })
    const data = await response.json()

    if (data.code === 200 && data.data) {
      sessionList.value = data.data
      // 如果有会话列表，加载最新会话的消息
      if (sessionList.value.length > 0) {
        const latestSession = sessionList.value[0]
        sessionId.value = latestSession.sessionId
        localStorage.setItem(SESSION_KEY, latestSession.sessionId)
        await loadSessionMessages(latestSession.sessionId)
      }
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
  }
}

// 加载指定会话的消息
const loadSessionMessages = async (sid) => {
  try {
    const response = await fetch(`http://localhost:8080/api/ai/session/${encodeURIComponent(sid)}`)
    const data = await response.json()

    if (data.code === 200 && data.data && data.data.length > 0) {
      // 取最近20条
      const history = data.data.slice(-MAX_HISTORY)
      messages.value = history.map(h => ({
        role: h.role === 'user' ? 'user' : 'assistant',
        content: h.content,
        time: getCurrentTime()
      }))
      scrollToBottom()
    } else {
      messages.value = []
    }
  } catch (error) {
    console.error('加载会话消息失败:', error)
    messages.value = []
  }
}

// 切换会话
const switchSession = async (sid) => {
  sessionId.value = sid
  localStorage.setItem(SESSION_KEY, sid)
  showSessionList.value = false
  await loadSessionMessages(sid)
}

// 创建新会话
const createNewSession = async () => {
  sessionId.value = 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
  localStorage.setItem(SESSION_KEY, sessionId.value)
  messages.value = []
  showSessionList.value = false
}

// 删除会话
const deleteSession = async (sid) => {
  if (!confirm('确定要删除这个会话吗？')) return

  try {
    const response = await fetch(`http://localhost:8080/api/ai/session/${encodeURIComponent(sid)}`, {
      method: 'DELETE',
      headers: {
        'X-User-Id': userId.value.toString()
      }
    })
    const data = await response.json()

    if (data.code === 200) {
      // 从列表中移除
      sessionList.value = sessionList.value.filter(s => s.sessionId !== sid)
      // 如果删除的是当前会话，创建新会话
      if (sessionId.value === sid) {
        await createNewSession()
      }
    }
  } catch (error) {
    console.error('删除会话失败:', error)
  }
}

const sendMessage = async () => {
  const content = inputMessage.value.trim()
  if (!content || loading.value) return

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content,
    time: getCurrentTime()
  })

  inputMessage.value = ''
  scrollToBottom()

  // 添加 AI 消息（加载中状态）
  const aiMessageIndex = messages.value.length
  messages.value.push({
    role: 'assistant',
    content: '',
    loading: true,
    time: getCurrentTime()
  })

  loading.value = true

  try {
    const requestBody = { 
      question: content,
      sessionId: sessionId.value
    }
    
    const headers = {
      'Content-Type': 'application/json'
    }
    
    // 如果用户已登录，添加用户ID
    if (isLoggedIn.value && userId.value) {
      headers['X-User-Id'] = userId.value.toString()
    }
    
    const response = await fetch('http://localhost:8080/api/ai/chat', {
      method: 'POST',
      headers,
      body: JSON.stringify(requestBody)
    })

    const data = await response.json()

    // 更新 AI 消息
    if (data.code === 200) {
      // 保存返回的sessionId
      if (data.data?.sessionId) {
        sessionId.value = data.data.sessionId
        localStorage.setItem(SESSION_KEY, data.data.sessionId)
      }
      messages.value[aiMessageIndex] = {
        role: 'assistant',
        content: data.data?.answer || '抱歉，我没有获取到有效的回复。',
        time: getCurrentTime()
      }
      // 不需要刷新会话列表，保持当前会话
    } else {
      messages.value[aiMessageIndex] = {
        role: 'assistant',
        content: data.message || '请求失败，请稍后重试。',
        time: getCurrentTime()
      }
    }
  } catch (error) {
    console.error('AI 请求失败:', error)
    messages.value[aiMessageIndex] = {
      role: 'assistant',
      content: '抱歉，服务暂时不可用，请稍后重试。',
      time: getCurrentTime()
    }
  }

  loading.value = false
  scrollToBottom()
}

onMounted(() => {
  loadHistory()
})
</script>

<style scoped>
.ai-chat-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
  min-height: 500px;
  position: relative;
}

/* 页面标题 */
.page-header {
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 24px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  text-align: left;
}

.session-btn {
  padding: 8px 16px;
  font-size: 13px;
  color: var(--color-ink);
  background: var(--color-paper);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.session-btn:hover {
  background: var(--color-ink);
  color: var(--color-white);
  border-color: var(--color-ink);
}

.session-icon {
  font-size: 14px;
}

/* 会话列表侧边栏 */
.session-sidebar {
  position: absolute;
  top: 100px;
  right: 20px;
  width: 320px;
  max-height: calc(100vh - 180px);
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
  z-index: 100;
  display: flex;
  flex-direction: column;
}

.session-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid var(--color-border);
}

.session-header h3 {
  font-size: 15px;
  font-family: var(--font-serif);
  color: var(--color-ink);
  margin: 0;
}

.new-session-btn {
  padding: 6px 12px;
  font-size: 12px;
  color: var(--color-white);
  background: var(--color-cinnabar);
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s;
}

.new-session-btn:hover {
  background: var(--color-cinnabar-light);
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.session-item {
  padding: 12px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 4px;
}

.session-item:hover {
  background: var(--color-paper);
}

.session-item.active {
  background: rgba(196, 92, 72, 0.1);
  border-left: 3px solid var(--color-cinnabar);
}

.session-title {
  font-size: 13px;
  color: var(--color-ink);
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.session-count {
  font-size: 11px;
  color: var(--color-ink-muted);
}

.delete-btn {
  padding: 2px 8px;
  font-size: 11px;
  color: var(--color-ink-muted);
  background: transparent;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s;
}

.delete-btn:hover {
  color: #fff;
  background: #e74c3c;
  border-color: #e74c3c;
}

.no-sessions {
  text-align: center;
  padding: 40px 20px;
  color: var(--color-ink-muted);
  font-size: 13px;
}

/* 聊天容器 */
.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  overflow: hidden;
  transition: margin-right 0.3s;
}

.chat-container.with-sidebar {
  margin-right: 340px;
}

.page-title {
  font-size: 22px;
  font-family: var(--font-serif);
  font-weight: 600;
  color: var(--color-ink);
  margin-bottom: 6px;
}

.page-desc {
  font-size: 13px;
  color: var(--color-ink-muted);
}

/* 聊天容器 */
.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  overflow: hidden;
}

/* 消息列表 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

/* 欢迎消息 */
.welcome-message {
  text-align: center;
  padding: 40px 20px;
}

.welcome-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-family: var(--font-serif);
  background: var(--color-ink);
  color: var(--color-white);
  border-radius: var(--radius-md);
  margin: 0 auto 16px;
}

.welcome-message h3 {
  font-size: 18px;
  font-family: var(--font-serif);
  color: var(--color-ink);
  margin-bottom: 8px;
}

.welcome-message p {
  font-size: 13px;
  color: var(--color-ink-muted);
  margin-bottom: 20px;
}

.quick-questions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
}

.quick-label {
  width: 100%;
  font-size: 12px;
  color: var(--color-ink-muted);
  margin-bottom: 4px;
}

.quick-question {
  padding: 8px 14px;
  font-size: 13px;
  color: var(--color-ink-light);
  background: var(--color-paper);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    background: var(--color-ink);
    color: var(--color-white);
    border-color: var(--color-ink);
  }
}

/* 消息样式 */
.message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  
  &.user {
    flex-direction: row-reverse;
    
    .message-content {
      align-items: flex-end;
    }
    
    .message-bubble {
      background: linear-gradient(135deg, #c45c48, #e07b6d);
      color: #fff;
      border: none;
      box-shadow: 0 4px 12px rgba(196, 92, 72, 0.25);
    }
    
    .message-time {
      text-align: right;
    }
  }
}

.message-avatar {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-family: var(--font-serif);
  background: linear-gradient(135deg, #faf8f3, #f0ebe3);
  color: var(--color-ink);
  border-radius: 50%;
  flex-shrink: 0;
  border: 2px solid var(--color-border);
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.message.user .message-avatar {
  background: linear-gradient(135deg, #c45c48, #e07b6d);
  color: #fff;
  border-color: transparent;
}

.message-content {
  display: flex;
  flex-direction: column;
  max-width: 75%;
}

.message-bubble {
  padding: 14px 18px;
  font-size: 14px;
  line-height: 1.7;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  border-top-left-radius: 4px;
  color: var(--color-ink);
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  
  p {
    margin: 0;
  }
}

.message.user .message-bubble {
  border-top-left-radius: 16px;
  border-top-right-radius: 4px;
}

.message-time {
  font-size: 11px;
  color: var(--color-ink-muted);
  margin-top: 6px;
  opacity: 0.7;
}

/* AI 消息内容样式优化 */
.message-bubble :deep(.msg-p) {
  margin: 0 0 10px 0;
  line-height: 1.7;
}

.message-bubble :deep(.msg-p:last-child) {
  margin-bottom: 0;
}

.message-bubble :deep(.msg-h2) {
  font-size: 18px;
  font-family: var(--font-serif);
  color: var(--color-cinnabar);
  margin: 0 0 12px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid rgba(196, 92, 72, 0.2);
}

.message-bubble :deep(.msg-h3) {
  font-size: 15px;
  font-family: var(--font-serif);
  color: var(--color-ink);
  margin: 12px 0 8px 0;
  font-weight: 600;
}

.message-bubble :deep(.msg-h4) {
  font-size: 13px;
  font-family: var(--font-serif);
  color: var(--color-ink-light);
  margin: 10px 0 6px 0;
}

.message-bubble :deep(.msg-quote) {
  margin: 8px 0;
  padding: 10px 14px;
  background: rgba(196, 92, 72, 0.06);
  border-left: 3px solid var(--color-cinnabar);
  border-radius: 0 8px 8px 0;
  color: var(--color-ink-light);
  font-size: 13px;
}

.message-bubble :deep(.msg-ul) {
  margin: 8px 0;
  padding-left: 20px;
  list-style: none;
}

.message-bubble :deep(.msg-li) {
  position: relative;
  padding-left: 16px;
  margin-bottom: 4px;
}

.message-bubble :deep(.msg-li)::before {
  content: '•';
  position: absolute;
  left: 0;
  color: var(--color-cinnabar);
  font-weight: bold;
}

.message-bubble :deep(.msg-code) {
  margin: 10px 0;
  padding: 12px;
  background: #2d2d2d;
  border-radius: 8px;
  overflow-x: auto;
}

.message-bubble :deep(.msg-code code) {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
  color: #a9dc76;
  background: transparent;
  border: none;
  padding: 0;
}

.message-bubble :deep(.msg-inline-code) {
  padding: 2px 6px;
  background: rgba(196, 92, 72, 0.1);
  color: var(--color-cinnabar);
  border-radius: 4px;
  font-size: 12px;
  font-family: monospace;
}

.message-bubble :deep(.msg-bold) {
  font-weight: 600;
  color: var(--color-ink);
}

.message-bubble :deep(.msg-bold-italic) {
  font-weight: 600;
  font-style: italic;
  color: var(--color-cinnabar);
}

.message-bubble :deep(.msg-italic) {
  font-style: italic;
  color: var(--color-ink-light);
}

.message-bubble :deep(.msg-hr) {
  margin: 14px 0;
  border: none;
  border-top: 1px dashed var(--color-border);
}

/* 加载动画 */
.loading-text {
  display: flex;
  gap: 4px;
  justify-content: center;
}

.loading-dot {
  width: 6px;
  height: 6px;
  background: var(--color-ink-muted);
  border-radius: 50%;
  animation: loading 1.4s infinite ease-in-out both;
  
  &:nth-child(1) { animation-delay: -0.32s; }
  &:nth-child(2) { animation-delay: -0.16s; }
}

@keyframes loading {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

/* 输入区域 */
.chat-input-area {
  padding: 16px 20px;
  background: var(--color-cream);
  border-top: 1px solid var(--color-border);
}

.input-container {
  display: flex;
  gap: 12px;
}

.chat-input {
  flex: 1;
  padding: 12px 16px;
  font-size: 14px;
  line-height: 1.5;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-white);
  color: var(--color-ink);
  resize: none;
  
  &:focus {
    outline: none;
    border-color: var(--color-ochre);
  }
  
  &::placeholder {
    color: var(--color-ink-muted);
  }
}

.send-btn {
  padding: 12px 24px;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-white);
  background: var(--color-cinnabar);
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover:not(:disabled) {
    background: var(--color-cinnabar-light);
  }
  
  &:disabled {
    background: var(--color-border);
    cursor: not-allowed;
  }
}

.input-hint {
  font-size: 11px;
  color: var(--color-ink-muted);
  margin-top: 8px;
  text-align: center;
}

/* 响应式 */
@media (max-width: 768px) {
  .ai-chat-page {
    height: calc(100vh - 100px);
  }
  
  .message-content {
    max-width: 85%;
  }
}
</style>
