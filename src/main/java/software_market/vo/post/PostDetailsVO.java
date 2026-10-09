package software_market.vo.post;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostDetailsVO {

    /**
     * 帖子ID
     */
    private Integer postId;

    /**
     * 发帖用户ID
     */
    private Integer userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户身份
     */
    private String userIdentity;

    /**
     * 用户积分
     */
    private Integer userScore;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private String tag;

    /**
     * 图片
     */
    private String photos;

    /**
     * 点赞数
     */
    private Integer like;

    /**
     * 评论数
     */
    private Integer comment;

    /**
     * 浏览数
     */
    private Integer browse;

    /**
     * 热度
     */
    private Double heat;

    /**
     * 发布时间
     */
    private LocalDateTime postTime;

    /**
     * 当前用户是否点赞
     */
    private Boolean isLiked;

    /**
     * 当前用户是否收藏
     */
    private Boolean isSaved;
}