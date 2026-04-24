package com.zhuxianwiki.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhuxianwiki.entity.KnowledgeLog;
import com.zhuxianwiki.service.KnowledgeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge/log")
public class KnowledgeLogController {
    
    @Autowired
    private KnowledgeLogService logService;
    
    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Map<String, Object> result = new HashMap<>();
        IPage<KnowledgeLog> pageResult = logService.getRecentLogs(page, size);
        result.put("code", 200);
        result.put("data", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        return result;
    }
}
