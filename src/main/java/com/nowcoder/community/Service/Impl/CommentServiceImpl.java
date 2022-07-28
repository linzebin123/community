package com.nowcoder.community.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nowcoder.community.Service.CommentService;
import com.nowcoder.community.Service.DiscussPostService;
import com.nowcoder.community.common.CommunityConstant;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.mapper.CommentMapper;
import com.nowcoder.community.utils.SensitiveFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private SensitiveFilter sensitiveFilter;
    @Autowired
    private DiscussPostService discussPostService;

    @Override
    public List<Comment> findCommentById(int commentId,Page page) {

        LambdaQueryWrapper<Comment> queryWrapper=new LambdaQueryWrapper<>();
        //评论状态为0时表示评论正常
        queryWrapper.eq(Comment::getStatus,0);
        //代表此数据为评论数据
        queryWrapper.eq(Comment::getEntityType, CommunityConstant.ENTITY_TYPE_POST);
        queryWrapper.eq(Comment::getEntityId,commentId);

        queryWrapper.orderByAsc(Comment::getCreateTime);
        commentMapper.selectPage(page,queryWrapper);
        return page.getRecords();
    }

    @Override
    public List<Comment> findReplyById(int replyId) {
        LambdaQueryWrapper<Comment> queryWrapper=new LambdaQueryWrapper<>();
        //回复状态为0时表示回复正常
        queryWrapper.eq(Comment::getStatus,0);
        //代表此数据为回复数据
        queryWrapper.eq(Comment::getEntityType,CommunityConstant.ENTITY_TYPE_COMMENT);
        queryWrapper.eq(Comment::getEntityId,replyId);
        queryWrapper.orderByAsc(Comment::getCreateTime);
        List<Comment> replyList = commentMapper.selectList(queryWrapper);
        return replyList;

    }

    @Override
    public int findReplyCountByCommentId(int commentId) {

        LambdaQueryWrapper<Comment> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getEntityType,CommunityConstant.ENTITY_TYPE_COMMENT);
        queryWrapper.eq(Comment::getStatus,0);
        queryWrapper.eq(Comment::getEntityId,commentId);

        Integer replyCount = commentMapper.selectCount(queryWrapper);

        return replyCount;
    }



    @Override
    @Transactional
    public int addComment(Comment comment) {
        if (comment==null){
            throw new IllegalArgumentException("参数不能为空");
        }
        String content;
        //对评论内容进行转义
        content= HtmlUtils.htmlEscape(comment.getContent());
        content=sensitiveFilter.filter(content);
        comment.setContent(content);

        int rows = commentMapper.insert(comment);
        //更新帖子回复数量
        if (comment.getEntityType()==CommunityConstant.ENTITY_TYPE_POST){

            DiscussPost discussPost = discussPostService.getById(comment.getEntityId());
            int commentCount = discussPost.getCommentCount();
            discussPost.setCommentCount(commentCount+1);
            discussPostService.updateById(discussPost);
        }
        return rows;



    }

    @Override
    public List<Map<String,Object>> findCommentByUserId(int userId, Page page) {
        LambdaQueryWrapper<Comment> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getStatus,0);
        queryWrapper.eq(Comment::getUserId,userId);
        //回复的实体类型为1代表回复的是帖子
        queryWrapper.eq(Comment::getEntityType,CommunityConstant.ENTITY_TYPE_POST);
        queryWrapper.orderByDesc(Comment::getCreateTime);
        commentMapper.selectPage(page,queryWrapper);
        List<Map<String,Object>> commentVoList=new ArrayList<>();
        List<Comment> commentList=page.getRecords();
        if (commentList!=null){
            for(Comment comment:commentList){
                Map<String,Object> commentVo=new HashMap<>();
                commentVo.put("comment",comment);
                DiscussPost post = discussPostService.getById(comment.getEntityId());
                commentVo.put("post",post);
                commentVoList.add(commentVo);
            }
        }

        return commentVoList;

    }
}
