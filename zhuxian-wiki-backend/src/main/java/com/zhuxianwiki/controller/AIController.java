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
        String sessionId = params.getOrDefault("sessionId", null);

        // 参数校验
        if (question == null || question.trim().isEmpty()) {
            result.put("code", 400);
            result.put("message", "问题不能为空");
            return result;
        }

        // 如果有用户ID，使用用户ID作为session前缀，确保用户会话独立
        if (userId != null) {
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = "user_" + userId + "_" + UUID.randomUUID().toString().substring(0, 8);
            } else {
                sessionId = "user_" + userId + "_" + sessionId;
            }
        } else {
            sessionId = sessionId != null ? sessionId : UUID.randomUUID().toString();
        }

        String response = aiService.chat(question.trim(), sessionId, userId);

        result.put("code", 200);
        Map<String, Object> data = new HashMap<>();
        data.put("answer", response);
        data.put("sessionId", sessionId);
        result.put("data", data);
        return result;
    }

    @GetMapping("/history")
    public Map<String, Object> getChatHistory(
            @RequestParam(required = false) String sessionId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Map<String, Object> result = new HashMap<>();

        if (userId != null && (sessionId == null || sessionId.isEmpty())) {
            // 登录用户：返回该用户的所有会话列表
            List<Map<String, Object>> sessionList = aiService.getUserSessionList(userId);
            result.put("code", 200);
            result.put("data", sessionList);
            result.put("type", "session_list");
        } else if (sessionId != null && userId != null) {
            // 指定会话：返回该会话的消息
            List<Map<String, String>> history = aiService.getChatHistory(sessionId);
            result.put("code", 200);
            result.put("data", history);
            result.put("type", "messages");
        } else if (sessionId != null) {
            // 游客指定会话
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

    /** 获取指定会话的消息列表 */
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

    /** 删除指定会话 */
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
}
