package com.chasion.niukewang.aspect;

import com.sun.media.jfxmediaimpl.HostUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * @ClassName AspectTest
 * @Description TODO
 * @Author chasion
 * @Date 2022/7/2 21:10
 */
//@Component
//@Aspect
public class AspectTest {

    // 切点是service文件夹内，所有service中所有方法（所有参数），任意返回值类型
    @Pointcut("execution(* com.chasion.niukewang.service.*.*(..))")
    public void pointcut(){

    }

    // 通知
    // 连接点开始之前 before
    // 之后 after
    // 返回值之后 afterReturning
    // 抛出异常时 afterThrowing
    // 环绕（前后） Around
    @Before("pointcut()")
    public void before(){
        System.out.println("before");
    }

    @After("pointcut()")
    public void after(){
        System.out.println("after");
    }

    @AfterReturning("pointcut()")
    public void afterReturning(){
        System.out.println("afterReturning");
    }

    @AfterThrowing("pointcut()")
    public void afterThrowing(){
        System.out.println("afterThrowing");
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("before");
        // 调用目标组件的方法
        Object obj = joinPoint.proceed();
        System.out.println("after");
        return obj;
    }


}
