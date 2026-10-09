package software_market.dto.request.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SueActionDTO {
    @NotNull(message = "举报ID不能为空")
    private Integer sueID;
}
