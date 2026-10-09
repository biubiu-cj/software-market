package software_market.dto.request.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShowPcommentsDTO {

    /**
     * 帖子ID
     */
    @NotNull(message = "帖子ID不能为空")
    private Integer postID;
}