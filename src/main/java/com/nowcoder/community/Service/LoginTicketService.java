package com.nowcoder.community.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nowcoder.community.entity.LoginTicket;


public interface LoginTicketService extends IService<LoginTicket> {
    public LoginTicket selectByTicket(String ticket);
}
