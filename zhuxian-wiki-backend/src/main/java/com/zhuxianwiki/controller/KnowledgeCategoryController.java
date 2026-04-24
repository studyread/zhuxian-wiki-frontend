package com.zhuxianwiki.controller;

import com.zhuxianwiki.entity.KnowledgeCategory;
import com.zhuxianwiki.service.KnowledgeCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge/category")
public class KnowledgeCategoryController {
    
    @Autowired
    private KnowledgeCategoryService categoryService;
    
    @GetMapping("/list")
    public Map<String, Object> list() {
        Map<String, Object> result = new HashMap<>();
        List<KnowledgeCategory> categories = categoryService.getAllEnabled();
        result.put("code", 200);
        result.put("data", categories);
        return result;
    }
    
    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        KnowledgeCategory category = categoryService.getById(id);
        result.put("code", 200);
        result.put("data", category);
        return result;
    }
    
    @PostMapping
    public Map<String, Object> create(@RequestBody KnowledgeCategory category) {
        Map<String, Object> result = new HashMap<>();
        categoryService.save(category);
        result.put("code", 200);
        result.put("data", category);
        return result;
    }
    
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody KnowledgeCategory category) {
        Map<String, Object> result = new HashMap<>();
        category.setId(id);
        categoryService.updateById(category);
        result.put("code", 200);
        result.put("data", category);
        return result;
    }
    
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        categoryService.removeById(id);
        result.put("code", 200);
        result.put("message", "删除成功");
        return result;
    }
}
