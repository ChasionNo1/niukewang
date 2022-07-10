package com.chasion.niukewang.entity;

import lombok.Data;

import java.util.Date;

/**
 * @ClassName Message
 * @Description TODO
 * @Author chasion
 * @Date 2022/7/1 18:30
 */
@Data
public class Message {

    private int id;
    private int fromId;
    private int toId;
    // 两个用户会话窗口id
    private String conversationId;
    private String content;
    private int status;
    private Date createTime;

}
