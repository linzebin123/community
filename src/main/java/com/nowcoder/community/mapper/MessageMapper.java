package com.nowcoder.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface MessageMapper extends BaseMapper<Message> {

    //查询当前用户的会话列表，针对每个会话返回一条最新的信息
//    public List<Message> selectConversations(int userId, int offset, int limit);

    //查询当前用户的会话数量
    public int selectConversationCount(int userId);

    //查询未读私信数量
    public int selectLetterUnreadCount(int userId,String conversationId);
}
