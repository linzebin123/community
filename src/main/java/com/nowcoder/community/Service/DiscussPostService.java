package com.nowcoder.community.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nowcoder.community.entity.DiscussPost;

import java.util.List;
import java.util.Map;

public interface DiscussPostService extends IService<DiscussPost> {
    /**
     * 查询帖子及作者信息并分页
     * @param userId
     * @return
     */
    public List<Map<String,Object>> getDiscussPostWithUser(int userId, Page page);
}