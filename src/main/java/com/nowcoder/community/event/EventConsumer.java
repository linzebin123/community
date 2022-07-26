package com.nowcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.Service.MessageService;
import com.nowcoder.community.common.CommunityConstant;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class EventConsumer {
    @Autowired
    private MessageService messageService;

    @KafkaListener(topics = {CommunityConstant.TOPIC_COMMENT,CommunityConstant.TOPIC_FOLLOW,CommunityConstant.TOPIC_LIKE})
    public void handleCommentMessage(ConsumerRecord record){
        if (record==null||record.value()==null){
            log.error("消息的内容为空");
            return;
        }
        Event event= JSONObject.parseObject(record.value().toString(),Event.class);
        if (event==null){
            log.error("消息格式错误");
            return;
        }
        //发送站内通知
        Message message=new Message();
        message.setFromId(CommunityConstant.SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        //设置通知内容
        Map<String,Object> content=new HashMap<>();
        content.put("userId",event.getUserId());
        content.put("entityType",event.getEntityType());
        content.put("entityId",event.getEntityId());

        for(Map.Entry<String,Object> entry:event.getData().entrySet()){
            content.put(entry.getKey(),entry.getValue());
        }

        message.setContent(JSONObject.toJSONString(content));
        messageService.save(message);




    }
}
