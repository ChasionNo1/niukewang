package com.chasion.niukewang.util;

import org.springframework.util.StringUtils;

/**
 * @ClassName RedisKeyUtil
 * @Description TODO
 * @Author chasion
 * @Date 2022/7/3 16:00
 */
public class RedisKeyUtil {

    private static final String SPLIT = ":";
    // 实体的赞
    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    private static final String PREFIX_USER_LIKE = "like:user";

    // 我关注了多少人，多少人关注了我
    // 目标
    private static final String PREFIX_FOLLOWEE = "followee";
    // 粉丝
    private static final String PREFIX_FOLLOWER = "follower";

    // 验证码
    private static final String PREFIX_KAPTCHA = "kaptcha";

    // 登录凭证
    private static final String PREFIX_TICKET = "ticket";

    private static final String PREFIX_USER = "user";


    // 某个实体的赞，实体类型，是评论还是回复，以及它们的id
    // like:entity:entityType:entityId -> set(点赞的用户id)
    public static String getEntityLikeKey(int entityType, int entityId){
        return  PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // 某个用户的赞
    // like:user:userId --> int
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    // 某个用户关注的实体
    // followee；userId:entityType  -> zset(entityId,now)
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    // 某个用户拥有的粉丝
    // follower:entityType:entityId --> zset(userId,now)
    public static String getFollowerKey(int entityType, int entityId){
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    // 验证码的key
    // 输入验证码时，还没登录，用一个临时凭证
    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    // 登录凭证的key
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET + SPLIT + ticket;
    }

    public static String getUserKey(int userId){
        return PREFIX_USER + SPLIT + userId;
    }


}
