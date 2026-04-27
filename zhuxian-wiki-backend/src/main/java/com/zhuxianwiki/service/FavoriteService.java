package com.zhuxianwiki.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuxianwiki.entity.Article;
import com.zhuxianwiki.entity.Favorite;
import com.zhuxianwiki.mapper.ArticleMapper;
import com.zhuxianwiki.mapper.FavoriteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private ArticleMapper articleMapper;

    // 收藏文章
    public boolean addFavorite(Long userId, Long articleId) {
        QueryWrapper<Favorite> query = new QueryWrapper<>();
        query.eq("user_id", userId).eq("article_id", articleId);
        if (favoriteMapper.selectCount(query) > 0) {
            return true; // 已收藏
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setArticleId(articleId);
        return favoriteMapper.insert(favorite) > 0;
    }

    // 取消收藏
    public boolean removeFavorite(Long userId, Long articleId) {
        QueryWrapper<Favorite> query = new QueryWrapper<>();
        query.eq("user_id", userId).eq("article_id", articleId);
        return favoriteMapper.delete(query) > 0;
    }

    // 获取用户收藏列表
    public List<Article> getUserFavorites(Long userId) {
        QueryWrapper<Favorite> query = new QueryWrapper<>();
        query.eq("user_id", userId).orderByDesc("created_at");
        List<Favorite> favorites = favoriteMapper.selectList(query);
        return favorites.stream().map(f -> {
            Article article = articleMapper.selectById(f.getArticleId());
            return article;
        }).filter(a -> a != null).toList();
    }

    // 检查是否已收藏
    public boolean isFavorited(Long userId, Long articleId) {
        QueryWrapper<Favorite> query = new QueryWrapper<>();
        query.eq("user_id", userId).eq("article_id", articleId);
        return favoriteMapper.selectCount(query) > 0;
    }

    // 获取用户收藏数量
    public int getFavoriteCount(Long userId) {
        QueryWrapper<Favorite> query = new QueryWrapper<>();
        query.eq("user_id", userId);
//        return (int) favoriteMapper.selectCount(query);
        return favoriteMapper.selectCount(query).intValue();
    }
}
