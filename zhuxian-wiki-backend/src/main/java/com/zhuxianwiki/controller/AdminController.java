package com.zhuxianwiki.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhuxianwiki.entity.Admin;
import com.zhuxianwiki.entity.Article;
import com.zhuxianwiki.service.AdminService;
import com.zhuxianwiki.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ArticleService articleService;
    
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String username = params.get("username");
        String password = params.get("password");
        
        Admin admin = adminService.login(username, password);
        if (admin != null) {
            // 更新最后登录时间
            admin.setLastLoginAt(LocalDateTime.now());
            adminService.updateById(admin);
            
            result.put("code", 200);
            result.put("message", "登录成功");
            Map<String, Object> data = new HashMap<>();
            data.put("id", admin.getId());
            data.put("username", admin.getUsername());
            data.put("nickname", admin.getNickname());
            data.put("role", admin.getRole());
            result.put("data", data);
        } else {
            result.put("code", 401);
            result.put("message", "用户名或密码错误");
        }
        return result;
    }
    
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Admin admin) {
        Map<String, Object> result = new HashMap<>();
        if (adminService.register(admin)) {
            result.put("code", 200);
            result.put("message", "注册成功");
        } else {
            result.put("code", 400);
            result.put("message", "用户名已存在");
        }
        return result;
    }
    
    @GetMapping("/info")
    public Map<String, Object> info(@RequestHeader(value = "X-Admin-Id", required = false) Long adminId) {
        Map<String, Object> result = new HashMap<>();
        if (adminId != null) {
            Admin admin = adminService.getById(adminId);
            if (admin != null) {
                result.put("code", 200);
                Map<String, Object> data = new HashMap<>();
                data.put("id", admin.getId());
                data.put("username", admin.getUsername());
                data.put("nickname", admin.getNickname());
                data.put("role", admin.getRole());
                result.put("data", data);
                return result;
            }
        }
        result.put("code", 401);
        result.put("message", "未登录");
        return result;
    }

    // ========== 文章管理接口 ==========

    /**
     * 获取文章列表（分页+搜索+筛选）
     */
    @GetMapping("/articles")
    public Map<String, Object> getArticleList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String sort) {

        Map<String, Object> result = new HashMap<>();
        IPage<Article> pageResult = articleService.getAdminArticlePage(page, size, keyword, categoryId, status, sort);

        result.put("code", 200);
        result.put("data", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("pages", pageResult.getPages());
        result.put("current", pageResult.getCurrent());
        return result;
    }

    /**
     * 获取文章详情
     */
    @GetMapping("/articles/{id}")
    public Map<String, Object> getArticleById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        Article article = articleService.getArticleByIdAdmin(id);

        if (article != null) {
            result.put("code", 200);
            result.put("data", article);
        } else {
            result.put("code", 404);
            result.put("message", "文章不存在");
        }
        return result;
    }

    /**
     * 新增文章
     */
    @PostMapping("/articles")
    public Map<String, Object> createArticle(
            @RequestBody Article article,
            @RequestHeader(value = "X-Admin-Id", required = false) Long adminId) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 设置默认状态为已发布
            if (article.getStatus() == null) {
                article.setStatus(1);
            }
            // 设置作者ID为管理员ID
            if (adminId != null) {
                article.setAuthorId(adminId);
            }

            boolean success = articleService.saveArticle(article);
            result.put("code", success ? 200 : 500);
            result.put("message", success ? "创建成功" : "创建失败");
            if (success) {
                result.put("data", article);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "创建失败: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 更新文章
     */
    @PutMapping("/articles/{id}")
    public Map<String, Object> updateArticle(
            @PathVariable Long id,
            @RequestBody Article article) {

        Map<String, Object> result = new HashMap<>();

        // 确保ID一致
        article.setId(id);

        boolean success = articleService.updateArticle(article);
        result.put("code", success ? 200 : 500);
        result.put("message", success ? "更新成功" : "更新失败");
        if (success) {
            result.put("data", article);
        }
        return result;
    }

    /**
     * 删除文章
     */
    @DeleteMapping("/articles/{id}")
    public Map<String, Object> deleteArticle(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        boolean success = articleService.deleteArticleAdmin(id);
        result.put("code", success ? 200 : 500);
        result.put("message", success ? "删除成功" : "删除失败");
        return result;
    }

    /**
     * 置顶文章
     */
    @PostMapping("/articles/{id}/top")
    public Map<String, Object> toggleTopArticle(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        boolean success = articleService.toggleTopArticle(id);
        result.put("code", success ? 200 : 500);
        result.put("message", success ? "置顶成功" : "置顶失败");
        return result;
    }

    /**
     * 取消置顶
     */
    @DeleteMapping("/articles/{id}/top")
    public Map<String, Object> untoggleTopArticle(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        boolean success = articleService.untoggleTopArticle(id);
        result.put("code", success ? 200 : 500);
        result.put("message", success ? "取消置顶成功" : "取消置顶失败");
        return result;
    }
}
