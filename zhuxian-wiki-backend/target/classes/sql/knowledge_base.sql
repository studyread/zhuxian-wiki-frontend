-- ==================== 诛仙世界 Wiki 知识库表 ====================

-- 知识库分类表
CREATE TABLE IF NOT EXISTS `knowledge_category` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `description` VARCHAR(255) DEFAULT '' COMMENT '分类描述',
  `icon` VARCHAR(20) DEFAULT '书' COMMENT '分类图标',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库分类表';

-- 知识库词条表
CREATE TABLE IF NOT EXISTS `knowledge_entry` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '词条ID',
  `title` VARCHAR(200) NOT NULL COMMENT '词条标题',
  `summary` VARCHAR(500) DEFAULT '' COMMENT '词条摘要',
  `content` LONGTEXT COMMENT '词条内容(Markdown)',
  `category_id` BIGINT DEFAULT NULL COMMENT '所属分类',
  `tags` VARCHAR(500) DEFAULT '' COMMENT '标签，逗号分隔',
  `author_id` BIGINT DEFAULT NULL COMMENT '作者ID',
  `view_count` INT DEFAULT 0 COMMENT '浏览量',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  `status` TINYINT DEFAULT 1 COMMENT '状态 0草稿 1已发布 2已下架',
  `source_url` VARCHAR(500) DEFAULT '' COMMENT '来源URL',
  `source_name` VARCHAR(100) DEFAULT '' COMMENT '来源名称',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`category_id`) REFERENCES `knowledge_category`(`id`) ON DELETE SET NULL,
  INDEX `idx_title` (`title`),
  INDEX `idx_category` (`category_id`),
  INDEX `idx_status` (`status`),
  FULLTEXT INDEX `ft_content` (`title`, `summary`, `content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库词条表';

-- 知识库索引表(用于快速检索)
CREATE TABLE IF NOT EXISTS `knowledge_index` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `entry_id` BIGINT NOT NULL COMMENT '词条ID',
  `index_title` VARCHAR(200) NOT NULL COMMENT '索引标题',
  `index_content` TEXT COMMENT '索引内容摘要',
  `link_to_entry` VARCHAR(200) DEFAULT '' COMMENT '关联到其他词条的链接',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`entry_id`) REFERENCES `knowledge_entry`(`id`) ON DELETE CASCADE,
  INDEX `idx_entry` (`entry_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库索引表';

-- 知识库操作日志表
CREATE TABLE IF NOT EXISTS `knowledge_log` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `operation` VARCHAR(50) NOT NULL COMMENT '操作类型: ingest/query/lint',
  `title` VARCHAR(200) DEFAULT '' COMMENT '相关词条标题',
  `description` TEXT COMMENT '操作描述',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作人',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_created` (`created_at`),
  INDEX `idx_operation` (`operation`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库操作日志表';

-- 管理员表
CREATE TABLE IF NOT EXISTS `admin` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '管理员ID',
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  `password` VARCHAR(200) NOT NULL COMMENT '密码(加密)',
  `nickname` VARCHAR(50) DEFAULT '' COMMENT '昵称',
  `avatar` VARCHAR(500) DEFAULT '' COMMENT '头像URL',
  `role` VARCHAR(20) DEFAULT 'editor' COMMENT '角色: super_admin/superadmin editor',
  `status` TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `last_login_at` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- ==================== 初始化数据 ====================

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

-- 插入示例词条
INSERT INTO `knowledge_entry` (`title`, `summary`, `content`, `category_id`, `tags`, `status`) VALUES
('青云门职业指南', '青云门是诛仙世界中的远程法术输出门派，擅长群体攻击和控制', '# 青云门职业指南\n\n## 职业定位\n青云门是经典的远程法术输出门派，擅长群体伤害和群体控制。\n\n## 核心技能\n- 天灵剑诀：单体高伤害技能\n- 寒冰咒：群体减速控制\n- 炼气还神：回蓝技能\n- 七劫杀：终极技能\n\n## 加点推荐\n主加青云门核心输出技能', '门派职业', '青云门,法术,输出,群攻', 1);

-- 插入管理员(密码: admin123)
INSERT INTO `admin` (`username`, `password`, `nickname`, `role`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '超级管理员', 'super_admin');
