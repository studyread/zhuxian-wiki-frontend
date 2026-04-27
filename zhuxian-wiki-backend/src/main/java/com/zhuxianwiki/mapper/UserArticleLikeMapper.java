package com.zhuxianwiki.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuxianwiki.entity.UserArticleLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserArticleLikeMapper extends BaseMapper<UserArticleLike> {
    
    // 检查用户是否已经点赞某文章
    int selectCountByUserAndArticle(@Param("userId") Long userId, @Param("articleId") Long articleId);
    
    // 删除用户的点赞记录
    int deleteByUserAndArticle(@Param("userId") Long userId, @Param("articleId") Long articleId);
}
