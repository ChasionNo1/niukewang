package com.chasion.niukewang.service;

import com.chasion.niukewang.dao.LoginTicketMapper;
import com.chasion.niukewang.dao.UserMapper;
import com.chasion.niukewang.entity.LoginTicket;
import com.chasion.niukewang.entity.User;
import com.chasion.niukewang.util.CommunityConstant;
import com.chasion.niukewang.util.CommunityUtil;
import com.chasion.niukewang.util.MailClient;
import com.chasion.niukewang.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName UserService
 * @Description TODO
 * @Author chasion
 * @Date 2022/6/21 15:46
 */
@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    // 注册用户
    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private RedisTemplate redisTemplate;


    public User findUserById(int id){
//        return userMapper.selectById(id);
        User user = getCache(id);
        if (user == null){
            user  = initCache(id);
        }
        return user;
    }

    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }


    // 注册方法
    public Map<String, Object> register(User user){
        Map<String, Object> map = new HashMap<>();
        if (user == null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (StringUtils.isEmpty(user.getUsername())){
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isEmpty(user.getPassword())){
            map.put("passwordMsg", "密码也不能为空！");
            return map;
        }
        if (StringUtils.isEmpty(user.getEmail())){
            map.put("emailMsg", "邮箱也不能为空！");
            return map;
        }

        // 验证账号是否存在
        User u = userMapper.selectByName(user.getUsername());
        if (u != null){
            map.put("usernameMsg", "该账号已存在！");
            return map;
        }

        // 验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null){
            map.put("emailMsg", "该邮箱已存在！");
            return map;
        }
        // 注册用户
        // 加盐
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        // 设置头像
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 发送激活邮件
        // 创建thymeleaf的上下文，传递参数
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // 激活地址
        // http://localhost:8080/activation/101/code
        String url = domain + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        // 发送内容
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    // 激活方法
    public int activation(int userId, String code){
        // 根据用户id查找激活码，是否一致
        User user = userMapper.selectById(userId);
        // 已激活，不要重复激活
        if (user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        }else if (user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId, 1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        }else {
            return ACTIVATION_FAILURE;
        }
    }

    /**
     * 登录方法：
     * 登录需要用户名和密码，密码为明文，然后通过加密后与数据库进行比较
     * expiredSecond：为账号登录存活时间，这里是int，需要注意毫秒值会越界
     * */
    public Map<String, Object> login(String username, String password, long expiredSecond){
        Map<String, Object> map = new HashMap<>();

        // 登录检查，空值处理
        if (StringUtils.isEmpty(username)){
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isEmpty(password)){
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        // 验证账号，用账号密码取数据库验证
        User user = userMapper.selectByName(username);
        if (user == null){
            map.put("usernameMsg", "该账号不存在!");
            return map;
        }

        // 查看账号激活状态
        if (user.getStatus() == 0){
            map.put("usernameMsg", "该账号未激活!");
            return map;
        }

        // 验证密码
        // 明文密码加salt--->md5加密，然后与数据库进行比较
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)){
            map.put("passwordMsg", "密码不正确！");
            return map;
        }

        // 登录成功，发放ticket
        LoginTicket loginTicket = new LoginTicket();
        // 设置ticket属性
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        // 前面是毫秒值
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * expiredSecond));
        // 存入数据库
        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        // 使用redis存储凭证，自动转换为json字符串
        redisTemplate.opsForValue().set(ticketKey, loginTicket);
//        loginTicketMapper.insertLoginTicket(loginTicket);

        // 发给客户端
        map.put("ticket", loginTicket.getTicket());

        return map;
    }

    // 退出登录
    public void logout(String ticket){
//        loginTicketMapper.updateStatus(ticket, 1);
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        // 修改状态后再存入redis中，完成更新
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey, loginTicket);
    }

    // 从login_ticket查找ticket相关的用户信息
    public LoginTicket findLoginTicket(String ticket){
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
    }

    // 更新头像地址
    public int updateHeader(int userId, String headerUrl){
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;
    }


    // 更新用户密码
    public int updatePassword(int userId, String password){
        int rows = userMapper.updatePassword(userId, password);
        clearCache(userId);
        return rows;
    }

    // 判断邮箱是否已注册
    public boolean isEmailExist(String email){
        User user = userMapper.selectByEmail(email);
        return user != null;
    }



    // 重制密码
    public Map<String, Object> resetPassword(String email, String password){
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isEmpty(email)){
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }
        if (StringUtils.isEmpty(password)){
            map.put("passwordMsg", "密码不能为空");
            return map;
        }

        // 验证邮箱
        User user = userMapper.selectByEmail(email);
        if (user == null){
            map.put("emailMsg", "邮箱尚未注册！");
            return map;
        }

        // 重置密码
        password = CommunityUtil.md5(password + user.getSalt());
        int res = userMapper.updatePassword(user.getId(), password);
        map.put("user", user);
        return map;

    }

    // 1. 优先从缓存中取值
    private User getCache(int userId){
        String userKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }

    // 2. 取不到数据时，初始化缓存数据
    private User initCache(int userId){
        User user = userMapper.selectById(userId);
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    // 3. 数据变更时，清除缓存数据
    private void clearCache(int userId){
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }

}
