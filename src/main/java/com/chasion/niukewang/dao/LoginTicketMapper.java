package com.chasion.niukewang.dao;

import com.chasion.niukewang.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

/**
 * @ClassName LoginTicketMapper
 * @Description TODO
 * @Author chasion
 * @Date 2022/6/23 8:58
 */
@Mapper
@Deprecated  // 过时的，不推荐使用
public interface LoginTicketMapper {

    @Insert({"insert into login_ticket (user_id, ticket, status, expired) ",
            "values(#{userId}, #{ticket}, #{status}, #{expired})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);


    // 使用ticket字段查询，
    @Select({
            "select id, user_id, ticket, status, expired ",
            "from login_ticket where ticket=#{ticket}"
    })
    LoginTicket selectByTicket(String ticket);

    // 过期后，修改状态
    @Update({
            "update login_ticket set status=#{status} where ticket=#{ticket}"
    })
    int updateStatus(@Param("ticket") String ticket, @Param("status") int status);
}
