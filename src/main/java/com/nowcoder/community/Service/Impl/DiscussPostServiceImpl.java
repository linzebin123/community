package com.nowcoder.community.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nowcoder.community.Service.DiscussPostService;
import com.nowcoder.community.Service.LikeService;
import com.nowcoder.community.Service.UserService;
import com.nowcoder.community.common.CommunityConstant;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.mapper.DiscussPostMapper;
import com.nowcoder.community.utils.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DiscussPostServiceImpl extends ServiceImpl<DiscussPostMapper, DiscussPost> implements DiscussPostService {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private UserService userService;
    @Autowired
    private SensitiveFilter sensitiveFilter;
    @Autowired
    private LikeService likeService;

    @Override
    public List<Map<String, Object>> getDiscussPostWithUser(int userId,Page page) {

        LambdaQueryWrapper<DiscussPost> queryWrapper=new LambdaQueryWrapper<>();
        //传入的userId如果为0表明查询全部帖子
        queryWrapper.eq(userId!=0,DiscussPost::getUserId,userId);
        queryWrapper.orderByDesc(DiscussPost::getType);
        queryWrapper.orderByDesc(DiscussPost::getCreateTime);
        queryWrapper.last(" limit "+(page.getCurrent()-1)*page.getSize()+","+page.getSize());
        List<DiscussPost> discussPostList = discussPostService.list(queryWrapper);
        List<Map<String,Object>> list=new ArrayList<>();
        if (discussPostList!=null){
            for(DiscussPost discussPost:discussPostList){
                Map<String,Object> map=new HashMap<>();
                User user = userService.getById(discussPost.getUserId());
                map.put("post",discussPost);
                map.put("user",user);
                long likeCount = likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_POST, discussPost.getId());
                map.put("likeCount",likeCount);
                list.add(map);
            }
        }


        return list;
    }

    @Override
    public int addDiscussPost(DiscussPost discussPost) {

        if (discussPost==null){
            throw new IllegalArgumentException("参数不能为空");
        }
        //转义HTML标签，防止用户输入的内容改变页面结构
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));
        //过滤敏感词汇

        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));
        //存入数据库
        return discussPostMapper.insert(discussPost);
    }

    @Override
    public DiscussPost selectDiscussPostById(int id) {

        DiscussPost discussPost = discussPostMapper.selectById(id);
        return discussPost;
    }

    @Override
    public int findCountByUserId(int userId) {
        LambdaQueryWrapper<DiscussPost> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DiscussPost::getUserId,userId);
        queryWrapper.ne(DiscussPost::getStatus,2);
        int count = discussPostMapper.selectCount(queryWrapper);
        return count;
    }
}
