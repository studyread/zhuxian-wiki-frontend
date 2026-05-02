package com.zhuxianwiki.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ai_feedback")
public class AiFeedback {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("session_id")
    private String sessionId;

    @TableField("question")
    private String question;

    @TableField("answer")
    private String answer;

    @TableField("rating")
    private Integer rating; // 1 = 满意, 0 = 不满意

    @TableField("feedback_text")
    private String feedbackText;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
