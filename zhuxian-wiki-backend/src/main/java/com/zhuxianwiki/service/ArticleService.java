package com.zhuxianwiki.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuxianwiki.entity.Article;
import com.zhuxianwiki.mapper.ArticleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ArticleService {
    
    private static final Logger log = LoggerFactory.getLogger(ArticleService.class);
    
    @Autowired
    private ArticleMapper articleMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String HOT_ARTICLES_KEY = "article:hot";
    private static final String LATEST_ARTICLES_KEY = "article:latest";
    
    public IPage<Article> getArticlePage(Long categoryId, int page, int size) {
        Page<Article> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        
        if (categoryId != null) {
            wrapper.eq(Article::getCategoryId, categoryId);
        }
        wrapper.eq(Article::getStatus, 1)
               .orderByDesc(Article::getCreatedAt);
        
        return articleMapper.selectPage(pageParam, wrapper);
    }
    
    @SuppressWarnings("unchecked")
    public List<Article> getHotArticles() {
        try {
            List<Article> cached = (List<Article>) redisTemplate.opsForValue().get(HOT_ARTICLES_KEY);
            if (cached != null && !cached.isEmpty()) {
                return cached;
            }
        } catch (Exception e) {
            log.warn("Redis unavailable, fallback to DB for hot articles");
        }
        
        List<Article> articles = articleMapper.selectHotArticles(10);
        try {
            if (articles != null && !articles.isEmpty()) {
                redisTemplate.opsForValue().set(HOT_ARTICLES_KEY, articles, 30, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            log.warn("Redis write failed for hot articles");
        }
        return articles;
    }
    
    @SuppressWarnings("unchecked")
    public List<Article> getLatestArticles() {
        try {
            List<Article> cached = (List<Article>) redisTemplate.opsForValue().get(LATEST_ARTICLES_KEY);
            if (cached != null && !cached.isEmpty()) {
                return cached;
            }
        } catch (Exception e) {
            log.warn("Redis unavailable, fallback to DB for latest articles");
        }
        
        List<Article> articles = articleMapper.selectLatestArticles(10);
        try {
            if (articles != null && !articles.isEmpty()) {
                redisTemplate.opsForValue().set(LATEST_ARTICLES_KEY, articles, 10, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            log.warn("Redis write failed for latest articles");
        }
        return articles;
    }
    
    public Article getArticleById(Long id) {
        Article article = articleMapper.selectById(id);
        if (article != null) {
            article.setViewCount(article.getViewCount() == null ? 1 : article.getViewCount() + 1);
            articleMapper.updateById(article);
        }
        return article;
    }
    
    public List<Article> searchArticles(String keyword) {
        return articleMapper.searchArticles(keyword);
    }
    
    public boolean saveArticle(Article article) {
        return articleMapper.insert(article) > 0;
    }
    
    public boolean updateArticle(Article article) {
        return articleMapper.updateById(article) > 0;
    }
    
    public boolean deleteArticle(Long id) {
        return articleMapper.deleteById(id) > 0;
    }
}
