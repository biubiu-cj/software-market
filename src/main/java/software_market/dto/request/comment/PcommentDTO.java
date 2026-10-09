package software_market.dto.request.comment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PcommentDTO {

    @NotNull(message = "一级评论ID不能为空")
    private Integer pcommentID;
}