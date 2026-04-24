package com.zhuxianwiki.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuxianwiki.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
    
    List<Article> selectHotArticles(@Param("limit") int limit);
    
    List<Article> selectLatestArticles(@Param("limit") int limit);
    
    List<Article> searchArticles(@Param("keyword") String keyword);
}
