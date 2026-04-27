package com.zhuxianwiki.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuxianwiki.entity.UserArticleCollect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserArticleCollectMapper extends BaseMapper<UserArticleCollect> {
    
    // 检查用户是否已经收藏某文章
    int selectCountByUserAndArticle(@Param("userId") Long userId, @Param("articleId") Long articleId);
    
    // 删除用户的收藏记录
    int deleteByUserAndArticle(@Param("userId") Long userId, @Param("articleId") Long articleId);
}
