package com.chasion.niukewang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class NiukewangApplication {

    @PostConstruct
    public void init() {
        // 解决netty启动冲突问题
        // 由于redis底层也是用netty，然后elastic初始化默认没有人使用
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

    public static void main(String[] args) {
        SpringApplication.run(NiukewangApplication.class, args);
    }

}
