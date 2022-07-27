package com.nowcoder.community.utils;

public class RedisUtil {
    public static final String SPILIT=":";

    public static final String PREFIX_ENTITY_LIKE="like:entity";

    public static final String PREFIX_USER_LIKE="like:user";

    public static final String PREFIX_FOLLOWEE="followee";

    public static final String PREFIX_FOLLOWER="follower";
    //某个实体的赞
    //like:entity:entityType:entityId ->set(userId)
    public static String getEntityLikeKey(int entityType,int entityId){
        return PREFIX_ENTITY_LIKE+SPILIT+entityType+SPILIT+entityId;
    }

    //某个用户获得的赞
    //like:user:userId -> int
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE+SPILIT+userId;
    }

    //某个用户关注的实体
    //followee:userId:entityType -> zset(entityId,now)
    public static String getFolloweeKey(int userId,int entityType){
        return PREFIX_FOLLOWEE+SPILIT+userId+SPILIT+entityType;
    }

    //某个实体拥有的粉丝
    //follower:entityType:entityId ->zset(userId,now)
    public static String getFollowerKey(int entityType,int entityId){
        return PREFIX_FOLLOWER+SPILIT+entityType+SPILIT+entityId;
    }

}