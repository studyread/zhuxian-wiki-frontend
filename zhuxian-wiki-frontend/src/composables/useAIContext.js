/**
 * AI 上下文记忆管理组合式函数
 *
 * 功能：
 * 1. 在 localStorage 中存储对话上下文摘要
 * 2. 摘要自动在 7 天后过期
 * 3. 当对话消息达到 5 条时，触发摘要生成
 * 4. 摘要包含对话主题关键词
 */

import { ref } from 'vue'

const CONTEXT_KEY = 'ai_context_'
const SUMMARIZE_THRESHOLD = 5  // 5条消息后触发摘要
const CONTEXT_TTL = 7 * 24 * 60 * 60 * 1000  // 7天过期（毫秒）

/**
 * 获取所有会话的上下文摘要列表
 */
export function getAllContextSummaries() {
  const summaries = []
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i)
    if (key && key.startsWith(CONTEXT_KEY)) {
      try {
        const data = JSON.parse(localStorage.getItem(key))
        summaries.push({
          sessionId: key.replace(CONTEXT_KEY, ''),
          ...data
        })
      } catch (e) {
        // ignore parse error
      }
    }
  }
  return summaries
}

/**
 * AI 上下文管理组合式函数
 */
export function useAIContext() {
  /**
   * 保存上下文摘要到 localStorage
   * @param {string} sessionId - 会话ID
   * @param {string} summary - 摘要内容
   * @param {Array} topics - 主题关键词列表
   */
  const saveContextSummary = (sessionId, summary, topics = []) => {
    const key = CONTEXT_KEY + sessionId
    const data = {
      summary,
      topics,
      lastUpdate: Date.now(),
      messageCount: 0
    }
    localStorage.setItem(key, JSON.stringify(data))
  }

  /**
   * 获取上下文摘要
   * @param {string} sessionId - 会话ID
   * @returns {Object|null} 摘要数据或null（过期或不存在）
   */
  const getContextSummary = (sessionId) => {
    const key = CONTEXT_KEY + sessionId
    const data = localStorage.getItem(key)
    if (!data) return null

    try {
      const parsed = JSON.parse(data)
      // 检查7天过期
      if (Date.now() - parsed.lastUpdate > CONTEXT_TTL) {
        localStorage.removeItem(key)
        return null
      }
      return parsed
    } catch (e) {
      return null
    }
  }

  /**
   * 更新消息计数
   * @param {string} sessionId - 会话ID
   */
  const incrementMessageCount = (sessionId) => {
    const key = CONTEXT_KEY + sessionId
    const data = getContextSummary(sessionId)
    if (data) {
      data.messageCount++
      data.lastUpdate = Date.now()
      localStorage.setItem(key, JSON.stringify(data))
    }
  }

  /**
   * 重置消息计数
   * @param {string} sessionId - 会话ID
   */
  const resetMessageCount = (sessionId) => {
    const key = CONTEXT_KEY + sessionId
    const data = getContextSummary(sessionId)
    if (data) {
      data.messageCount = 0
      data.lastUpdate = Date.now()
      localStorage.setItem(key, JSON.stringify(data))
    }
  }

  /**
   * 是否需要生成摘要
   * @param {string} sessionId - 会话ID
   * @returns {boolean}
   */
  const shouldSummarize = (sessionId) => {
    const data = getContextSummary(sessionId)
    return !data || data.messageCount >= SUMMARIZE_THRESHOLD
  }

  /**
   * 获取摘要文本（用于显示）
   * @param {string} sessionId - 会话ID
   * @returns {string}
   */
  const getSummaryText = (sessionId) => {
    const data = getContextSummary(sessionId)
    return data?.summary || ''
  }

  /**
   * 获取主题关键词
   * @param {string} sessionId - 会话ID
   * @returns {Array}
   */
  const getTopics = (sessionId) => {
    const data = getContextSummary(sessionId)
    return data?.topics || []
  }

  /**
   * 清除上下文
   * @param {string} sessionId - 会话ID
   */
  const clearContext = (sessionId) => {
    localStorage.removeItem(CONTEXT_KEY + sessionId)
  }

  /**
   * 清除所有上下文
   */
  const clearAllContexts = () => {
    const keysToRemove = []
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i)
      if (key && key.startsWith(CONTEXT_KEY)) {
        keysToRemove.push(key)
      }
    }
    keysToRemove.forEach(key => localStorage.removeItem(key))
  }

  /**
   * 获取上下文摘要并格式化为提示文本
   * @param {string} sessionId - 会话ID
   * @returns {string} 格式化的上下文提示
   */
  const getContextualPrompt = (sessionId) => {
    const data = getContextSummary(sessionId)
    if (!data || !data.summary) return ''

    let prompt = `【对话摘要】\n${data.summary}`
    if (data.topics && data.topics.length > 0) {
      prompt += `\n\n涉及主题：${data.topics.join('、')}`
    }
    return prompt
  }

  return {
    saveContextSummary,
    getContextSummary,
    incrementMessageCount,
    resetMessageCount,
    shouldSummarize,
    getSummaryText,
    getTopics,
    clearContext,
    clearAllContexts,
    getContextualPrompt,
    SUMMARIZE_THRESHOLD,
    CONTEXT_TTL
  }
}
