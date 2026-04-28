-- 用户文章点赞表（每个用户对每篇文章只能点赞一次）
CREATE TABLE IF NOT EXISTS `user_article_like` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `article_id` BIGINT NOT NULL COMMENT '文章ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_user_article` (`user_id`, `article_id`),
  INDEX `idx_user` (`user_id`),
  INDEX `idx_article` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户文章点赞表';
