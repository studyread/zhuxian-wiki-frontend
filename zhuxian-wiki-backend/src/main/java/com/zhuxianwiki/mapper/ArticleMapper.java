package com.zhuxianwiki.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuxianwiki.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
    
    List<Article> selectHotArticles(@Param("limit") int limit);
    
    List<Article> selectLatestArticles(@Param("limit") int limit);
    
    List<Article> searchArticles(@Param("keyword") String keyword);
    
    // 统计总浏览量
    Integer getTotalViews();
    
    // 获取分类统计（含文章数）
    List<Map<String, Object>> getCategoryStats();
}
