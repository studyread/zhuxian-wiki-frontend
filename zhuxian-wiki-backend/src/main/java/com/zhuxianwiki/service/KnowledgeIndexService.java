package com.zhuxianwiki.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuxianwiki.entity.KnowledgeIndex;
import com.zhuxianwiki.mapper.KnowledgeIndexMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class KnowledgeIndexService extends ServiceImpl<KnowledgeIndexMapper, KnowledgeIndex> {
    
    public List<KnowledgeIndex> getByEntryId(Long entryId) {
        return lambdaQuery().eq(KnowledgeIndex::getEntryId, entryId).list();
    }
}
