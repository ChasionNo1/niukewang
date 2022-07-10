package com.chasion.niukewang.controller.interceptor;

import com.chasion.niukewang.entity.LoginTicket;
import com.chasion.niukewang.entity.User;
import com.chasion.niukewang.service.UserService;
import com.chasion.niukewang.util.CookieUtil;
import com.chasion.niukewang.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @ClassName LoginTicketInterceptor
 * @Description TODO
 * @Author chasion
 * @Date 2022/6/23 20:46
 */
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从cookie中获取ticket
        String ticket = CookieUtil.getValue(request, "ticket");
        if (ticket != null){
            // 查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            // 检查凭证是否有效，是否过期
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())){
                // 根据凭证中的userId从user表中查用户信息
                User user = userService.findUserById(loginTicket.getUserId());
                // 在本次请求中持有用户
                // 实现线程隔离
                hostHolder.setUser(user);
            }
        }
        return true;
    }

    // 模板引擎执行之前，handler执行之后，将user放到modelAndView里
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null){
            modelAndView.addObject("loginUser", user);
        }
    }

    // 在模板引擎执行完，清除掉user
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
