-- 修复所有文章的status为已发布（1）
UPDATE article SET status = 1 WHERE status IS NULL OR status = 0;

-- 验证修复结果
SELECT id, title, status FROM article;
