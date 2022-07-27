package com.nowcoder.community.Service;

import java.util.List;
import java.util.Map;

public interface FollowService {

    //关注功能
    public void follow(int userId,int entityType,int entityId);

    //取消关注功能
    public void unFollow(int userId,int entityType,int entityId);

    //查询关注的实体数量
    public long findFolloweeCount(int userId,int entityType);

    //查询某实体的粉丝数量
    public long findFollowerCount(int entityType,int entityId);

    //查询当前用户是否关注某实体
    public boolean hasFollowed(int userId,int entityType,int entityId);

    //查询用户关注的人
    public List<Map<String,Object>> findFollowees(int userId,int offset,int limit);

    //查询用户粉丝列表

    public List<Map<String,Object>> findFollowers(int userId,int offset,int limit);
}
