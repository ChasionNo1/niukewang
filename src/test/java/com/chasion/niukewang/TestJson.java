package com.chasion.niukewang;

import com.chasion.niukewang.util.CommunityUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.HashMap;

/**
 * @ClassName TestJson
 * @Description TODO
 * @Author chasion
 * @Date 2022/6/30 15:39
 */
@SpringBootTest
@ContextConfiguration(classes = NiukewangApplication.class)
public class TestJson {

    @Test
    public void test1(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", "chasion");
        map.put("age", "23");
        System.out.println(CommunityUtil.getJSONString(0, "ok", map));
    }
}
