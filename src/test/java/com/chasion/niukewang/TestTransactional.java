package com.chasion.niukewang;

import com.chasion.niukewang.service.TestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * @ClassName TestTransactional
 * @Description TODO
 * @Author chasion
 * @Date 2022/7/1 8:43
 */
@SpringBootTest
@ContextConfiguration(classes = NiukewangApplication.class)
public class TestTransactional {

    @Autowired
    private TestService testService;


    @Test
    public void test1(){
        testService.save();
    }

    @Test
    public void test2(){
        Object o = testService.save2();
        System.out.println(o);
    }
}
