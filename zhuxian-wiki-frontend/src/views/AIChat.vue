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

    <!-- 会话列表浮层（点击外部关闭，不挤压主区） -->
    <transition name="session-fade">
      <div v-if="showSessionList && isLoggedIn" class="session-popup" ref="sessionPopup">
        <div class="session-header">
          <h3>我的会话</h3>
          <button class="new-session-btn" @click="createNewSession">+ 新会话</button>
        </div>
        <div class="session-list">
          <div
            v-for="session in limitedSessionList"
            :key="session.sessionId"
            class="session-item"
            :class="{ active: currentSessionId === session.sessionId }"
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
          <div v-if="sessionList.length > MAX_SESSION_DISPLAY" class="session-limit-tip">
            ⚠ 最多显示最近 {{ MAX_SESSION_DISPLAY }} 条会话
          </div>
        </div>
      </div>
    </transition>

    <!-- 聊天区域（不受会话列表影响） -->
    <div class="chat-container">
      <!-- 上下文摘要（仅内部使用，不显示给用户） -->
      <div v-if="contextSummary" class="context-summary" style="display: none;">
        <p class="context-text">{{ contextSummary }}</p>
      </div>

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
              <!-- 加载中动画 -->
              <p v-if="msg.role === 'assistant' && msg.loading" class="loading-text">
                <span class="loading-dot"></span>
                <span class="loading-dot"></span>
                <span class="loading-dot"></span>
              </p>
              <!-- 消息内容 -->
              <div v-else v-html="formatMessage(msg.content)"></div>

              <!-- AI回复：满意度反馈 -->
              <div v-if="msg.role === 'assistant' && !msg.loading && msg.showFeedback" class="feedback-area">
                <span class="feedback-label">回答有帮助吗？</span>
                <button
                  class="feedback-btn thumbs-up"
                  :class="{ active: msg.feedbackRating === 1 }"
                  @click="submitFeedback(msg, 1)"
                  title="有帮助"
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3H14z"/>
                    <path d="M7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/>
                  </svg>
                </button>
                <button
                  class="feedback-btn thumbs-down"
                  :class="{ active: msg.feedbackRating === 0 }"
                  @click="submitFeedback(msg, 0)"
                  title="没帮助"
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M10 15v4a3 3 0 0 0 3 3l4-9V2H5.72a2 2 0 0 0-2 1.7l-1.38 9a2 2 0 0 0 2 2.3H10z"/>
                    <path d="M17 2h2.67A2.31 2.31 0 0 1 22 4v7a2.31 2.31 0 0 1-2.33 2H17"/>
                  </svg>
                </button>
              </div>

              <!-- 反馈提交后提示 -->
              <div v-if="msg.role === 'assistant' && !msg.loading && msg.feedbackSubmitted" class="feedback-thanks">
                {{ msg.feedbackRating === 1 ? '👍 感谢你的认可！' : '📝 已记录，我们会继续优化' }}
              </div>
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
import { ref, computed, nextTick, onMounted, onUnmounted, watch } from 'vue'
import { aiApi } from '@/api/article'
import { useAIContext } from '@/composables/useAIContext'

// AI 上下文管理
const { saveContextSummary, getContextSummary, incrementMessageCount, shouldSummarize, clearContext: clearCtx, getSummaryText, getTopics } = useAIContext()

const SESSION_KEY = 'ai_chat_session_id'
const USER_SESSION_KEY = 'ai_chat_user_session'
const MAX_HISTORY = 20
const MAX_SESSION_DISPLAY = 3

const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const messagesContainer = ref(null)
const sessionPopup = ref(null)
const sessionId = ref('')
const currentSessionId = ref('')
const userId = ref(null)
const isLoggedIn = ref(false)
const showSessionList = ref(false)
const sessionList = ref([])

// AI 上下文摘要
const contextSummary = ref('')
const contextTopics = ref([])

// 最多显示3条会话
const limitedSessionList = computed(() => sessionList.value.slice(0, MAX_SESSION_DISPLAY))

const quickQuestions = [
  '推荐一个适合新手的门派',
  '青云门怎么加点',
  '60级副本攻略',
  '装备强化技巧',
]

// ========== 满意度反馈 ==========
const submitFeedback = async (msg, rating) => {
  if (msg.feedbackRating !== null) return

  msg.feedbackRating = rating
  msg.feedbackSubmitted = true

  try {
    await aiApi.submitFeedback({
      sessionId: sessionId.value,
      question: msg.question || '',
      answer: msg.content,
      rating: rating
    })
  } catch (e) {
    console.error('反馈提交失败', e)
  }
}

