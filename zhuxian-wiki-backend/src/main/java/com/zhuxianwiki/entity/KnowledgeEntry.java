package com.zhuxianwiki.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("knowledge_entry")
public class KnowledgeEntry {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    private String summary;
    private String content;
    private Long categoryId;
    private String tags;
    private Long authorId;
    private Integer viewCount;
    private Integer likeCount;
    private Integer status;
    private String sourceUrl;
    private String sourceName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
