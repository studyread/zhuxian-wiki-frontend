package com.zhuxianwiki.controller;

import com.zhuxianwiki.mapper.ArticleMapper;
import com.zhuxianwiki.mapper.CategoryMapper;
import com.zhuxianwiki.mapper.KnowledgeCategoryMapper;
import com.zhuxianwiki.mapper.KnowledgeEntryMapper;
import com.zhuxianwiki.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "*")
public class StatisticsController {
    
    @Autowired
    private ArticleMapper  articleMapper;
    
    @Autowired
    private KnowledgeEntryMapper knowledgeEntryMapper;
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Autowired
    private KnowledgeCategoryMapper knowledgeCategoryMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 获取站点统计数据
     */
    @GetMapping
    public Map<String, Object> getStatistics() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 文章统计（已发布）
            long articleCount = articleMapper.selectCount(null);
            Integer articleViews = articleMapper.getTotalViews();
            
            // 知识库统计（已发布）
            long knowledgeCount = knowledgeEntryMapper.selectCount(null);
            Integer knowledgeViews = knowledgeEntryMapper.getTotalViews();
            
            // 分类统计
            long categoryCount = categoryMapper.selectCount(null);
            long knowledgeCategoryCount = knowledgeCategoryMapper.selectCount(null);
            
            // 用户统计
            long userCount = userMapper.selectCount(null);
            
            // 总浏览量
            int totalViews = (articleViews != null ? articleViews : 0) + 
                            (knowledgeViews != null ? knowledgeViews : 0);
            
            Map<String, Object> data = new HashMap<>();
            data.put("articleCount", articleCount);
            data.put("knowledgeCount", knowledgeCount);
            data.put("categoryCount", categoryCount + knowledgeCategoryCount);
            data.put("userCount", userCount);
            data.put("totalViews", totalViews);
            
            result.put("code", 200);
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取统计数据失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取分类统计（含文章数）
     */
    @GetMapping("/categories")
    public Map<String, Object> getCategoryStats() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            result.put("code", 200);
            result.put("data", articleMapper.getCategoryStats());
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取分类统计失败: " + e.getMessage());
        }
        
        return result;
    }
}
