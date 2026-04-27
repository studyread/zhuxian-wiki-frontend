package com.zhuxianwiki.AI;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zhuxianwiki.entity.Article;
import com.zhuxianwiki.entity.ChatHistory;
import com.zhuxianwiki.entity.KnowledgeEntry;
import com.zhuxianwiki.mapper.ArticleMapper;
import com.zhuxianwiki.mapper.ChatHistoryMapper;
import com.zhuxianwiki.mapper.KnowledgeEntryMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AIService {

    private static final Logger log = LoggerFactory.getLogger(AIService.class);

    @Value("${ai.api-url}")
    private String apiUrl;

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.model}")
    private String model;

    @Value("${ai.max-tokens}")
    private int maxTokens;

    @Value("${ai.temperature}")
    private double temperature;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private ChatHistoryMapper chatHistoryMapper;

    @Autowired
    private KnowledgeEntryMapper knowledgeEntryMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String SYSTEM_PROMPT = """
        你是诛仙世界游戏攻略助手，请严格遵守以下规则：

        1. **知识库优先原则**：必须首先从知识库和攻略文章中检索相关内容再回答
        2. **准确引用原则**：当有知识库内容时，必须直接引用原文内容，不要转述或概括，而是告诉用户具体内容
        3. **诚实回答原则**：当知识库和攻略都无相关内容时，必须如实告知用户
        4. **禁止胡编乱造**：不要基于模糊记忆或推理来回答游戏细节
        5. **回答格式**：使用Markdown格式化回答，善用引用块展示原文内容

        【重要】当有知识库内容时，你的回答必须：
        - 直接引用原文内容（使用引用块 > 格式）
        - 结构化展示信息

        当没有相关内容时，明确告知用户并引导查看其他功能。
        """;

    public String chat(String question, String sessionId, Long userId) {
        try {
            // 1. 知识库优先搜索
            List<KnowledgeEntry> knowledgeEntries = searchKnowledge(question);
            List<Article> articles = searchArticles(question);

            boolean hasKnowledge = knowledgeEntries != null && !knowledgeEntries.isEmpty();
            boolean hasArticles = articles != null && !articles.isEmpty();
            boolean hasContent = hasKnowledge || hasArticles;

            // 2. 构建上下文
            String context = buildContext(knowledgeEntries, articles);

            // 3. 构建用户提示词（优化输出格式）
            String userPrompt;
            if (hasContent) {
                StringBuilder prompt = new StringBuilder();
                prompt.append(context);
                prompt.append("\n\n【用户问题】\n").append(question);
                prompt.append("\n\n【回答要求】\n");
                prompt.append("1. 优先使用知识库内容回答\n");
                prompt.append("2. 使用引用块（>）直接展示原文关键内容\n");
                prompt.append("3. 如有攻略文章，提示用户可在网站查看完整攻略\n");
                prompt.append("4. 回答要准确、完整，不要遗漏重要信息\n");
                userPrompt = prompt.toString();
            } else {
                userPrompt = context + "\n\n用户问题：" + question + "\n请告知用户当前网站暂无相关信息，建议查看其他分类或稍后再来查询。";
            }

            saveChatHistory(sessionId, "user", question, userId);
            String response = callAI(userPrompt);
            saveChatHistory(sessionId, "assistant", response, userId);

            return response;
        } catch (Exception e) {
            log.error("AI服务异常: {}", e.getMessage(), e);
            return "抱歉，AI服务暂时不可用：" + e.getMessage();
        }
    }

    /**
     * 搜索知识库词条 - 优化版：分词搜索
     * 把用户问题拆成多个关键词，任一匹配即可
     */
    private List<KnowledgeEntry> searchKnowledge(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            // 提取关键词：中文按长度切割，英文按空格分词
            List<String> keywords = extractKeywords(keyword);
            log.info("提取的搜索关键词: {}", keywords);
            
            if (keywords.isEmpty()) {
                return Collections.emptyList();
            }
            
            // 优先用分词搜索
            List<KnowledgeEntry> results = knowledgeEntryMapper.searchByKeywords(keywords);
            
            // 如果分词搜索结果太少，再用单关键词搜索作为补充
            if (results.size() < 3) {
                List<KnowledgeEntry> fallback = knowledgeEntryMapper.searchByKeyword(keywords.get(0));
                for (KnowledgeEntry entry : fallback) {
                    if (!results.contains(entry)) {
                        results.add(entry);
                    }
                }
            }
            
            return results;
        } catch (Exception e) {
            log.error("搜索知识库失败: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 提取搜索关键词
     * 中文：每2-4个字符作为一个词
     * 英文：按空格分词
     */
    private List<String> extractKeywords(String text) {
        List<String> keywords = new ArrayList<>();
        
        // 移除标点符号
        String cleaned = text.replaceAll("[，。！？、：；\"\"''【】《》（）\\-—_.,!?():;\\[\\]{}]", " ");
        
        // 移除常见停用词
        String[] stopWords = {"的", "是", "在", "有", "和", "与", "或", "以及", "请问", "怎么", "如何", "什么", "哪个", "哪里", "为什么", "能不能", "可以", "帮我", "告诉", "一下", "吗", "呢", "吧", "啊"};
        for (String stop : stopWords) {
            cleaned = cleaned.replace(stop, " ");
        }
        
        // 中文分词：每2-4个连续中文字符作为一个关键词
        String chinesePattern = "[\\u4e00-\\u9fa5]{2,6}";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(chinesePattern);
        java.util.regex.Matcher matcher = pattern.matcher(cleaned);
        while (matcher.find()) {
            String word = matcher.group();
            if (word.length() >= 2) {
                keywords.add(word);
                // 同时添加前2字和前3字作为备选（如"云梦宗门" -> "云梦", "云梦宗"）
                if (word.length() >= 3) {
                    keywords.add(word.substring(0, 2));
                    keywords.add(word.substring(0, 3));
                }
            }
        }
        
        // 英文分词
        String[] englishWords = cleaned.split("\\s+");
        for (String word : englishWords) {
            word = word.trim();
            if (word.length() >= 2 && !word.matches(".*\\d+.*")) {
                keywords.add(word);
            }
        }
        
        // 去重
        return keywords.stream().distinct().limit(10).collect(Collectors.toList());
    }

    /**
     * 搜索攻略文章 - 优化版：多重关键词搜索
     * 搜索时同时使用原始问题和提取的关键词，确保更全面匹配
     */
    private List<Article> searchArticles(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            // 提取关键词
            List<String> keywords = extractKeywords(keyword);
            if (keywords.isEmpty()) {
                return Collections.emptyList();
            }
            
            // 使用多个关键词搜索（更全面匹配）
            List<Article> results = new java.util.ArrayList<>();
            java.util.Set<Long> addedIds = new java.util.HashSet<>(); // 用于去重
            
            // 1. 用原始问题搜索（包含多词的完整查询，优先级最高）
            if (keyword.length() >= 2) {
                List<Article> original = articleMapper.searchArticles(keyword);
                if (original != null) {
                    for (Article a : original) {
                        if (addedIds.add(a.getId())) {
                            results.add(a);
                        }
                    }
                }
            }
            
            // 2. 用所有提取的关键词搜索（最多取前3个）
            int maxKeywords = Math.min(keywords.size(), 3);
            for (int i = 0; i < maxKeywords; i++) {
                List<Article> found = articleMapper.searchArticles(keywords.get(i));
                if (found != null) {
                    for (Article a : found) {
                        if (addedIds.add(a.getId())) {
                            results.add(a);
                        }
                    }
                }
            }
            
            // 去重并限制返回数量
            return results.stream().limit(5).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("搜索攻略文章失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 构建上下文（攻略文章 + 知识库）
     * 攻略文章优先显示，因为是用户贡献的最新内容
     */
    private String buildContext(List<KnowledgeEntry> knowledgeEntries, List<Article> articles) {
        StringBuilder sb = new StringBuilder();
        sb.append("【网站内容检索结果】\n\n");

        // 优先添加攻略文章内容（用户贡献的最新攻略）
        if (articles != null && !articles.isEmpty()) {
            sb.append("【攻略文章】（来自网站用户的最新攻略）\n");
            for (int i = 0; i < Math.min(articles.size(), 3); i++) {
                Article article = articles.get(i);
                sb.append("## 攻略：").append(article.getTitle()).append("\n");
                if (article.getSummary() != null && !article.getSummary().isEmpty()) {
                    sb.append("简介：").append(article.getSummary()).append("\n");
                }
                String content = article.getContent();
                if (content != null && !content.isEmpty()) {
                    // 处理Markdown内容：移除多余的空行，保留基本格式
                    content = cleanMarkdownContent(content);
                    // 增加内容长度到1500字符
                    if (content.length() > 1500) {
                        content = content.substring(0, 1500) + "\n...(内容已截断，请访问网站查看完整攻略)";
                    }
                    sb.append("原文内容：\n").append(content);
                }
                sb.append("\n---\n");
            }
            sb.append("\n");
        }

        // 其次添加知识库内容
        if (knowledgeEntries != null && !knowledgeEntries.isEmpty()) {
            sb.append("【知识库词条】\n");
            for (int i = 0; i < Math.min(knowledgeEntries.size(), 5); i++) {
                KnowledgeEntry entry = knowledgeEntries.get(i);
                sb.append("## 词条：").append(entry.getTitle()).append("\n");
                if (entry.getSummary() != null && !entry.getSummary().isEmpty()) {
                    sb.append("摘要：").append(entry.getSummary()).append("\n");
                }
                String content = entry.getContent();
                if (content != null && !content.isEmpty()) {
                    // 增加内容长度到800字符
                    if (content.length() > 800) {
                        content = content.substring(0, 800) + "\n...(内容已截断)";
                    }
                    sb.append("原文内容：\n").append(content);
                }
                sb.append("\n---\n");
            }
            sb.append("\n");
        }

        if ((knowledgeEntries == null || knowledgeEntries.isEmpty())
         && (articles == null || articles.isEmpty())) {
            sb.append("【检索结果】\n当前网站暂无相关内容，敬请期待！\n");
        }

        return sb.toString();
    }

    private String callAI(String userPrompt) throws Exception {
        log.info("调用AI API, prompt长度: {}", userPrompt.length());

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(apiUrl);
            request.setHeader("Authorization", "Bearer " + apiKey);
            request.setHeader("Content-Type", "application/json");

            JSONObject body = new JSONObject();
            body.put("model", model);
            body.put("max_tokens", maxTokens);
            body.put("temperature", temperature);

            JSONArray messages = new JSONArray();

            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", SYSTEM_PROMPT);
            messages.add(systemMsg);

            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", userPrompt);
            messages.add(userMsg);

            body.put("messages", messages);

            String jsonBody = body.toJSONString();
            log.debug("请求体: {}", jsonBody);

            request.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = client.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                log.info("AI API响应状态码: {}, 响应体: {}", statusCode, responseBody);

                if (statusCode != 200) {
                    return "AI服务返回错误，状态码: " + statusCode + "，请检查API配置。";
                }

                JSONObject jsonResponse = JSON.parseObject(responseBody);

                if (jsonResponse.containsKey("choices")) {
                    JSONArray choices = jsonResponse.getJSONArray("choices");
                    if (choices != null && !choices.isEmpty()) {
                        return choices.getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");
                    }
                }

                log.warn("AI响应格式异常: {}", responseBody);
                return "抱歉，未能获取到有效的回复。响应: " + responseBody;
            }
        }
    }

    private static final int MAX_HISTORY_SIZE = 10; // 最多保留10条消息

    private void saveChatHistory(String sessionId, String role, String content, Long userId) {
        try {
            // 先检查当前会话的记录数量
            int currentCount = chatHistoryMapper.countBySessionId(sessionId);
            
            // 如果记录数已达到上限，删除最老的2条（用户消息+AI回复）
            if (currentCount >= MAX_HISTORY_SIZE) {
                int deleteCount = 2; // 每轮对话包含用户消息和AI回复，删除2条
                chatHistoryMapper.deleteOldestBySessionId(sessionId, deleteCount);
                log.info("聊天记录超限，已删除最老的{}条记录", deleteCount);
            }
            
            // 保存新记录
            ChatHistory history = new ChatHistory();
            history.setUserId(userId);
            history.setSessionId(sessionId);
            history.setRole(role);
            history.setContent(content != null ? content : "");
            history.setCreatedAt(java.time.LocalDateTime.now());
            chatHistoryMapper.insertWithContent(history);
        } catch (Exception e) {
            log.error("保存聊天记录失败: {}", e.getMessage());
        }
    }

    public List<Map<String, String>> getChatHistory(String sessionId) {
        List<ChatHistory> histories = chatHistoryMapper.selectBySessionId(sessionId);
        if (histories == null) {
            return Collections.emptyList();
        }
        return histories.stream()
            .map(h -> {
                Map<String, String> map = new HashMap<>();
                map.put("role", h.getRole());
                map.put("content", h.getContent());
                return map;
            })
            .collect(Collectors.toList());
    }

    public void clearChatHistory(String sessionId) {
        try {
            chatHistoryMapper.clearSessionHistory(sessionId);
        } catch (Exception e) {
            log.error("清除聊天记录失败: {}", e.getMessage());
        }
    }

    // 根据用户ID获取聊天记录
    public List<Map<String, String>> getChatHistoryByUserId(Long userId) {
        // 查询所有以 "user_{userId}_" 开头的 sessionId 的记录
        // 然后按时间排序返回最近的记录
        try {
            // 使用 LambdaQueryWrapper 查询 - 获取最近40条记录（10轮对话）
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatHistory> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.like(ChatHistory::getSessionId, "user_" + userId + "_")
                   .orderByDesc(ChatHistory::getCreatedAt)
                   .last("LIMIT 40");  // 改为40条，支持10轮完整对话
            
            List<ChatHistory> histories = chatHistoryMapper.selectList(wrapper);
            if (histories == null || histories.isEmpty()) {
                return Collections.emptyList();
            }
            
            // 返回完整对话（按 sessionId 分组，然后按时间排序）
            // 获取该用户最近的10轮完整对话
            Map<String, List<ChatHistory>> sessionMap = new LinkedHashMap<>();
            for (ChatHistory h : histories) {
                String sid = h.getSessionId();
                sessionMap.computeIfAbsent(sid, k -> new ArrayList<>()).add(h);
            }
            
            // 取最新会话的完整历史（支持10轮对话）
            List<Map<String, String>> result = new ArrayList<>();
            String latestSession = sessionMap.keySet().iterator().next();
            List<ChatHistory> latestHistory = sessionMap.get(latestSession);
            latestHistory.sort(Comparator.comparing(ChatHistory::getCreatedAt));
            
            // 只取最新的20条（10轮对话：10条用户 + 10条AI）
            int maxMessages = 20;
            for (int i = Math.max(0, latestHistory.size() - maxMessages); i < latestHistory.size(); i++) {
                ChatHistory h = latestHistory.get(i);
                Map<String, String> map = new HashMap<>();
                map.put("role", h.getRole());
                map.put("content", h.getContent());
                map.put("sessionId", h.getSessionId());
                result.add(map);
            }
            return result;
        } catch (Exception e) {
            log.error("获取用户聊天记录失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 获取用户的所有会话列表
     * 每个会话包含：sessionId、第一条用户消息（作为标题）、消息数量、最后更新时间
     */
    public List<Map<String, Object>> getUserSessionList(Long userId) {
        try {
            List<ChatHistory> allHistories = chatHistoryMapper.selectUserSessionMessages(userId);
            if (allHistories == null || allHistories.isEmpty()) {
                return Collections.emptyList();
            }

            // 按sessionId分组
            Map<String, List<ChatHistory>> sessionMap = new LinkedHashMap<>();
            for (ChatHistory h : allHistories) {
                sessionMap.computeIfAbsent(h.getSessionId(), k -> new ArrayList<>()).add(h);
            }

            // 构建会话列表
            List<Map<String, Object>> sessionList = new ArrayList<>();
            for (Map.Entry<String, List<ChatHistory>> entry : sessionMap.entrySet()) {
                String sessionId = entry.getKey();
                List<ChatHistory> messages = entry.getValue();
                messages.sort(Comparator.comparing(ChatHistory::getCreatedAt));

                Map<String, Object> session = new LinkedHashMap<>();
                session.put("sessionId", sessionId);

                // 第一条用户消息作为标题
                String title = "新会话";
                for (ChatHistory m : messages) {
                    if ("user".equals(m.getRole())) {
                        title = m.getContent();
                        if (title.length() > 30) {
                            title = title.substring(0, 30) + "...";
                        }
                        break;
                    }
                }
                session.put("title", title);
                session.put("messageCount", messages.size());

                // 最后更新时间
                ChatHistory lastMsg = messages.get(messages.size() - 1);
                session.put("lastUpdate", lastMsg.getCreatedAt().toString());

                // 最后一条消息预览（如果是AI回复）
                String preview = "";
                for (int i = messages.size() - 1; i >= 0; i--) {
                    if ("assistant".equals(messages.get(i).getRole())) {
                        preview = messages.get(i).getContent();
                        if (preview.length() > 50) {
                            preview = preview.substring(0, 50) + "...";
                        }
                        break;
                    }
                }
                session.put("preview", preview);

                sessionList.add(session);
            }

            return sessionList;
        } catch (Exception e) {
            log.error("获取用户会话列表失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 清理Markdown内容，避免格式问题导致AI识别乱码
     */
    private String cleanMarkdownContent(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        // 移除多余的连续空行（保留最多一个空行）
        content = content.replaceAll("\\n{3,}", "\n\n");
        // 移除行尾多余空格
        content = content.replaceAll("[ \t]+\n", "\n");
        // 移除行首多余空格
        content = content.replaceAll("\n[ \t]+", "\n");
        // 确保使用标准换行符
        content = content.replace("\r\n", "\n").replace("\r", "\n");
        return content;
    }
}
