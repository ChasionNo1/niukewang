package com.chasion.niukewang.dao;

import com.chasion.niukewang.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName CommentMapper
 * @Description TODO
 * @Author chasion
 * @Date 2022/7/1 9:30
 */
@Mapper
public interface CommentMapper {

    // 根据实体查询，带分页功能
    List<Comment> selectCommentsByEntity(@Param("entityType") int entityType,
                                         @Param("entityId")int entityId,
                                         @Param("offset")int offset,
                                         @Param("limit")int limit);

    // 查询评论总条数
    int selectCountByEntity(@Param("entityType")int entityType,
                            @Param("entityId")int entityId);

    // 增加评论的方法
    int insertComment(Comment comment);

    // 查询某个用户发过的评论
    List<Comment> selectCommentByUserId(@Param("userId") int userId, @Param("entityType") int entityType);

    // 查询某个用户发过评论的数量
    int selectCountByUserId(@Param("userId") int userId, @Param("entityType") int entityType);

    Comment selectCommentById(int id);
}
