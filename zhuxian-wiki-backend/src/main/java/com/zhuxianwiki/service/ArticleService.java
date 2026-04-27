package com.zhuxianwiki.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuxianwiki.entity.Article;
import com.zhuxianwiki.entity.UserArticleLike;
import com.zhuxianwiki.mapper.ArticleMapper;
import com.zhuxianwiki.mapper.UserArticleLikeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
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
    
    @Autowired
    private UserArticleLikeMapper userArticleLikeMapper;
    
    private static final String HOT_ARTICLES_KEY = "article:hot";
    private static final String LATEST_ARTICLES_KEY = "article:latest";
    
    public IPage<Article> getArticlePage(Long categoryId, int page, int size) {
        return getArticlePage(categoryId, page, size, "latest");
    }
    
    public IPage<Article> getArticlePage(Long categoryId, int page, int size, String sort) {
        Page<Article> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        
        if (categoryId != null) {
            wrapper.eq(Article::getCategoryId, categoryId);
        }
        wrapper.eq(Article::getStatus, 1);
        
        // 支持按阅读量、点赞量排序
        if ("hot".equals(sort)) {
            wrapper.orderByDesc(Article::getViewCount, Article::getCreatedAt);
        } else if ("like".equals(sort)) {
            wrapper.orderByDesc(Article::getLikeCount, Article::getCreatedAt);
        } else {
            wrapper.orderByDesc(Article::getCreatedAt);
        }
        
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
        boolean result = articleMapper.insert(article) > 0;
        // 清除缓存，让新文章立即显示
        if (result) {
            clearCache();
        }
        return result;
    }

    public boolean updateArticle(Article article) {
        boolean result = articleMapper.updateById(article) > 0;
        // 清除缓存
        if (result) {
            clearCache();
        }
        return result;
    }

    // 清除文章相关缓存
    private void clearCache() {
        try {
            redisTemplate.delete(HOT_ARTICLES_KEY);
            redisTemplate.delete(LATEST_ARTICLES_KEY);
        } catch (Exception e) {
            log.warn("Redis缓存清除失败，但文章已保存成功");
        }
    }
    
    public boolean deleteArticle(Long id, Long userId) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            return false;
        }
        // 验证是否是作者本人删除
        if (article.getAuthorId() != null && !article.getAuthorId().equals(userId)) {
            log.warn("用户 {} 试图删除作者为 {} 的文章 {}", userId, article.getAuthorId(), id);
            return false;
        }
        return articleMapper.deleteById(id) > 0;
    }

    // 按作者ID获取文章列表
    public List<Article> getArticlesByAuthorId(Long authorId) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getAuthorId, authorId)
               .orderByDesc(Article::getCreatedAt);
        return articleMapper.selectList(wrapper);
    }

    // 文章点赞（无用户ID，普通点赞）
    public boolean likeArticle(Long id) {
        Article article = articleMapper.selectById(id);
        if (article != null) {
            article.setLikeCount(article.getLikeCount() == null ? 1 : article.getLikeCount() + 1);
            return articleMapper.updateById(article) > 0;
        }
        return false;
    }

    // 用户点赞文章（每个用户只能点赞一次）
    @Transactional
    public Map<String, Object> likeArticleWithUser(Long articleId, Long userId) {
        Map<String, Object> result = new java.util.HashMap<>();
        
        // 检查用户是否已点赞
        int existingCount = userArticleLikeMapper.selectCountByUserAndArticle(userId, articleId);
        if (existingCount > 0) {
            // 取消点赞
            userArticleLikeMapper.deleteByUserAndArticle(userId, articleId);
            Article article = articleMapper.selectById(articleId);
            if (article != null) {
                article.setLikeCount(Math.max(0, (article.getLikeCount() == null ? 0 : article.getLikeCount()) - 1));
                articleMapper.updateById(article);
            }
            result.put("liked", false);
            result.put("likeCount", article != null ? article.getLikeCount() : 0);
            return result;
        } else {
            // 添加点赞
            UserArticleLike like = new UserArticleLike();
            like.setUserId(userId);
            like.setArticleId(articleId);
            userArticleLikeMapper.insert(like);
            
            Article article = articleMapper.selectById(articleId);
            if (article != null) {
                article.setLikeCount((article.getLikeCount() == null ? 0 : article.getLikeCount()) + 1);
                articleMapper.updateById(article);
                result.put("likeCount", article.getLikeCount());
            } else {
                result.put("likeCount", 1);
            }
            result.put("liked", true);
            return result;
        }
    }

    // 检查用户是否已点赞某文章
    public boolean hasUserLikedArticle(Long articleId, Long userId) {
        return userArticleLikeMapper.selectCountByUserAndArticle(userId, articleId) > 0;
    }
}
