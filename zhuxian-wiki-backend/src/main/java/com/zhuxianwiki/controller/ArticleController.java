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
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        IPage<Article> pageResult = articleService.getArticlePage(categoryId, page, size);
        
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
    public Map<String, Object> saveArticle(@RequestBody Article article) {
        Map<String, Object> result = new HashMap<>();
        boolean success = articleService.saveArticle(article);
        
        result.put("code", success ? 200 : 500);
        result.put("message", success ? "保存成功" : "保存失败");
        return result;
    }
    
    @PutMapping("/{id}")
    public Map<String, Object> updateArticle(@PathVariable Long id, @RequestBody Article article) {
        Map<String, Object> result = new HashMap<>();
        article.setId(id);
        boolean success = articleService.updateArticle(article);
        
        result.put("code", success ? 200 : 500);
        result.put("message", success ? "更新成功" : "更新失败");
        return result;
    }
    
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteArticle(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        boolean success = articleService.deleteArticle(id);
        
        result.put("code", success ? 200 : 500);
        result.put("message", success ? "删除成功" : "删除失败");
        return result;
    }
}
