package com.zhuxianwiki.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhuxianwiki.entity.Category;
import com.zhuxianwiki.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    public List<Category> getAllCategories() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getStatus, 1)
               .orderByAsc(Category::getSortOrder);
        return categoryMapper.selectList(wrapper);
    }
    
    public List<Category> getRootCategories() {
        return categoryMapper.selectRootCategories();
    }
    
    public List<Category> getChildrenByParentId(Long parentId) {
        return categoryMapper.selectChildrenByParentId(parentId);
    }
    
    public Category getCategoryById(Long id) {
        return categoryMapper.selectById(id);
    }
    
    public boolean saveCategory(Category category) {
        return categoryMapper.insert(category) > 0;
    }
    
    public boolean updateCategory(Category category) {
        return categoryMapper.updateById(category) > 0;
    }
    
    public boolean deleteCategory(Long id) {
        return categoryMapper.deleteById(id) > 0;
    }
}
