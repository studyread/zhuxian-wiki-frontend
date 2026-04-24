package com.zhuxianwiki.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("knowledge_index")
public class KnowledgeIndex {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long entryId;
    private String indexTitle;
    private String indexContent;
    private String linkToEntry;
    private LocalDateTime createdAt;
}
