-- 诛仙世界Wiki数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS zhuxian_wiki DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE zhuxian_wiki;

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `avatar` VARCHAR(255) COMMENT '头像URL',
    `email` VARCHAR(100) COMMENT '邮箱',
    `role` VARCHAR(20) DEFAULT 'user' COMMENT '角色: admin/user',
    `status` INT DEFAULT 1 COMMENT '状态: 1正常 0禁用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 分类表
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `icon` VARCHAR(50) COMMENT '图标',
    `color` VARCHAR(20) COMMENT '颜色',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` INT DEFAULT 1 COMMENT '状态: 1正常 0禁用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- 文章表
DROP TABLE IF EXISTS `article`;
CREATE TABLE `article` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文章ID',
    `title` VARCHAR(255) NOT NULL COMMENT '标题',
    `summary` VARCHAR(500) COMMENT '摘要',
    `content` TEXT COMMENT '内容(Markdown)',
    `cover_image` VARCHAR(255) COMMENT '封面图',
    `category_id` BIGINT COMMENT '分类ID',
    `tags` VARCHAR(500) COMMENT '标签,逗号分隔',
    `author_id` BIGINT COMMENT '作者ID',
    `view_count` INT DEFAULT 0 COMMENT '浏览量',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT DEFAULT 0 COMMENT '评论数',
    `status` INT DEFAULT 1 COMMENT '状态: 1已发布 0草稿',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_category` (`category_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

-- 聊天记录表
DROP TABLE IF EXISTS `chat_history`;
CREATE TABLE `chat_history` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT COMMENT '用户ID',
    `session_id` VARCHAR(100) COMMENT '会话ID',
    `role` VARCHAR(20) NOT NULL COMMENT '角色: user/assistant',
    `content` TEXT NOT NULL COMMENT '内容',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天记录表';

-- 插入默认管理员
INSERT INTO `user` (`username`, `password`, `nickname`, `role`) VALUES
('admin', 'admin123', '管理员', 'admin');

-- 插入默认分类
INSERT INTO `category` (`name`, `icon`, `color`, `parent_id`, `sort_order`) VALUES
('门派', 'icon-menpai', '#e94560', 0, 1),
('副本', 'icon-fuben', '#4ecdc4', 0, 2),
('攻略', 'icon-gonglue', '#45b7d1', 0, 3),
('任务', 'icon-renwu', '#f9ca24', 0, 4),
('装备', 'icon-zhuangbei', '#6c5ce7', 0, 5),
('伙伴', 'icon-huoban', '#fd79a8', 0, 6);

-- 插入子分类
INSERT INTO `category` (`name`, `icon`, `color`, `parent_id`, `sort_order`) VALUES
('青云门', 'icon-qingyun', '#00b894', 1, 1),
('天音寺', 'icon-tianyin', '#fdcb6e', 1, 2),
('鬼王宗', 'icon-guiwang', '#636e72', 1, 3),
('合欢派', 'icon-hehuan', '#e17055', 1, 4),
('焚香谷', 'icon-fenxiang', '#d63031', 1, 5),
('云梦宗', 'icon-yunmeng', '#74b9ff', 1, 6),
('普通副本', 'icon-putong', '#55a3ff', 2, 1),
('精英副本', 'icon-jingying', '#ff9500', 2, 2),
('团队副本', 'icon-tuandui', '#ff4757', 2, 3);

-- 插入示例文章
INSERT INTO `article` (`title`, `summary`, `content`, `category_id`, `tags`, `view_count`, `like_count`, `status`) VALUES
('青云门新手入门攻略', '详细介绍青云门的技能、加点、副本玩法', '青云门是诛仙世界中的法师门派，擅长远程法术攻击。\n\n## 技能介绍\n\n1. 寒冰咒：基础输出技能\n2. 五雷轰顶：AOE技能\n3. 剑引苍龙：终极技能\n\n## 加点推荐\n\n主加灵力，副加体质。青云门作为游戏中的核心输出门派，需要注意走位和技能释放时机。', 1, '青云门,新手,入门,攻略', 1523, 89, 1),
('天音寺职业指南', '天音寺治疗与辅助能力全面解析', '天音寺是游戏中的治疗辅助门派。\n\n## 核心定位\n\n- 治疗输出\n- 辅助控制\n- 团队增益\n\n## 技能搭配\n\n推荐使用金刚咒+狮子吼组合。天音寺在团队副本中不可或缺，是保护队友的重要角色。', 2, '天音寺,职业,治疗,攻略', 984, 56, 1),
('云梦宗连招技巧', '云梦宗PVP连招教学', '云梦宗是刺客型门派，爆发伤害高。\n\n## 连招顺序\n\n1. 隐身接近\n2. 暗影突袭起手\n3. 致命一击\n4. 瞬移撤离\n\n## 注意事项\n\n连招需要一定的手速和预判。云梦宗在PVP中非常强力，但生存能力较弱。', 1, '云梦宗,连招,PVP,技巧', 2156, 134, 1),
('七里峒副本通关攻略', '详细讲解BOSS机制和通关要点', '七里峒是游戏中的经典副本。\n\n## BOSS机制\n\n1. 毒雾：需要及时走位躲避\n2. 范围攻击：注意站位\n3. 召唤小怪：优先击杀\n\n## 通关要点\n\n队伍配置建议：1T+2奶+3输出。团队副本需要良好的配合和走位。', 7, '副本,七里峒,通关,攻略', 3456, 201, 1),
('装备强化技巧', '如何高效强化装备', '强化是提升战力的重要途径。\n\n## 强化技巧\n\n1. 选择合适的强化时间\n2. 使用保护符\n3. 关注强化成功率活动\n\n## 强化优先级\n\n武器 > 防具 > 饰品\n\n## 注意事项\n\n强化失败不会降级，但会消耗材料。建议在有活动时进行强化。', 5, '装备,强化,技巧,战力', 2890, 178, 1);
