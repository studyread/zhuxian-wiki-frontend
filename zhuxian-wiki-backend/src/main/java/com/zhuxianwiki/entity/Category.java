package com.zhuxianwiki.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("category")
public class Category {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String icon;
    
    private String color;
    
    @TableField("parent_id")
    private Long parentId;
    
    @TableField("sort_order")
    private Integer sortOrder;
    
    private Integer status;
    
@TableField(fill = FieldFill.INSERT)
    @TableField("create_time")
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @TableField("update_time")
    private LocalDateTime updatedAt;
}
