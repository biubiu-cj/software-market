package software_market.dto.request.comment;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PostCommentDTO {

    @NotNull(message = "帖子ID不能为空")
    private Integer postID;

    @NotBlank(message = "评论内容不能为空")
    private String content;

    /**
     * 可选评分
     * 0~5
     */
    @Min(value = 0, message = "评分不能小于0")
    @Max(value = 5, message = "评分不能大于5")
    private Integer rating;
}