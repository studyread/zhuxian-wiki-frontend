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
        // 检查是否存在管理员，不存在则创建，存在则强制重置密码
        Admin existingAdmin = adminMapper.selectByUsername("admin");
        if (existingAdmin == null) {
            Admin defaultAdmin = new Admin();
            defaultAdmin.setUsername("admin");
            defaultAdmin.setNickname("超级管理员");
            defaultAdmin.setRole("super_admin");
            defaultAdmin.setStatus((byte) 1);
            adminMapper.insert(defaultAdmin);
            existingAdmin = adminMapper.selectByUsername("admin");
            System.out.println("默认管理员已创建: admin / admin123");
        }
        // 强制重置默认管理员密码
        if (existingAdmin != null) {
            existingAdmin.setPassword(passwordEncoder.encode("admin123"));
            adminMapper.updateById(existingAdmin);
            System.out.println("管理员密码已重置: admin / admin123");
        }
    }
}
