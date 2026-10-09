package software_market.dto.request.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmitReportDTO {

    /**
     * 被举报目标ID
     */
    @NotNull(message = "目标ID不能为空")
    private Integer targetID;

    /**
     * 举报目标类型
     * post：帖子
     * pcomment：一级评论
     * ccomment：二级评论
     */
    @NotBlank(message = "举报目标类型不能为空")
    private String targetType;

    /**
     * 举报原因
     */
    @NotBlank(message = "举报原因不能为空")
    private String reason;
}