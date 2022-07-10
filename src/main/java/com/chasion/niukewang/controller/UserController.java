package com.chasion.niukewang.controller;

import com.chasion.niukewang.annotation.LoginRequired;
import com.chasion.niukewang.entity.Comment;
import com.chasion.niukewang.entity.DiscussPost;
import com.chasion.niukewang.entity.Page;
import com.chasion.niukewang.entity.User;
import com.chasion.niukewang.service.*;
import com.chasion.niukewang.util.CommunityConstant;
import com.chasion.niukewang.util.CommunityUtil;
import com.chasion.niukewang.util.HostHolder;
import org.apache.catalina.Host;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName UserController
 * @Description TODO
 * @Author chasion
 * @Date 2022/6/25 15:19
 */
@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;


    // 打开设置页面
    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    // 响应上传文件请求
    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if (headerImage == null){
            model.addAttribute("error", "没有选择文件");
            return "/site/setting";
        }
        
        // 获取原始上传的名称。获取后缀，得到文件类型
        // 这个地方有问题，如果上传文件没有后缀呢？这里应该有更为安全的措施！
        String originalFilename = headerImage.getOriginalFilename();
        if (StringUtils.isEmpty(originalFilename) || !originalFilename.contains(".")) {
            model.addAttribute("error", "文件格式不正确");
            return "/site/setting";
        }
        String suffix = originalFilename.substring(originalFilename.lastIndexOf('.'));
        if (StringUtils.isEmpty(suffix)){
            model.addAttribute("error", "文件格式不正确");
            return "/site/setting";
        }

        // 生成随机文件名
        String fileName = CommunityUtil.generateUUID() + suffix;
        // 确定文件存放的路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            // 存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败：" + e.getMessage());
            throw new RuntimeException("上传文件失败");
        }

        // 更新当前用户头像的路径（web访问路径）
        // http://localhost:8080/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);
        return "redirect:/index";

    }

    // 获取头像
    @GetMapping("/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        // 服务器存放路径
        fileName = uploadPath + "/" + fileName;
        // 输出文件格式，获取文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf('.'));
        // 响应图片
        response.setContentType("image/" + suffix);
        FileInputStream fis = null;
        try {
            ServletOutputStream os = response.getOutputStream();
            fis = new FileInputStream(fileName);
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1){
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败" + e.getMessage());
        }finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    // 更改密码
    @LoginRequired
    @PostMapping("/updatePassword")
    public String updatePassword(@Param("oldPassword") String oldPassword, @Param("newPassword") String newPassword,
                                 @Param("confirmPassword") String confirmPassword, Model model){

        // 接收前端原密码和更改密码
        // 前端没有校验，两次密码输入不一致的问题
        // 前端做了空值检测了，这里就不判断了
        if (!StringUtils.isEmpty(oldPassword) && !StringUtils.isEmpty(newPassword) && !StringUtils.isEmpty(confirmPassword)){
            // 验证输入密码是否正确
            User user = hostHolder.getUser();
            // user里的密码是明文+salt -->md5加密
            oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
            // 如果旧密码输入不正确
            if (!user.getPassword().equals(oldPassword)){
                model.addAttribute("oldMsg", "原密码错误！");
                return "/site/setting";
            }
            // 接下来验证，两次新密码是否一致
            if (!newPassword.equals(confirmPassword)){
                model.addAttribute("newMsg", "两次密码不一致！");
                // 仍然跳转到修改页面，虽然网址是post请求
                return "/site/setting";
            }
            // 到这里密码已经没问题了，就是在serv层更新用户密码
            newPassword = CommunityUtil.md5(newPassword + user.getSalt());
            userService.updatePassword(user.getId(), newPassword);

        }
        return "redirect:/logout";
    }

    // 个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model){
        User user = userService.findUserById(userId);
        if (user == null){
            throw new RuntimeException("该用户不存在！");
        }

        // 用户的基本信息
        model.addAttribute("user", user);
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);
        // 查询关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 查询粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 查询是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);


        return "/site/profile";
    }


    // 我的帖子
    @RequestMapping(path = "/profile/post/{userId}", method = RequestMethod.GET)
    public String getMyPost(@PathVariable("userId") int userId, Model model, Page page){
        // 设置分页信息
        page.setLimit(5);
        page.setPath("/profile/post/" + userId);
        page.setRows(discussPostService.findDiscussPostRows(userId));
        // 查询用户的帖子列表
        List<DiscussPost> postList = discussPostService.findAllDiscussPostById(userId, page.getOffset(), page.getLimit());
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        if (postList != null){
            // 遍历每一个帖子，获取每个帖子的赞，封装在一起即可
            for (DiscussPost post :
                    postList) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("post", post);
                // 获取帖子点赞的个数
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));
                list.add(map);
            }
            // 封装信息
            model.addAttribute("posts", list);
            // 帖子总数
            model.addAttribute("postCount", discussPostService.findDiscussPostRows(userId));

        }

        return "/site/my-post";
    }

    // 我的回复
    @RequestMapping(path = "/profile/reply/{userId}", method = RequestMethod.GET)
    public String getMyReply(@PathVariable("userId") int userId, Model model, Page page){
        // 带分页功能
        page.setLimit(5);
        page.setPath("/profile/reply/" + userId);
        int commentCount = commentService.findCommentCountByUserId(userId, CommunityConstant.ENTITY_TYPE_POST);
        page.setRows(commentCount);

        // 获取评论列表
        List<Comment> commentList = commentService.findCommentByUserId(userId, CommunityConstant.ENTITY_TYPE_POST);
        ArrayList<Map<String, Object>> comments = new ArrayList<>();
        if (commentList != null){
            for (Comment comment :
                    commentList) {
                HashMap<String, Object> map = new HashMap<>();
                // 封装信息
                // 帖子的标题，回复的内容和时间
                map.put("post", discussPostService.findDiscussPostById(comment.getEntityId()));
                map.put("comment", comment);
                comments.add(map);
            }

            model.addAttribute("comments", comments);
            model.addAttribute("commentCount", commentCount);
        }

        return "/site/my-reply";
    }


}
