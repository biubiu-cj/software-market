package software_market.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteUserDTO {
    @NotBlank(message = "用户名不能为空")
    private String name;
}
