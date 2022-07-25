package com.nowcoder.community.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.User;

import java.util.List;

public interface MessageService extends IService<Message> {
    //查询当前用户的会话列表，针对每个会话返回一条最新的信息
    public List<Message> selectConversations(int userId, Page page);

    //查询某个会话的私息数量
    public int selectConversationCount(String conversationId);

    //查询某个会话的私信列表
    public List<Message> selectLetters(String conversationId,Page page);

    //查询某个会话的私信数量
    public int selectLetterCount(String conversationId);

    //查询未读私信数量
    public int selectLetterUnreadCount(int userId,String conversationId);



}
