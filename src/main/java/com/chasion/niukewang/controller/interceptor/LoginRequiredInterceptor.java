package com.chasion.niukewang.controller.interceptor;

import com.chasion.niukewang.annotation.LoginRequired;
import com.chasion.niukewang.entity.User;
import com.chasion.niukewang.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * @ClassName LoginRequiredInterceptor
 * @Description TODO
 * @Author chasion
 * @Date 2022/6/28 20:36
 */
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 判断拦截的是否是处理器方法
        if (handler instanceof HandlerMethod){
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            // 获取当前方法需要登录的注解
            if (loginRequired != null && hostHolder.getUser() == null){
                // 如果没登陆，重定向到登录页面
                // 同时返回false，拦截请求
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
        }
        return true;
    }
}
