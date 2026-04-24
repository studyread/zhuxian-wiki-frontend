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
    public Map<String, Object> chat(@RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String question = params.get("question");
        String sessionId = params.getOrDefault("sessionId", UUID.randomUUID().toString());
        
        // 参数校验
        if (question == null || question.trim().isEmpty()) {
            result.put("code", 400);
            result.put("message", "问题不能为空");
            return result;
        }
        
        String response = aiService.chat(question.trim(), sessionId);
        
        result.put("code", 200);
        Map<String, Object> data = new HashMap<>();
        data.put("answer", response);
        data.put("sessionId", sessionId);
        result.put("data", data);
        return result;
    }
    
    @GetMapping("/history/{sessionId}")
    public Map<String, Object> getChatHistory(@PathVariable String sessionId) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, String>> history = aiService.getChatHistory(sessionId);
        
        result.put("code", 200);
        result.put("data", history);
        return result;
    }
    
    @DeleteMapping("/history/{sessionId}")
    public Map<String, Object> clearChatHistory(@PathVariable String sessionId) {
        Map<String, Object> result = new HashMap<>();
        aiService.clearChatHistory(sessionId);
        
        result.put("code", 200);
        result.put("message", "聊天记录已清除");
        return result;
    }
}
