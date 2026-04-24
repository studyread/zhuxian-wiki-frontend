package com.zhuxianwiki.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhuxianwiki.entity.KnowledgeEntry;
import com.zhuxianwiki.service.KnowledgeEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge/entry")
public class KnowledgeEntryController {
    
    @Autowired
    private KnowledgeEntryService entryService;
    
    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> result = new HashMap<>();
        IPage<KnowledgeEntry> pageResult = entryService.getPage(categoryId, status, page, size);
        result.put("code", 200);
        result.put("data", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", pageResult.getCurrent());
        result.put("size", pageResult.getSize());
        return result;
    }
    
    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        KnowledgeEntry entry = entryService.getById(id);
        if (entry != null) {
            entryService.incrementViewCount(id);
        }
        result.put("code", 200);
        result.put("data", entry);
        return result;
    }
    
    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam String keyword) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", entryService.search(keyword));
        return result;
    }
    
    @PostMapping
    public Map<String, Object> create(@RequestBody KnowledgeEntry entry) {
        Map<String, Object> result = new HashMap<>();
        entryService.create(entry);
        result.put("code", 200);
        result.put("data", entry);
        result.put("message", "创建成功");
        return result;
    }
    
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody KnowledgeEntry entry) {
        Map<String, Object> result = new HashMap<>();
        entry.setId(id);
        entryService.update(entry);
        result.put("code", 200);
        result.put("data", entry);
        result.put("message", "更新成功");
        return result;
    }
    
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        entryService.delete(id);
        result.put("code", 200);
        result.put("message", "删除成功");
        return result;
    }
    
    @GetMapping("/export")
    public Map<String, Object> export() {
        Map<String, Object> result = new HashMap<>();
        String markdown = entryService.exportAll();
        result.put("code", 200);
        result.put("data", markdown);
        result.put("message", "导出成功");
        return result;
    }
}
