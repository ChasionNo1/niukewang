package com.chasion.niukewang;

import com.chasion.niukewang.util.CommunityUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * @ClassName Md5Test
 * @Description TODO
 * @Author chasion
 * @Date 2022/6/25 21:54
 */
@SpringBootTest
@ContextConfiguration(classes = NiukewangApplication.class)
public class Md5Test {

    @Test
    public void testMd5(){
        String password = "123";
        String salt = "e17df";
        String encoder = CommunityUtil.md5(password + salt);
        System.out.println(encoder);

    }


}
