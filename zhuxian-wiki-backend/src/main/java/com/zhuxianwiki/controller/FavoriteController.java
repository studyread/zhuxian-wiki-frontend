package com.zhuxianwiki.controller;

import com.zhuxianwiki.entity.Article;
import com.zhuxianwiki.service.ArticleService;
import com.zhuxianwiki.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins = "*")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private ArticleService articleService;

    // 添加收藏
    @PostMapping
    public Map<String, Object> addFavorite(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestBody Map<String, Long> params) {
        Map<String, Object> result = new HashMap<>();
        
        if (userId == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return result;
        }

        Long articleId = params.get("articleId");
        boolean success = favoriteService.addFavorite(userId, articleId);
        result.put("code", success ? 200 : 500);
        result.put("message", success ? "收藏成功" : "收藏失败");
        return result;
    }

    // 取消收藏
    @DeleteMapping("/{articleId}")
    public Map<String, Object> removeFavorite(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long articleId) {
        Map<String, Object> result = new HashMap<>();
        
        if (userId == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return result;
        }

        boolean success = favoriteService.removeFavorite(userId, articleId);
        result.put("code", success ? 200 : 500);
        result.put("message", success ? "取消收藏成功" : "取消收藏失败");
        return result;
    }

    // 获取用户收藏列表
    @GetMapping
    public Map<String, Object> getUserFavorites(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        if (userId == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return result;
        }

        List<Article> favorites = favoriteService.getUserFavorites(userId);
        result.put("code", 200);
        result.put("data", favorites);
        return result;
    }

    // 检查是否已收藏
    @GetMapping("/check/{articleId}")
    public Map<String, Object> checkFavorite(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long articleId) {
        Map<String, Object> result = new HashMap<>();
        
        if (userId == null) {
            result.put("code", 200);
            result.put("favorited", false);
            return result;
        }

        boolean favorited = favoriteService.isFavorited(userId, articleId);
        result.put("code", 200);
        result.put("favorited", favorited);
        return result;
    }
}
