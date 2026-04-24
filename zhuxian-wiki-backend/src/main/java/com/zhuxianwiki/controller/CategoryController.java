package com.zhuxianwiki.controller;

import com.zhuxianwiki.entity.Category;
import com.zhuxianwiki.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    @GetMapping
    public Map<String, Object> getAllCategories() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", categoryService.getAllCategories());
        return result;
    }
    
    @GetMapping("/root")
    public Map<String, Object> getRootCategories() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", categoryService.getRootCategories());
        return result;
    }
    
    @GetMapping("/{id}/children")
    public Map<String, Object> getChildrenByParentId(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", categoryService.getChildrenByParentId(id));
        return result;
    }
    
    @GetMapping("/by-name/{name}")
    public Map<String, Object> getCategoryByName(@PathVariable String name) {
        Map<String, Object> result = new HashMap<>();
        List<Category> all = categoryService.getAllCategories();
        Category category = all.stream()
            .filter(c -> name.equals(c.getName()))
            .findFirst()
            .orElse(null);
        
        if (category != null) {
            result.put("code", 200);
            result.put("data", category);
        } else {
            result.put("code", 404);
            result.put("message", "分类不存在");
        }
        return result;
    }
    
    @GetMapping("/{id}")
    public Map<String, Object> getCategoryById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        Category category = categoryService.getCategoryById(id);
        
        if (category != null) {
            result.put("code", 200);
            result.put("data", category);
        } else {
            result.put("code", 404);
            result.put("message", "分类不存在");
        }
        return result;
    }
    
    @PostMapping
    public Map<String, Object> saveCategory(@RequestBody Category category) {
        Map<String, Object> result = new HashMap<>();
        boolean success = categoryService.saveCategory(category);
        
        result.put("code", success ? 200 : 500);
        result.put("message", success ? "保存成功" : "保存失败");
        return result;
    }
    
    @PutMapping("/{id}")
    public Map<String, Object> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        Map<String, Object> result = new HashMap<>();
        category.setId(id);
        boolean success = categoryService.updateCategory(category);
        
        result.put("code", success ? 200 : 500);
        result.put("message", success ? "更新成功" : "更新失败");
        return result;
    }
    
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteCategory(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        boolean success = categoryService.deleteCategory(id);
        
        result.put("code", success ? 200 : 500);
        result.put("message", success ? "删除成功" : "删除失败");
        return result;
    }
}
