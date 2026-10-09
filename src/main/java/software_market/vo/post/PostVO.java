package software_market.vo.post;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "帖子返回对象")
public class PostVO {

    @Schema(description = "帖子ID")
    private Integer postId;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "用户积分")
    private Integer userScore;

    @Schema(description = "用户头像")
    private String userAvatar;

    @Schema(description = "用户身份")
    private String userIdentity;

    @Schema(description = "帖子标题")
    private String title;

    @Schema(description = "帖子内容")
    private String content;

    @Schema(description = "点赞数")
    private Integer like;

    @Schema(description = "评论数")
    private Integer comment;

    @Schema(description = "浏览数")
    private Integer browse;

    @Schema(description = "热度")
    private Double heat;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "发布时间")
    private LocalDateTime postTime;

    @Schema(description = "当前用户是否已收藏")
    private Boolean saved;

    @Schema(description = "当前用户是否已点赞")
    private Boolean liked;

    @Schema(description = "图片")
    private String photos;

    @Schema(description = "标签")
    private String tag;

    @Schema(description = "帖子分区")
    private String category;
}