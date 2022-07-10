package com.chasion.niukewang.controller;

import com.chasion.niukewang.dao.DiscussPostMapper;
import com.chasion.niukewang.dao.UserMapper;
import com.chasion.niukewang.entity.DiscussPost;
import com.chasion.niukewang.entity.Page;
import com.chasion.niukewang.entity.User;
import com.chasion.niukewang.service.LikeService;
import com.chasion.niukewang.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName HomeController
 * @Description TODO
 * @Author chasion
 * @Date 2022/6/21 15:51
 */
@Controller
public class HomeController implements CommunityConstant {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page){
        // 方法调用之前，SpringMVC会自动实例化Model和Page，并将Page注入Model
        // 所以，在thymeleaf中可以直接访问Page对象中的数据
        page.setRows(discussPostMapper.selectDiscussPostRows(0));
        page.setPath("/index");

        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null){
            for (DiscussPost post :
                    list) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user = userMapper.selectById(post.getUserId());
                map.put("user", user);
                // 查询每个帖子的点赞数
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        return "/index";
    }

    // 500 的错，服务器内部的错
    @RequestMapping(path = "/error", method = RequestMethod.GET)
    public String getErrorPage(){
        return "/error/500";
    }


}
