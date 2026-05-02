package com.zhuxianwiki.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文章简化VO（用于列表页，不包含大字段）
 */
@Data
public class ArticleSimpleVO {
    private Long id;
    private String title;
    private String summary;      // 列表页只显示简短摘要
    private Long categoryId;
    private Integer viewCount;
    private Integer likeCount;
    private LocalDateTime createdAt;
}
