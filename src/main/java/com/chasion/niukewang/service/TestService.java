package com.chasion.niukewang.service;

import com.chasion.niukewang.dao.DiscussPostMapper;
import com.chasion.niukewang.dao.UserMapper;
import com.chasion.niukewang.entity.DiscussPost;
import com.chasion.niukewang.entity.User;
import com.chasion.niukewang.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;

/**
 * @ClassName TestService
 * @Description TODO
 * @Author chasion
 * @Date 2022/6/30 21:55
 */
@Service
public class TestService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    // REQUIRED:支持当前事务（外部事务），如果不存在则创建新事务
    // REQUIRES:_NEW:创建一个新事务，并且暂停当前事务（外部事务）
    // NESTED:如果当前存在事务（外部事务），则嵌套在该事务中执行（独立的提交和回滚），否则就会REQUIRED一样
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public Object save(){
        // 新增用户
        User user = new User();
        user.setUsername("doggo");
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
        user.setCreateTime(new Date());
        user.setHeaderUrl("http://image.nowcoder.com/head/99t.png");
        user.setEmail("1234@qq.com");
        userMapper.insertUser(user);

        // 新增帖子
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle("hello");
        post.setContent("新人报道");
        post.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(post);

        Integer.valueOf("abc");

        return "ok";
    }



    public Object save2(){
        // 设置隔离级别和传播机制
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        return transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus transactionStatus) {
                // 新增用户
                User user = new User();
                user.setUsername("split");
                user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
                user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
                user.setCreateTime(new Date());
                user.setHeaderUrl("http://image.nowcoder.com/head/999t.png");
                user.setEmail("123324@qq.com");
                userMapper.insertUser(user);

                // 新增帖子
                DiscussPost post = new DiscussPost();
                post.setUserId(user.getId());
                post.setTitle("hello 123");
                post.setContent("新人报道");
                post.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(post);

                Integer.valueOf("abc");

                return "ok";
            }
        });
    }
}
