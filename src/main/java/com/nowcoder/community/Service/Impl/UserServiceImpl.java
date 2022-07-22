package com.nowcoder.community.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nowcoder.community.Service.UserService;
import com.nowcoder.community.common.CommunityConstant;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.mapper.LoginTicketMapper;
import com.nowcoder.community.mapper.UserMapper;
import com.nowcoder.community.utils.CommunityUtil;
import com.nowcoder.community.utils.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private LoginTicketMapper loginTicketMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Override
    public Map<String, Object> register(User user) {
        Map<String,Object> map=new HashMap<>();
        if (user==null){
            throw new IllegalArgumentException("参数不能为空");
        }
        if (StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮件不能为空");
            return map;
        }
        //验证账号是否存在
        LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername,user.getUsername());
        User u = userMapper.selectOne(queryWrapper);
        if (u!=null){
            map.put("usernameMsg","该账号已被注册");
            return map;
        }
        //验证邮箱是否存在
        LambdaQueryWrapper<User> emailQueryWrapper=new LambdaQueryWrapper<>();
        emailQueryWrapper.eq(User::getEmail,user.getEmail());
        u = userMapper.selectOne(emailQueryWrapper);
        if (u!=null){
            map.put("emailMsg","该邮箱已被注册");
            return map;
        }
        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        //加密方法：用户输入的密码+salt然后进行md5加密
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        user.setStatus(0);
        user.setType(0);
        userMapper.insert(user);

        //激活邮件
        Context context=new Context();
        context.setVariable("email",user.getEmail());
        //获取自增的用户id

        //url:http://localhost:8080/community/activation/userId/activationCode
        String url=domain+contextPath+"/activation/"+user.getId()+"/"+user.getActivationCode();
        context.setVariable("url",url);
        String content=templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(),"激活账号",content);
        return map;
    }

    public int activation(int userId,String code){
        User user = userMapper.selectById(userId);
        if (user.getStatus()==1){
            return CommunityConstant.ACTIVATION_REPEAT;
        }else if(code.equals(user.getActivationCode())){
            user.setStatus(1);
            userMapper.updateById(user);
            return CommunityConstant.ACTIVATION_SUCCESS;
        }else {
            return CommunityConstant.ACTIVATION_FAIL;
        }
    }

    @Override
    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String,Object> map=new HashMap<>();
        if (StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        //验证账号是否存在
        LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername,username);
        User user = userMapper.selectOne(queryWrapper);
        if (user==null){
            map.put("usernameMsg","账号不存在");
            return map;
        }
        //验证密码
        password=CommunityUtil.md5(password+user.getSalt());
        if (!password.equals(user.getPassword())){
            map.put("passwordMsg","密码错误");
            return map;
        }
        //验证账号状态
        if (user.getStatus()==0){
            map.put("usernameMsg","账号未激活");
            return map;
        }
        //生成登陆凭证
        LoginTicket loginTicket=new LoginTicket();
        loginTicket.setUserId(user.getId());

        loginTicket.setExpired(new Date(System.currentTimeMillis()+expiredSeconds*1000L));
        loginTicket.setStatus(0);
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicketMapper.insert(loginTicket);

        map.put("ticket",loginTicket.getTicket());
        return map;


    }

    @Override
    public void logout(String ticket) {
        LambdaQueryWrapper<LoginTicket> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(LoginTicket::getTicket,ticket);
        LoginTicket loginTicket = loginTicketMapper.selectOne(queryWrapper);
        //将状态码改为1为无效
        loginTicket.setStatus(1);
        loginTicketMapper.updateById(loginTicket);
    }




}