// ========== 消息格式化 ==========
const formatMessage = (content) => {
  if (!content) return ''

  let html = content

  const escapeHtml = (str) => {
    return str
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
  }

  const segments = []
  let lastIndex = 0
  const codeBlockRegex = /```[\s\S]*?```/g
  let match

  while ((match = codeBlockRegex.exec(content)) !== null) {
    if (match.index > lastIndex) {
      segments.push({ type: 'text', content: content.slice(lastIndex, match.index) })
    }
    segments.push({ type: 'code', content: match[0] })
    lastIndex = match.index + match[0].length
  }

  if (lastIndex < content.length) {
    segments.push({ type: 'text', content: content.slice(lastIndex) })
  }

  const processedSegments = segments.map(segment => {
    if (segment.type === 'code') {
      const codeContent = segment.content
        .replace(/^```(\w*)/, '')
        .replace(/```$/, '')
      return `<pre class="msg-code"><code>${escapeHtml(codeContent)}</code></pre>`
    } else {
      let text = escapeHtml(segment.content)
      text = text.replace(/`([^`]+)`/g, '<code class="msg-inline-code">$1</code>')
      text = text.replace(/^### (.+)$/gm, '<h4 class="msg-h4">$1</h4>')
      text = text.replace(/^## (.+)$/gm, '<h3 class="msg-h3">$1</h3>')
      text = text.replace(/^# (.+)$/gm, '<h2 class="msg-h2">$1</h2>')
      text = text.replace(/^&gt; (.+)$/gm, '<blockquote class="msg-quote">$1</blockquote>')
      text = text.replace(/^[-*] (.+)$/gm, '<li class="msg-li">$1</li>')
      text = text.replace(/(<li class="msg-li">.+?<\/li>\n?)+/g, '<ul class="msg-ul">$&</ul>')
      text = text.replace(/\*\*\*(.+?)\*\*\*/g, '<strong class="msg-bold-italic">$1</strong>')
      text = text.replace(/\*\*(.+?)\*\*/g, '<strong class="msg-bold">$1</strong>')
      text = text.replace(/\*(.+?)\*/g, '<em class="msg-italic">$1</em>')
      text = text.replace(/^---+$/gm, '<hr class="msg-hr">')
      text = text.replace(/\n\n/g, '</p><p class="msg-p">')
      text = text.replace(/\n/g, '<br>')
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

// ========== 用户信息 ==========
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

// ========== 历史消息加载 ==========
const loadHistory = async () => {
  getUserInfo()

  const savedUserSession = localStorage.getItem(USER_SESSION_KEY)
  const currentUserId = userId.value || 'guest'
  const previousUserId = savedUserSession || 'guest'

  const userLoggedOut = previousUserId !== 'guest' && currentUserId === 'guest'

  if (userLoggedOut) {
    localStorage.setItem(USER_SESSION_KEY, currentUserId)
    const newSid = 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
    localStorage.setItem(SESSION_KEY, newSid)
    sessionId.value = newSid
    currentSessionId.value = newSid
    messages.value = []
    sessionList.value = []
    return
  }

  localStorage.setItem(USER_SESSION_KEY, currentUserId)

  let sid = localStorage.getItem(SESSION_KEY)
  if (!sid) {
    sid = 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
    localStorage.setItem(SESSION_KEY, sid)
  }
  sessionId.value = sid
  currentSessionId.value = sid

  try {
    if (isLoggedIn.value && userId.value) {
      await loadSessionList()
    } else {
      await loadSessionMessages(sid)
    }
  } catch (error) {
    console.error('加载历史消息失败:', error)
  }
}

const loadSessionList = async () => {
  try {
    const response = await fetch('/api/ai/history', {
      headers: { 'X-User-Id': userId.value.toString() }
    })
    const data = await response.json()

    if (data.code === 200 && data.data) {
      sessionList.value = data.data
      if (sessionList.value.length > 0) {
        const latestSession = sessionList.value[0]
        currentSessionId.value = latestSession.sessionId
        localStorage.setItem(SESSION_KEY, latestSession.sessionId)
        sessionId.value = latestSession.sessionId
        await loadSessionMessages(latestSession.sessionId)
      }
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
  }
}

const loadSessionMessages = async (sid) => {
  try {
    const response = await fetch(`/api/ai/session/${encodeURIComponent(sid)}`)
    const data = await response.json()

    if (data.code === 200 && data.data && data.data.length > 0) {
      const history = data.data.slice(-MAX_HISTORY)
      messages.value = history.map(h => ({
        role: h.role === 'user' ? 'user' : 'assistant',
        content: h.content,
        time: getCurrentTime(),
        showFeedback: false,
        feedbackRating: null,
        feedbackSubmitted: false,
        references: []
      }))
      scrollToBottom()
    } else {
      messages.value = []
    }

    // 加载上下文摘要
    loadContextSummary(sid)
  } catch (error) {
    console.error('加载会话消息失败:', error)
    messages.value = []
  }
}

// 加载上下文摘要
const loadContextSummary = (sid) => {
  const summaryData = getContextSummary(sid)
  if (summaryData) {
    contextSummary.value = summaryData.summary
    contextTopics.value = summaryData.topics || []
  } else {
    contextSummary.value = ''
    contextTopics.value = []
  }
}

// 清除上下文摘要
const clearContextSummary = () => {
  clearCtx(sessionId.value)
  contextSummary.value = ''
  contextTopics.value = []
}

// 生成上下文摘要
const generateSummary = async () => {
  if (!sessionId.value) return

  try {
    const response = await fetch('/api/ai/summarize', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(userId.value ? { 'X-User-Id': userId.value.toString() } : {})
      },
      body: JSON.stringify({ sessionId: sessionId.value })
    })

    const data = await response.json()
    if (data.code === 200 && data.data) {
      const summary = data.data.summary || ''
      const topics = data.data.topics || []

      saveContextSummary(sessionId.value, summary, topics)
      contextSummary.value = summary
      contextTopics.value = topics
    }
  } catch (e) {
    console.error('生成摘要失败:', e)
  }
}

const switchSession = async (sid) => {
  currentSessionId.value = sid
  sessionId.value = sid
  localStorage.setItem(SESSION_KEY, sid)
  showSessionList.value = false
  await loadSessionMessages(sid)
}

const createNewSession = async () => {
  sessionId.value = 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
  currentSessionId.value = sessionId.value
  localStorage.setItem(SESSION_KEY, sessionId.value)
  messages.value = []
  showSessionList.value = false
  // 清除上下文摘要
  contextSummary.value = ''
  contextTopics.value = []
}

const deleteSession = async (sid) => {
  if (!confirm('确定要删除这个会话吗？')) return

  try {
    const response = await fetch(`/api/ai/session/${encodeURIComponent(sid)}`, {
      method: 'DELETE',
      headers: { 'X-User-Id': userId.value.toString() }
    })
    const data = await response.json()

    if (data.code === 200) {
      sessionList.value = sessionList.value.filter(s => s.sessionId !== sid)
      if (currentSessionId.value === sid) {
        await createNewSession()
      }
    }
  } catch (error) {
    console.error('删除会话失败:', error)
  }
}

// ========== 发送消息（打字机效果） ==========
const sendMessage = async () => {
  const content = inputMessage.value.trim()
  if (!content || loading.value) return

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content,
    time: getCurrentTime(),
    showFeedback: false,
    feedbackRating: null,
    feedbackSubmitted: false,
    references: []
  })

  inputMessage.value = ''
  scrollToBottom()

  // 添加 AI 消息（加载中状态）
  const aiMessageIndex = messages.value.length
  messages.value.push({
    role: 'assistant',
    content: '',
    loading: true,
    time: getCurrentTime(),
    showFeedback: false,
    feedbackRating: null,
    feedbackSubmitted: false,
    references: [],
    question: content
  })

  loading.value = true

  try {
    const headers = { 'Content-Type': 'application/json' }
    if (isLoggedIn.value && userId.value) {
      headers['X-User-Id'] = userId.value.toString()
    }

    const response = await fetch('/api/ai/chat', {
      method: 'POST',
      headers,
      body: JSON.stringify({ question: content, sessionId: sessionId.value })
    })

    const data = await response.json()

    if (data.code === 200) {
      // 保存返回的sessionId
      if (data.data?.sessionId) {
        sessionId.value = data.data.sessionId
        currentSessionId.value = data.data.sessionId
        localStorage.setItem(SESSION_KEY, data.data.sessionId)
      }

      // 更新消息：清除loading，开始打字机效果
      messages.value[aiMessageIndex].loading = false

      // 打字机效果
      const fullAnswer = data.data?.answer || '抱歉，我没有获取到有效的回复。'
      const references = data.data?.references || []
      const hasKnowledge = data.data?.hasKnowledge || false

      await typewriterEffect(messages.value[aiMessageIndex], fullAnswer, references, hasKnowledge)
    } else {
      messages.value[aiMessageIndex].loading = false
      messages.value[aiMessageIndex].content = data.message || '请求失败，请稍后重试。'
    }
  } catch (error) {
    console.error('AI 请求失败:', error)
    messages.value[aiMessageIndex].loading = false
    messages.value[aiMessageIndex].content = '抱歉，服务暂时不可用，请稍后重试。'
  }

  loading.value = false
  scrollToBottom()

  // 检查是否需要生成上下文摘要
  if (messages.value.length >= 3) {
    incrementMessageCount(sessionId.value)
    if (shouldSummarize(sessionId.value)) {
      // 在后台生成摘要，不阻塞用户操作
      generateSummary()
    }
  }
}

// ========== 打字机效果 ==========
const typewriterEffect = async (msgObj, fullText, references, hasKnowledge) => {
  msgObj.content = ''
  msgObj.references = references
  msgObj.hasKnowledge = hasKnowledge

  // 分段渲染：每20-40个字符渲染一次
  const chunkSize = () => Math.floor(Math.random() * 20) + 20
  let displayed = ''

  for (let i = 0; i < fullText.length; i += chunkSize()) {
    const end = Math.min(i + chunkSize(), fullText.length)
    displayed += fullText.slice(i, end)
    msgObj.content = displayed

    // 显示到引用区域
    if (references && references.length > 0) {
      msgObj.references = references
    }

    scrollToBottom()
    await new Promise(r => setTimeout(r, 25))
  }

  // 确保最终内容完整
  msgObj.content = fullText

  // 显示满意度反馈（延迟2秒后出现）
  setTimeout(() => {
    msgObj.showFeedback = true
  }, 2000)
}

// 点击外部关闭会话列表
const handleOutsideClick = (e) => {
  if (!showSessionList.value) return
  const popup = sessionPopup.value
  const btn = document.querySelector('.session-btn')
  if (popup && !popup.contains(e.target) && btn && !btn.contains(e.target)) {
    showSessionList.value = false
  }
}

onMounted(() => {
  loadHistory()
  document.addEventListener('click', handleOutsideClick)
})

onUnmounted(() => {
  document.removeEventListener('click', handleOutsideClick)
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

.header-left { text-align: left; }

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

/* 会话列表浮层 */
.session-popup {
  position: absolute;
  top: 84px;
  right: 0;
  width: 300px;
  max-height: 380px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: 0 8px 32px rgba(0,0,0,0.12);
  z-index: 200;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 会话列表浮层动画 */
.session-fade-enter-active,
.session-fade-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}
.session-fade-enter-from,
.session-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
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

.new-session-btn:hover { background: var(--color-cinnabar-light); }

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

.session-item:hover { background: var(--color-paper); }
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

.session-limit-tip {
  text-align: center;
  padding: 10px 12px;
  color: var(--color-ink-muted);
  font-size: 11px;
  background: var(--color-paper);
  border-top: 1px solid var(--color-border);
  margin-top: 4px;
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

/* 上下文摘要 */
.context-summary {
  margin: 12px 20px;
  padding: 12px 16px;
  background: linear-gradient(135deg, rgba(196, 92, 72, 0.08), rgba(196, 92, 72, 0.04));
  border: 1px solid rgba(196, 92, 72, 0.2);
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}

.context-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.context-icon {
  font-size: 14px;
}

.context-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-cinnabar);
}

.context-clear {
  margin-left: auto;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  color: var(--color-ink-muted);
  cursor: pointer;
  font-size: 16px;
  border-radius: 50%;
  transition: all 0.2s;
}

.context-clear:hover {
  background: rgba(196, 92, 72, 0.15);
  color: var(--color-cinnabar);
}

.context-text {
  font-size: 12px;
  color: var(--color-ink-light);
  line-height: 1.6;
  margin: 0 0 8px 0;
}

.context-topics {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.topic-tag {
  padding: 2px 8px;
  background: rgba(196, 92, 72, 0.1);
  color: var(--color-cinnabar);
  border-radius: 10px;
  font-size: 11px;
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
}

.quick-question:hover {
  background: var(--color-ink);
  color: var(--color-white);
  border-color: var(--color-ink);
}

/* 消息 */
.message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.message.user {
  flex-direction: row-reverse;
}

.message.user .message-content { align-items: flex-end; }

.message.user .message-bubble {
  background: linear-gradient(135deg, #c45c48, #e07b6d);
  color: #fff;
  border: none;
  box-shadow: 0 4px 12px rgba(196, 92, 72, 0.25);
}

.message.user .message-time { text-align: right; }

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
}

.message-bubble p { margin: 0; }

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

/* 满意度反馈 */
.feedback-area {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid rgba(0,0,0,0.06);
}

.feedback-label {
  font-size: 12px;
  color: var(--color-ink-muted);
}

.feedback-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 1px solid var(--color-border);
  background: var(--color-white);
  color: var(--color-ink-muted);
  cursor: pointer;
  transition: all 0.2s;
}

.feedback-btn:hover {
  border-color: var(--color-cinnabar);
  color: var(--color-cinnabar);
}

.feedback-btn.thumbs-up.active {
  background: #27ae60;
  border-color: #27ae60;
  color: #fff;
}

.feedback-btn.thumbs-down.active {
  background: #e74c3c;
  border-color: #e74c3c;
  color: #fff;
}

.feedback-thanks {
  font-size: 12px;
  color: var(--color-ink-muted);
  margin-top: 8px;
  font-style: italic;
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
}

.loading-dot:nth-child(1) { animation-delay: -0.32s; }
.loading-dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes loading {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
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
  font-family: inherit;
}

.chat-input:focus {
  outline: none;
  border-color: var(--color-ochre);
}

.chat-input::placeholder { color: var(--color-ink-muted); }

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
}

.send-btn:hover:not(:disabled) { background: var(--color-cinnabar-light); }
.send-btn:disabled {
  background: var(--color-border);
  cursor: not-allowed;
}

.input-hint {
  font-size: 11px;
  color: var(--color-ink-muted);
  margin-top: 8px;
  text-align: center;
}

/* Markdown 样式 */
.message-bubble :deep(.msg-p) {
  margin: 0 0 10px 0;
  line-height: 1.7;
}

.message-bubble :deep(.msg-p:last-child) { margin-bottom: 0; }

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

/* 响应式 */
@media (max-width: 768px) {
  .ai-chat-page {
    height: calc(100vh - 56px - 56px);
    padding-bottom: 56px;
  }

  .page-header {
    padding: 16px;
    margin-bottom: 12px;
  }

  .page-title {
    font-size: 18px;
  }

  .page-desc {
    font-size: 12px;
  }

  .session-btn {
    padding: 6px 12px;
    font-size: 12px;
  }

  .session-popup {
    top: 70px;
    right: 8px;
    width: 260px;
  }

  .chat-container {
    border-radius: 0;
    border-left: none;
    border-right: none;
  }

  .welcome-message {
    padding: 24px 16px;
  }

  .welcome-icon {
    width: 48px;
    height: 48px;
    font-size: 24px;
  }

  .welcome-message h3 {
    font-size: 16px;
  }

  .quick-questions {
    gap: 6px;
  }

  .quick-question {
    padding: 6px 10px;
    font-size: 12px;
  }

  .chat-messages {
    padding: 16px;
  }

  .message-content {
    max-width: 88%;
  }

  .message-bubble {
    padding: 12px 14px;
    font-size: 14px;
  }

  .message-avatar {
    width: 32px;
    height: 32px;
    font-size: 14px;
  }

  .message-time {
    font-size: 10px;
  }

  .feedback-area {
    gap: 6px;
  }

  .feedback-label {
    font-size: 11px;
  }

  .feedback-btn {
    width: 28px;
    height: 28px;
  }

  .chat-input-area {
    padding: 12px 16px;
  }

  .chat-input {
    padding: 10px 14px;
    font-size: 15px;
  }

  .send-btn {
    padding: 10px 18px;
    font-size: 13px;
  }
}

/* 超小屏幕优化 */
@media (max-width: 375px) {
  .ai-chat-page {
    height: calc(100vh - 50px - 50px);
  }

  .message-content {
    max-width: 90%;
  }

  .message-bubble {
    padding: 10px 12px;
    font-size: 13px;
  }
}
</style>
