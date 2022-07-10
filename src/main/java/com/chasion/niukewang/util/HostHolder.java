package com.chasion.niukewang.util;

import com.chasion.niukewang.entity.User;
import org.springframework.stereotype.Component;

/**
 * @ClassName HostHolder
 * @Description TODO
 * @Author chasion
 * @Date 2022/6/23 21:38
 * 持有用户信息，代替session对象
 */
@Component
public class HostHolder {
    // 以线程为key存取值
    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user){
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }

    public void clear(){
        users.remove();
    }
}
