package com.chasion.niukewang.entity;

import lombok.Data;

import java.util.Date;

/**
 * @ClassName LoginTicket
 * @Description TODO
 * @Author chasion
 * @Date 2022/6/23 8:56
 */
@Data
public class LoginTicket {
    private int id;
    private int userId;
    private String ticket;
    private int status;
    // 身份证过期时间
    private Date expired;
}
