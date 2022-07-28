package com.nowcoder.community.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nowcoder.community.entity.Comment;

import java.util.List;
import java.util.Map;

public interface CommentService extends IService<Comment> {
    /**
     * 分页查询：查询帖子评论信息
     * @param commentId
     * @param page
     * @return
     */
    public List<Comment> findCommentById(int commentId,Page page);

    /**
     * 查询每条评论的回复列表
     * @param replyId
     * @return
     */
    public List<Comment> findReplyById(int replyId);

    /**
     * 计算每条评论的回复数量
     * @param commentId
     * @return
     */
    public int findReplyCountByCommentId(int commentId);




    /**
     * 添加一条评论
     * @param comment
     * @return
     */
    public int addComment(Comment comment);

    /**
     * 根据用户id查找评论列表
     * @param userId
     * @return
     */
    public List<Map<String,Object>> findCommentByUserId(int userId, Page page);
}
