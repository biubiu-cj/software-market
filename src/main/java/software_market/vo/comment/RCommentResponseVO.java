package software_market.vo.comment;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RCommentResponseVO {

    private Integer commentId;

    private Integer userId;

    private String username;

    private String replyUsername;

    private String content;

    private LocalDateTime createTime;

    private Integer likeNum;

    private Integer denyNum;

    private Boolean isLiked;

    private Boolean isDenied;
}