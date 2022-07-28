package com.nowcoder.community.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nowcoder.community.Service.LoginTicketService;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.mapper.LoginTicketMapper;
import com.nowcoder.community.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LoginTicketServiceImpl extends ServiceImpl<LoginTicketMapper, LoginTicket> implements LoginTicketService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public LoginTicket selectByTicket(String ticket) {
//        LambdaQueryWrapper<LoginTicket> queryWrapper=new LambdaQueryWrapper<>();
//        queryWrapper.eq(LoginTicket::getTicket,ticket);
//        LoginTicket loginTicket = loginTicketMapper.selectOne(queryWrapper);
        String ticketKey = RedisUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(ticketKey);

    }
}
