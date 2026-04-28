-- ==================== 诛仙世界 Wiki 完整数据库 ====================

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL UNIQUE,
  `password` VARCHAR(200) NOT NULL,
  `nickname` VARCHAR(50) DEFAULT '',
  `avatar` VARCHAR(500) DEFAULT '',
  `email` VARCHAR(100) DEFAULT '',
  `role` VARCHAR(20) DEFAULT 'user',
  `status` INT DEFAULT 1,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 管理员表
CREATE TABLE IF NOT EXISTS `admin` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL UNIQUE,
  `password` VARCHAR(200) NOT NULL,
  `nickname` VARCHAR(50) DEFAULT '',
  `avatar` VARCHAR(500) DEFAULT '',
  `role` VARCHAR(20) DEFAULT 'editor',
  `status` INT DEFAULT 1,
  `last_login_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 3. 文章分类表
CREATE TABLE IF NOT EXISTS `category` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `icon` VARCHAR(20) DEFAULT '',
  `color` VARCHAR(20) DEFAULT '',
  `parent_id` BIGINT DEFAULT NULL,
  `sort_order` INT DEFAULT 0,
  `status` INT DEFAULT 1,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章分类表';

-- 4. 文章表
CREATE TABLE IF NOT EXISTS `article` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL,
  `summary` VARCHAR(500) DEFAULT '',
  `content` LONGTEXT,
  `cover_image` VARCHAR(500) DEFAULT '',
  `category_id` BIGINT DEFAULT NULL,
  `tags` VARCHAR(500) DEFAULT '',
  `author_id` BIGINT DEFAULT NULL,
  `view_count` INT DEFAULT 0,
  `like_count` INT DEFAULT 0,
  `comment_count` INT DEFAULT 0,
  `status` INT DEFAULT 1,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_category` (`category_id`),
  INDEX `idx_author` (`author_id`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

-- 5. 聊天历史表
CREATE TABLE IF NOT EXISTS `chat_history` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT DEFAULT NULL,
  `session_id` VARCHAR(100) NOT NULL,
  `role` VARCHAR(20) NOT NULL,
  `content` TEXT,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_user` (`user_id`),
  INDEX `idx_session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天历史表';

-- 6. 用户文章点赞表
CREATE TABLE IF NOT EXISTS `user_article_like` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `article_id` BIGINT NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_user_article` (`user_id`, `article_id`),
  INDEX `idx_user` (`user_id`),
  INDEX `idx_article` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户文章点赞表';

-- 7. 收藏表
CREATE TABLE IF NOT EXISTS `favorite` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `article_id` BIGINT NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_user_article` (`user_id`, `article_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_article_id` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 8. 用户文章收藏表
CREATE TABLE IF NOT EXISTS `user_article_collect` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `article_id` BIGINT NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_user_article` (`user_id`, `article_id`),
  INDEX `idx_user` (`user_id`),
  INDEX `idx_article` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户文章收藏表';

-- 9. 知识库分类表
CREATE TABLE IF NOT EXISTS `knowledge_category` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `description` VARCHAR(255) DEFAULT '',
  `icon` VARCHAR(20) DEFAULT '书',
  `sort_order` INT DEFAULT 0,
  `status` TINYINT DEFAULT 1,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库分类表';

-- 10. 知识库词条表
CREATE TABLE IF NOT EXISTS `knowledge_entry` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL,
  `summary` VARCHAR(500) DEFAULT '',
  `content` LONGTEXT,
  `category_id` BIGINT DEFAULT NULL,
  `tags` VARCHAR(500) DEFAULT '',
  `author_id` BIGINT DEFAULT NULL,
  `view_count` INT DEFAULT 0,
  `like_count` INT DEFAULT 0,
  `status` TINYINT DEFAULT 1,
  `source_url` VARCHAR(500) DEFAULT '',
  `source_name` VARCHAR(100) DEFAULT '',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_title` (`title`),
  INDEX `idx_category` (`category_id`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库词条表';

-- 11. 知识库索引表
CREATE TABLE IF NOT EXISTS `knowledge_index` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `entry_id` BIGINT NOT NULL,
  `index_title` VARCHAR(200) NOT NULL,
  `index_content` TEXT,
  `link_to_entry` VARCHAR(200) DEFAULT '',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_entry` (`entry_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库索引表';

-- 12. 知识库操作日志表
CREATE TABLE IF NOT EXISTS `knowledge_log` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `operation` VARCHAR(50) NOT NULL,
  `title` VARCHAR(200) DEFAULT '',
  `description` TEXT,
  `operator_id` BIGINT DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_created` (`created_at`),
  INDEX `idx_operation` (`operation`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库操作日志表';

-- ==================== 初始化数据 ====================

-- 插入管理员 (密码: admin123)
INSERT INTO `admin` (`username`, `password`, `nickname`, `role`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '超级管理员', 'super_admin');

-- 插入知识库分类
INSERT INTO `knowledge_category` (`name`, `description`, `icon`, `sort_order`) VALUES
('门派职业', '各大门派职业技能、副本攻略', '门', 1),
('装备系统', '装备打造、强化、洗练攻略', '装', 2),
('副本攻略', '各类副本通关技巧', '副', 3),
('任务指引', '主线支线任务攻略', '任', 4),
('宠物坐骑', '宠物培养、坐骑获取', '宠', 5),
('生活技能', '采集、制造、炼金等', '技', 6),
('PVP指南', '竞技场、帮战攻略', '战', 7),
('新手入门', '新手玩家必读攻略', '新', 8);

-- 插入文章分类
INSERT INTO `category` (`name`, `icon`, `color`, `sort_order`) VALUES
('攻略', '攻', '#c45c48', 1),
('新闻', '新', '#4a90d9', 2),
('公告', '公', '#e67e22', 3),
('活动', '活', '#27ae60', 4);
