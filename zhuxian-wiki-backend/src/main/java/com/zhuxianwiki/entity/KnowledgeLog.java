package com.zhuxianwiki.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("knowledge_log")
public class KnowledgeLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String operation;
    private String title;
    private String description;
    private Long operatorId;
    private LocalDateTime createdAt;
}
