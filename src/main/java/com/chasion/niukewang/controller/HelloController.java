package com.chasion.niukewang.controller;

import com.chasion.niukewang.util.CommunityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @ClassName HelloController
 * @Description TODO
 * @Author chasion
 * @Date 2022/6/21 12:44
 */
//@Controller
//public class HelloController {
//
//    @GetMapping("/")
//    public String index(){
//        return "index";
//    }
//}

// cookies示例
@Controller
public class HelloController {

    @GetMapping("/cookie/set")
    @ResponseBody
    public String setCookie(HttpServletResponse response){
        // 创建cookie
        Cookie cookie = new Cookie("pt_key", CommunityUtil.generateUUID());
        // 设置cookie的生效范围
        cookie.setPath("/");
        // 设置cookie的生存时间
        cookie.setMaxAge(60 * 10);
        // 发送cookie
        response.addCookie(cookie);
        return "set cookie";
    }

//    @GetMapping("/test")
//    public String test(){
//
//    }

    // 获取cookie
    @GetMapping("/cookie/get")
    @ResponseBody
    public String getCookie(@CookieValue("pt_key") String ptKey){
        System.out.println(ptKey);
        return "get cookie";
    }

    // session示例
    @GetMapping("/session/set")
    @ResponseBody
    public String setSession(HttpSession session){
        // cookie只能存键值对字符串，因为存储在浏览器端
        // 而session存在在服务器端
        session.setAttribute("id", 1);
        session.setAttribute("name", "test");
        return "set session";
    }

    @GetMapping("/session/get")
    @ResponseBody
    public String getSession(HttpSession session){
        System.out.println(session.getAttribute("id"));
        System.out.println(session.getAttribute("name"));
        return "get session";
    }


    // ajax示例
    @PostMapping("/ajax")
    @ResponseBody
    public String testAjax(String name, int age){
        System.out.println(name);
        System.out.println(age);
        return CommunityUtil.getJSONString(0, "操作成功");
    }
}

