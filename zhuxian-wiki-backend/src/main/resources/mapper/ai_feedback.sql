-- AI 聊天满意度反馈表
CREATE TABLE IF NOT EXISTS `ai_feedback` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `user_id` BIGINT COMMENT '用户ID（可为空，游客反馈）',
    `session_id` VARCHAR(255) COMMENT '会话ID',
    `question` TEXT COMMENT '用户问题',
    `answer` TEXT COMMENT 'AI回答',
    `rating` TINYINT COMMENT '评分：1=满意，0=不满意',
    `feedback_text` TEXT COMMENT '用户反馈文字（可选）',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_session_id` (`session_id`),
    INDEX `idx_rating` (`rating`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI聊天满意度反馈表';
