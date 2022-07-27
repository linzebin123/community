package com.nowcoder.community.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nowcoder.community.Service.MessageService;
import com.nowcoder.community.Service.UserService;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.utils.CommunityUtil;
import com.nowcoder.community.utils.HostHolder;
import com.nowcoder.community.utils.SensitiveFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController {
    @Autowired
    private MessageService messageService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;
    @Autowired
    private SensitiveFilter sensitiveFilter;

    //私信列表
    @GetMapping("/letter/list")
    public String getLetterList(Model model, Page page){
        User user = hostHolder.getUser();
        //会话列表
        List<Message> conversationList = messageService.selectConversations(user.getId(), page);
        List<Map<String,Object>> conversations=new ArrayList<>();
        if (conversationList!=null){
            for (Message message:conversationList){
                Map<String,Object> map=new HashMap<>();
                map.put("conversation",message);
                map.put("letterCount",messageService.selectLetterCount(message.getConversationId()));
                map.put("unreadCount",messageService.selectLetterUnreadCount(user.getId(),message.getConversationId()));
                int targetId=user.getId()==message.getFromId() ? message.getToId():message.getFromId();
                map.put("target",userService.getById(targetId));
                conversations.add(map);
            }

        }
        model.addAttribute("conversations",conversations);

        //查询当前用户未读私信总数量
        int letterUnreadCount = messageService.selectLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        return "/site/letter";
    }

    //会话详情
    @GetMapping("/letter/detail/{conversationId}")
    public String getLetterDetail(@PathVariable("conversationId") String conversationId,Model model,Page page){
        page.setSize(5);

        //获取会话私信列表
        List<Message> letterList = messageService.selectLetters(conversationId,page);
        List<Map<String,Object>> letters=new ArrayList<>();
        if (letterList!=null){
            for(Message message:letterList){
                Map<String ,Object> map=new HashMap<>();
                map.put("letter",message);
                map.put("fromUser",userService.getById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters",letters);
        model.addAttribute("conversationId",conversationId);
        //查询发送私信的用户
        User target = getLetterTarget(conversationId);
        model.addAttribute("target",target);
        //读取未读的消息
        messageService.readMessage(letterList,hostHolder.getUser().getId());

        return "/site/letter-detail";
    }
    @PostMapping("/letter/send")
    @ResponseBody
    public String sendLetter(String toName,String content){
        User target = userService.findByName(toName);
        if (target==null){

            return CommunityUtil.getJsonString(1,"目标用户不存在");

        }
        if (hostHolder.getUser().getId()==target.getId()){
            return CommunityUtil.getJsonString(1,"私信目标不能为自己");
        }
        if (StringUtils.isBlank(content)){
            return CommunityUtil.getJsonString(1,"消息内容不能为空");
        }
        Message message=new Message();
        //对发送的私信内容进行处理
        content= HtmlUtils.htmlEscape(content);
        //过滤敏感词
        content=sensitiveFilter.filter(content);
        message.setContent(content);
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        //设置会话id（要求用户id从小到大拼接 如：111_222）
        if (message.getFromId()>message.getToId()){
            message.setConversationId(message.getToId()+"_"+message.getFromId());
        }else {
            message.setConversationId(message.getFromId()+"_"+message.getToId());
        }
        message.setCreateTime(new Date());
        messageService.save(message);
        return CommunityUtil.getJsonString(200,"发送消息成功");
    }

    private User getLetterTarget(String conversationId){
        String[] ids = conversationId.split("_");
        int id1=Integer.parseInt(ids[0]);
        int id2=Integer.parseInt(ids[1]);
        if (hostHolder.getUser().getId()==id1){
            return userService.getById(id2);
        }else {
            return userService.getById(id1);
        }
    }
}
