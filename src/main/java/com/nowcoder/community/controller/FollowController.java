package com.nowcoder.community.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nowcoder.community.Service.FollowService;
import com.nowcoder.community.Service.UserService;
import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.common.CommunityConstant;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.utils.CommunityUtil;
import com.nowcoder.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController {
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private FollowService followService;
    @Autowired
    private UserService userService;
    @Autowired
    private EventProducer eventProducer;

    @PostMapping("/follow")
    @ResponseBody
    public String follow(int entityType,int entityId){
        User user = hostHolder.getUser();
        followService.follow(user.getId(),entityType,entityId);

        //触发关注事件
        Event event=new Event()
                .setTopic(CommunityConstant.TOPIC_FOLLOW)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJsonString(0,"关注成功");
    }

    @PostMapping("/unFollow")
    @ResponseBody
    public String unFollow(int entityType,int entityId){
        User user = hostHolder.getUser();
        followService.unFollow(user.getId(),entityType,entityId);
        return CommunityUtil.getJsonString(0,"取消关注成功");
    }

    @GetMapping("/followees/{userId}")
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.getById(userId);
        if (user==null){
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user",user);

        page.setSize(5);
        long followeeCount = followService.findFolloweeCount(userId, CommunityConstant.ENTITY_TYPE_USER);
        page.setTotal(followeeCount);
        List<Map<String, Object>> userList = followService.findFollowees(userId, (int) ((page.getCurrent() - 1) * page.getSize()), (int) (page.getSize()));
        if (userList!=null){
            for(Map<String,Object> map:userList){
                User u = (User) map.get("user");
                boolean hasFollowed = hasFollowed(u.getId());
                map.put("hasFollowed",hasFollowed);
            }
        }
        model.addAttribute("users",userList);
        return "/site/followee";


    }
    @GetMapping("/followers/{userId}")
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.getById(userId);
        if (user==null){
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user",user);

        page.setSize(5);
        long followerCount = followService.findFollowerCount(CommunityConstant.ENTITY_TYPE_USER,userId);
        page.setTotal(followerCount);
        List<Map<String, Object>> userList = followService.findFollowers(userId, (int) ((page.getCurrent() - 1) * page.getSize()), (int) (page.getSize()));
        if (userList!=null){
            for(Map<String,Object> map:userList){
                User u = (User) map.get("user");
                boolean hasFollowed = hasFollowed(u.getId());
                map.put("hasFollowed",hasFollowed);
            }
        }
        model.addAttribute("users",userList);
        return "/site/follower";


    }

    private boolean hasFollowed(int userId){
        if (hostHolder.getUser()==null){
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(),CommunityConstant.ENTITY_TYPE_USER,userId);
    }
}
