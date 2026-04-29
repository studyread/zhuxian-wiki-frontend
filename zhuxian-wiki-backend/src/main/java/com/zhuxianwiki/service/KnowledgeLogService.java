package com.zhuxianwiki.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuxianwiki.entity.KnowledgeLog;
import com.zhuxianwiki.mapper.KnowledgeLogMapper;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class KnowledgeLogService extends ServiceImpl<KnowledgeLogMapper, KnowledgeLog> {
    
    public IPage<KnowledgeLog> getRecentLogs(int pageNum, int pageSize) {
        Page<KnowledgeLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<KnowledgeLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(KnowledgeLog::getCreatedAt);
        return page(page, wrapper);
    }
    
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        long totalLogs = count();
        long ingestCount = count(new LambdaQueryWrapper<KnowledgeLog>().eq(KnowledgeLog::getAction, "ingest"));
        long queryCount = count(new LambdaQueryWrapper<KnowledgeLog>().eq(KnowledgeLog::getAction, "query"));
        stats.put("totalLogs", totalLogs);
        stats.put("ingest", ingestCount);
        stats.put("query", queryCount);
        return stats;
    }
}
