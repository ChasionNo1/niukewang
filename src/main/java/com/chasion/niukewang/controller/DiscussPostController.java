package com.chasion.niukewang.controller;

import com.chasion.niukewang.dao.DiscussPostMapper;
import com.chasion.niukewang.entity.Comment;
import com.chasion.niukewang.entity.DiscussPost;
import com.chasion.niukewang.entity.Page;
import com.chasion.niukewang.entity.User;
import com.chasion.niukewang.service.CommentService;
import com.chasion.niukewang.service.DiscussPostService;
import com.chasion.niukewang.service.LikeService;
import com.chasion.niukewang.service.UserService;
import com.chasion.niukewang.util.CommunityConstant;
import com.chasion.niukewang.util.CommunityUtil;
import com.chasion.niukewang.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @ClassName DiscussPostController
 * @Description TODO
 * @Author chasion
 * @Date 2022/6/30 16:07
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content){
        User user = hostHolder.getUser();
        if (user == null){
            return CommunityUtil.getJSONString(403, "请先登录");
        }

        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);

        // 报错异常情况，统一处理

        return CommunityUtil.getJSONString(0, "发布成功");

    }

    // 处理查询帖子请求
    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page){
        // 帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);
        // 作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        // 帖子的点赞数
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);

        // 点赞状态
        int likeStatus = hostHolder.getUser() == null ?
                0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);

        // 帖子的评论信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());
        // 评论和回复，评论帖子，回复平均
        // 评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        // 评论vo列表，存放map集合，每个map存放评论和作者信息
        ArrayList<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null){
            for (Comment c :
                    commentList) {
                HashMap<String, Object> map = new HashMap<>();
                // 评论和作者
                map.put("comment", c);
                map.put("user", userService.findUserById(c.getUserId()));
                // 评论的数量和状态
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, c.getId());
                map.put("likeCount", likeCount);
                likeStatus = hostHolder.getUser() == null ?
                        0 :likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, c.getId());
                map.put("likeStatus", likeStatus);

                // 回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, c.getId(), 0, Integer.MAX_VALUE);
                // 回复的vo列表
                ArrayList<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null){
                    for (Comment reply:
                     replyList) {
                        HashMap<String, Object> replyVo = new HashMap<>();
                        replyVo.put("reply", reply);
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        // 回复点赞的情况
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);
                        likeStatus = hostHolder.getUser() == null ?
                                0 :likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus);
                        // 处理回复的目标：
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);

                        replyVoList.add(replyVo);
                    }
                }
                map.put("replys", replyVoList);
                // 记录每一条评论的回复数
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, c.getId());
                map.put("replyCount", replyCount);
                commentVoList.add(map);

            }
        }

        model.addAttribute("comments", commentVoList);

        return "/site/discuss-detail";

    }


}
