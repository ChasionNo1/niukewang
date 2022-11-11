package com.chasion.niukewang.entity;

import lombok.Data;

import java.util.Date;

/**
 * @ClassName Comment
 * @Description TODO
 * @Author chasion
 * @Date 2022/7/1 9:28
 * 评论实体类
 */
@Data
public class Comment {

    private int id;
    private int userId;
    // 评论的类型，是给帖子评论，还是给评论回复，1是给帖子，2是回复
    private int entityType;
    // 帖子的id，或者是回复的id
    // 对帖子的评论的，帖子的id
    private int entityId;
    // 回复评论，评论的作者id
    private int targetId;
    private String content;
    private int status;
    private Date createTime;

}
