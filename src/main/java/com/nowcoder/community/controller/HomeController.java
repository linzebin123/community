package com.nowcoder.community.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nowcoder.community.Service.DiscussPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private DiscussPostService discussPostService;
    @GetMapping("/index")
    public String getIndexPage(Model model, Page page){
        page.setSize(10);
        List<Map<String, Object>> mapList = discussPostService.getDiscussPostWithUser(0,page);
        model.addAttribute("discussPosts",mapList);
        page.setRecords(mapList);
        page.setTotal(discussPostService.count());

        return "/index";

    }

    @GetMapping("/erroe")
    public String getErrorPage(){
        return "/error/500";
    }
}
