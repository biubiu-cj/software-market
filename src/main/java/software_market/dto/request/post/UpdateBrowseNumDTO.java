package software_market.dto.request.post;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateBrowseNumDTO {

    @NotNull(message = "帖子ID不能为空")
    private Integer postID;
}
