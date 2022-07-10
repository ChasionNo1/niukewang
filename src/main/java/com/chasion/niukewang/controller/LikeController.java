package com.chasion.niukewang.controller;

import com.chasion.niukewang.entity.Event;
import com.chasion.niukewang.entity.User;
import com.chasion.niukewang.event.EventProducer;
import com.chasion.niukewang.service.LikeService;
import com.chasion.niukewang.util.CommunityConstant;
import com.chasion.niukewang.util.CommunityUtil;
import com.chasion.niukewang.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;

/**
 * @ClassName LikeController
 * @Description TODO
 * @Author chasion
 * @Date 2022/7/3 16:19
 */
@Controller
public class LikeController implements CommunityConstant {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    // 处理点赞异步请求
    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId){
        User user = hostHolder.getUser();
        // 点赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        // 数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        // 状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        // 返回结果
        HashMap<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

//         触发点赞事件，点赞的时候触发，取消就不触发了
        if (likeStatus == 1){
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId)
                    ;
            eventProducer.fireEvent(event);
        }

        return CommunityUtil.getJSONString(0, null, map);

    }

}
