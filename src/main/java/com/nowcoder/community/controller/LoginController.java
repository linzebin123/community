package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.Service.UserService;
import com.nowcoder.community.common.CommunityConstant;
import com.nowcoder.community.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

@Controller
@Slf4j
public class LoginController {

    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    private Producer kaptchaProducer;
    @Autowired
    private UserService userService;
    @GetMapping("/register")
    public String register(){
        return "/site/register";
    }

    @GetMapping("/login")
    public String login(){
        return "/site/login";
    }

    //注册账号
    @PostMapping("/register")
    public String register(Model model, User user){
        Map<String,Object> map=userService.register(user);
        if (map==null||map.isEmpty()){
            model.addAttribute("msg","注册成功，请尽快前往邮箱进行激活");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/register";
        }
    }
    //http://localhost:8080/community/activation/userId/activationCode
    //激活账号
    @GetMapping("/activation/{userId}/{code}")
    public String activation(Model model,@PathVariable("userId") int userId,@PathVariable("code") String code){
        int result = userService.activation(userId, code);
        if (result== CommunityConstant.ACTIVATION_SUCCESS){

            model.addAttribute("msg","账号激活成功");
            model.addAttribute("target","/login");
        }else if (result==CommunityConstant.ACTIVATION_REPEAT){
            model.addAttribute("msg","无效操作，您的账号已经激活不要重复激活");
            model.addAttribute("target","/index");
        }else {
            model.addAttribute("msg","激活失败，激活码不正确");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }
    //获取验证码
    @GetMapping("/kaptcha")
    public void getKaptcha(HttpSession session, HttpServletResponse response){
        //生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);
        //存入session
        session.setAttribute("kaptcha",text);

        response.setContentType("image/png");
        try {
            ImageIO.write(image,"png",response.getOutputStream());
        } catch (IOException e) {
            log.error("验证码响应失败："+e.getMessage());
        }
    }

    //账号登陆
    @PostMapping("/login")
    public String login(Model model,String username,String password,String code,boolean rememberme,HttpSession session,HttpServletResponse response){
        //检查验证码
        String kaptcha = (String) session.getAttribute("kaptcha");
        if (StringUtils.isBlank(code)||StringUtils.isBlank(kaptcha)||!StringUtils.endsWithIgnoreCase(code,kaptcha)){
            model.addAttribute("codeMsg","验证码不正确");
            return "/site/login";
        }
        //检查账号密码
        int expiredSeconds=rememberme ? CommunityConstant.REMEMBER_EXPIRED_SECONDS:CommunityConstant.DEFAULT_EXPIRED_SECONDS;

        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        //生成cookie返回给客户端
        if (map.containsKey("ticket")){
            Cookie cookie=new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);

            return "redirect:/index";
        }else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }
    }
    @GetMapping("/logout")
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        return "redirect:/login";
    }
}
