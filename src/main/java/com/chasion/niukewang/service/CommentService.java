package com.chasion.niukewang.service;

import com.chasion.niukewang.dao.CommentMapper;
import com.chasion.niukewang.entity.Comment;
import com.chasion.niukewang.util.CommunityConstant;
import com.chasion.niukewang.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @ClassName CommentService
 * @Description TODO
 * @Author chasion
 * @Date 2022/7/1 9:43
 */
@Service
public class CommentService implements CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;


    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit){
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    public int findCommentCount(int entityType, int entityId){
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    // 增加评论，事务管理
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
        if (comment == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        // 添加评论
        // 过滤符号
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        // 过滤敏感词
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);

        // 更新评论数量
        if (comment.getEntityType() == ENTITY_TYPE_POST){
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }
        return rows;

    }

    // 获取某个用户法发过的对帖子的评论
    public List<Comment> findCommentByUserId(int userId, int entityType){
        return commentMapper.selectCommentByUserId(userId, entityType);
    }

    // 获取评论的数量
    public int findCommentCountByUserId(int userId, int entityType){
        return commentMapper.selectCountByUserId(userId, entityType);
    }

    public Comment findCommentById(int id){
        return commentMapper.selectCommentById(id);
    }
}
