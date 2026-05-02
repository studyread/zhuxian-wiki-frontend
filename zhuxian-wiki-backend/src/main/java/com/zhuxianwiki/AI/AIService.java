package com.zhuxianwiki.AI;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zhuxianwiki.entity.AiFeedback;
import com.zhuxianwiki.entity.Article;
import com.zhuxianwiki.entity.ChatHistory;
import com.zhuxianwiki.entity.KnowledgeEntry;
import com.zhuxianwiki.mapper.AiFeedbackMapper;
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
    private AiFeedbackMapper aiFeedbackMapper;

    @Autowired(required = false)
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

    /**
     * 聊天接口 - 返回完整响应信息（答案 + 引用来源）
     * 支持上下文记忆功能
     */
    public Map<String, Object> chat(String question, String sessionId, Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 0. 构建带上下文的提示词（如果之前有对话历史）
            String contextualQuestion = buildContextualPrompt(question, sessionId);

            // 1. 知识库优先搜索
            List<KnowledgeEntry> knowledgeEntries = searchKnowledge(question);
            List<Article> articles = searchArticles(question);

            boolean hasKnowledge = knowledgeEntries != null && !knowledgeEntries.isEmpty();
            boolean hasArticles = articles != null && !articles.isEmpty();
            boolean hasContent = hasKnowledge || hasArticles;

            // 2. 构建上下文
            String context = buildContext(knowledgeEntries, articles);

            // 3. 构建用户提示词
            String userPrompt;
            if (hasContent) {
                StringBuilder prompt = new StringBuilder();
                // 如果有对话历史，先添加上下文摘要
                if (!contextualQuestion.equals(question)) {
                    prompt.append("【对话上下文】\n").append(contextualQuestion).append("\n\n");
                }
                prompt.append(context);
                prompt.append("\n\n【用户问题】\n").append(question);
                prompt.append("\n\n【回答要求】\n");
                prompt.append("1. 优先使用知识库内容回答\n");
                prompt.append("2. 使用引用块（>）直接展示原文关键内容\n");
                prompt.append("3. 如有攻略文章，提示用户可在网站查看完整攻略\n");
                prompt.append("4. 回答要准确、完整，不要遗漏重要信息\n");
                if (!contextualQuestion.equals(question)) {
                    prompt.append("5. 注意参考对话上下文，保持对话连贯性\n");
                }
                userPrompt = prompt.toString();
            } else {
                String baseQuestion = contextualQuestion.equals(question) ? question : contextualQuestion;
                userPrompt = context + "\n\n用户问题：" + baseQuestion + "\n请告知用户当前网站暂无相关信息，建议查看其他分类或稍后再来查询。";
            }

            saveChatHistory(sessionId, "user", question, userId);
            String response = callAI(userPrompt);
            saveChatHistory(sessionId, "assistant", response, userId);

            // 4. 构建引用来源
            List<Map<String, Object>> references = buildReferences(knowledgeEntries, articles);

            result.put("answer", response);
            result.put("references", references);
            result.put("hasKnowledge", hasKnowledge || hasArticles);
            return result;
        } catch (Exception e) {
            log.error("AI服务异常: {}", e.getMessage(), e);
            result.put("answer", "抱歉，AI服务暂时不可用：" + e.getMessage());
            result.put("references", Collections.emptyList());
            result.put("hasKnowledge", false);
            return result;
        }
    }

    /**
     * 构建引用来源列表
     */
    private List<Map<String, Object>> buildReferences(List<KnowledgeEntry> knowledgeEntries, List<Article> articles) {
        List<Map<String, Object>> refs = new ArrayList<>();

        if (articles != null) {
            for (Article a : articles) {
                if (a.getId() != null) {
                    Map<String, Object> ref = new HashMap<>();
                    ref.put("type", "article");
                    ref.put("id", a.getId());
                    ref.put("title", a.getTitle());
                    ref.put("summary", a.getSummary());
                    refs.add(ref);
                }
            }
        }

        if (knowledgeEntries != null) {
            for (KnowledgeEntry e : knowledgeEntries) {
                if (e.getId() != null) {
                    Map<String, Object> ref = new HashMap<>();
                    ref.put("type", "knowledge");
                    ref.put("id", e.getId());
                    ref.put("title", e.getTitle());
                    ref.put("summary", e.getSummary());
                    refs.add(ref);
                }
            }
        }

        return refs;
    }

    /**
     * 搜索知识库词条 - 优化版：分词搜索
     */
    private List<KnowledgeEntry> searchKnowledge(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            List<String> keywords = extractKeywords(keyword);
            log.info("提取的搜索关键词: {}", keywords);

            if (keywords.isEmpty()) {
                return Collections.emptyList();
            }

            List<KnowledgeEntry> results = knowledgeEntryMapper.searchByKeywords(keywords);

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
     */
    private List<String> extractKeywords(String text) {
        List<String> keywords = new ArrayList<>();

        String cleaned = text.replaceAll("[，。！？、：；\"\"''【】《》（）\\-—_.,!?():;\\[\\]{}]", " ");

        String[] stopWords = {"的", "是", "在", "有", "和", "与", "或", "以及", "请问", "怎么", "如何", "什么", "哪个", "哪里", "为什么", "能不能", "可以", "帮我", "告诉", "一下", "吗", "呢", "吧", "啊"};
        for (String stop : stopWords) {
            cleaned = cleaned.replace(stop, " ");
        }

        String chinesePattern = "[\\u4e00-\\u9fa5]{2,6}";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(chinesePattern);
        java.util.regex.Matcher matcher = pattern.matcher(cleaned);
        while (matcher.find()) {
            String word = matcher.group();
            if (word.length() >= 2) {
                keywords.add(word);
                if (word.length() >= 3) {
                    keywords.add(word.substring(0, 2));
                    keywords.add(word.substring(0, 3));
                }
            }
        }

        String[] englishWords = cleaned.split("\\s+");
        for (String word : englishWords) {
            word = word.trim();
            if (word.length() >= 2 && !word.matches(".*\\d+.*")) {
                keywords.add(word);
            }
        }

        return keywords.stream().distinct().limit(10).collect(Collectors.toList());
    }

    /**
     * 搜索攻略文章
     */
    private List<Article> searchArticles(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            List<String> keywords = extractKeywords(keyword);
            if (keywords.isEmpty()) {
                return Collections.emptyList();
            }

            List<Article> results = new java.util.ArrayList<>();
            java.util.Set<Long> addedIds = new java.util.HashSet<>();

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

            return results.stream().limit(5).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("搜索攻略文章失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 构建上下文
     */
    private String buildContext(List<KnowledgeEntry> knowledgeEntries, List<Article> articles) {
        StringBuilder sb = new StringBuilder();
        sb.append("【网站内容检索结果】\n\n");

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
                    content = cleanMarkdownContent(content);
                    if (content.length() > 1500) {
                        content = content.substring(0, 1500) + "\n...(内容已截断，请访问网站查看完整攻略)";
                    }
                    sb.append("原文内容：\n").append(content);
                }
                sb.append("\n---\n");
            }
            sb.append("\n");
        }

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

    private static final int MAX_HISTORY_SIZE = 10;

    private void saveChatHistory(String sessionId, String role, String content, Long userId) {
        try {
            int currentCount = chatHistoryMapper.countBySessionId(sessionId);

            if (currentCount >= MAX_HISTORY_SIZE) {
                int deleteCount = 2;
                chatHistoryMapper.deleteOldestBySessionId(sessionId, deleteCount);
                log.info("聊天记录超限，已删除最老的{}条记录", deleteCount);
            }

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

    public List<Map<String, String>> getChatHistoryByUserId(Long userId) {
        try {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatHistory> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.like(ChatHistory::getSessionId, "user_" + userId + "_")
                   .orderByDesc(ChatHistory::getCreatedAt)
                   .last("LIMIT 40");

            List<ChatHistory> histories = chatHistoryMapper.selectList(wrapper);
            if (histories == null || histories.isEmpty()) {
                return Collections.emptyList();
            }

            Map<String, List<ChatHistory>> sessionMap = new LinkedHashMap<>();
            for (ChatHistory h : histories) {
                String sid = h.getSessionId();
                sessionMap.computeIfAbsent(sid, k -> new ArrayList<>()).add(h);
            }

            List<Map<String, String>> result = new ArrayList<>();
            String latestSession = sessionMap.keySet().iterator().next();
            List<ChatHistory> latestHistory = sessionMap.get(latestSession);
            latestHistory.sort(Comparator.comparing(ChatHistory::getCreatedAt));

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

    public List<Map<String, Object>> getUserSessionList(Long userId) {
        try {
            List<ChatHistory> allHistories = chatHistoryMapper.selectUserSessionMessages(userId);
            if (allHistories == null || allHistories.isEmpty()) {
                return Collections.emptyList();
            }

            Map<String, List<ChatHistory>> sessionMap = new LinkedHashMap<>();
            for (ChatHistory h : allHistories) {
                sessionMap.computeIfAbsent(h.getSessionId(), k -> new ArrayList<>()).add(h);
            }

            List<Map<String, Object>> sessionList = new ArrayList<>();
            for (Map.Entry<String, List<ChatHistory>> entry : sessionMap.entrySet()) {
                String sessionId = entry.getKey();
                List<ChatHistory> messages = entry.getValue();
                messages.sort(Comparator.comparing(ChatHistory::getCreatedAt));

                Map<String, Object> session = new LinkedHashMap<>();
                session.put("sessionId", sessionId);

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

                ChatHistory lastMsg = messages.get(messages.size() - 1);
                session.put("lastUpdate", lastMsg.getCreatedAt().toString());

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
     * 提交满意度反馈
     */
    public void submitFeedback(Long userId, String sessionId, String question,
                               String answer, Integer rating, String feedbackText) {
        try {
            AiFeedback feedback = new AiFeedback();
            feedback.setUserId(userId);
            feedback.setSessionId(sessionId);
            feedback.setQuestion(question);
            feedback.setAnswer(answer);
            feedback.setRating(rating);
            feedback.setFeedbackText(feedbackText);
            feedback.setCreatedAt(java.time.LocalDateTime.now());
            aiFeedbackMapper.insert(feedback);
            log.info("AI反馈已保存: userId={}, rating={}", userId, rating);
        } catch (Exception e) {
            log.error("保存AI反馈失败: {}", e.getMessage());
        }
    }

    /**
     * 生成会话上下文摘要
     * 将对话历史精炼为简短摘要，包含主要话题和关键信息
     */
    public Map<String, Object> generateContextSummary(String sessionId) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<ChatHistory> histories = chatHistoryMapper.selectBySessionId(sessionId);
            if (histories == null || histories.isEmpty()) {
                result.put("summary", "");
                result.put("topics", Collections.emptyList());
                return result;
            }

            // 构建摘要提示词
            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append("请将以下对话历史精炼为一个简短的摘要（不超过150字），包含：\n");
            promptBuilder.append("1. 用户的主要问题或需求\n");
            promptBuilder.append("2. AI回答的关键内容\n");
            promptBuilder.append("3. 提取3-5个关键词（用顿号分隔）\n\n");

            // 只取最近10条消息进行摘要
            int count = Math.min(histories.size(), 10);
            for (int i = histories.size() - count; i < histories.size(); i++) {
                ChatHistory h = histories.get(i);
                promptBuilder.append(h.getRole()).append(": ").append(h.getContent()).append("\n");
            }

            String summaryPrompt = promptBuilder.toString();
            log.info("生成摘要，prompt长度: {}", summaryPrompt.length());

            // 调用AI生成摘要
            String summary = callAI(summaryPrompt);

            // 提取关键词（简单处理：取摘要中的名词性词汇）
            List<String> topics = extractTopics(summary);

            result.put("summary", summary);
            result.put("topics", topics);
            result.put("messageCount", histories.size());

            log.info("摘要生成成功: {}", summary.substring(0, Math.min(50, summary.length())));
            return result;

        } catch (Exception e) {
            log.error("生成摘要失败: {}", e.getMessage(), e);
            result.put("summary", "");
            result.put("topics", Collections.emptyList());
            return result;
        }
    }

    /**
     * 提取关键词
     */
    private List<String> extractTopics(String text) {
        List<String> topics = new ArrayList<>();

        // 简单关键词提取：查找游戏相关词汇
        String[] keywords = {"青云门", "天音寺", "焚香谷", "鬼王宗", "合欢派",
            "新手", "升级", "加点", "副本", "装备", "宠物", "坐骑",
            "PVP", "PVE", "攻略", "门派", "职业", "技能", "法宝"};

        for (String keyword : keywords) {
            if (text.contains(keyword) && topics.size() < 5) {
                topics.add(keyword);
            }
        }

        return topics;
    }

    /**
     * 获取带有上下文摘要的提示词
     * 用于在后续对话中提供上下文
     * 注意：不再调用AI生成摘要，直接使用对话历史构建上下文
     */
    public String buildContextualPrompt(String question, String sessionId) {
        try {
            List<ChatHistory> histories = chatHistoryMapper.selectBySessionId(sessionId);
            if (histories == null || histories.isEmpty()) {
                return question;
            }

            // 只要有历史记录（至少1条），就构建上下文
            // 注意：当前问题还未保存，所以历史记录只包含之前的对话

            StringBuilder contextBuilder = new StringBuilder();

            // 取最近几轮对话作为上下文（最多6条）
            int recentCount = Math.min(histories.size(), 6);
            int startIndex = histories.size() - recentCount;

            for (int i = startIndex; i < histories.size(); i++) {
                ChatHistory h = histories.get(i);
                String role = "user".equals(h.getRole()) ? "用户" : "助手";
                String content = h.getContent();

                // 截断过长的内容
                if (content != null && content.length() > 300) {
                    content = content.substring(0, 300) + "...";
                }

                if (content != null) {
                    contextBuilder.append(role).append("：").append(content).append("\n");
                }
            }

            String context = contextBuilder.toString();
            if (context.isEmpty()) {
                return question;
            }

            // 构建带有上下文的提示
            StringBuilder prompt = new StringBuilder();
            prompt.append("【之前的对话内容】\n").append(context).append("\n");
            prompt.append("【当前问题】\n").append(question);

            return prompt.toString();

        } catch (Exception e) {
            log.error("构建上下文提示词失败: {}", e.getMessage());
            return question;
        }
    }

    /**
     * 清理Markdown内容
     */
    private String cleanMarkdownContent(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        content = content.replaceAll("\\n{3,}", "\n\n");
        content = content.replaceAll("[ \t]+\n", "\n");
        content = content.replaceAll("\n[ \t]+", "\n");
        content = content.replace("\r\n", "\n").replace("\r", "\n");
        return content;
    }
}
