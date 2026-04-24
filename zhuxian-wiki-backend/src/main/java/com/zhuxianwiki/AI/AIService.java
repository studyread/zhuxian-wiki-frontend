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
        
        1. **知识库优先原则**：必须首先从知识库中检索相关内容再回答
        2. **诚实回答原则**：知识库无相关内容时，必须如实告知用户"抱歉，当前知识库暂无相关信息"
        3. **禁止胡编乱造**：不要基于模糊记忆或推理来回答游戏细节
        4. **回答格式**：使用Markdown格式化，如有相关词条提供链接
        
        当知识库有相关内容时，基于知识库内容回答。
        当知识库没有相关内容时，明确告知用户并引导查看其他功能。
        """;

    public String chat(String question, String sessionId) {
        try {
            // 1. 优先从知识库检索
            List<KnowledgeEntry> knowledgeEntries = searchKnowledge(question);
            boolean hasKnowledgeContent = knowledgeEntries != null && !knowledgeEntries.isEmpty();
            
            // 2. 构建上下文
            String context = buildKnowledgeContext(knowledgeEntries);
            
            // 3. 如果知识库有内容，优先使用知识库；没有则如实告知
            String userPrompt;
            if (hasKnowledgeContent) {
                userPrompt = context + "\n\n用户问题：" + question + "\n请根据以上知识库内容，准确、有条理地回答用户问题。";
            } else {
                userPrompt = context + "\n\n用户问题：" + question + "\n请告知用户当前知识库暂无相关信息，建议查看其他分类或稍后再来查询。";
            }

            saveChatHistory(sessionId, "user", question);
            String response = callAI(userPrompt);
            saveChatHistory(sessionId, "assistant", response);

            return response;
        } catch (Exception e) {
            log.error("AI服务异常: {}", e.getMessage(), e);
            return "抱歉，AI服务暂时不可用：" + e.getMessage();
        }
    }

    /**
     * 从知识库检索相关内容
     */
    private List<KnowledgeEntry> searchKnowledge(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        try {
            // 搜索知识库词条
            return knowledgeEntryMapper.searchByKeyword(keyword.trim());
        } catch (Exception e) {
            log.error("搜索知识库失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 构建知识库上下文
     */
    private String buildKnowledgeContext(List<KnowledgeEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return "【知识库状态】\n当前知识库中暂无相关内容。\n\n请如实告知用户知识库暂无该信息。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("【知识库内容】\n\n");

        for (int i = 0; i < entries.size(); i++) {
            KnowledgeEntry entry = entries.get(i);
            sb.append("【").append(i + 1).append("】").append(entry.getTitle()).append("\n");
            if (entry.getSummary() != null && !entry.getSummary().isEmpty()) {
                sb.append("摘要：").append(entry.getSummary()).append("\n");
            }
            String content = entry.getContent();
            if (content != null) {
                // 截取前800字符
                if (content.length() > 800) {
                    content = content.substring(0, 800) + "...";
                }
                sb.append(content);
            }
            sb.append("\n\n");
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

    private void saveChatHistory(String sessionId, String role, String content) {
        try {
            ChatHistory history = new ChatHistory();
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
}
