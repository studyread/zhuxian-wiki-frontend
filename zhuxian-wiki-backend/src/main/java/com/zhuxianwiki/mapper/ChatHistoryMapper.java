package com.zhuxianwiki.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuxianwiki.entity.ChatHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ChatHistoryMapper extends BaseMapper<ChatHistory> {
    
    List<ChatHistory> selectBySessionId(@Param("sessionId") String sessionId);
    
    /** 按用户ID查询所有会话列表（每个会话返回一条最新记录） */
    List<ChatHistory> selectUserSessionList(@Param("userId") Long userId);
    
    /** 按用户ID查询该用户的所有消息 */
    List<ChatHistory> selectUserSessionMessages(@Param("userId") Long userId);
    
    void clearSessionHistory(@Param("sessionId") String sessionId);
    
    /** 删除指定会话中最老的N条记录 */
    void deleteOldestBySessionId(@Param("sessionId") String sessionId, @Param("limit") int limit);
    
    /** 统计指定会话的记录数量 */
    int countBySessionId(@Param("sessionId") String sessionId);
    
    void insertWithContent(ChatHistory chatHistory);
}
