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


}
