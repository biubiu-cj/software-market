package software_market.dto.request.feedback;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class SubmitFeedBackDTO {
    @NotBlank(message = "反馈内容不能为空")
    private String ftext;

    /**
     * 附件
     */
    private String attachment;
}
