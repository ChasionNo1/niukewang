package com.chasion.niukewang;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName RedisTest
 * @Description TODO
 * @Author chasion
 * @Date 2022/7/3 14:19
 */
@SpringBootTest
@ContextConfiguration(classes = NiukewangApplication.class)
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void test1(){
        // String 类型的访问
        String redisKey = "key:count";
        redisTemplate.opsForValue().set(redisKey, 1);
        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
    }

    @Test
    public void test2(){
        // hash
        String key = "test:user";
        redisTemplate.opsForHash().put(key, "id", 1);
        redisTemplate.opsForHash().put(key, "username", "chasion");
        System.out.println(redisTemplate.opsForHash().get(key, "id"));
        System.out.println(redisTemplate.opsForHash().get(key, "username"));

    }

    @Test
    public void test3(){
        // list
        String key = "test:ids";
        redisTemplate.opsForList().leftPush(key, 101);
        redisTemplate.opsForList().leftPush(key, 102);
        redisTemplate.opsForList().leftPush(key, 103);
        redisTemplate.opsForList().leftPush(key, 104);

        System.out.println(redisTemplate.opsForList().size(key));
        // 获取某个索引位置上的数
        System.out.println(redisTemplate.opsForList().index(key, 0));
        System.out.println(redisTemplate.opsForList().range(key, 0, 2));
        System.out.println(redisTemplate.opsForList().leftPop(key));
        System.out.println(redisTemplate.opsForList().leftPop(key));
        System.out.println(redisTemplate.opsForList().leftPop(key));

    }

    @Test
    public void test4(){
        // set
        String key = "test:teachers";
        redisTemplate.opsForSet().add(key, "a", "b", "c", "d");
        System.out.println(redisTemplate.opsForSet().size(key));
        System.out.println(redisTemplate.opsForSet().pop(key));
        System.out.println(redisTemplate.opsForSet().members(key));
    }

    @Test
    public void test5(){
        // sorted set
        String key = "test:students";
        redisTemplate.opsForZSet().add(key,  "a", 80);
        redisTemplate.opsForZSet().add(key,  "b", 90);
        redisTemplate.opsForZSet().add(key,  "c", 60);
        redisTemplate.opsForZSet().add(key,  "d", 50);
        System.out.println(redisTemplate.opsForZSet().zCard(key));
        System.out.println(redisTemplate.opsForZSet().score(key, "b"));
        // 由大到小排序
        System.out.println(redisTemplate.opsForZSet().reverseRank(key, "b"));
        System.out.println(redisTemplate.opsForZSet().range(key, 0, 2));

    }

    @Test
    public void test6(){
        redisTemplate.delete("test:students");
        System.out.println(redisTemplate.hasKey("test:students"));
        // 设置key过期时间
        redisTemplate.expire("test:user", 10, TimeUnit.SECONDS);
    }

    @Test
    public void test7(){
        // 多次访问同一个key，绑定
        String key = "test:count";
        // 绑定字符串操作
        BoundValueOperations operations = redisTemplate.boundValueOps(key);

        operations.increment();
    }

    @Test
    public void test8(){
        // redis事务：开启事务后，将命令放在队列里，提交事务后，才一起执行
        // 事务之内的命令不会立刻执行，不要在事务中间执行查询操作，
        // 编程式事务，声明式事务
        // 使用编程式事务，将范围缩小
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String key = "test:tx";
                // 启动事务
                redisOperations.multi();
                redisOperations.opsForSet().add(key, "a");
                redisOperations.opsForSet().add(key, "b");
                redisOperations.opsForSet().add(key, "c");
                // 查询命令不会立刻执行
                System.out.println(redisOperations.opsForSet().members(key));
                // 提交事务
                return redisOperations.exec();
            }
        });
        System.out.println(obj);
    }
}
