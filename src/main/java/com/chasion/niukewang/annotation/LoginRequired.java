package com.chasion.niukewang.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName LoginRequired
 * @Description TODO
 * @Author chasion
 * @Date 2022/6/28 20:33
 *
 * 自定义注解标识，哪些请求需要验证登录
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {
}
