package com.nowcoder.community.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nowcoder.community.Service.DiscussPostService;
import com.nowcoder.community.Service.UserService;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.mapper.DiscussPostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DiscussPostServiceImpl extends ServiceImpl<DiscussPostMapper, DiscussPost> implements DiscussPostService {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private UserService userService;

    @Override
    public List<Map<String, Object>> getDiscussPostWithUser(int userId,Page page) {

        LambdaQueryWrapper<DiscussPost> queryWrapper=new LambdaQueryWrapper<>();
        //传入的userId如果为0表明查询全部帖子
        queryWrapper.eq(userId!=0,DiscussPost::getUserId,userId);
        queryWrapper.orderByDesc(DiscussPost::getType);
        queryWrapper.orderByDesc(DiscussPost::getCreateTime);
        queryWrapper.last(" limit "+(page.getCurrent()-1)*10+",10");
        List<DiscussPost> discussPostList = discussPostService.list(queryWrapper);
        List<Map<String,Object>> list=new ArrayList<>();
        if (discussPostList!=null){
            for(DiscussPost discussPost:discussPostList){
                Map<String,Object> map=new HashMap<>();
                User user = userService.getById(discussPost.getUserId());
                map.put("post",discussPost);
                map.put("user",user);
                list.add(map);
            }
        }


        return list;
    }
}
