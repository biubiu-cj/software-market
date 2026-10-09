package software_market.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteAdminDTO {
    @NotBlank(message = "管理员账号不能为空")
    private String account;
}
