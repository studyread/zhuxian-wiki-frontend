package com.zhuxianwiki.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("knowledge_log")
public class KnowledgeLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("operator_id")
    private Long userId;
    
    @TableField("operation")
    private String action;
    
    @TableField("title")
    private String targetType;
    
    private Long targetId;
    
    @TableField("description")
    private String detail;
    
    private String ipAddress;
    private LocalDateTime createdAt;
}