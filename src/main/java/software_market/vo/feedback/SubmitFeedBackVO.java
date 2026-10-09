package software_market.vo.feedback;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubmitFeedBackVO {
    @NotBlank(message = "反馈内容不能为空")
    private String ftext;

    /**
     * 附件
     */
    private String attachment;

    /**
     * 反馈Id
     */
    private Integer feedbackId;
}
