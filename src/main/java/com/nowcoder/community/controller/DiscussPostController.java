package com.nowcoder.community.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nowcoder.community.Service.CommentService;
import com.nowcoder.community.Service.DiscussPostService;
import com.nowcoder.community.Service.UserService;
import com.nowcoder.community.common.CommunityConstant;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.utils.CommunityUtil;
import com.nowcoder.community.utils.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private UserService userService;
    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;


    @PostMapping("/add")
    @ResponseBody
    public String addDiscussPost(String title,String content){
        User user = hostHolder.getUser();
        if (user==null){
            return CommunityUtil.getJsonString(403,"您还没有登陆!");

        }
        if (StringUtils.isBlank(title)||StringUtils.isBlank(content)){
            return CommunityUtil.getJsonString(20007,"标题或内容不能为空");
        }
        DiscussPost discussPost=new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setContent(content);
        discussPost.setTitle(title);
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);
        //发生错误的情况统一进行处理
        return CommunityUtil.getJsonString(0,"发布成功!");
    }
    //查询帖子详情
    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(Model model, @PathVariable("discussPostId") int id, Page page){
        //查询帖子
        DiscussPost discussPost = discussPostService.selectDiscussPostById(id);
        model.addAttribute("post",discussPost);
        //查询帖子作者
        int userId = discussPost.getUserId();
        User user = userService.getById(userId);
        model.addAttribute("user",user);

        page.setSize(5);
        //查询帖子评论列表
        List<Comment> commentList = commentService.findCommentById(id, page);
        //返回给页面需要携带用户信息
        List<Map<String,Object>> commentVoList=new ArrayList<>();
        if (commentList!=null){
            for(Comment comment:commentList){
                //评论VO
                Map<String,Object> commentVo=new HashMap<>();
                commentVo.put("comment",comment);
                //评论用户的信息
                commentVo.put("user",userService.getById(comment.getUserId()));

                //评论下面还有回复列表
                List<Comment> replyList=commentService.findReplyById(comment.getId());
                //回复VO列表
                List<Map<String,Object>> replyVoList=new ArrayList<>();
                if (replyList!=null){
                    for(Comment reply:replyList){
                        //回复VO
                        Map<String,Object> replyVo=new HashMap<>();
                        replyVo.put("reply",reply);
                        //回复作者
                        replyVo.put("user",userService.getById(reply.getUserId()));
                        //回复目标
                        User target=reply.getTargetId()==0 ? null:userService.getById(reply.getTargetId());
                        replyVo.put("target",target);
                        replyVoList.add(replyVo);
                    }

                }
                commentVo.put("replys",replyVoList);
                //回复数量
                int replyCount=commentService.findReplyCountByCommentId(comment.getId());
                commentVo.put("replyCount",replyCount);

                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments",commentVoList);
        return "/site/discuss-detail";
    }

}
