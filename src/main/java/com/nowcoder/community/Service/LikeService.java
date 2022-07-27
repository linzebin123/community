package com.nowcoder.community.Service;

public interface LikeService {

    //点赞
    public void like(int userId,int entityType,int entityId,int entityUserId);

    //查询某实体点赞数量
    public long findEntityLikeCount(int entityType,int entityId);

    //查询用户对某实体的点赞状态
    public int findEntityLikeStatus(int userId,int entityType,int entityId);

    //查询某个用户获得的赞数量
    public int findUserLikeCount(int userId);
}
