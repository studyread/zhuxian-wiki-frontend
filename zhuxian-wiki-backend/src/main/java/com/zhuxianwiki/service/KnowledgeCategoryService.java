package com.zhuxianwiki.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuxianwiki.entity.KnowledgeCategory;
import com.zhuxianwiki.mapper.KnowledgeCategoryMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class KnowledgeCategoryService extends ServiceImpl<KnowledgeCategoryMapper, KnowledgeCategory> {
    
    public List<KnowledgeCategory> getAllEnabled() {
        LambdaQueryWrapper<KnowledgeCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeCategory::getStatus, 1)
               .orderByAsc(KnowledgeCategory::getSortOrder);
        return list(wrapper);
    }
}
