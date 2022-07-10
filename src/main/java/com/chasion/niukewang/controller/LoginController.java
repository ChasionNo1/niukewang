package com.chasion.niukewang.controller;

import com.chasion.niukewang.entity.User;
import com.chasion.niukewang.service.UserService;
import com.chasion.niukewang.util.CommunityConstant;
import com.chasion.niukewang.util.CommunityUtil;
import com.chasion.niukewang.util.MailClient;
import com.chasion.niukewang.util.RedisKeyUtil;
import com.google.code.kaptcha.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.imageio.ImageIO;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName LoginController
 * @Description TODO
 * @Author chasion
 * @Date 2022/6/22 10:06
 */
@Controller
public class LoginController implements CommunityConstant {
    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptcha;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private RedisTemplate redisTemplate;

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    // 打开注册页面
    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }

    // 提交表单注册
    @PostMapping("/register")
    public String register(Model model, User user){
        Map<String, Object> map = userService.register(user);
        // 注册成功
        if (map == null || map.isEmpty()){
            // 页面提示信息
            model.addAttribute("msg", "注册成功，邮件已发，快去激活！");
            // 跳转到首页
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        }else {
            // 失败时候，传递失败原因，重新注册
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    // 激活处理，成功则跳转到登录页面，否则会到首页，并提示信息
    // 在中间操作页面显示
    @GetMapping("/activation/{userId}/{code}")
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code){
        int res = userService.activation(userId, code);
        if (res == ACTIVATION_SUCCESS){
            model.addAttribute("msg", "激活成功，可以正常使用！");
            // 跳转到首页
            model.addAttribute("target", "/login");
        }else if (res == ACTIVATION_REPEAT){
            model.addAttribute("msg", "账号已激活，不要重复激活！");
            // 跳转到首页
            model.addAttribute("target", "/index");
        }else {
            model.addAttribute("msg", "激活失败，激活码不正确！");
            // 跳转到首页
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    @GetMapping("/login")
    public String getLoginPage(){
        return "/site/login";
    }

    // 请求验证码
    @GetMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response, HttpSession session){
        // 生成验证码
        String text = kaptcha.createText();
        BufferedImage image = kaptcha.createImage(text);
        // 将验证码存入session
//        session.setAttribute("kaptcha", text);

        // 代码重构，使用redis存储验证码
        // 验证码的归属
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath("/");
        response.addCookie(cookie);

        // 将验证码存入redis里
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(kaptchaKey, text, 60, TimeUnit.SECONDS);

        // 将图片输出给浏览器
        response.setContentType("image/png");
        ServletOutputStream os = null;
        try {
            os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("验证码响应失败：" + e.getMessage());
        }

    }

    /**
     * 登录处理方法，
     * 参数传入：用户名、密码、验证码、是否记住我
     * */
    @PostMapping("/login")
    public String login(String username, String password, String code, boolean rememberMe,
                        Model model, @CookieValue("kaptchaOwner") String kaptchaOwner, HttpServletResponse response){
        // 首先看看验证码是否正确
        // 从session里取验证码
//        String kaptcha = (String) session.getAttribute("kaptcha");

        // 从redis里获取验证码
        String kaptcha = null;
        if (!StringUtils.isEmpty(kaptchaOwner)){
            String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
        }

        if (StringUtils.isEmpty(kaptcha) || StringUtils.isEmpty(code) || !kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg", "验证码不正确！");
            // 回到登录页面
            return "/site/login";
        }
        // 检查账号。密码
        int expiredSeconds = rememberMe ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        // 获取登录结果
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")){
            // 登录成功
            Cookie cookie = new Cookie("ticket", (String) map.get("ticket"));
            // 设置cookie有效路径和时间
            cookie.setPath("/");
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            // 回到首页
            return "redirect:/index";
        }else {
            // 错误时
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    // 退出登录
    @GetMapping("/logout")
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        return "redirect:/login";
    }

    // 忘记密码页面
    @GetMapping("/forget")
    public String getForgetPage(){
        return "/site/forget";
    }


    // 忘记密码请求
    @PostMapping("/forget/password")
    public String forgetPassword(String email, String verifyCode, String password, Model model, HttpSession session){
        // 验证表单信息，这里在发送验证码的时候，已经验证过邮箱是否存在了
        // 需要验证发送验证码邮箱和表单提交邮箱是否一致
        System.out.println(email);
        System.out.println(session.getAttribute("email"));
        if (!email.equals(session.getAttribute("email"))){

            model.addAttribute("emailMsg", "重新发送验证码");
            return "/site/forget";
        }
        // 验证码存活时间
        Date alive = (Date) session.getAttribute("alive");
        if (new Date(System.currentTimeMillis()  - 1000 * 60 * 5).after(alive)){
            model.addAttribute("emailMsg", "验证码过期");
            return "/site/forget";
        }

        String code = (String) session.getAttribute("verifyCode");
        if (StringUtils.isEmpty(verifyCode) || StringUtils.isEmpty(code) || !verifyCode.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg", "验证码错误");
            return "/site/forget";
        }

        Map<String, Object> map = userService.resetPassword(email, password);
        if (map.containsKey("user")){
            return "redirect:/login";
        }else {
            model.addAttribute("emailMsg", map.get("emailMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    // 获取验证码
    @GetMapping("/forget/code")
    @ResponseBody
    public String getForgetCode(String email, HttpSession session){
        if (StringUtils.isEmpty(email)){
            return CommunityUtil.getJSONString(1, "邮箱不能为空！");
        }
        // 验证邮箱是否注册，未注册则提示
        if (!userService.isEmailExist(email)){
            return CommunityUtil.getJSONString(1, "该邮箱尚未注册");
        }
        logger.info("到此");
        // 发送邮件
        Context context = new Context();
        context.setVariable("email", email);
        // 生成验证码
        String code = CommunityUtil.generateUUID().substring(0, 4);
        context.setVariable("code", code);
        String content = templateEngine.process("/mail/forget", context);
        mailClient.sendMail(email, "找回密码", content);

        // 保存验证码到session
        session.setAttribute("verifyCode", code);
        session.setAttribute("email", email);
        session.setAttribute("alive", new Date());
        return CommunityUtil.getJSONString(0);
    }



}
