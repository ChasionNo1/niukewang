package com.chasion.niukewang.controller.advice;

import com.chasion.niukewang.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @ClassName ExceptionAdvice
 * @Description TODO
 * @Author chasion
 * @Date 2022/7/2 19:54
 *
 * 统一对controller处理
 */
@ControllerAdvice(annotations = Controller.class)   // 缩小扫描范围，只扫描controller注解的类
public class ExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("服务器发生异常：" + e.getMessage());
        for (StackTraceElement element :
                e.getStackTrace()) {
            logger.error(element.toString());
        }

        String xRequestedWith = request.getHeader("x-requested-with");
        if ("XMLHttpRequest".equals(xRequestedWith)){
            // 这是一个异步请求
            // 返回普通字符串，json格式
            response.setContentType("application/plain;charset=utf-8");
            System.out.println("进入异步---------------");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1, "服务器异常"));
        } else {
            // 普通请求
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
}
