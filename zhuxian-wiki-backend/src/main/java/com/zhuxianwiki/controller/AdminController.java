package com.zhuxianwiki.controller;

import com.zhuxianwiki.entity.Admin;
import com.zhuxianwiki.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    @Autowired
    private AdminService adminService;
    
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
}
