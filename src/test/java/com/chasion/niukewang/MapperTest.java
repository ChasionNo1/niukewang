package com.chasion.niukewang;

import com.chasion.niukewang.dao.*;
import com.chasion.niukewang.entity.*;
import com.chasion.niukewang.service.CommentService;
import org.apache.ibatis.annotations.Param;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;

/**
 * @ClassName MapperTest
 * @Description TODO
 * @Author chasion
 * @Date 2022/6/21 14:43
 */
@SpringBootTest
@ContextConfiguration(classes = NiukewangApplication.class)
public class MapperTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Test
    public void testSelectUser(){
        User user = userMapper.selectById(101);
        System.out.println(user);

        User user1 = userMapper.selectByName("aaa");
        System.out.println(user1);

        User user2 = userMapper.selectByEmail("nowcoder112@sina.com");
        System.out.println(user2);
    }

    @Test
    public void testInsertUser(){
        User user = new User();
        user.setUsername("chasion");
        user.setPassword("123456");
        user.setSalt("angd");
        user.setEmail("185897780@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());
        int row = userMapper.insertUser(user);
        System.out.println(row);
        int id = user.getId();
        System.out.println(id);
    }

    @Test
    public void testUpdateUser(){
        userMapper.updateHeader(150, "http://www.nowcoder.com/102.png");
        userMapper.updatePassword(150, "2222333");
        userMapper.updateStatus(150, 1);
    }

    @Test
    public void testSelectPosts(){
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(0, 0, 10);
        for (DiscussPost d :
                discussPosts) {
            System.out.println(d);
        }

        int rows = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(rows);
    }

    @Test
    public void testInsertLoginTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 60 * 1000 * 10));

        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectLoginTicket(){
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);

        loginTicketMapper.updateStatus("abc", 1);
        loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
    }


    @Test
    public void testMessage(){
        // 查询111用户与其他人对话框
        List<Message> messages = messageMapper.selectConversations(111, 0, 20);
        for (Message message :
                messages) {
            System.out.println(message);
        }

        // 查询111对话框的数量
        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);

        // 查询这个对话框内的message
        List<Message> letters = messageMapper.selectLetters("111_112", 0, 12);
        for (Message letter :
                letters) {
            System.out.println(letter);
        }

        // 查询这个对话框内message的个数
        int letterCount = messageMapper.selectLetterCount("111_112");
        System.out.println(letterCount);

        // 查询未读消息
        int unread = messageMapper.selectLetterUnreadCount(131, "111_131");
        System.out.println(unread);


    }

    @Test
    public void testSelectAllPostsByUsrId(){
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(101, 0, 5);
        for (DiscussPost d :
                discussPosts) {
            System.out.println(d);
        }
    }

    @Test
    public void testSelectAllComments(){
        List<Comment> comments = commentMapper.selectCommentByUserId(111, 1);
        for (Comment c :
                comments) {
            System.out.println(c);
        }

        int count = commentMapper.selectCountByUserId(111, 1);
        System.out.println(count);
    }
}
