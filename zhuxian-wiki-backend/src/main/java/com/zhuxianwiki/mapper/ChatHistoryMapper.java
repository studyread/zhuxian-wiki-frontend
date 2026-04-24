package com.zhuxianwiki.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuxianwiki.entity.ChatHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ChatHistoryMapper extends BaseMapper<ChatHistory> {
    
    List<ChatHistory> selectBySessionId(@Param("sessionId") String sessionId);
    
    void clearSessionHistory(@Param("sessionId") String sessionId);
    
    void insertWithContent(ChatHistory chatHistory);
}
