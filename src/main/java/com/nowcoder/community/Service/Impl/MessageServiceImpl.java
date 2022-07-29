package com.nowcoder.community.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nowcoder.community.Service.MessageService;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.mapper.MessageMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {
    @Autowired
    private MessageMapper messageMapper;
    @Override
    public List<Message> selectConversations( int userId,Page page) {

        //每页显示5条信息
        page.setSize(5);
        LambdaQueryWrapper<Message> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.inSql(Message::getId,
                "select max(id) from message\n" +
                "where status!=2 and from_id!=1\n" +
                "and (from_id="+userId+" or to_id="+userId+")\n" +
                "group by conversation_id").orderByDesc(Message::getId);


        messageMapper.selectPage(page,queryWrapper);
        List<Message> conversationList = page.getRecords();
        return conversationList;
    }

//    @Override
//    public int selectConversationCount(String conversationId) {
//
//        LambdaQueryWrapper<Message> queryWrapper=new LambdaQueryWrapper<>();
//        queryWrapper.eq(Message::getConversationId,conversationId);
//        queryWrapper.ne(Message::getFromId,1);
//        queryWrapper.ne(Message::getStatus,2);
//        queryWrapper.eq(Message::getConversationId,conversationId);
//        return messageMapper.selectCount(queryWrapper);
//
//    }

    @Override
    public List<Message> selectLetters(String conversationId,Page page) {

        LambdaQueryWrapper<Message> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.ne(Message::getStatus,2);
        queryWrapper.ne(Message::getFromId,1);
        queryWrapper.eq(Message::getConversationId,conversationId);
        queryWrapper.orderByDesc(Message::getId);

        messageMapper.selectPage(page,queryWrapper);

        List<Message> letterList= page.getRecords();
        return letterList;
    }

    @Override
    public int selectLetterCount(String conversationId) {
        LambdaQueryWrapper<Message> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.ne(Message::getStatus,2);
        queryWrapper.ne(Message::getFromId,1);
        queryWrapper.eq(Message::getConversationId,conversationId);
        Integer letterCount = messageMapper.selectCount(queryWrapper);
        return letterCount;

    }

    @Override
    public int selectLetterUnreadCount(int userId, String conversationId) {

        return messageMapper.selectLetterUnreadCount(userId,conversationId);
    }

    @Override
    public void readMessage(List<Message> messageList,int userId) {
        List<Message> newMessageList=new ArrayList<>();

        for(Message message:messageList){
            //提取出当前用户是接收方并且是未读消息
            if (message.getToId()==userId && message.getStatus()==0){
                message.setStatus(1);
                newMessageList.add(message);
            }
        }
        this.updateBatchById(newMessageList);
    }

    @Override
    public Message selectLatesNotice(int userId, String topic) {

        LambdaQueryWrapper<Message> queryWrapper=new LambdaQueryWrapper<>();

        queryWrapper.eq(Message::getConversationId,topic);
        //status为2代表已删除
        queryWrapper.ne(Message::getStatus,2);
        queryWrapper.eq(Message::getFromId,1);
        queryWrapper.eq(Message::getToId,userId);

        queryWrapper.orderByDesc(Message::getId);
        queryWrapper.last(" limit 1");
        Message message = messageMapper.selectOne(queryWrapper);
        return message;
    }

    @Override
    public int selectNoticeCount(int userId, String topic) {
        LambdaQueryWrapper<Message> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.ne(Message::getStatus,2);
        queryWrapper.eq(Message::getToId,userId);
        //1代表系统发送的消息
        queryWrapper.eq(Message::getFromId,1);
        queryWrapper.eq(Message::getConversationId,topic);
        int noticeCount = messageMapper.selectCount(queryWrapper);
        return noticeCount;
    }

    @Override
    public int selectNoticeUnreadCount(int userId, String topic) {
        LambdaQueryWrapper<Message> queryWrapper=new LambdaQueryWrapper<>();
        //状态0代表未读
        queryWrapper.eq(Message::getStatus,0);
        queryWrapper.eq(Message::getToId,userId);
        queryWrapper.eq(Message::getFromId,1);
        queryWrapper.eq(topic!=null,Message::getConversationId,topic);
        int noticeUnreadCount = messageMapper.selectCount(queryWrapper);
        return noticeUnreadCount;


    }

    @Override
    public List<Message> selectNoticesByTopic(int userId, String topic, Page page) {

        LambdaQueryWrapper<Message> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getToId,userId);
        queryWrapper.eq(Message::getConversationId,topic);
        queryWrapper.eq(Message::getFromId,1);
        //状态2代表已经被删除
        queryWrapper.ne(Message::getStatus,2);
        queryWrapper.orderByDesc(Message::getCreateTime);
        messageMapper.selectPage(page,queryWrapper);
        return page.getRecords();
    }


}
