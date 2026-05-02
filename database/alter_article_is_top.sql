-- 为 article 表添加 is_top 字段
-- 执行此 SQL 来支持文章置顶功能

ALTER TABLE article ADD COLUMN is_top INT DEFAULT 0 COMMENT '是否置顶：0-否，1-是';
