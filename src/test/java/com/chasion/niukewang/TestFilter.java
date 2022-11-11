package com.chasion.niukewang;

import com.chasion.niukewang.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * @ClassName TestFilter
 * @Description TODO
 * @Author chasion
 * @Date 2022/6/30 9:09
 */
@SpringBootTest
@ContextConfiguration(classes = NiukewangApplication.class)
public class TestFilter {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void test1(){
        String s = sensitiveFilter.filter("fabc");
        System.out.println(s);
    }


}
