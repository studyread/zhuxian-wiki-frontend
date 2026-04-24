package com.zhuxianwiki.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhuxianwiki.entity.Admin;
import com.zhuxianwiki.mapper.AdminMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService extends ServiceImpl<AdminMapper, Admin> {
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    public Admin login(String username, String password) {
        Admin admin = baseMapper.selectByUsername(username);
        if (admin != null && passwordEncoder.matches(password, admin.getPassword())) {
            return admin;
        }
        return null;
    }
    
    public boolean register(Admin admin) {
        // 检查用户名是否已存在
        Admin existing = baseMapper.selectByUsername(admin.getUsername());
        if (existing != null) {
            return false;
        }
        // 加密密码
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        admin.setStatus((byte) 1);
        return save(admin);
    }
}
