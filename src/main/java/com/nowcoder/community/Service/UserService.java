package com.nowcoder.community.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nowcoder.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

public interface UserService extends IService<User> {
    /**
     * 账号注册
     * @param user
     * @return
     */
    public Map<String,Object> register(User user);

    /**
     * 账号激活
     * @param userId
     * @param code
     * @return
     */
    public int activation(int userId,String code);

    /**
     * 账号登陆
     * @param username
     * @param password
     * @param expiredSeconds
     * @return
     */
    public Map<String,Object> login(String username,String password,int expiredSeconds);

    /**
     * 退出登陆
     * @param ticket
     */
    public void logout(String ticket);

    /**
     * 根据账号查找用户
     * @param username
     * @return
     */
    public User findByName(String username);

    /**
     * 优先从redis缓存中取值
     * @param userId
     * @return
     */
    public User getCache(int userId);

    /**
     * 如果从缓存中取不到则初始化缓存并返回
     * @param userId
     * @return
     */
    public User initCache(int userId);

    /**
     * 当用户数据更新时清理缓存
     * @param userId
     */
    public void clearCache(int userId);
}
