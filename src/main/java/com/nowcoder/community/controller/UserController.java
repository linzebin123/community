package com.nowcoder.community.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nowcoder.community.Service.*;
import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.common.CommunityConstant;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.utils.CommunityUtil;
import com.nowcoder.community.utils.HostHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private LikeService likeService;
    @Autowired
    private FollowService followService;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private CommentService commentService;

    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage(){
        return "/site/setting";
    }

    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile headerImage, Model model){
        if (headerImage==null){
            model.addAttribute("error","您还没有选择图片");
            return "/site/setting";
        }
        //获取图片原始名字
        String filename = headerImage.getOriginalFilename();
        //切取图片后缀
        String suffix=filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件格式不正确");
            return "/site/setting";
        }
        //拼接图片名字
        filename= CommunityUtil.generateUUID()+suffix;
        File dest=new File(uploadPath+"/"+filename);
        try {
            //存储图片
            headerImage.transferTo(dest);
        } catch (IOException e) {
            log.error("上传文件失败："+e.getMessage());
            throw new RuntimeException("上传文件失败，服务器异常",e);
        }
        //更新数据库中用户头像路径（http://localhost:8080/community/user/header/xxx.png）
        User user = hostHolder.getUser();
        String headerUrl=domain+contextPath+"/user/header/"+filename;
        user.setHeaderUrl(headerUrl);
        userService.updateById(user);
        return "redirect:/index";
    }

    @GetMapping("/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        //服务器存放路径
        fileName=uploadPath+"/"+fileName;
        //切取文件后缀
        String suffix=fileName.substring(fileName.lastIndexOf("."));
        //设置响应图片格式
        response.setContentType("image/"+suffix);
        //写入图片
        try (
                OutputStream os=response.getOutputStream();
                FileInputStream fis=new FileInputStream(fileName);
                ){
            byte[] buffer=new byte[1024];
            int b=0;
            while((b=fis.read(buffer))!=-1){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            log.error("读取头像失败："+e.getMessage());
        }


    }
    //修改密码
    @LoginRequired
    @PostMapping("/updatePassword")
    public String updatePassword(Model model,String oldPassword,String newPassword){
        User user = hostHolder.getUser();
        //校验输入的原始密码是否正确
        oldPassword=CommunityUtil.md5(oldPassword+user.getSalt());
        String password=user.getPassword();
        if (!password.equals(oldPassword)){
            model.addAttribute("oldPasswordMsg","输入的原始密码不正确");
            return "/site/setting";
        }
        password=CommunityUtil.md5(newPassword+user.getSalt());
        user.setPassword(password);
        return "redirect:/logout";
    }

    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId,Model model){
        User user = userService.getById(userId);
        if (user==null){
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user",user);
        //该用户获得的点赞数
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);

        //关注数量
        long followeeCount = followService.findFolloweeCount(userId, CommunityConstant.ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        //粉丝数量
        long followerCount = followService.findFollowerCount(CommunityConstant.ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount",followerCount);
        //判断当前用户是否关注某用户
        boolean hasFollowed=false;
        if (hostHolder.getUser()!=null){
            hasFollowed  = followService.hasFollowed(hostHolder.getUser().getId(), CommunityConstant.ENTITY_TYPE_USER, userId);

        }
        model.addAttribute("hasFollowed",hasFollowed);
        return "/site/profile";
    }

    @GetMapping("/profile/{userId}/mypost")
    public String getMyPost(@PathVariable("userId") int userId, Page page,Model model){
        page.setSize(5);
        List<Map<String, Object>> mapList = discussPostService.getDiscussPostWithUser(userId, page);
        User user = userService.getById(userId);
        model.addAttribute("user",user);
        page.setTotal(discussPostService.findCountByUserId(userId));
        model.addAttribute("postList",mapList);


        return "/site/my-post";

    }

    @GetMapping("/profile/{userId}/myreply")
    public String getMyReply(@PathVariable("userId") int userId,Page page,Model model){
        page.setSize(10);
        List<Map<String,Object>> commentVoList = commentService.findCommentByUserId(userId, page);
        model.addAttribute("userId",userId);
        model.addAttribute("comments",commentVoList);

        return "/site/my-reply";

    }


}
