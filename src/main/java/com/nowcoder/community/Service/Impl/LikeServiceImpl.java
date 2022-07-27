package com.nowcoder.community.Service.Impl;

import com.nowcoder.community.Service.LikeService;
import com.nowcoder.community.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeServiceImpl implements LikeService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void like(int userId,int entityType, int entityId,int entityUserId) {
//      //由于需要一次性对两种key进行操作，因此采用事务方式执行
        redisTemplate.execute(new SessionCallback() {


            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String entityLikeKey=RedisUtil.getEntityLikeKey(entityType,entityId);
                String userLikeKey=RedisUtil.getUserLikeKey(entityUserId);
                Boolean isMember = redisOperations.opsForSet().isMember(entityLikeKey, userId);
                redisOperations.multi();
                if (isMember){
                    //如果此时已经点赞过，则再次点击时取消点赞并且目标用户获赞数-1
                    redisOperations.opsForSet().remove(entityLikeKey,userId);
                    redisOperations.opsForValue().decrement(userLikeKey);
                }else {
                    redisOperations.opsForSet().add(entityLikeKey,userId);
                    redisOperations.opsForValue().increment(userLikeKey);
                }
                return redisOperations.exec();

            }
        });

    }

    @Override
    public long findEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey= RedisUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);

    }

    @Override
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey= RedisUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey,userId) ? 1:0;

    }

    @Override
    public int findUserLikeCount(int userId) {
        String userLikeKey=RedisUtil.getUserLikeKey(userId);
        Integer userLikeCount = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return userLikeCount==null ? 0:userLikeCount.intValue();
    }

}
