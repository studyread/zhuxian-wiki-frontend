package com.zhuxianwiki.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuxianwiki.entity.KnowledgeEntry;
import com.zhuxianwiki.entity.KnowledgeLog;
import com.zhuxianwiki.mapper.KnowledgeEntryMapper;
import com.zhuxianwiki.mapper.KnowledgeLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class KnowledgeEntryService extends ServiceImpl<KnowledgeEntryMapper, KnowledgeEntry> {
    
    @Autowired
    private KnowledgeLogMapper logMapper;
    
    @Autowired
    private KnowledgeIndexService indexService;
    
    public IPage<KnowledgeEntry> getPage(Long categoryId, Integer status, int pageNum, int pageSize) {
        Page<KnowledgeEntry> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectByPage(page, categoryId, status);
    }
    
    public List<KnowledgeEntry> search(String keyword) {
        return baseMapper.searchByKeyword(keyword);
    }
    
    @Transactional
    public KnowledgeEntry create(KnowledgeEntry entry) {
        entry.setCreatedAt(LocalDateTime.now());
        entry.setUpdatedAt(LocalDateTime.now());
        entry.setViewCount(0);
        entry.setLikeCount(0);
        if (entry.getStatus() == null) {
            entry.setStatus(1);
        }
        save(entry);
        
        // 记录日志
        logOperation("ingest", entry.getTitle(), "新增词条: " + entry.getTitle(), null);
        
        return entry;
    }
    
    @Transactional
    public KnowledgeEntry update(KnowledgeEntry entry) {
        entry.setUpdatedAt(LocalDateTime.now());
        updateById(entry);
        
        logOperation("ingest", entry.getTitle(), "更新词条: " + entry.getTitle(), null);
        
        return entry;
    }
    
    @Transactional
    public boolean delete(Long id) {
        KnowledgeEntry entry = getById(id);
        if (entry != null) {
            logOperation("lint", entry.getTitle(), "删除词条: " + entry.getTitle(), null);
        }
        return removeById(id);
    }
    
    public void incrementViewCount(Long id) {
        KnowledgeEntry entry = getById(id);
        if (entry != null) {
            entry.setViewCount(entry.getViewCount() + 1);
            updateById(entry);
        }
    }
    
    public String exportAll() {
        List<KnowledgeEntry> entries = list(new LambdaQueryWrapper<KnowledgeEntry>()
                .eq(KnowledgeEntry::getStatus, 1)
                .orderByDesc(KnowledgeEntry::getUpdatedAt));
        
        StringBuilder md = new StringBuilder();
        md.append("# 诛仙世界游戏攻略知识库\n\n");
        md.append("> 导出时间: ").append(LocalDateTime.now()).append("\n\n");
        md.append("---\n\n");
        
        for (KnowledgeEntry entry : entries) {
            md.append("## ").append(entry.getTitle()).append("\n\n");
            if (entry.getSummary() != null && !entry.getSummary().isEmpty()) {
                md.append("**摘要**: ").append(entry.getSummary()).append("\n\n");
            }
            if (entry.getTags() != null && !entry.getTags().isEmpty()) {
                md.append("**标签**: ").append(entry.getTags()).append("\n\n");
            }
            md.append(entry.getContent()).append("\n\n");
            md.append("---\n\n");
        }
        
        return md.toString();
    }
    
    private void logOperation(String operation, String title, String description, Long operatorId) {
        KnowledgeLog log = new KnowledgeLog();
        log.setAction(operation);
        log.setTargetType("entry");
        log.setDetail("新增词条: " + title);
        log.setUserId(operatorId);
        log.setCreatedAt(LocalDateTime.now());
        logMapper.insert(log);
    }
}
