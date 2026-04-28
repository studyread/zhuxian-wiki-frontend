package com.zhuxianwiki;

import com.zhuxianwiki.entity.Admin;
import com.zhuxianwiki.mapper.AdminMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@MapperScan("com.zhuxianwiki.mapper")
public class ZhuxianWikiApplication implements CommandLineRunner {
    
    @Autowired
    private AdminMapper adminMapper;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    public static void main(String[] args) {
        SpringApplication.run(ZhuxianWikiApplication.class, args);
    }
    
    @Override
    public void run(String... args) {
        // 检查是否存在管理员，不存在则创建
        Admin existingAdmin = adminMapper.selectByUsername("admin");
        if (existingAdmin == null) {
            Admin defaultAdmin = new Admin();
            defaultAdmin.setUsername("admin");
            defaultAdmin.setPassword(passwordEncoder.encode("admin123"));
            defaultAdmin.setNickname("超级管理员");
            defaultAdmin.setRole("super_admin");
            defaultAdmin.setStatus((byte) 1);
            adminMapper.insert(defaultAdmin);
            System.out.println("默认管理员已创建: admin / admin123");
        }
        // 不再强制重置密码，允许用户修改密码后保持不变
    }
}
