package com.zhuxianwiki.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhuxianwiki.entity.User;
import com.zhuxianwiki.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {
    
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    private static final String USER_TOKEN_PREFIX = "user:token:";
    
    public User login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username)
               .eq(User::getPassword, password)
               .eq(User::getStatus, 1);
        User user = userMapper.selectOne(wrapper);
        
        if (user != null) {
            String token = generateToken(user.getId());
            try {
                redisTemplate.opsForValue().set(USER_TOKEN_PREFIX + token, user, 7, TimeUnit.DAYS);
            } catch (Exception e) {
                log.warn("Redis unavailable, token not cached");
            }
            user.setPassword(null);
            return user;
        }
        return null;
    }
    
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }
    
    public User getUserByToken(String token) {
        try {
            return (User) redisTemplate.opsForValue().get(USER_TOKEN_PREFIX + token);
        } catch (Exception e) {
            log.warn("Redis unavailable, cannot get user by token");
            return null;
        }
    }
    
    public boolean register(User user) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            return false;
        }
        user.setRole("user");
        user.setStatus(1);
        return userMapper.insert(user) > 0;
    }
    
    public boolean updateUser(User user) {
        return userMapper.updateById(user) > 0;
    }
    
    public void logout(String token) {
        try {
            redisTemplate.delete(USER_TOKEN_PREFIX + token);
        } catch (Exception e) {
            log.warn("Redis unavailable, cannot delete token");
        }
    }
    
    private String generateToken(Long userId) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }
}
