package com.zhuxianwiki.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhuxianwiki.entity.Article;
import com.zhuxianwiki.entity.UserArticleCollect;
import com.zhuxianwiki.mapper.ArticleMapper;
import com.zhuxianwiki.mapper.UserArticleCollectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CollectService {
    
    @Autowired
    private UserArticleCollectMapper collectMapper;
    
    @Autowired
    private ArticleMapper articleMapper;
    
    // 收藏或取消收藏文章
    @Transactional
    public Map<String, Object> collectArticle(Long articleId, Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        // 检查是否已经收藏
        int existingCount = collectMapper.selectCountByUserAndArticle(userId, articleId);
        
        if (existingCount > 0) {
            // 已收藏，取消收藏
            collectMapper.deleteByUserAndArticle(userId, articleId);
            result.put("collected", false);
            result.put("message", "已取消收藏");
        } else {
            // 未收藏，添加收藏
            UserArticleCollect collect = new UserArticleCollect();
            collect.setUserId(userId);
            collect.setArticleId(articleId);
            collectMapper.insert(collect);
            result.put("collected", true);
            result.put("message", "收藏成功");
        }
        
        return result;
    }
    
    // 检查用户是否已收藏某文章
    public boolean hasUserCollectedArticle(Long articleId, Long userId) {
        return collectMapper.selectCountByUserAndArticle(userId, articleId) > 0;
    }
    
    // 获取用户收藏的文章列表
    public List<Article> getUserCollectedArticles(Long userId) {
        LambdaQueryWrapper<UserArticleCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserArticleCollect::getUserId, userId)
               .orderByDesc(UserArticleCollect::getCreatedAt);
        
        List<UserArticleCollect> collects = collectMapper.selectList(wrapper);
        
        if (collects.isEmpty()) {
            return List.of();
        }
        
        List<Long> articleIds = collects.stream()
                .map(UserArticleCollect::getArticleId)
                .collect(Collectors.toList());
        
        LambdaQueryWrapper<Article> articleWrapper = new LambdaQueryWrapper<>();
        articleWrapper.in(Article::getId, articleIds);
        
        return articleMapper.selectList(articleWrapper);
    }
}
