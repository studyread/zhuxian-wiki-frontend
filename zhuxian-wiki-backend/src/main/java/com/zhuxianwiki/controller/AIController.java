package com.zhuxianwiki.controller;

import com.zhuxianwiki.AI.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {

    @Autowired
    private AIService aiService;

    @PostMapping("/chat")
    public Map<String, Object> chat(
            @RequestBody Map<String, String> params,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Map<String, Object> result = new HashMap<>();
        String question = params.get("question");
        String sessionId = params.get("sessionId");

        if (question == null || question.trim().isEmpty()) {
            result.put("code", 400);
            result.put("message", "问题不能为空");
            return result;
        }

        // 如果没有 sessionId，则生成新的
        if (sessionId == null || sessionId.isEmpty()) {
            if (userId != null) {
                // 登录用户：使用固定前缀 + 用户ID + 随机后缀
                sessionId = "user_" + userId + "_" + UUID.randomUUID().toString().substring(0, 8);
            } else {
                // 游客：直接使用随机ID
                sessionId = UUID.randomUUID().toString();
            }
        }
        // 注意：不再修改已有的 sessionId，保持与数据库中的一致

        Map<String, Object> aiResult = aiService.chat(question.trim(), sessionId, userId);

        result.put("code", 200);
        Map<String, Object> data = new HashMap<>();
        data.put("answer", aiResult.get("answer"));
        data.put("sessionId", sessionId);
        data.put("references", aiResult.get("references"));
        data.put("hasKnowledge", aiResult.get("hasKnowledge"));
        result.put("data", data);
        return result;
    }

    @GetMapping("/history")
    public Map<String, Object> getChatHistory(
            @RequestParam(required = false) String sessionId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Map<String, Object> result = new HashMap<>();

        if (userId != null && (sessionId == null || sessionId.isEmpty())) {
            List<Map<String, Object>> sessionList = aiService.getUserSessionList(userId);
            result.put("code", 200);
            result.put("data", sessionList);
            result.put("type", "session_list");
        } else if (sessionId != null && userId != null) {
            List<Map<String, String>> history = aiService.getChatHistory(sessionId);
            result.put("code", 200);
            result.put("data", history);
            result.put("type", "messages");
        } else if (sessionId != null) {
            List<Map<String, String>> history = aiService.getChatHistory(sessionId);
            result.put("code", 200);
            result.put("data", history);
            result.put("type", "messages");
        } else {
            result.put("code", 200);
            result.put("data", List.of());
            result.put("type", "session_list");
        }
        return result;
    }

    @GetMapping("/session/{sessionId}")
    public Map<String, Object> getSessionMessages(
            @PathVariable String sessionId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, String>> history = aiService.getChatHistory(sessionId);
        result.put("code", 200);
        result.put("data", history);
        return result;
    }

    @DeleteMapping("/session/{sessionId}")
    public Map<String, Object> deleteSession(
            @PathVariable String sessionId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Map<String, Object> result = new HashMap<>();
        aiService.clearChatHistory(sessionId);
        result.put("code", 200);
        result.put("message", "会话已删除");
        return result;
    }

    /**
     * 提交AI回答满意度反馈
     */
    @PostMapping("/feedback")
    public Map<String, Object> submitFeedback(
            @RequestBody Map<String, Object> params,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Map<String, Object> result = new HashMap<>();

        Integer rating = params.get("rating") != null
            ? Integer.valueOf(params.get("rating").toString()) : null;
        String sessionId = params.get("sessionId") != null
            ? params.get("sessionId").toString() : null;
        String question = params.get("question") != null
            ? params.get("question").toString() : null;
        String answer = params.get("answer") != null
            ? params.get("answer").toString() : null;
        String feedbackText = params.get("feedbackText") != null
            ? params.get("feedbackText").toString() : null;

        if (rating == null) {
            result.put("code", 400);
            result.put("message", "评分不能为空");
            return result;
        }

        aiService.submitFeedback(userId, sessionId, question, answer, rating, feedbackText);
        result.put("code", 200);
        result.put("message", "反馈已提交");
        return result;
    }

    /**
     * 生成会话上下文摘要
     */
    @PostMapping("/summarize")
    public Map<String, Object> summarizeContext(
            @RequestBody Map<String, Object> params,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        Map<String, Object> result = new HashMap<>();
        String sessionId = params.get("sessionId") != null
            ? params.get("sessionId").toString() : null;

        if (sessionId == null || sessionId.isEmpty()) {
            result.put("code", 400);
            result.put("message", "sessionId不能为空");
            return result;
        }

        Map<String, Object> summaryData = aiService.generateContextSummary(sessionId);
        result.put("code", 200);
        result.put("data", summaryData);
        return result;
    }

    /**
     * 获取带有上下文的聊天响应
     */
    @PostMapping("/chat-with-context")
    public Map<String, Object> chatWithContext(
            @RequestBody Map<String, String> params,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        Map<String, Object> response = new HashMap<>();
        String question = params.get("question");
        String sessionId = params.get("sessionId");

        if (question == null || question.trim().isEmpty()) {
            response.put("code", 400);
            response.put("message", "问题不能为空");
            return response;
        }

        if (userId != null) {
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = "user_" + userId + "_" + UUID.randomUUID().toString().substring(0, 8);
            } else {
                sessionId = "user_" + userId + "_" + sessionId;
            }
        } else {
            sessionId = sessionId != null ? sessionId : UUID.randomUUID().toString();
        }

        // 构建带上下文的提示词
        String contextualPrompt = aiService.buildContextualPrompt(question.trim(), sessionId);

        // 调用标准chat接口
        Map<String, Object> chatResult = aiService.chat(question.trim(), sessionId, userId);

        response.put("code", 200);
        Map<String, Object> data = new HashMap<>();
        data.put("answer", chatResult.get("answer"));
        data.put("sessionId", sessionId);
        data.put("references", chatResult.get("references"));
        data.put("hasKnowledge", chatResult.get("hasKnowledge"));
        response.put("data", data);
        return response;
    }
}
