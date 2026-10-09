package software_market.dto.request.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PostCcommentDTO {

    /**
     * 一级评论ID
     */
    @NotNull(message = "一级评论ID不能为空")
    private Integer pcommentID;

    /**
     * 帖子ID
     */
    @NotNull(message = "帖子ID不能为空")
    private Integer postID;

    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    private String content;

    /**
     * 回复的二级评论ID
     */
    private Integer ccommentID;
}