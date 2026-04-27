package com.zhuxianwiki.controller;

import com.zhuxianwiki.entity.Article;
import com.zhuxianwiki.service.CollectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CollectController {
    
    @Autowired
    private CollectService collectService;
    
    // 收藏/取消收藏文章
    @PostMapping("/articles/{id}/collect")
    public Map<String, Object> collectArticle(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        if (userId == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return result;
        }
        
        Map<String, Object> collectResult = collectService.collectArticle(id, userId);
        result.put("code", 200);
        result.put("data", collectResult);
        result.put("message", collectResult.get("message"));
        return result;
    }
    
    // 检查用户是否已收藏某文章
    @GetMapping("/articles/{id}/collect/status")
    public Map<String, Object> getCollectStatus(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Map<String, Object> result = new HashMap<>();
        if (userId == null) {
            result.put("code", 200);
            result.put("data", false);
            return result;
        }
        boolean collected = collectService.hasUserCollectedArticle(id, userId);
        result.put("code", 200);
        result.put("data", collected);
        return result;
    }
    
    // 获取用户收藏的文章列表
    @GetMapping("/user/collects")
    public Map<String, Object> getUserCollects(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        if (userId == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return result;
        }
        
        List<Article> articles = collectService.getUserCollectedArticles(userId);
        result.put("code", 200);
        result.put("data", articles);
        return result;
    }
}
