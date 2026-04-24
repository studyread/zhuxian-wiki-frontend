package com.zhuxianwiki.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuxianwiki.entity.KnowledgeLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface KnowledgeLogMapper extends BaseMapper<KnowledgeLog> {
    
    IPage<KnowledgeLog> selectRecentLogs(Page<KnowledgeLog> page);
}
