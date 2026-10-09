package software_market.vo.comment;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentResponseVO {

    private Integer commentId;

    private Integer userId;

    private String username;

    private String content;

    private LocalDateTime createTime;

    private Integer likeNum;

    private Integer denyNum;

    private Boolean isLiked;

    private Boolean isDenied;

    private List<RCommentResponseVO> children;
}