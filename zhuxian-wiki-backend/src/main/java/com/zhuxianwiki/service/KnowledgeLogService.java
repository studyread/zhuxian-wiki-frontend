package com.zhuxianwiki.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuxianwiki.entity.KnowledgeLog;
import com.zhuxianwiki.mapper.KnowledgeLogMapper;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeLogService extends ServiceImpl<KnowledgeLogMapper, KnowledgeLog> {
    
    public IPage<KnowledgeLog> getRecentLogs(int pageNum, int pageSize) {
        Page<KnowledgeLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<KnowledgeLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(KnowledgeLog::getCreatedAt);
        return page(page, wrapper);
    }
}
