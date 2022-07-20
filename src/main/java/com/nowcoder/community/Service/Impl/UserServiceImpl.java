package com.nowcoder.community.Service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nowcoder.community.Service.UserService;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.mapper.UserMapper;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
