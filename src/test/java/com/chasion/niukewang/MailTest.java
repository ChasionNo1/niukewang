package com.chasion.niukewang;

import com.chasion.niukewang.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * @ClassName MailTest
 * @Description TODO
 * @Author chasion
 * @Date 2022/6/21 21:39
 */
@SpringBootTest
@ContextConfiguration(classes = NiukewangApplication.class)
public class MailTest {

    @Autowired
    private MailClient mailClient;

    // 模板引擎，发送thymeleaf页面
    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void test1(){
        mailClient.sendMail("185897780@qq.com", "Test", "hello");
    }

    @Test
    public void test2(){
        Context context = new Context();
        context.setVariable("username", "sunny");

        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);
        mailClient.sendMail("185897780@qq.com", "HTML", content);
    }
}
