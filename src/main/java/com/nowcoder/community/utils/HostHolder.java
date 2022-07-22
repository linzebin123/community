package com.nowcoder.community.utils;

import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 存储用户信息代替session
 */
@Component
public class HostHolder {
    private ThreadLocal<User> users=new ThreadLocal<>();

    public void setUser(User user){
        users.set(user);
    }
    public User getUser(){
        return users.get();
    }
    public void clear(){
        users.remove();
    }
}
