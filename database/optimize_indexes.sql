-- 优化文章表查询性能
-- 执行前请备份数据库

-- 为 created_at 添加索引（用于最新文章排序）
CREATE INDEX idx_article_created_at ON article(created_at DESC);

-- 为 status 添加索引（用于过滤已发布文章）
CREATE INDEX idx_article_status ON article(status);

-- 组合索引：status + created_at（优化最新文章查询）
CREATE INDEX idx_article_status_created ON article(status, created_at DESC);

-- 为 view_count 和 like_count 添加索引（用于热门文章排序）
CREATE INDEX idx_article_view_count ON article(view_count DESC);
CREATE INDEX idx_article_like_count ON article(like_count DESC);

-- 组合索引：status + view_count + like_count（优化热门文章查询）
CREATE INDEX idx_article_hot ON article(status, view_count DESC, like_count DESC);
