package com.zhuxianwiki.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhuxianwiki.entity.Article;
import com.zhuxianwiki.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/articles")
@CrossOrigin(origins = "*")
public class ArticleController {
    
    @Autowired
    private ArticleService articleService;
    
    @GetMapping
    public Map<String, Object> getArticles(
            @RequestParam(required = false) String categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sort) {
        
        // 处理中文分类名，转换为数字ID
        Long realCategoryId = null;
        if (categoryId != null && !categoryId.isEmpty()) {
            try {
                realCategoryId = Long.parseLong(categoryId);
            } catch (NumberFormatException e) {
                // 不是数字，按名称查询分类ID
                realCategoryId = articleService.getCategoryIdByName(categoryId);
            }
        }
        
        IPage<Article> pageResult = articleService.getArticlePage(realCategoryId, page, size, sort);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("pages", pageResult.getPages());
        result.put("current", pageResult.getCurrent());
        return result;
    }
    
    @GetMapping("/hot")
    public Map<String, Object> getHotArticles() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", articleService.getHotArticles());
        return result;
    }
    
    @GetMapping("/latest")
    public Map<String, Object> getLatestArticles() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", articleService.getLatestArticles());
        return result;
    }
    
    @GetMapping("/{id}")
    public Map<String, Object> getArticleById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        Article article = articleService.getArticleById(id);
        
        if (article != null) {
            result.put("code", 200);
            result.put("data", article);
        } else {
            result.put("code", 404);
            result.put("message", "文章不存在");
        }
        return result;
    }
    
    @GetMapping("/search")
    public Map<String, Object> searchArticles(@RequestParam String keyword) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", articleService.searchArticles(keyword));
        return result;
    }
    
    @PostMapping
    public Map<String, Object> saveArticle(
            @RequestBody Article article,
            @RequestHeader(value = "X-User-Id", required = false) Long authorId) {
        Map<String, Object> result = new HashMap<>();
        
        // 未登录时 authorId 为 null 仍可发布（作为游客）
        if (authorId != null) {
            article.setAuthorId(authorId);
        }
        
        try {
            // 设置默认状态为已发布（1=已发布，0=草稿）
            if (article.getStatus() == null) {
                article.setStatus(1);
            }
            boolean success = articleService.saveArticle(article);
            result.put("code", success ? 200 : 500);
            result.put("message", success ? "保存成功" : "保存失败");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "保存失败: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
    
    @PutMapping("/{id}")
    public Map<String, Object> updateArticle(
            @PathVariable Long id, 
            @RequestBody Article article,
            @RequestHeader(value = "X-User-Id", required = false) Long authorId) {
        Map<String, Object> result = new HashMap<>();
        
        if (authorId != null) {
            article.setAuthorId(authorId);
        }
        
        boolean success = articleService.updateArticle(article);
        
        result.put("code", success ? 200 : 500);
        result.put("message", success ? "更新成功" : "更新失败");
        return result;
    }
    
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteArticle(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        if (userId == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return result;
        }
        
        boolean success = articleService.deleteArticle(id, userId);

        result.put("code", success ? 200 : 500);
        result.put("message", success ? "删除成功" : "无权限删除他人的文章");
        return result;
    }

    // 按用户获取文章列表
    @GetMapping("/user/{userId}")
    public Map<String, Object> getArticlesByUser(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", articleService.getArticlesByAuthorId(userId));
        return result;
    }

    // 文章点赞
    @PostMapping("/{id}/like")
    public Map<String, Object> likeArticle(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        boolean success = articleService.likeArticle(id);
        result.put("code", success ? 200 : 500);
        result.put("message", success ? "点赞成功" : "点赞失败");
        return result;
    }

    // 用户点赞文章（带用户ID，每个用户只能点赞一次）
    @PostMapping("/{id}/like/user")
    public Map<String, Object> likeArticleWithUser(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        if (userId == null) {
            result.put("code", 401);
            result.put("message", "请先登录");
            return result;
        }
        
        Map<String, Object> likeResult = articleService.likeArticleWithUser(id, userId);
        result.put("code", 200);
        result.put("data", likeResult);
        result.put("message", (Boolean) likeResult.get("liked") ? "点赞成功" : "已取消点赞");
        return result;
    }

    // 检查用户是否已点赞某文章
    @GetMapping("/{id}/like/status")
    public Map<String, Object> getLikeStatus(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Map<String, Object> result = new HashMap<>();
        if (userId == null) {
            result.put("code", 200);
            result.put("data", false);
            return result;
        }
        boolean liked = articleService.hasUserLikedArticle(id, userId);
        result.put("code", 200);
        result.put("data", liked);
        return result;
    }
}
