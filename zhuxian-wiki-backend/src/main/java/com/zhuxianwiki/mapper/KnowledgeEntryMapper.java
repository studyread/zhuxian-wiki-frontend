package com.zhuxianwiki.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuxianwiki.entity.KnowledgeEntry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface KnowledgeEntryMapper extends BaseMapper<KnowledgeEntry> {
    
    IPage<KnowledgeEntry> selectByPage(Page<KnowledgeEntry> page, @Param("categoryId") Long categoryId, @Param("status") Integer status);
    
    List<KnowledgeEntry> searchByKeyword(@Param("keyword") String keyword);
}
