<template>
  <div class="ai-chat-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">游戏百科助手</h1>
      <p class="page-desc">基于游戏知识库，AI 智能解答各类问题</p>
    </div>

    <!-- 聊天区域 -->
    <div class="chat-container">
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
import { ref, nextTick } from 'vue'

const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const messagesContainer = ref(null)

const quickQuestions = [
  '推荐一个适合新手的门派',
  '青云门怎么加点',
  '60级副本攻略',
  '装备强化技巧',
]

const formatMessage = (content) => {
  // 简单的文本格式化
  return content
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
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
    const response = await fetch('http://localhost:8080/api/ai/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ question: content })  // 修正：改为 question
    })

    const data = await response.json()
    
    // 更新 AI 消息
    if (data.code === 200) {
      messages.value[aiMessageIndex] = {
        role: 'assistant',
        content: data.data?.answer || '抱歉，我没有获取到有效的回复。',
        time: getCurrentTime()
      }
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
</script>

<style scoped>
.ai-chat-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
  min-height: 500px;
}

/* 页面标题 */
.page-header {
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 24px;
  margin-bottom: 20px;
  text-align: center;
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
      background: var(--color-ink);
      color: var(--color-white);
    }
    
    .message-time {
      text-align: right;
    }
  }
}

.message-avatar {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-family: var(--font-serif);
  background: var(--color-paper-dark);
  color: var(--color-ink);
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}

.message.user .message-avatar {
  background: var(--color-cinnabar);
  color: var(--color-white);
}

.message-content {
  display: flex;
  flex-direction: column;
  max-width: 70%;
}

.message-bubble {
  padding: 12px 16px;
  font-size: 14px;
  line-height: 1.6;
  background: var(--color-paper);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  color: var(--color-ink);
  
  p {
    margin: 0;
  }
}

.message-time {
  font-size: 11px;
  color: var(--color-ink-muted);
  margin-top: 4px;
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
