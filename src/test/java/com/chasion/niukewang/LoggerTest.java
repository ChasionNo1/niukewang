package com.chasion.niukewang;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * @ClassName LoggerTest
 * @Description TODO
 * @Author chasion
 * @Date 2022/6/21 20:04
 */
@SpringBootTest
@ContextConfiguration(classes = NiukewangApplication.class)
public class LoggerTest {
    /**
     * logger级别从小到大：只能通过大于等于自己级别的
     * trace,debug,info,warn,error
     * */

    private static final Logger logger = LoggerFactory.getLogger(LoggerTest.class);

    @Test
    public void testLogger(){
        System.out.println(logger.getName());
        logger.debug("debug logger");
        logger.info("info logger");
        logger.warn("warn logger");
        logger.error("error logger");

    }
}
