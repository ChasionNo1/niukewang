package com.chasion.niukewang.dao;

import com.chasion.niukewang.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @ClassName UserMapper
 * @Description TODO
 * @Author chasion
 * @Date 2022/6/21 14:19
 */
@Mapper
public interface UserMapper {

    User selectById(int id);

    User selectByName(String name);

    User selectByEmail(String email);

    int insertUser(User user);

    // spring中使用mybatis中遇到的本身传入的字段和数据库中的是一致的，在做MD5加密的时候传入密码，使得字段不对应
    int updateStatus(@Param("id") int id, @Param("status")int status);

    int updateHeader(@Param("id")int id, @Param("headerUrl")String headerUrl);

    //Parameter 'password' not found. Available parameters are [arg1, arg0, param1
    int updatePassword(@Param("id")int id, @Param("password")String password);
}
